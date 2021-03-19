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

  static Grader descendingPriority(Grader... graders) {
    return FactoryProvider.factory.descendingPriority(graders);
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

  @ApiStatus.Internal
  interface Factory {
    TestAwareBuilder testAwareBuilder();

    Grader descendingPriority(Grader... graders);
  }
}
