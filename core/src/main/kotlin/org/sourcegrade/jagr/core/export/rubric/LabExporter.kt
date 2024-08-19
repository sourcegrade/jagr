package org.sourcegrade.jagr.core.export.rubric

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.sourcegrade.jagr.api.rubric.GradedRubric
import org.sourcegrade.jagr.launcher.io.GradedRubricExporter
import org.sourcegrade.jagr.launcher.io.Resource
import org.sourcegrade.jagr.launcher.io.buildResource

class LabExporter : GradedRubricExporter.Lab {

    override fun export(gradedRubric: GradedRubric): Resource {
        val jUnitResult = gradedRubric.testCycle.jUnitResult

        if (jUnitResult != null) {
            val testPlan = jUnitResult.testPlan
            val statusListener = jUnitResult.statusListener

            // Gather detailed test results
            val testResults = testPlan.roots.flatMap { root ->
                // Collect detailed information about each test
                testPlan.getDescendants(root).mapNotNull { testIdentifier ->
                    val testExecutionResult = statusListener.testResults[testIdentifier]

                    // If the test has a result, collect the information
                    testExecutionResult?.let {
                        TestResult(
                            id = testIdentifier.uniqueId,
                            name = testIdentifier.displayName,
                            type = testIdentifier.type.toString(),
                            status = testExecutionResult.status.toString(),
//                            duration = Duration.between(it.startTime, it.endTime).toMillis(),
                            stackTrace = testExecutionResult.throwable.orElse(null)?.stackTraceToString()
                        )
                    }
                }
            }

            // Serialize the results to JSON
            val testResultsJson = TestResults(testResults)
            val jsonString = Json.encodeToString(testResultsJson)

            // Build the Resource with the JSON string
            return buildResource {
                name = "${gradedRubric.testCycle.submission.info}.json"
                outputStream.bufferedWriter().use { it.write(jsonString) }
            }
        } else {
            throw IllegalArgumentException("No JUnitResult present in the test cycle.")
        }
    }

    @Serializable
    data class TestResult(
        val id: String,
        val name: String,
        val type: String,
        val status: String,
//        val duration: Long,
        val stackTrace: String? = null
    )

    @Serializable
    data class TestResults(
        val tests: List<TestResult>
    )
}
