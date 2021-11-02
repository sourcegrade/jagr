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

package org.sourcegrade.jagr

import org.sourcegrade.jagr.api.rubric.GradedRubric
import org.sourcegrade.jagr.launcher.env.Jagr
import org.sourcegrade.jagr.launcher.env.logger
import org.sourcegrade.jagr.launcher.executor.RubricCollector

fun RubricCollector.logHistogram(jagr: Jagr) {
  val histogram = mutableMapOf<Int, Int>()
  var correctPoints = 0
  var incorrectPoints = 0
  var maxPoints = 0
  val allRubrics = gradingFinished.flatMap { it.rubrics.keys }
  for (rubric in allRubrics) {
    val prev = histogram.computeIfAbsent(rubric.grade.correctPoints) { 0 }
    histogram[rubric.grade.correctPoints] = prev + 1
    correctPoints += rubric.grade.correctPoints
    incorrectPoints += rubric.grade.incorrectPoints
    maxPoints += rubric.rubric.maxPoints
  }
  if (allRubrics.isEmpty()) {
    return
  }
  jagr.logger.info(
    "Result: Correct: $correctPoints, Incorrect: $incorrectPoints, Max: $maxPoints, Average: " +
      "${correctPoints.toDouble() / allRubrics.size.toDouble()}, Rubrics: ${allRubrics.size}"
  )
  for ((points, count) in histogram.toSortedMap()) {
    StringBuilder().apply {
      append("Points: ")
      append(points.toString().padStart(length = 3))
      append(" Nr: ")
      append(count.toString().padStart(length = 3))
      append(" |")
      for (i in 0 until count) {
        append('-')
      }
    }.also { jagr.logger.info(it.toString()) }
  }
}

fun GradedRubric.logGradedRubric(jagr: Jagr) {
  val succeeded = testCycle.testsSucceededCount
  val total = testCycle.testsStartedCount
  val info = if (total == 0) {
    " (no tests found)"
  } else {
    " ($succeeded/$total tests)" +
      " points=${grade.correctPoints} -points=${grade.incorrectPoints} maxPoints=${rubric.maxPoints}" +
      " from '${rubric.title}'"
  }
  jagr.logger.info("${testCycle.submission} :: $info")
}
