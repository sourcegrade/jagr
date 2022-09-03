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

package org.sourcegrade.jagr.api.rubric;

import com.google.inject.Inject;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.sourcegrade.jagr.api.testing.TestCycle;

/**
 * A functional interface used by Jagr to determine the points for a given {@link TestCycle} and {@link Criterion}.
 *
 * <p>
 * There are two base implementations {@link #testAwareBuilder()} and {@link #descendingPriority(Grader...)} or it may be
 * implemented directly for more fine-grained control.
 * </p>
 */
@FunctionalInterface
public interface Grader {

    /**
     * Creates a new {@link TestAwareBuilder} that can be used to build a {@link Grader} that uses test results to determine
     * the points.
     *
     * @return A new {@link TestAwareBuilder}
     */
    static TestAwareBuilder testAwareBuilder() {
        return FactoryProvider.factory.testAwareBuilder();
    }

    /**
     * Creates a new {@link Grader} that uses the given {@link GradedCriterion}s in descending priority order.
     *
     * @param graders The {@link GradedCriterion}s in descending priority order
     * @return A new {@link Grader}
     */
    static Grader descendingPriority(Grader... graders) {
        return FactoryProvider.factory.descendingPriority(graders);
    }

    /**
     * The grading function determines points for a given {@link TestCycle} and {@link Criterion}.
     *
     * @param testCycle The {@link TestCycle} used to grade
     * @param criterion The {@link Criterion} used to grade
     * @return A {@link GradeResult} resulting from the grading process
     */
    GradeResult grade(TestCycle testCycle, Criterion criterion);

    /**
     * A builder used to create {@link Grader}s.
     *
     * @param <B> The builder type
     */
    @ApiStatus.NonExtendable
    interface Builder<B extends Builder<B>> {

        /**
         * Gives maximum points if this grader passes.
         *
         * @return {@code this}
         */
        default B pointsPassedMax() {
            return pointsPassed((ignored, criterion) -> GradeResult.ofMax(criterion));
        }

        /**
         * Gives minimum points if this grader passes.
         *
         * @return {@code this}
         */
        default B pointsPassedMin() {
            return pointsPassed((ignored, criterion) -> GradeResult.ofMin(criterion));
        }

        /**
         * Sets the grader that should be used to determine the points if this grader passes.
         *
         * @param grader The grader that should be used to determine the points if this grader passes
         * @return {@code this}
         */
        B pointsPassed(@Nullable Grader grader);

        /**
         * Gives maximum points if this grader fails.
         *
         * @return {@code this}
         */
        default B pointsFailedMax() {
            return pointsFailed((ignored, criterion) -> GradeResult.ofMax(criterion));
        }

        /**
         * Gives minimum points if this grader fails.
         *
         * @return {@code this}
         */
        default B pointsFailedMin() {
            return pointsFailed((ignored, criterion) -> GradeResult.ofMin(criterion));
        }

        /**
         * Sets the grader that should be used to determine the points if this grader fails.
         *
         * @param grader The grader that should be used to determine the points if this grader fails
         * @return {@code this}
         */
        B pointsFailed(@Nullable Grader grader);

        /**
         * Sets (or unsets) the general "if failed" comment on this grader. This is added after comments set by other methods on
         * this builder and can only be removed by invoking this method with {@code null}.
         *
         * @param comment The comment to write in the exported rubric if this grader fails. Passing {@code null} unsets the value.
         * @return {@code this}
         */
        B commentIfFailed(@Nullable String comment);

        /**
         * Builds the {@link Grader} that was configured by this builder.
         *
         * @return A new {@link Grader}
         */
        Grader build();
    }

    /**
     * Builds a grader that uses JUnit to determine a grade.
     */
    @ApiStatus.NonExtendable
    interface TestAwareBuilder extends Builder<TestAwareBuilder> {

        /**
         * Requires that the provided {@code testRef} passes.
         *
         * <p>
         * This method may be invoked several times on the same builder to stack effects.
         * </p>
         *
         * @param testRef The test to require
         * @return {@code this}
         */
        default TestAwareBuilder requirePass(TestRef<?> testRef) {
            return requirePass(testRef, null);
        }

        /**
         * Requires that the provided {@code testRef} passes.
         *
         * <p>
         * This method may be invoked several times on the same builder to stack effects.
         * </p>
         *
         * @param testRef The test to require
         * @param comment The comment to write in the exported rubric if this test fails
         * @return {@code this}
         */
        TestAwareBuilder requirePass(TestRef<?> testRef, @Nullable String comment);

        /**
         * Requires that the provided {@code testRef} fails.
         *
         * <p>
         * This method may be invoked several times on the same builder to stack effects.
         * </p>
         *
         * @param testRef The test to require
         * @return {@code this}
         */
        default TestAwareBuilder requireFail(TestRef<?> testRef) {
            return requireFail(testRef, null);
        }

        /**
         * Requires that the provided {@code testRef} fails.
         *
         * <p>
         * This method may be invoked several times on the same builder to stack effects.
         * </p>
         *
         * @param testRef The test to require
         * @param comment The comment to write in the exported rubric if this test passes
         * @return {@code this}
         */
        TestAwareBuilder requireFail(TestRef<?> testRef, @Nullable String comment);
    }

    @ApiStatus.Internal
    final class FactoryProvider {
        @Inject
        private static Factory factory;
    }

    @ApiStatus.Internal
    interface Factory {
        TestAwareBuilder testAwareBuilder();

        Grader descendingPriority(Grader... graders);
    }
}
