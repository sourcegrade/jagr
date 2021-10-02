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
import org.sourcegrade.jagr.launcher.env.Environment
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class MultiWorkerExecutor internal constructor(private val workerPool: WorkerPool) : Executor {

  open class Factory internal constructor(val workerPoolFactory: WorkerPool.Factory) : Executor.Factory {
    companion object Default : Factory(ProcessWorkerPool.Factory)

    override fun create(environment: Environment): Executor = MultiWorkerExecutor(workerPoolFactory.create(environment))
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

  private suspend fun checkWorkers(rubricCollector: MutableRubricCollector) {
    val requests = mutex.withLock {
      workerPool.createWorkers(scheduled.size)
        .mapNotNull { worker -> scheduled.next()?.let { request -> worker to request } }
    }
    // protect against requests being empty
    // the only way out of this method is by a request completing
    // no requests, no exit
    if (requests.isEmpty()) {
      mutex.withLock {
        scheduled.removeFirstOrNull()
      }
      return
    }
    suspendCoroutine<Unit> { continuation ->
      for (request in requests) {
        val job = rubricCollector.start(request.second)
        job.result.invokeOnCompletion {
          if (it == null) {
            continuation.resume(Unit)
          } else {
            continuation.resumeWithException(it)
          }
        }
        request.first.assignJob(job)
      }
    }
  }

  override suspend fun start(rubricCollector: MutableRubricCollector) {
    while (scheduled.isNotEmpty()) {
      checkWorkers(rubricCollector)
    }
  }
}
