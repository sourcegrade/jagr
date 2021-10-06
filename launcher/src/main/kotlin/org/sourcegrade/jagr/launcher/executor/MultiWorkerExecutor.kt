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
import java.util.Deque
import java.util.LinkedList
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
      police.handleExistingResults()
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
  private var existingFinishedJobs = false
  private var currentContinuation: Continuation<Unit>? = null

  fun setContinuation(continuation: Continuation<Unit>) {
    currentContinuation = continuation
    existingFinishedJobs = resultQueue.isNotEmpty()
  }

  private fun clear() {
    currentContinuation = null
  }

  private val resultQueue: Deque<Result<Unit>> = LinkedList()

  @Synchronized
  fun notifyContinuation(throwable: Throwable? = null) {
    val result = throwable.toResult()
    if (existingFinishedJobs) {
      resultQueue.add(result)
      return
    }
    val cont = currentContinuation
    if (cont == null) {
      resultQueue.add(result)
    } else if (resultQueue.isEmpty()) {
      cont.resumeWith(result)
      clear()
    } else {
      handleMultipleResults()
    }
  }

  @Synchronized
  fun handleExistingResults() {
    if (existingFinishedJobs) {
      handleMultipleResults()
      existingFinishedJobs = false
    }
  }

  private fun handleMultipleResults() {
    for (result in resultQueue) {
      if (result.isFailure) {
        Jagr.logger.error("Encountered error in worker", result.exceptionOrNull())
      }
    }
    resultQueue.clear()
    currentContinuation!!.resume(Unit)
  }

  private fun Throwable?.toResult(): Result<Unit> = if (this == null) Result.success(Unit) else Result.failure(this)
}
