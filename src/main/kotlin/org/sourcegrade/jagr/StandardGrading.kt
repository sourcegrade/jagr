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
import org.sourcegrade.jagr.launcher.env.Environment
import org.sourcegrade.jagr.launcher.env.Jagr
import org.sourcegrade.jagr.launcher.env.config
import org.sourcegrade.jagr.launcher.env.extrasManager
import org.sourcegrade.jagr.launcher.env.gradingQueueFactory
import org.sourcegrade.jagr.launcher.env.logger
import org.sourcegrade.jagr.launcher.executor.MultiWorkerExecutor
import org.sourcegrade.jagr.launcher.executor.ProcessWorkerPool
import org.sourcegrade.jagr.launcher.executor.ProgressBar
import org.sourcegrade.jagr.launcher.executor.RubricCollector
import org.sourcegrade.jagr.launcher.executor.SyncExecutor
import org.sourcegrade.jagr.launcher.executor.ThreadWorkerPool
import org.sourcegrade.jagr.launcher.executor.emptyCollector
import org.sourcegrade.jagr.launcher.io.GradedRubricExporter
import org.sourcegrade.jagr.launcher.io.ProgressAwareOutputStream
import org.sourcegrade.jagr.launcher.io.SubmissionExporter
import org.sourcegrade.jagr.launcher.io.buildGradingBatch
import org.sourcegrade.jagr.launcher.io.export
import org.sourcegrade.jagr.launcher.io.writeAsDirIn
import org.sourcegrade.jagr.launcher.io.writeIn
import java.io.File

class StandardGrading(
    private val rainbowProgressBar: Boolean,
    private val jagr: Jagr = Jagr,
) {
    fun grade(exportOnly: Boolean) = runBlocking {
        val config = jagr.config
        File(config.dir.submissions).ensure(jagr.logger)
        jagr.extrasManager.runExtras()
        val batch = buildGradingBatch {
            discoverGraders(config.dir.graders) { _, n -> n.endsWith("jar") }
            discoverSubmissions(config.dir.submissions) { _, n -> n.endsWith("jar") }
            discoverLibraries(config.dir.libs) { _, n -> n.endsWith("jar") }
        }
        val queue = jagr.gradingQueueFactory.create(batch)
        jagr.logger.info("Beginning export")
        val submissionExportFile = File(config.dir.submissionsExport).ensure(jagr.logger)!!
        for (resourceContainer in jagr.injector.getInstance(SubmissionExporter.Gradle::class.java).export(queue)) {
            resourceContainer.writeAsDirIn(submissionExportFile)
        }
        if (exportOnly) {
            jagr.logger.info("Only exporting, finished!")
            return@runBlocking
        }
        jagr.logger.info("Expected submission: ${batch.expectedSubmissions}")
        val mode = config.executor.mode
        val executor = if (mode == "single") {
            SyncExecutor(jagr)
        } else {
            MultiWorkerExecutor.Factory {
                workerPoolFactory = when (mode) {
                    "process" -> ProcessWorkerPool.Factory { concurrency = config.executor.concurrency }
                    "thread" -> ThreadWorkerPool.Factory { concurrency = config.executor.concurrency }
                    else -> error("Invalid executor mode $mode. Must be one of \"single\", \"thread\" or \"process\".")
                }
            }.create(jagr)
        }
        val collector = emptyCollector(jagr)
        val progress = ProgressBar(collector, rainbowProgressBar)
        ProgressAwareOutputStream.progressBar = progress
        collector.setListener { result ->
            result.rubrics.keys.forEach { it.logGradedRubric(jagr) }
        }
        collector.allocate(queue)
        executor.schedule(queue)
        executor.start(collector)
        ProgressAwareOutputStream.progressBar = null
        Environment.cleanupMainProcess()
        collector.logHistogram(jagr)
        export(collector)
    }

    private fun export(collector: RubricCollector) {
        val csvExporter = jagr.injector.getInstance(GradedRubricExporter.CSV::class.java)
        val htmlExporter = jagr.injector.getInstance(GradedRubricExporter.HTML::class.java)
        val config = jagr.config
        val rubricsFile = File(config.dir.rubrics).ensure(jagr.logger)!!
        val csvFile = rubricsFile.resolve("csv").ensure(jagr.logger)!!
        val htmlFile = rubricsFile.resolve("moodle").ensure(jagr.logger)!!
        if (collector.gradingFinished.isEmpty()) {
            jagr.logger.warn("No rubrics!")
            return
        }
        for (gradedRubric in collector.gradingFinished.asSequence().flatMap { it.rubrics.keys }) {
            try {
                csvExporter.export(gradedRubric).writeIn(csvFile)
            } catch (e: Exception) {
                jagr.logger.error("Could not export $csvFile", e)
            }
            try {
                htmlExporter.export(gradedRubric).writeIn(htmlFile)
            } catch (e: Exception) {
                jagr.logger.error("Could not export $htmlFile")
            }
        }
    }
}
