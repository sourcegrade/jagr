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

import org.sourcegrade.jagr.api.testing.Submission
import org.sourcegrade.jagr.launcher.io.GraderJar
import org.sourcegrade.jagr.launcher.io.GradingBatch
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.concurrent.atomic.AtomicBoolean

interface GradingQueue {

    val graders: List<GraderJar>

    val submissions: List<Submission>

    /**
     * The total number of submissions in this queue.
     */
    val total: Int

    /**
     * The number of submissions in this queue which have not started being graded.
     */
    val remaining: Int

    val startedUtc: Instant

    val finishedUtc: Instant?

    suspend fun next(): GradingRequest?

    interface Factory {
        /**
         * Constructs a [GradingQueue] that may be asynchronously polled.
         */
        fun create(batch: GradingBatch): GradingQueue
    }
}

fun GradingRequest.toGradingQueue(): GradingQueue = SingletonGradingQueue(this)

private class SingletonGradingQueue(private val job: GradingRequest) : GradingQueue {
    override val graders: List<GraderJar> = job.graders
    override val submissions: List<Submission> = listOf(job.submission)
    override val total: Int = 1
    override val remaining: Int
        get() = if (wasRead.get()) 0 else 1
    override val startedUtc: Instant = OffsetDateTime.now(ZoneOffset.UTC).toInstant()
    override val finishedUtc: Instant? = null

    val wasRead = AtomicBoolean()

    override suspend fun next(): GradingRequest? {
        if (wasRead.get()) return null
        wasRead.set(true)
        return job
    }
}

/**
 * Finds the next [GradingRequest] in a list of [GradingQueues][GradingQueue] and removes empty queues.
 */
suspend fun MutableCollection<GradingQueue>.next(): GradingRequest? {
    val iter = iterator()
    while (iter.hasNext()) {
        val request = iter.next().next()
        if (request == null) {
            iter.remove()
        } else {
            return request
        }
    }
    return null
}
