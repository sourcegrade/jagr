package org.jagrkt.common.testing

import com.google.inject.Inject
import org.jagrkt.api.rubric.GradedRubric
import org.jagrkt.api.rubric.RubricProvider
import org.jagrkt.api.testing.Submission
import org.jagrkt.api.testing.TestCycle
import org.slf4j.Logger

class RuntimeGrader @Inject constructor(
  private val logger: Logger,
  private val testers: Set<RuntimeTester>,
) {
  fun grade(tests: List<TestJar>, submission: Submission): Map<GradedRubric, String> {
    val gradedRubrics: MutableMap<GradedRubric, String> = mutableMapOf()
    for (test in tests) {
      for (tester in testers) {
        tester.createTestCycle(test, submission)?.collectResults()?.also { gradedRubrics += it }
      }
    }
    return gradedRubrics
  }

  private fun TestCycle.collectResults(): Map<GradedRubric, String> {
    val result: MutableMap<GradedRubric, String> = mutableMapOf()
    for (rubricProviderName in rubricProviderClassNames) {
      val rubricProvider = try {
        // rubric provider must first be loaded again together with submission classes
        classLoader.loadClass(rubricProviderName).getConstructor().newInstance() as RubricProvider
      } catch (e: Throwable) {
        logger.error("Failed to initialize rubricProvider $rubricProviderName for $submission", e)
        continue
      }
      val exportFileName = rubricProvider.getOutputFileName(submission) ?: submission.info.toString()
      result[rubricProvider.rubric.grade(this)] = exportFileName
    }
    return result
  }
}
