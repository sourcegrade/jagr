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

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.google.common.io.ByteStreams
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.sourcegrade.jagr.core.export.rubric.GradedRubricExportManager
import org.sourcegrade.jagr.core.testing.GraderJarImpl
import org.sourcegrade.jagr.launcher.ensure
import org.sourcegrade.jagr.launcher.env.Config
import org.sourcegrade.jagr.launcher.env.Jagr
import org.sourcegrade.jagr.launcher.env.gradingQueueFactory
import org.sourcegrade.jagr.launcher.env.logger
import org.sourcegrade.jagr.launcher.executor.GradingQueue
import org.sourcegrade.jagr.launcher.executor.GradingRequest
import org.sourcegrade.jagr.launcher.executor.GradingResult
import org.sourcegrade.jagr.launcher.executor.MultiWorkerExecutor
import org.sourcegrade.jagr.launcher.executor.ProcessWorker.Companion.MARK_RESULT_BYTE
import org.sourcegrade.jagr.launcher.executor.ProcessWorkerPool
import org.sourcegrade.jagr.launcher.executor.ProgressBar
import org.sourcegrade.jagr.launcher.executor.RubricCollector
import org.sourcegrade.jagr.launcher.executor.SyncExecutor
import org.sourcegrade.jagr.launcher.executor.emptyCollector
import org.sourcegrade.jagr.launcher.executor.toGradingQueue
import org.sourcegrade.jagr.launcher.io.SerializerFactory
import org.sourcegrade.jagr.launcher.io.buildGradingBatch
import org.sourcegrade.jagr.launcher.io.get
import org.sourcegrade.jagr.launcher.io.getScoped
import org.sourcegrade.jagr.launcher.io.openScope
import java.io.ByteArrayOutputStream
import java.io.File

fun main(vararg args: String) = MainCommand().main(args)

class MainCommand : CliktCommand() {
  /**
   * Command line option to indicate that this process will listen to (via std in) to a grading request
   */
  private val child by option("--child", "-c").flag()
  override fun run() {
    if (!child) {
      return standardGrading()
    }
    runBlocking {
      val jagr = Jagr
      val queue = next(jagr)
      val collector = emptyCollector()
      collector.allocate(queue)
      val executor = SyncExecutor(jagr)
      executor.schedule(queue)
      executor.start(collector)
      notifyParent(collector)
    }
  }

  private suspend fun next(jagr: Jagr): GradingQueue = withContext(Dispatchers.IO) {
    openScope(ByteStreams.newDataInput(System.`in`.readAllBytes()), jagr) {
      SerializerFactory.getScoped<GradingRequest>(jagr).readScoped(this)
    }.toGradingQueue()
  }

  private fun notifyParent(collector: RubricCollector) {
    val outputStream = ByteArrayOutputStream(8192)
    val output = ByteStreams.newDataOutput(outputStream)
    System.out.write(MARK_RESULT_BYTE)
    val before = collector.gradingFinished[0]
    openScope(output, Jagr) {
      SerializerFactory.get<GradingResult>().write(before, this)
    }
    outputStream.writeTo(System.out)
    System.out.close()
  }
}

fun standardGrading() {
  runBlocking {
    val startTime = System.currentTimeMillis()
    val batch = buildGradingBatch {
      discoverSubmissions("submissions") { _, n -> n.endsWith("jar") }
      discoverSubmissionLibraries("libs") { _, n -> n.endsWith("jar") }
      discoverGraders("graders") { _, n -> n.endsWith("jar") }
      discoverGraderLibraries("solutions") { _, n -> n.endsWith("jar") }
    }
    val queue = Jagr.gradingQueueFactory.create(batch)
    Jagr.logger.info("Expected submission: ${batch.expectedSubmissions}")
    val executor = MultiWorkerExecutor.Factory {
      workerPoolFactory = ProcessWorkerPool.Factory {
        concurrency = Jagr.injector.getInstance(Config::class.java).grading.concurrentThreads
      }
    }.create(Jagr)
    val collector = emptyCollector()
    val progress = ProgressBar(collector)
    collector.setListener {
      progress.print()
    }
    collector.allocate(queue)
    executor.schedule(queue)
    executor.start(collector)
    export(collector)
    println("Time taken: ${System.currentTimeMillis() - startTime}")
  }
}

fun export(collector: RubricCollector) {
  val exporter = Jagr.injector.getInstance(GradedRubricExportManager::class.java)
  val rubricsFile = File("rubrics").ensure(Jagr.logger)!!
  val graderJars = collector.gradingFinished.firstOrNull()?.request?.graderJars ?: return
  exporter.initialize(rubricsFile, graderJars as List<GraderJarImpl>)
  var counter = 0
  for ((gradedRubric, exportFileName) in collector.gradingFinished.toList()
    .asSequence()
    .map { it.rubrics }
    .reduce { acc, map -> acc + map }) {
    exporter.export(gradedRubric, rubricsFile, (counter++).toString() + exportFileName)
  }
}
