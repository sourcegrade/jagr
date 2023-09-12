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

import java.util.List;

/**
 * An immutable int-int tuple containing the points given for a criterion with additional comments.
 */
@ApiStatus.NonExtendable
public interface GradeResult extends PointRange {

    /**
     * Creates a grade result with {@code maxPoints} and {@code minPoints} set to the provided {@code points}.
     *
     * @param points The number of correct points
     * @return A grade result with the provided amount of correct points
     */
    static GradeResult ofCorrect(int points) {
        return FactoryProvider.factory.ofCorrect(points);
    }

    /**
     * Creates a grade result with {@code maxPoints} and {@code minPoints} set to {@code 0}.
     *
     * @return A grade result with no points
     */
    static GradeResult ofNone() {
        return FactoryProvider.factory.ofNone();
    }

    /**
     * Creates a grade result with {@code maxPoints} and {@code minPoints} set based on the provided values.
     *
     * @param minPoints The {@code minPoints} to set
     * @param maxPoints The {@code maxPoints} to set
     * @return A grade result with {@code minPoints} and {@code maxPoints}
     */
    static GradeResult of(int minPoints, int maxPoints) {
        return FactoryProvider.factory.of(minPoints, maxPoints);
    }

    /**
     * Creates a grade result with {@code maxPoints} and {@code minPoints} set based on the provided values and
     * the provided comment.
     *
     * @param minPoints The {@code minPoints} to set
     * @param maxPoints The {@code maxPoints} to set
     * @param comment   The comment to set
     * @return A grade result with {@code minPoints} and {@code maxPoints}
     */
    static GradeResult of(int minPoints, int maxPoints, String comment) {
        return FactoryProvider.factory.of(minPoints, maxPoints, comment);
    }

    /**
     * Extracts {@code minPoints} and {@code maxPoints} from the provided {@link PointRange} and creates a new grade result
     * with those values.
     *
     * @param pointRange The {@link PointRange} to extract {@code minPoints} and {@code maxPoints} from
     * @return A grade result with values from the provided {@link PointRange}
     */
    static GradeResult of(PointRange pointRange) {
        return of(pointRange.getMinPoints(), pointRange.getMaxPoints());
    }

    /**
     * Extracts {@code minPoints} and {@code maxPoints} from the provided {@link PointRange} and creates a new grade result
     * with those values and the provided comment.
     *
     * @param pointRange The {@link PointRange} to extract {@code minPoints} and {@code maxPoints} from
     * @param comment    The comment to set
     * @return A grade result with values from the provided {@link PointRange}
     */
    static GradeResult of(PointRange pointRange, String comment) {
        return of(pointRange.getMinPoints(), pointRange.getMaxPoints(), comment);
    }

    /**
     * Creates a grade result by summing the provided grade results.
     *
     * <p>
     * The {@code maxPoints} and {@code minPoints} values are the sum of all {@code maxPoints} and {@code minPoints} values
     * respectively. All of the comments from the provided grade results are included in order.
     * </p>
     *
     * @param grade       The first grade result
     * @param otherGrades Additional grade results
     * @return A grade result produced from the provided grade results
     */
    static GradeResult of(GradeResult grade, GradeResult... otherGrades) {
        return FactoryProvider.factory.of(grade, otherGrades);
    }

    /**
     * Creates a grade result by summing the provided grade results.
     *
     * <p>
     * The {@code maxPoints} and {@code minPoints} values are the sum of all {@code maxPoints} and {@code minPoints} values
     * respectively. All of the comments from the provided grade results are included in order.
     * </p>
     *
     * @param grade       The first grade result
     * @param otherGrades Additional grade results
     * @return A grade result produced from the provided grade results
     */
    static GradeResult of(GradeResult grade, Iterable<? extends GradeResult> otherGrades) {
        return FactoryProvider.factory.of(grade, otherGrades);
    }

    /**
     * Creates a grade result with both {@code minPoints} and {@code maxPoints} set to the value of
     * {@link Criterion#getMaxPoints()} for the provided {@link Criterion}.
     *
     * @param criterion The {@link Criterion} to check the max points of
     * @return A grade result with the maximum points for the provided {@link Criterion}
     */
    static GradeResult ofMax(Criterion criterion) {
        return FactoryProvider.factory.ofMax(criterion);
    }

    /**
     * Creates a grade result with both {@code minPoints} and {@code maxPoints} set to the value of
     * {@link Criterion#getMinPoints()} for the provided {@link Criterion}.
     *
     * @param criterion The {@link Criterion} to check the min points of
     * @return A grade result with the minimum points for the provided {@link Criterion}
     */
    static GradeResult ofMin(Criterion criterion) {
        return FactoryProvider.factory.ofMin(criterion);
    }

    /**
     * Creates a grade result based on the provided grade result with the provided additional comments.
     *
     * @param grade    The grade result to use
     * @param comments The additional comments
     * @return A grade result with the provided additional comments
     */
    static GradeResult withComments(GradeResult grade, Iterable<String> comments) {
        return FactoryProvider.factory.withComments(grade, comments);
    }

    /**
     * Ensures that the values of provided grade result lie within the range of the provided {@link Gradable}.
     *
     * @param grade    The grade result to use
     * @param gradable The {@link Gradable} to check
     * @return A grade result with both {@code minPoints} and {@code maxPoints} clamped in range
     */
    static GradeResult clamped(GradeResult grade, Gradable<?> gradable) {
        return FactoryProvider.factory.clamped(grade, gradable);
    }

    /**
     * The lower bound (inclusive) of points that are definitely correct as determined by the automatic grader.
     *
     * @return The minimum points
     */
    @Override
    int getMinPoints();

    /**
     * The upper bound (inclusive) of points that are definitely correct as determined by the automatic grader.
     *
     * @return The maximum points
     */
    @Override
    int getMaxPoints();

    List<String> getComments();

    /**
     * Returns a grade result with the same {@link #getMinPoints() minPoints} and {@link #getMaxPoints() maxPoints} as
     * this grade result but without comments.
     *
     * @return A grade result with the same points as this one but without cmments
     */
    default GradeResult withoutComments() {
        if (getComments().isEmpty()) {
            return this;
        } else {
            return GradeResult.of(this);
        }
    }

    @ApiStatus.Internal
    final class FactoryProvider {
        @Inject
        private static Factory factory;
    }

    @ApiStatus.Internal
    interface Factory {
        GradeResult ofCorrect(int points);

        GradeResult ofNone();

        GradeResult of(int minPoints, int maxPoints);

        GradeResult of(int minPoints, int maxPoints, String comment);

        GradeResult of(GradeResult grade, GradeResult... otherGrades);

        GradeResult of(GradeResult grade, Iterable<? extends GradeResult> otherGrades);

        GradeResult ofMax(Criterion criterion);

        GradeResult ofMin(Criterion criterion);

        GradeResult withComments(GradeResult grade, Iterable<String> comments);

        GradeResult clamped(GradeResult grade, Gradable<?> gradable);
    }
}
