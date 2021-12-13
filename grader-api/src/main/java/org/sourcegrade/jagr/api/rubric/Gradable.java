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

import org.jetbrains.annotations.ApiStatus;
import org.sourcegrade.jagr.api.testing.TestCycle;

/**
 * Something that can be graded via a {@link TestCycle}
 *
 * @param <G> The resulting graded type
 */
@ApiStatus.NonExtendable
public interface Gradable<G extends Graded> {

    int getMaxPoints();

    int getMinPoints();

    /**
     * The true maximum number of points is used to calculate the final grade and is not always equal to {@link #getMaxPoints()}.
     *
     * <p>
     * If the maximum number of points is based on other entities (as may be the case in a {@link CriterionHolder}), this
     * method will always return the sum of the true maximum points of all children. Otherwise, if the maximum number of points
     * is <strong>not</strong> based on other entities, this will return the same value as {@link #getMaxPoints()}.
     * </p>
     *
     * @return The true maximum number of points
     */
    int getTrueMaxPoints();

    /**
     * The true minimum number of points is used to calculate the final grade and is not always equal to {@link #getMaxPoints()}.
     *
     * <p>
     * If the minimum number of points is based on other entities (as may be the case in a {@link CriterionHolder}), this
     * method will always return the sum of the true minimum points of all children. Otherwise, if the minimum number of points
     * is <strong>not</strong> based on other entities, this will return the same value as {@link #getMaxPoints()}.
     * </p>
     *
     * @return The true minimum number of points
     */
    int getTrueMinPoints();

    /**
     * Grade the provided {@link TestCycle}
     */
    G grade(TestCycle testCycle);
}
