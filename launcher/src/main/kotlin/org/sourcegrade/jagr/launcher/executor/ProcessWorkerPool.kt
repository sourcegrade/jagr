/*
 *   Jagr - SourceGrade.org
 *   Copyright (C) 2021-2022 Alexander Staeding
 *   Copyright (C) 2021-2022 Contributors
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

import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.sourcegrade.jagr.launcher.env.Jagr
import java.util.concurrent.Executors

class ProcessWorkerPool(
    private val jagr: Jagr,
    private val concurrency: Int,
) : WorkerPool {
    private val activeWorkers: MutableList<Worker> = mutableListOf()
    private val mutex = Mutex()
    private suspend fun removeActiveWorker(worker: Worker) = mutex.withLock { activeWorkers -= worker }

    /**
     * These threads are used to send and listen to results from each child process.
     */
    private val processIODispatcher = Executors.newFixedThreadPool(concurrency * 2).asCoroutineDispatcher()

    override suspend fun <T> withActiveWorkers(block: suspend (List<Worker>) -> T) = mutex.withLock { block(activeWorkers) }

    override suspend fun createWorkers(maxCount: Int): List<Worker> {
        if (maxCount == 0) return emptyList()
        val workerCount = minOf(maxCount, concurrency - activeWorkers.size)
        return List(workerCount) {
            ProcessWorker(jagr, this::removeActiveWorker, processIODispatcher).also(activeWorkers::add)
        }
    }

    override fun close() {
        processIODispatcher.close()
    }

    open class Factory internal constructor(val concurrency: Int) : WorkerPool.Factory {
        companion object Default : Factory(concurrency = 4)

        override fun create(jagr: Jagr): WorkerPool = ProcessWorkerPool(jagr, concurrency)
    }

    class FactoryBuilder internal constructor(factory: Factory) {
        var concurrency: Int = factory.concurrency
        fun factory() = Factory(concurrency)
    }

    companion object {
        fun Factory(from: Factory = Factory.Default, builderAction: FactoryBuilder.() -> Unit): Factory =
            FactoryBuilder(from).also(builderAction).factory()
    }
}
