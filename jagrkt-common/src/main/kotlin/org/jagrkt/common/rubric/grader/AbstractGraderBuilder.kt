package org.jagrkt.common.rubric.grader

import org.jagrkt.api.rubric.Criterion
import org.jagrkt.api.rubric.GradeResult
import org.jagrkt.api.rubric.Grader
import org.jagrkt.api.testing.TestCycle

abstract class AbstractGraderBuilder<B : Grader.Builder<B>> : Grader.Builder<B> {

  var predicate: ((TestCycle, Criterion) -> Boolean)? = null
  var pointCalculatorPassed: ((TestCycle, Criterion) -> GradeResult)? = null
  var pointCalculatorFailed: ((TestCycle, Criterion) -> GradeResult)? = null

  abstract fun getThis(): B

  private fun Grader.Builder.PointCalculator?.expand(): ((TestCycle, Criterion) -> GradeResult)? {
    return if (this == null) null else this::calculate
  }

  override fun pointsPassed(pointCalculator: Grader.Builder.PointCalculator?): B {
    pointCalculatorPassed = pointCalculator.expand()
    return getThis()
  }

  override fun pointsFailed(pointCalculator: Grader.Builder.PointCalculator?): B {
    pointCalculatorFailed = pointCalculator.expand()
    return getThis()
  }
}
