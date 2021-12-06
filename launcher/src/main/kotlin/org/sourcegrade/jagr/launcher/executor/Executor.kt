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

import org.sourcegrade.jagr.api.rubric.GradedRubric
import org.sourcegrade.jagr.api.testing.Submission
import org.sourcegrade.jagr.launcher.env.Jagr
import org.sourcegrade.jagr.launcher.io.GraderJar
import java.time.OffsetDateTime
import java.time.ZoneOffset

interface Executor {
  suspend fun schedule(queue: GradingQueue)
  suspend fun start(rubricCollector: MutableRubricCollector)
  interface Factory {
    fun create(jagr: Jagr): Executor
  }
}

interface RuntimeGrader {
  fun grade(graders: List<GraderJar>, submission: Submission): Map<GradedRubric, String>

  fun gradeFallback(graders: List<GraderJar>, submission: Submission): Map<GradedRubric, String>
}

fun RuntimeGrader.grade(job: GradingJob) {
  val startedUtc = OffsetDateTime.now(ZoneOffset.UTC).toInstant()
  val rubrics = with(job.request) {
    grade(graders, submission)
  }
  val finishedUtc = OffsetDateTime.now(ZoneOffset.UTC).toInstant()
  job.result.complete(GradingResult(startedUtc, finishedUtc, job.request, rubrics))
}
