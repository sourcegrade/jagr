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

import kotlinx.coroutines.runBlocking
import org.sourcegrade.jagr.launcher.env.Jagr
import kotlin.concurrent.thread

class ThreadWorker(
    private val jagr: Jagr,
    private val removeActive: suspend (Worker) -> Unit,
) : Worker {
    override var job: GradingJob? = null
    override var status: WorkerStatus = WorkerStatus.READY
    override var userTime: Long = 0

    private lateinit var thread: Thread

    override fun assignJob(job: GradingJob) {
        check(this.job == null) { "Worker already has a job!" }
        status = WorkerStatus.RUNNING
        this.job = job
        thread(
            isDaemon = true,
            name = GRADING_THREAD_PREFIX + job.request.submission.info.toString(),
            priority = 3,
        ) {
            runBlocking {
                job.gradeCatching(jagr)
                status = WorkerStatus.FINISHED
                removeActive(this@ThreadWorker)
            }
        }
    }

    override fun kill() {
        thread.stop()
        runBlocking {
            removeActive(this@ThreadWorker)
        }
    }
}
