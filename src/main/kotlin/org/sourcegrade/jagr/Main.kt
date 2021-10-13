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
import org.sourcegrade.jagr.launcher.env.Jagr
import org.sourcegrade.jagr.launcher.env.gradingQueueFactory
import org.sourcegrade.jagr.launcher.env.logger
import org.sourcegrade.jagr.launcher.executor.Executor
import org.sourcegrade.jagr.launcher.executor.GradingQueue
import org.sourcegrade.jagr.launcher.executor.GradingRequest
import org.sourcegrade.jagr.launcher.executor.GradingResult
import org.sourcegrade.jagr.launcher.executor.MultiWorkerExecutor
import org.sourcegrade.jagr.launcher.executor.ProcessWorkerPool
import org.sourcegrade.jagr.launcher.executor.RubricCollector
import org.sourcegrade.jagr.launcher.executor.SyncExecutor
import org.sourcegrade.jagr.launcher.executor.emptyCollector
import org.sourcegrade.jagr.launcher.executor.toGradingQueue
import org.sourcegrade.jagr.launcher.io.SerializerFactory
import org.sourcegrade.jagr.launcher.io.buildGradingBatch
import org.sourcegrade.jagr.launcher.io.get
import org.sourcegrade.jagr.launcher.io.getScoped
import org.sourcegrade.jagr.launcher.io.openScope
import org.sourcegrade.jagr.launcher.opt.Config
import java.io.ByteArrayOutputStream
import java.io.File

fun main(vararg args: String) = MainCommand().main(args)

class MainCommand : CliktCommand() {
  /**
   * Command line option to indicate that this process will listen to (via std in) to a grading request
   */
  val child by option("--child", "-c").flag()
  override fun run() {
    Jagr.logger.info("Starting Jagr")
    if (!child) {
      Jagr.logger.info("Running in parent mode")
      return standardGrading()
    }
    Jagr.logger.info("Running in child mode")
    runBlocking {
      val req = next()
      val queue = req.toGradingQueue()
      Jagr.logger.info("Processing queue $queue")
      notifyParent(grade(queue, SyncExecutor(Jagr)))
    }
  }

  private suspend fun next(): GradingRequest = withContext(Dispatchers.IO) {
    openScope(ByteStreams.newDataInput(System.`in`.readAllBytes()), Jagr) {
      SerializerFactory.getScoped<GradingRequest>(jagr).readScoped(this)
    }
  }

  private fun notifyParent(collector: RubricCollector) {
    val outputStream = ByteArrayOutputStream(8192)
    val output = ByteStreams.newDataOutput(outputStream)
    System.out.write(7)
    val before = collector.gradingFinished[0]
    openScope(output, Jagr) {
      SerializerFactory.get<GradingResult>().write(before, this)
    }
//    val result = openScope(ByteStreams.newDataInput(outputStream.toByteArray()), Jagr) {
//      this[keyOf(GradingRequest::class)] = request
//      this[keyOf(JavaSubmission::class)] = request.submission as JavaSubmission
//      request as GradingRequestImpl
//      this[RuntimeResources.base] = request.baseRuntimeLibraries
//      this[RuntimeResources.grader] = request.graderRuntimeLibraries
//      SerializerFactory.get<GradingResult>().read(this)
//    }
//    println("==============================================")
//    println("================ Before ======================")
//    println("==============================================")
//    println(before)
//    println("==============================================")
//    println("================ After ======================")
//    println("==============================================")
//    println(result)
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
    export(grade(queue, executor))
    println("Time taken: ${System.currentTimeMillis() - startTime}")
  }
}

suspend fun grade(queue: GradingQueue, executor: Executor): RubricCollector {
  val collector = emptyCollector()
  collector.allocate(queue)
  executor.schedule(queue)
  executor.start(collector)
  return collector
}

fun export(collector: RubricCollector) {
  val exporter = Jagr.injector.getInstance(GradedRubricExportManager::class.java)
  val rubricsFile = File("rubrics").ensure(Jagr.logger)!!
  val graderJars = collector.gradingFinished.firstOrNull()?.request?.graderJars ?: return
  exporter.initialize(rubricsFile, graderJars as List<GraderJarImpl>)
  for ((gradedRubric, exportFileName) in collector.gradingFinished.toList()
    .asSequence()
    .map { it.rubrics }
    .reduce { acc, map -> acc + map }) {
    exporter.export(gradedRubric, rubricsFile, exportFileName)
  }
}
