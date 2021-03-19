package org.jagrkt.common.rubric.grader

import org.jagrkt.api.rubric.Criterion
import org.jagrkt.api.rubric.GradeResult
import org.jagrkt.api.rubric.Grader
import org.jagrkt.api.rubric.Grader.ContextAwareBuilder
import org.jagrkt.api.context.CodeContext
import org.jagrkt.api.context.ContextResolver
import org.jagrkt.api.testing.TestCycle

class ContextAwareGraderBuilderImpl<C : CodeContext>(
  private val contextResolver: ContextResolver<C>,
) : AbstractGraderBuilder<ContextAwareBuilder<C>>(), ContextAwareBuilder<C> {
  override fun getThis(): ContextAwareBuilder<C> = this

  private fun ContextAwareBuilder.ContextualPredicate<C>?.expand(): ((TestCycle, Criterion) -> Boolean)? {
    return if (this == null) null else { t, c -> test(t, c, null) }
  }

  private fun ContextAwareBuilder.ContextualPointCalculator<C>?.expand(): ((TestCycle, Criterion) -> GradeResult)? {
    return if (this == null) null else { t, c -> calculate(t, c, null) }
  }

  override fun requireMatch(contextPredicate: ContextAwareBuilder.ContextualPredicate<C>?): ContextAwareBuilder<C> {
    predicate = contextPredicate.expand()
    return this
  }

  override fun pointsPassed(pointCalculator: ContextAwareBuilder.ContextualPointCalculator<C>?): ContextAwareBuilder<C> {
    pointCalculatorPassed = pointCalculator.expand()
    return this
  }

  override fun pointsFailed(pointCalculator: ContextAwareBuilder.ContextualPointCalculator<C>?): ContextAwareBuilder<C> {
    pointCalculatorFailed = pointCalculator.expand()
    return this
  }

  override fun build(): Grader {
    TODO("Not yet implemented")
  }
}
