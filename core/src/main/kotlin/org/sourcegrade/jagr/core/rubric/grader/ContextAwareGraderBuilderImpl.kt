/*
 *   Jagr - SourceGrade.org
 *   Copyright (C) 2021 Alexander Staeding
 *   Copyright (C) 2021 Contributors
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

package org.sourcegrade.jagr.core.rubric.grader

import org.sourcegrade.jagr.api.rubric.Criterion
import org.sourcegrade.jagr.api.rubric.GradeResult
import org.sourcegrade.jagr.api.rubric.Grader
import org.sourcegrade.jagr.api.rubric.Grader.ContextAwareBuilder
import org.sourcegrade.jagr.api.inspect.CodeContext
import org.sourcegrade.jagr.api.inspect.ContextResolver
import org.sourcegrade.jagr.api.testing.TestCycle

class ContextAwareGraderBuilderImpl<C : CodeContext>(
  private val contextResolver: ContextResolver<C>,
) : AbstractGraderBuilder<ContextAwareBuilder<C>>(), ContextAwareBuilder<C> {
  override fun getThis(): ContextAwareBuilder<C> = this

  private fun ContextAwareBuilder.ContextualPredicate<C>?.expand(): ((TestCycle, Criterion) -> Boolean)? {
    return if (this == null) null else { t, c -> test(t, c, null) }
  }

  private fun ContextAwareBuilder.ContextualGrader<C>?.expand(): ((TestCycle, Criterion) -> GradeResult)? {
    return if (this == null) null else { t, c -> calculate(t, c, null) }
  }

  override fun requireMatch(contextPredicate: ContextAwareBuilder.ContextualPredicate<C>?): ContextAwareBuilder<C> {
    predicate = contextPredicate.expand()
    return this
  }

  override fun pointsPassed(pointCalculator: ContextAwareBuilder.ContextualGrader<C>?): ContextAwareBuilder<C> {
    //pointCalculatorPassed = pointCalculator.expand()
    return this
  }

  override fun pointsFailed(pointCalculator: ContextAwareBuilder.ContextualGrader<C>?): ContextAwareBuilder<C> {
    //pointCalculatorFailed = pointCalculator.expand()
    return this
  }

  override fun build(): Grader {
    TODO("Not yet implemented")
  }
}
