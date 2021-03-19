/*
 *   JagrKt - JagrKt.org
 *   Copyright (C) 2021 Alexander Staeding
 *   Copyright (C) 2021 Contributors
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.jagrkt.api.rubric;

import com.google.inject.Inject;
import org.jagrkt.api.context.CodeContext;
import org.jagrkt.api.context.ContextResolver;
import org.jagrkt.api.testing.TestCycle;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface Grader {

  @ApiStatus.Internal
  class FactoryProvider {
    @Inject
    private static Factory factory;
  }

  static TestAwareBuilder testAwareBuilder() {
    return FactoryProvider.factory.testAwareBuilder();
  }

  static <C extends CodeContext> ContextAwareBuilder<C> contextAwareBuilder(ContextResolver<C> resolver) {
    return FactoryProvider.factory.contextAwareBuilder(resolver);
  }

  static Grader descendingPriority(Grader... graders) {
    return FactoryProvider.factory.descendingPriority(graders);
  }

  static Grader minIfAllUnchanged(ContextResolver<?>... contexts) {
    return FactoryProvider.factory.minIfAllUnchanged(contexts);
  }

  @Nullable GradeResult grade(TestCycle testCycle, Criterion criterion);

  @ApiStatus.NonExtendable
  interface Builder<B extends Builder<B>> {

    default B pointsPassedMax() {
      return pointsPassed((ignored, criterion) -> GradeResult.ofMax(criterion));
    }

    default B pointsPassedMin() {
      return pointsPassed((ignored, criterion) -> GradeResult.ofMin(criterion));
    }

    B pointsPassed(@Nullable PointCalculator pointCalculator);

    default B pointsFailedMax() {
      return pointsFailed((ignored, criterion) -> GradeResult.ofMax(criterion));
    }

    default B pointsFailedMin() {
      return pointsFailed((ignored, criterion) -> GradeResult.ofMin(criterion));
    }

    B pointsFailed(@Nullable PointCalculator pointCalculator);

    @FunctionalInterface
    interface PointCalculator {
      GradeResult calculate(TestCycle testCycle, Criterion criterion);
    }

    Grader build();
  }

  /**
   * Builds a grader that uses JUnit to determine a grade
   */
  @ApiStatus.NonExtendable
  interface TestAwareBuilder extends Builder<TestAwareBuilder> {

    TestAwareBuilder requirePass(JUnitTestRef... test);

    TestAwareBuilder requireFail(JUnitTestRef... test);
  }

  /**
   * Builds a grader that looks at the code itself to determine a grade
   */
  @ApiStatus.NonExtendable
  interface ContextAwareBuilder<C extends CodeContext> extends Builder<ContextAwareBuilder<C>> {

    ContextAwareBuilder<C> requireMatch(@Nullable ContextualPredicate<C> contextPredicate);

    ContextAwareBuilder<C> pointsPassed(@Nullable ContextualPointCalculator<C> pointCalculator);

    ContextAwareBuilder<C> pointsFailed(@Nullable ContextualPointCalculator<C> pointCalculator);

    @FunctionalInterface
    interface ContextualPredicate<C extends CodeContext> {
      boolean test(TestCycle testCycle, Criterion criterion, C context);
    }

    @FunctionalInterface
    interface ContextualPointCalculator<C extends CodeContext> {
      GradeResult calculate(TestCycle testCycle, Criterion criterion, C context);
    }
  }

  /**
   * For documentation see static methods above
   */
  @ApiStatus.Internal
  interface Factory {
    TestAwareBuilder testAwareBuilder();

    <C extends CodeContext> ContextAwareBuilder<C> contextAwareBuilder(ContextResolver<C> resolver);

    Grader descendingPriority(Grader... graders);

    Grader minIfAllUnchanged(ContextResolver<?>... contexts);
  }
}
