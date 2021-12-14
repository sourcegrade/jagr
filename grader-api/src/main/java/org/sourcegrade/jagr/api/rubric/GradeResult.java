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

package org.sourcegrade.jagr.api.rubric;

import com.google.inject.Inject;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;

/**
 * Instances of this interface are immutable
 */
@ApiStatus.NonExtendable
public interface GradeResult extends PointRange {

    static GradeResult ofCorrect(int points) {
        return FactoryProvider.factory.ofCorrect(points);
    }

    static GradeResult ofNone() {
        return FactoryProvider.factory.ofNone();
    }

    static GradeResult of(int minReachedPoints, int maxReachedPoints) {
        return FactoryProvider.factory.of(minReachedPoints, maxReachedPoints);
    }

    static GradeResult of(int minReachedPoints, int maxReachedPoints, String comment) {
        return FactoryProvider.factory.of(minReachedPoints, maxReachedPoints, comment);
    }

    static GradeResult of(GradeResult grade, GradeResult... otherGrades) {
        return FactoryProvider.factory.of(grade, otherGrades);
    }

    static GradeResult of(GradeResult grade, Iterable<? extends GradeResult> otherGrades) {
        return FactoryProvider.factory.of(grade, otherGrades);
    }

    static GradeResult ofMax(Criterion criterion) {
        return FactoryProvider.factory.ofMax(criterion);
    }

    static GradeResult ofMin(Criterion criterion) {
        return FactoryProvider.factory.ofMin(criterion);
    }

    static GradeResult withComments(GradeResult grade, Iterable<String> comments) {
        return FactoryProvider.factory.withComments(grade, comments);
    }

    static GradeResult clamped(GradeResult grade, Gradable<?> gradable) {
        return FactoryProvider.factory.clamped(grade, gradable);
    }

    /**
     * @return The lower bound (inclusive) of points that are definitely correct as determined by the automatic grader.
     */
    @Override
    int getMinPoints();

    /**
     * @return The upper bound (inclusive) of points that are definitely correct as determined by the automatic grader.
     */
    @Override
    int getMaxPoints();

    List<String> getComments();

    @ApiStatus.Internal
    final class FactoryProvider {
        @Inject
        private static Factory factory;
    }

    /**
     * For documentation see static methods above
     */
    @ApiStatus.Internal
    interface Factory {
        GradeResult ofCorrect(int points);

        GradeResult ofNone();

        GradeResult of(int minReachedPoints, int maxReachedPoints);

        GradeResult of(int minReachedPoints, int maxReachedPoints, String comment);

        GradeResult of(GradeResult grade, GradeResult... otherGrades);

        GradeResult of(GradeResult grade, Iterable<? extends GradeResult> otherGrades);

        GradeResult ofMax(Criterion criterion);

        GradeResult ofMin(Criterion criterion);

        GradeResult withComments(GradeResult grade, Iterable<String> comments);

        GradeResult clamped(GradeResult grade, Gradable<?> gradable);
    }
}
