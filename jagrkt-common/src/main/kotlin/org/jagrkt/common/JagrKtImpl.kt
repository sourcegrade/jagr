/*
 *   JagrKt - JagrKt.org
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

package org.jagrkt.common

import com.google.inject.Inject
import org.jagrkt.api.testing.Submission
import org.jagrkt.common.compiler.java.CompiledClass
import org.jagrkt.common.compiler.java.RuntimeJarLoader
import org.jagrkt.common.export.rubric.GradedRubricExportManager
import org.jagrkt.common.export.submission.SubmissionExportManager
import org.jagrkt.common.extra.ExtrasManager
import org.jagrkt.common.testing.JavaSubmission
import org.jagrkt.common.testing.RuntimeGrader
import org.jagrkt.common.testing.TestJar
import org.slf4j.Logger
import java.io.File

class JagrKtImpl @Inject constructor(
  private val config: Config,
  private val logger: Logger,
  private val runtimeJarLoader: RuntimeJarLoader,
  private val runtimeGrader: RuntimeGrader,
  private val extrasManager: ExtrasManager,
  private val gradedRubricExportManager: GradedRubricExportManager,
  private val submissionExportManager: SubmissionExportManager,
) {

  private fun loadTestJars(testJarsLocation: File): List<TestJar> {
    return testJarsLocation.listFiles { _, t -> t.endsWith(".jar") }!!.parallelMap {
      val classStorage = runtimeJarLoader.loadCompiledJar(it)
      logger.info("Loaded test jar ${it.name}")
      TestJar(logger, it, classStorage)
    }
  }

  private fun loadLibs(libsLocation: File): Map<String, CompiledClass> {
    return libsLocation.listFiles { _, t -> t.endsWith(".jar") }!!
      .asSequence()
      .map {
        val classStorage = runtimeJarLoader.loadCompiledJar(it)
        logger.info("Loaded lib jar ${it.name}")
        classStorage
      }
      .ifEmpty { listOf(mapOf<String, CompiledClass>()).asSequence() }
      .reduce { a, b -> a + b }
  }

  private fun loadSubmissionJars(submissionJarsLocation: File, libsLocation: File): List<JavaSubmission> {
    val libsClasspath = loadLibs(libsLocation)
    return submissionJarsLocation.listFiles { _, t -> t.endsWith(".jar") }!!.parallelMapNotNull {
      with(runtimeJarLoader.loadSourcesJar(it, libsClasspath)) {
        if (submissionInfo == null) {
          logger.error("$it does not have a submission-info.json! Skipping...")
          return@parallelMapNotNull null
        }
        printMessages(
          logger,
          { "Submission $submissionInfo(${file.name}) has $warnings warnings and $errors errors!" },
          { "Submission $file has $warnings warnings!" },
        )
        JavaSubmission(file, submissionInfo, compiledClasses, sourceFiles)
          .apply { logger.info("Loaded submission jar $this") }
      }
    }
  }

  fun run() {
    ensureDirs()
    extrasManager.runExtras()
    val tests = loadTestJars(File(config.dir.tests))
    val submissions = loadSubmissionJars(File(config.dir.submissions), File(config.dir.libs))
    val rubricExportLocation = File(config.dir.rubrics).takeIf { !it.ensure(logger) }
    gradedRubricExportManager.ensureDirs(rubricExportLocation)
    val submissionExportLocation = File(config.dir.submissionsExport).takeIf { !it.ensure(logger) }
    submissionExportManager.ensureDirs(submissionExportLocation)
    if (tests.isEmpty() || submissions.isEmpty()) {
      logger.info("Nothing to do! Exiting...")
      return
    }
    submissions.parallelForEach {
      handleSubmission(it, tests, rubricExportLocation, submissionExportLocation)
    }
  }

  private fun handleSubmission(
    submission: Submission, testJars: List<TestJar>,
    rubricExportLocation: File?, submissionExportLocation: File?,
  ) {
    submissionExportManager.export(submission, submissionExportLocation)
    val gradedRubrics = runtimeGrader.grade(testJars, submission)
    if (gradedRubrics.isEmpty()) {
      logger.warn("$submission :: No matching rubrics!")
      return
    }
    for ((gradedRubric, exportFileName) in gradedRubrics) {
      gradedRubricExportManager.export(gradedRubric, rubricExportLocation, exportFileName)
    }
  }

  private fun ensureDirs() {
    with(config.dir) {
      File("logs").ensure(logger)
      File(libs).ensure(logger)
      File(rubrics).ensure(logger)
      File(submissions).ensure(logger)
      File(tests).ensure(logger)
    }
  }
}
