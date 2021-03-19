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

package org.jagrkt.common.testing

import com.google.inject.Inject
import org.jagrkt.api.testing.Submission
import org.jagrkt.api.testing.TestCycle
import org.jagrkt.common.compiler.java.RuntimeClassLoader
import org.junit.platform.engine.DiscoverySelector
import org.junit.platform.engine.discovery.DiscoverySelectors
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder
import org.junit.platform.launcher.core.LauncherFactory
import org.junit.platform.launcher.listeners.SummaryGeneratingListener
import org.slf4j.Logger

class JavaRuntimeTester @Inject constructor(
  private val logger: Logger,
) : RuntimeTester {
  override fun createTestCycle(testJar: TestJar, submission: Submission): TestCycle? {
    if (submission !is JavaSubmission) return null
    val info = submission.info
    val rubricProviders = testJar.rubricProviders[info.assignmentId] ?: return null
    val classLoader = RuntimeClassLoader(testJar.classes + submission.compiledClasses)
    val testCycle = JavaTestCycle(rubricProviders, submission, classLoader)
    testJar.testProviders[info.assignmentId]
      ?.map { DiscoverySelectors.selectClass(classLoader.loadClass(it)) }?.runJUnit(testCycle)
    return testCycle
  }

  private fun List<DiscoverySelector>.runJUnit(testCycle: JavaTestCycle): JUnitResultImpl? {
    return try {
      val launcher = LauncherFactory.create()
      val testPlan = launcher.discover(LauncherDiscoveryRequestBuilder.request().selectors(this).build())
      val summaryListener = SummaryGeneratingListener()
      val statusListener = TestStatusListenerImpl(logger)
      launcher.execute(testPlan, summaryListener, statusListener)
      JUnitResultImpl(testPlan, summaryListener, statusListener)
    } catch (e: Throwable) {
      logger.error("Failed to run JUnit tests for ${testCycle.submission.info}", e)
      null
    }
  }
}
