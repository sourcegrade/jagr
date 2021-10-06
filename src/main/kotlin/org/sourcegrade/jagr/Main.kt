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
import org.sourcegrade.jagr.core.export.rubric.GradedRubricExportManager
import org.sourcegrade.jagr.core.testing.GraderJarImpl
import org.sourcegrade.jagr.launcher.ensure
import org.sourcegrade.jagr.launcher.env.Jagr
import org.sourcegrade.jagr.launcher.env.gradingQueueFactory
import org.sourcegrade.jagr.launcher.env.logger
import org.sourcegrade.jagr.launcher.executor.MultiWorkerExecutor
import org.sourcegrade.jagr.launcher.executor.ProgressBar
import org.sourcegrade.jagr.launcher.executor.ThreadWorkerPool
import org.sourcegrade.jagr.launcher.executor.emptyCollector
import org.sourcegrade.jagr.launcher.io.buildGradingBatch
import org.sourcegrade.jagr.launcher.io.createResourceContainer
import org.sourcegrade.jagr.launcher.opt.Config
import java.io.File

fun main(vararg args: String) {
  runBlocking {
    val startTime = System.currentTimeMillis()
    val batch = buildGradingBatch {
      val submissions = File("submissions").ensure(Jagr.logger) ?: throw AssertionError()
      //discoverSubmissions("submissions") { _, n -> n.endsWith("jar") }
      // TODO: Remove for production
      for (candidate in checkNotNull(submissions.listFiles { _, n -> n.endsWith("jar") }) { "Could not find $submissions" }) {
        repeat(120) {
          addSubmission(createResourceContainer(candidate))
        }
      }
      discoverSubmissionLibraries("libs") { _, n -> n.endsWith("jar") }
      discoverGraders("graders") { _, n -> n.endsWith("jar") }
      discoverGraderLibraries("solutions") { _, n -> n.endsWith("jar") }
    }
    val queue = Jagr.gradingQueueFactory.create(batch)
    Jagr.logger.info("Expected submission: ${batch.expectedSubmissions}")
    val executor = MultiWorkerExecutor.Factory {
      workerPoolFactory = ThreadWorkerPool.Factory {
        concurrency = Jagr.injector.getInstance(Config::class.java).grading.concurrentThreads
      }
    }.create(Jagr)
    val collector = emptyCollector()
    val progress = ProgressBar(collector)
    collector.allocate(queue)
    collector.setListener {
      progress.print()
    }
    executor.schedule(queue)
    executor.start(collector)
    val exporter = Jagr.injector.getInstance(GradedRubricExportManager::class.java)
    val rubricsFile = File("rubrics").ensure(Jagr.logger)!!
    val graderJars = collector.gradingFinished.firstOrNull()?.request?.graderJars ?: return@runBlocking
    exporter.initialize(rubricsFile, graderJars as List<GraderJarImpl>)
    for ((gradedRubric, exportFileName) in collector.gradingFinished
      .asSequence()
      .map { it.rubrics }
      .reduce { acc, map -> acc + map }) {
      exporter.export(gradedRubric, rubricsFile, exportFileName)
    }
    println("Time taken: ${System.currentTimeMillis() - startTime}ms")
  }
}
