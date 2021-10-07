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
      val remaining = scheduled.sumOf { it.remaining }
      println("Remaining: $remaining")
      workerPool.createWorkers(remaining)
        .mapNotNull { worker -> scheduled.next()?.let { request -> worker to request } }
    }
    println("Requests: ${requests.size}")
    // protect against requests being empty
    // the only way out of this method is by a request completing
    // no requests, no exit
    if (requests.isEmpty()) {
      mutex.withLock {
        scheduled.removeIf { it.remaining == 0 }
      }
      return
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
    while (scheduled.isNotEmpty()) {
      checkWorkers(police, rubricCollector)
    }
  }
}

private class ThreadPolice {
  private var finishedBetweenContinuation = AtomicBoolean()
  private var currentContinuation: Continuation<Unit>? = null

  fun setContinuation(continuation: Continuation<Unit>) {
    currentContinuation = continuation
  }

  @Synchronized
  fun notifyContinuation(throwable: Throwable? = null) {
    Jagr.logger.warn("Worker finished. State ${finishedBetweenContinuation.get()} : $currentContinuation")
    if (throwable != null) {
      Jagr.logger.error("Encountered error in worker", throwable)
    }
    if (finishedBetweenContinuation.get()) {
      return
    }
    val cont = currentContinuation
    if (cont == null) {
      finishedBetweenContinuation.set(true)
    } else {
      currentContinuation = null
      cont.resume(Unit)
    }
  }

  @Synchronized
  fun handleBetween() {
    Jagr.logger.warn("Handle between. State ${finishedBetweenContinuation.get()} : $currentContinuation")
    if (finishedBetweenContinuation.get()) {
      finishedBetweenContinuation.set(false)
      currentContinuation?.resume(Unit)
    }
  }
}
