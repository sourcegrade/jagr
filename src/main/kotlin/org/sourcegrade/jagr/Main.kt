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

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.sourcegrade.jagr.launcher.Jagr
import org.sourcegrade.jagr.launcher.executor.MultiWorkerExecutor
import org.sourcegrade.jagr.launcher.executor.ThreadWorkerPool
import org.sourcegrade.jagr.launcher.io.buildGradingBatch

fun main(vararg args: String) {
  runBlocking {
    delay(15000)
    val collector = Jagr {
      executorFactory = MultiWorkerExecutor.Factory {
        workerPoolFactory = ThreadWorkerPool.Factory
      }
    }.launch(
      buildGradingBatch {
        discoverSubmissions("submissions")
        discoverSubmissionLibraries("libs")
        discoverGraders("graders")
        discoverGraderLibraries("solutions")
      }
    )
    println(collector.gradingFinished.joinToString("\n") { it.rubrics.keys.first().grade.toString() })
  }
}
