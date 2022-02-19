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

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.sourcegrade.jagr.launcher.env.Jagr
import org.sourcegrade.jagr.launcher.env.logger

internal class RubricCollectorImpl(private val jagr: Jagr) : MutableRubricCollector {
    private val queued = mutableListOf<GradingQueue>()
    private val gradingScheduled = mutableListOf<GradingJob>()
    private val gradingRunning = mutableListOf<GradingJob>()
    private val gradingFinished = mutableListOf<GradingResult>()

    private val total: Int
        get() = queued.sumOf { it.total }
    private val remaining: Int
        get() = queued.sumOf { it.remaining }

    private var listener: (GradingResult) -> Unit = {}
    private val scope = CoroutineScope(Dispatchers.Unconfined)

    private val mutex = Mutex()

    override suspend fun <T> withGradingScheduled(block: suspend (List<GradingJob>) -> T): T = mutex.withLock {
        block(gradingScheduled)
    }

    override suspend fun <T> withGradingRunning(block: suspend (List<GradingJob>) -> T): T = mutex.withLock {
        block(gradingRunning)
    }

    override suspend fun <T> withGradingFinished(block: suspend (List<GradingResult>) -> T): T = mutex.withLock {
        block(gradingFinished)
    }

    override suspend fun getTotal(): Int = mutex.withLock { total }
    override suspend fun getRemaining(): Int = mutex.withLock { remaining }

    override suspend fun toSnapshot(): RubricCollector.Snapshot = mutex.withLock {
        RubricCollector.Snapshot(
            gradingScheduled.toList(),
            gradingRunning.toList(),
            gradingFinished.toList(),
            total,
            remaining,
        )
    }

    override suspend fun <T> withSnapshot(block: suspend (RubricCollector.Snapshot) -> T): T = mutex.withLock {
        return block(
            RubricCollector.Snapshot(
                gradingScheduled,
                gradingRunning,
                gradingFinished,
                total,
                remaining,
            )
        )
    }

    override suspend fun allocate(queue: GradingQueue) = mutex.withLock {
        queued += queue
    }

    override suspend fun setListener(listener: (GradingResult) -> Unit) = mutex.withLock {
        this.listener = listener
    }

    private fun startDirect(request: GradingRequest): GradingJob {
        val job = GradingJob(request)
        gradingRunning += job
        scope.launch {
            try {
                val result = job.result.await()
                mutex.withLock {
                    gradingFinished.add(result)
                }
                listener(result)
            } catch (e: Exception) {
                jagr.logger.error("An error occurred receiving result for grading job", e)
            }
        }
        return job
    }

    override suspend fun start(request: GradingRequest): GradingJob = mutex.withLock {
        startDirect(request)
    }

    override suspend fun <T> startBlock(block: suspend (MutableRubricCollector.StartBlock) -> T): T = mutex.withLock {
        block(object : MutableRubricCollector.StartBlock {
            override fun start(request: GradingRequest): GradingJob = startDirect(request)
        })
    }
}
