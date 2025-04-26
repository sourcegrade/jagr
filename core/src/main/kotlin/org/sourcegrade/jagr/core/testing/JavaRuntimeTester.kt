/*
 *   Jagr - SourceGrade.org
 *   Copyright (C) 2021-2022 Alexander Staeding
 *   Copyright (C) 2021-2022 Contributors
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

package org.sourcegrade.jagr.core.testing

import com.google.inject.Inject
import org.apache.logging.log4j.Logger
import org.junit.platform.commons.JUnitException
import org.junit.platform.engine.discovery.ClassSelector
import org.junit.platform.engine.discovery.DiscoverySelectors
import org.junit.platform.launcher.TestExecutionListener
import org.junit.platform.launcher.TestIdentifier
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder
import org.junit.platform.launcher.core.LauncherFactory
import org.junit.platform.launcher.listeners.SummaryGeneratingListener
import org.sourcegrade.jagr.api.testing.Submission
import org.sourcegrade.jagr.api.testing.TestCycle
import org.sourcegrade.jagr.core.compiler.java.RuntimeClassLoaderImpl
import org.sourcegrade.jagr.core.compiler.java.plus
import org.sourcegrade.jagr.core.executor.TimeoutHandler
import org.sourcegrade.jagr.launcher.env.Config

class JavaRuntimeTester @Inject constructor(
    private val logger: Logger,
    private val testCycleParameterResolver: TestCycleParameterResolver,
    private val config: Config,
) : RuntimeTester {
    override fun createTestCycle(grader: GraderJarImpl, submission: Submission): TestCycle? {
        if (submission !is JavaSubmission) return null
        val info = submission.submissionInfo
        if (info.assignmentId != grader.info.assignmentId) {
            logger.warn(
                "Submission $info assignmentId '${info.assignmentId}' != " +
                    "grader's ${grader.info.name} assignmentId '${grader.info.assignmentId}'",
            )
            return null
        }
        val classLoader = RuntimeClassLoaderImpl(
            submission.compileResult.runtimeResources +
                submission.libraries +
                grader.containerWithoutSolution.runtimeResources,
        )
        val testCycle = JavaTestCycle(grader.info.rubricProviderName, submission, classLoader)
        val result = runJUnit(
            grader.testClassNames.map { DiscoverySelectors.selectClass(classLoader.loadClass(it)) },
            testCycle,
        )
        testCycle.jUnitResult = result
        return testCycle
    }

    private fun runJUnit(selectors: List<ClassSelector>, testCycle: TestCycle): JUnitResultImpl? {
        testCycleParameterResolver.value = testCycle
        val info = (testCycle.submission as JavaSubmission).submissionInfo
        logger.info("Running JUnit @ $info :: [${selectors.joinToString { it.className }}]")
        val launcher = LauncherFactory.create()
        val testPlan = try {
            launcher.discover(LauncherDiscoveryRequestBuilder.request().selectors(selectors).build())
        } catch (e: JUnitException) {
            /*
             * If a LinkageError occurred in a JUnit test class, try again with the other test classes.
             * This may occur if a student did not implement a class or method a test class depends on.
             */
            if (e.cause is JUnitException && e.cause?.cause is LinkageError) {
                return selectors.partition { e.cause!!.message?.contains(it.className) == false }.let { (included, excluded) ->
                    val excludedClasses = excluded.joinToString { it.className }
                    val msg = e.cause!!.cause!!.message
                    logger.error("Linkage error @ $info :: $msg, retrying without test classes [$excludedClasses]")
                    runJUnit(included, testCycle)
                }
            }
            logger.error("Failed to discover JUnit tests for $info", e)
            return null
        }
        return try {
            val summaryListener = SummaryGeneratingListener()
            val statusListener = TestStatusListenerImpl(logger)
            if (config.transformers.timeout.enabled) {
                val timeoutListener = object : TestExecutionListener {
                    override fun executionStarted(testIdentifier: TestIdentifier) {
                        TimeoutHandler.resetTimeout()
                    }
                }
                TimeoutHandler.initialize(selectors.mapTo(mutableSetOf()) { it.className }, testCycle)
                launcher.execute(testPlan, summaryListener, statusListener, timeoutListener)
                // disable so that the rubric provider doesn't throw an error
                TimeoutHandler.disableTimeout()
            } else {
                launcher.execute(testPlan, summaryListener, statusListener)
            }
            statusListener.logLinkageErrors(info)
            JUnitResultImpl(testPlan, summaryListener, statusListener)
        } catch (e: Throwable) {
            logger.error("Failed to run JUnit tests for $info", e)
            null
        }
    }
}
