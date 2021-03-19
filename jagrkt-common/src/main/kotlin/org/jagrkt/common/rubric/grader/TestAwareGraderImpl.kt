package org.jagrkt.common.rubric.grader

import org.jagrkt.api.rubric.Criterion
import org.jagrkt.api.rubric.GradeResult
import org.jagrkt.api.rubric.Grader
import org.jagrkt.api.testing.TestCycle
import org.junit.platform.engine.TestSource

class TestAwareGraderImpl(
  private val predicate: ((TestCycle, Criterion) -> Boolean)?,
  private val pointCalculatorPassed: ((TestCycle, Criterion) -> GradeResult)?,
  private val pointCalculatorFailed: ((TestCycle, Criterion) -> GradeResult)?,
  private val requirePass: List<TestSource>,
  private val requireFail: List<TestSource>,
) : Grader {

  override fun grade(testCycle: TestCycle, criterion: Criterion): GradeResult? {
    if (predicate?.invoke(testCycle, criterion) == false) {
      return null
    }
    val statusListener = (testCycle.jUnitResult ?: return null).statusListener
    for (test in requirePass) {
      if (!statusListener.succeeded(test)) {
        return pointCalculatorFailed?.invoke(testCycle, criterion)
      }
    }
    for (test in requireFail) {
      if (!statusListener.failed(test)) {
        return pointCalculatorFailed?.invoke(testCycle, criterion)
      }
    }
    return pointCalculatorPassed?.invoke(testCycle, criterion)
  }
}
