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

import org.apache.logging.log4j.Logger
import org.junit.platform.engine.TestExecutionResult
import org.junit.platform.launcher.TestExecutionListener
import org.junit.platform.launcher.TestIdentifier
import org.junit.platform.launcher.TestPlan
import org.opentest4j.AssertionFailedError
import org.sourcegrade.jagr.api.testing.TestStatusListener
import org.sourcegrade.jagr.launcher.io.SubmissionInfo
import java.util.Collections

internal class TestStatusListenerImpl(
    private val logger: Logger,
) : TestExecutionListener, TestStatusListener {

    private val testResults = mutableMapOf<TestIdentifier, TestExecutionResult>()
    private val linkageErrors = mutableSetOf<Pair<String?, String>>()
    private lateinit var testPlan: TestPlan

    override fun testPlanExecutionStarted(testPlan: TestPlan) {
        this.testPlan = testPlan
        testResults.clear()
    }

    override fun executionFinished(testIdentifier: TestIdentifier, testExecutionResult: TestExecutionResult) {
        testResults[testIdentifier] = aggregate(testIdentifier, testExecutionResult)
        generateSequence(testExecutionResult.throwable.orElse(null)) { it.cause }.forEach { throwable ->
            if (throwable is LinkageError) {
                linkageErrors.add(throwable::class.simpleName to throwable.message + " @ " + throwable.stackTrace.firstOrNull())
            }
        }
    }

    private fun aggregate(testIdentifier: TestIdentifier, parentResult: TestExecutionResult): TestExecutionResult {
        if (parentResult.status == TestExecutionResult.Status.FAILED) {
            return parentResult
        }
        val failedChildren = testPlan.getDescendants(testIdentifier)
            .asSequence()
            .mapNotNull { identifier ->
                testResults[identifier]
                    ?.takeIf { it.status != TestExecutionResult.Status.SUCCESSFUL }
                    ?.let { result -> identifier to result }
            }
            .toMap()
        return if (failedChildren.isEmpty()) {
            parentResult
        } else {
            TestExecutionResult.failed(ContainerFailedError(failedChildren))
        }
    }

    internal fun logLinkageErrors(info: SubmissionInfo) {
        for (errorMessage in linkageErrors) {
            logger.error("Linkage error @ $info :: $errorMessage")
        }
    }

    override fun getTestResults(): Map<TestIdentifier, TestExecutionResult> = Collections.unmodifiableMap(testResults)

    private class ContainerFailedError(
        failedChildren: Map<TestIdentifier, TestExecutionResult>,
    ) : AssertionFailedError(
        failedChildren.entries.first().value.throwable.orElse(null)?.message + when (failedChildren.size) {
            1 -> ""
            2 -> "\n\nthere is 1 more failing test"
            else -> "\n\nthere are ${failedChildren.size - 1} more failing tests"
        },
    )
}
