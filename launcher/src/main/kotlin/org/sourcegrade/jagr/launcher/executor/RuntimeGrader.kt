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
import org.sourcegrade.jagr.launcher.env.logger
import org.sourcegrade.jagr.launcher.env.runtimeGrader
import org.sourcegrade.jagr.launcher.io.GraderJar
import java.time.OffsetDateTime
import java.time.ZoneOffset

interface RuntimeGrader {
    fun grade(graders: List<GraderJar>, submission: Submission): Map<GradedRubric, String>

    fun gradeFallback(graders: List<GraderJar>, submission: Submission): Map<GradedRubric, String>
}

fun GradingRequest.grade(gradingFun: (List<GraderJar>, Submission) -> Map<GradedRubric, String>): GradingResult {
    val startedUtc = OffsetDateTime.now(ZoneOffset.UTC).toInstant()
    val rubrics = gradingFun(graders, submission)
    val finishedUtc = OffsetDateTime.now(ZoneOffset.UTC).toInstant()
    return GradingResult(startedUtc, finishedUtc, this, rubrics)
}

fun GradingRequest.gradeCatching(
    jagr: Jagr,
    gradingFun: (List<GraderJar>, Submission) -> Map<GradedRubric, String>,
): GradingResult? {
    return try {
        grade(gradingFun)
    } catch (e: Throwable) {
        // catch throwable, we want to complete this job in any circumstance to not block the pipeline for other submissions
        jagr.logger.error("A fatal error occurred grading ${submission.info} :: ${e::class.simpleName} ${e.message}", e)
        null
    }
}

/**
 * Attempts to grade this [GradingJob] first with [primaryGrader].
 * If this fails (i.e. it returns null) grade with the fallback.
 */
fun GradingJob.gradeCatching(
    jagr: Jagr,
    primaryGrader: (GradingRequest) -> GradingResult? = { request.gradeCatching(jagr, jagr.runtimeGrader::grade) },
) {
    jagr.logger.info("gradeCatching!")
    try {
        // first try to grade normally, then try to grade with fallback
        val gradingResult = primaryGrader(request)
            ?: request.grade(jagr.runtimeGrader::gradeFallback)
        result.complete(gradingResult)
    } catch (e: Throwable) {
        // this should never happen; the fallback grader is only meant to create a minimum rubric
        // (without running external code) and shouldn't throw anything
        val causeSTE = e.stackTrace.firstOrNull { !it.className.startsWith("java") }
        val causeClass = causeSTE?.className
        val exception = IllegalStateException(
            """
A fatal error occurred in the fallback grader for submission ${request.submission.info} :: ${e::class.simpleName} ${e.message}
This is not an error in the submission, rather in the RuntimeGrader implementation @ $causeSTE.
Please report this to the maintainers of $causeClass.
""",
            e
        )
        result.completeExceptionally(exception)
    }
}
