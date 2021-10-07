/*
 *   Jagr - SourceGrade.org
 *   Copyright (C) 2021 Alexander Staeding
 *   Copyright (C) 2021 Contributors
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.sourcegrade.jagr.launcher.executor

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.sourcegrade.jagr.launcher.env.Jagr
import org.sourcegrade.jagr.launcher.env.logger
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class MultiWorkerExecutor internal constructor(private val workerPool: WorkerPool) : Executor {

  open class Factory internal constructor(val workerPoolFactory: WorkerPool.Factory) : Executor.Factory {
    companion object Default : Factory(ThreadWorkerPool.Factory)

    override fun create(jagr: Jagr): Executor = MultiWorkerExecutor(workerPoolFactory.create(jagr))
  }

  companion object {
    fun Factory(from: Factory = Factory.Default, builderAction: FactoryBuilder.() -> Unit): Factory =
      FactoryBuilder(from).also(builderAction).factory()
  }

  class FactoryBuilder internal constructor(factory: Factory) {
    var workerPoolFactory: WorkerPool.Factory = factory.workerPoolFactory
    fun factory() = Factory(workerPoolFactory)
  }

  private val mutex = Mutex()
  private val scheduled = mutableListOf<GradingQueue>()

  override suspend fun schedule(queue: GradingQueue) = mutex.withLock {
    scheduled += queue
  }

  private suspend fun checkWorkers(police: ThreadPolice, rubricCollector: MutableRubricCollector) {
    val requests = mutex.withLock {
      workerPool.createWorkers(scheduled.sumOf { it.remaining })
        .mapNotNull { worker -> scheduled.next()?.let { request -> worker to request } }
    }
    // protect against requests being empty
    // the only way out of this method is by a request completing
    // no requests, no exit
    if (requests.isEmpty()) {
      mutex.withLock {
        scheduled.removeIf { it.remaining == 0 }
      }
      if (workerPool.withActiveWorkers { it.isNotEmpty() }) {
        // there are no new requests, but workers are still handling submissions
        // wait here until at least one is finished
        suspendCoroutine<Unit> {
          police.setContinuation(it, exitDirectly = true)
        }
      }
      if (workerPool.withActiveWorkers { it.isEmpty() }) {
        return
      }
    }
    suspendCoroutine<Unit> { continuation ->
      police.setContinuation(continuation)
      for (request in requests) {
        val job = rubricCollector.start(request.second)
        job.result.invokeOnCompletion(police::notifyContinuation)
        request.first.assignJob(job)
      }
      police.handleBetween()
    }
  }

  override suspend fun start(rubricCollector: MutableRubricCollector) {
    val police = ThreadPolice()
    while (scheduled.isNotEmpty() || workerPool.withActiveWorkers { it.isNotEmpty() }) {
      checkWorkers(police, rubricCollector)
    }
  }
}

private class ThreadPolice {
  private val finishedBetweenContinuation = AtomicBoolean()
  private val exitDirectly = AtomicBoolean()
  private val lock = ReentrantLock()
  private var continuation: Continuation<Unit>? = null

  fun setContinuation(continuation: Continuation<Unit>, exitDirectly: Boolean = false) = lock.withLock {
    this.continuation = continuation
    this.exitDirectly.set(exitDirectly)
  }

  fun notifyContinuation(throwable: Throwable? = null) = lock.withLock {
    if (throwable != null) {
      Jagr.logger.error("Encountered error in worker", throwable)
    }
    if (finishedBetweenContinuation.get()) {
      if (exitDirectly.get()) {
        resume()
      } else {
        return
      }
    }
    val cont = continuation
    if (cont == null) {
      finishedBetweenContinuation.set(true)
    } else {
      resume()
    }
  }

  fun handleBetween() = lock.withLock {
    if (finishedBetweenContinuation.get()) {
      finishedBetweenContinuation.set(false)
      resume()
    }
  }

  private fun resume() {
    continuation?.resume(Unit)
    continuation = null
  }
}
