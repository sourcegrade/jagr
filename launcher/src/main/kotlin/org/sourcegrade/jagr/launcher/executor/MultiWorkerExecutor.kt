/*
 *   Jagr - SourceGrade.org
 *   Copyright (C) 2021 Alexander Staeding
 *   Copyright (C) 2021 Contributors
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.sourcegrade.jagr.launcher.executor

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.sourcegrade.jagr.launcher.env.Jagr
import kotlin.coroutines.suspendCoroutine

class MultiWorkerExecutor internal constructor(private val workerPool: WorkerPool) : Executor {
    private val mutex = Mutex()
    private val scheduled = mutableListOf<GradingQueue>()

    override suspend fun schedule(queue: GradingQueue) = mutex.withLock {
        scheduled += queue
    }

    private suspend fun checkWorkers(synchronizer: WorkerSynchronizer, rubricCollector: MutableRubricCollector) {
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
                    synchronizer.setContinuation(it, exitDirectly = true)
                }
            }
            if (workerPool.withActiveWorkers { it.isEmpty() }) {
                return
            }
        }
        val workersAndJobs = rubricCollector.startBlock { startBlock ->
            requests.map { it.first to startBlock.start(it.second) }
        }
        suspendCoroutine<Unit> { continuation ->
            synchronizer.setContinuation(continuation)
            for ((worker, job) in workersAndJobs) {
                job.result.invokeOnCompletion { synchronizer.notifyContinuation() }
                worker.assignJob(job)
            }
            synchronizer.handleBetween()
        }
    }

    override suspend fun start(rubricCollector: MutableRubricCollector) {
        val synchronizer = WorkerSynchronizer()
        workerPool.use {
            while (scheduled.isNotEmpty() || workerPool.withActiveWorkers { it.isNotEmpty() }) {
                checkWorkers(synchronizer, rubricCollector)
            }
        }
    }

    open class Factory internal constructor(val workerPoolFactory: WorkerPool.Factory) : Executor.Factory {
        companion object Default : Factory(ThreadWorkerPool.Factory)

        override fun create(jagr: Jagr): Executor = MultiWorkerExecutor(workerPoolFactory.create(jagr))
    }

    class FactoryBuilder internal constructor(factory: Factory) {
        var workerPoolFactory: WorkerPool.Factory = factory.workerPoolFactory
        fun factory() = Factory(workerPoolFactory)
    }

    companion object {
        fun Factory(from: Factory = Factory.Default, builderAction: FactoryBuilder.() -> Unit): Factory =
            FactoryBuilder(from).also(builderAction).factory()
    }
}
