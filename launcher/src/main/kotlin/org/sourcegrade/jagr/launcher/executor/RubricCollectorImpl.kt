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

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.sourcegrade.jagr.api.rubric.GradedRubric
import org.sourcegrade.jagr.launcher.env.Jagr
import org.sourcegrade.jagr.launcher.env.logger

internal class RubricCollectorImpl(private val jagr: Jagr) : MutableRubricCollector {
  private val queued = mutableListOf<GradingQueue>()
  override val gradingScheduled = mutableListOf<GradingJob>()
  override val gradingRunning = mutableListOf<GradingJob>()
  override val gradingFinished = mutableListOf<GradingResult>()
  override val total: Int
    get() = queued.sumOf { it.total }
  override val remaining: Int
    get() = queued.sumOf { it.remaining }

  private var listener: (GradingResult) -> Unit = {}
  private val scope = CoroutineScope(Dispatchers.Unconfined)

  override fun allocate(queue: GradingQueue) {
    queued += queue
  }

  override fun setListener(listener: (GradingResult) -> Unit) {
    this.listener = listener
  }

  override fun start(request: GradingRequest): GradingJob {
    val job = GradingJob(request)
    gradingRunning += job
    scope.launch {
      try {
        val result = job.result.await()
        gradingFinished.add(result)
        listener(result)
      } catch (e: Exception) {
        jagr.logger.error("An error occurred receiving result for grading job", e)
      }
    }
    return job
  }
}
