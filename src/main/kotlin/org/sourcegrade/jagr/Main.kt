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

import kotlinx.coroutines.runBlocking
import org.sourcegrade.jagr.launcher.env.Jagr
import org.sourcegrade.jagr.launcher.env.gradingQueueFactory
import org.sourcegrade.jagr.launcher.executor.emptyCollector
import org.sourcegrade.jagr.launcher.executor.executorForBatch
import org.sourcegrade.jagr.launcher.io.buildGradingBatch

fun main(vararg args: String) {
  runBlocking {
    val batch = buildGradingBatch {
      discoverSubmissions("submissions") { t, _ -> t.extension == "jar"}
      discoverSubmissionLibraries("libs") { t, _ -> t.extension == "jar"}
      discoverGraders("graders") { t, _ -> t.extension == "jar"}
      discoverGraderLibraries("solutions") { t, _ -> t.extension == "jar"}
    }
    val executor = executorForBatch(batch).create(Jagr)
    val collector = emptyCollector()
    executor.schedule(Jagr.gradingQueueFactory.create(batch))
    executor.start(collector)
    println(collector.gradingFinished.joinToString("\n") { it.rubrics.keys.first().grade.toString() })
  }
}
