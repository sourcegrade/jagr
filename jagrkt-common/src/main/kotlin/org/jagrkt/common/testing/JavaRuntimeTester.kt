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
    val junitResult = testJar.testProviders[info.assignmentId]
      ?.map { DiscoverySelectors.selectClass(classLoader.loadClass(it)) }?.runJUnit(submission)
    return JavaTestCycle(rubricProviders, submission, classLoader, junitResult)
  }

  private fun List<DiscoverySelector>.runJUnit(submission: JavaSubmission): JUnitResultImpl? {
    return try {
      val launcher = LauncherFactory.create()
      val testPlan = launcher.discover(LauncherDiscoveryRequestBuilder.request().selectors(this).build())
      val summaryListener = SummaryGeneratingListener()
      val statusListener = TestStatusListenerImpl(logger)
      launcher.execute(testPlan, summaryListener, statusListener)
      JUnitResultImpl(testPlan, summaryListener, statusListener)
    } catch (e: Throwable) {
      logger.error("Failed to run JUnit tests for ${submission.info}", e)
      null
    }
  }
}
