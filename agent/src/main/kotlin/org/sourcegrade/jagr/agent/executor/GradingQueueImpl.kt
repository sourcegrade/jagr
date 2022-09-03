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

package org.sourcegrade.jagr.agent.executor

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.slf4j.Logger
import org.sourcegrade.jagr.api.testing.Submission
import org.sourcegrade.jagr.launcher.executor.GradingQueue
import org.sourcegrade.jagr.launcher.executor.GradingRequest
import org.sourcegrade.jagr.domain.GraderJar
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.concurrent.atomic.AtomicInteger

class GradingQueueImpl(
    logger: Logger,
    private val compiledBatch: CompiledBatch,
) : GradingQueue {

    override val graders: List<GraderJar> get() = compiledBatch.graders
    override val submissions: List<Submission> get() = compiledBatch.submissions

    private val submissionIterator: Iterator<Submission> = submissions.iterator()
    private val mutex = Mutex()

    override val total: Int = submissions.size

    private val _remaining = AtomicInteger(total)

    override val remaining: Int
        get() = _remaining.get()

    override val startedUtc: Instant = OffsetDateTime.now(ZoneOffset.UTC).toInstant()

    @Volatile
    override var finishedUtc: Instant? = null

    override suspend fun next(): GradingRequest? {
        if (finishedUtc != null) return null
        return mutex.withLock {
            // check if next() was called while the last grading request is being processed by another worker
            if (finishedUtc != null) {
                null
            } else if (submissionIterator.hasNext()) {
                _remaining.getAndDecrement()
                GradingRequestImpl(submissionIterator.next(), graders, compiledBatch.libraries)
            } else {
                finishedUtc = OffsetDateTime.now(ZoneOffset.UTC).toInstant()
                null
            }
        }
    }
}
