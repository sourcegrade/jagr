package org.jagrkt.common.rubric.grader

import org.jagrkt.api.rubric.Criterion
import org.jagrkt.api.rubric.GradeResult
import org.jagrkt.api.rubric.Grader
import org.jagrkt.api.testing.TestCycle

class ContextAwareGraderImpl(
  private val predicate: ((TestCycle, Criterion) -> Boolean)?,
  private val pointCalculatorPassed: ((TestCycle, Criterion) -> GradeResult)?,
  private val pointCalculatorFailed: ((TestCycle, Criterion) -> GradeResult)?,
) : Grader {

  override fun grade(testCycle: TestCycle, criterion: Criterion): GradeResult? {
    if (predicate?.invoke(testCycle, criterion) == false) {
      return null
    }
    return null // TODO: Finish implementing this
  }
}
