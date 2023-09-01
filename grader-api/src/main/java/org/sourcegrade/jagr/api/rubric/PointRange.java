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

import org.jetbrains.annotations.ApiStatus;

/**
 * A range of points, usually represented by the interval (both inclusive) <code>[minPoints, maxPoints]</code>.
 *
 * <p>
 * Usually not used directly, instead may be constructed for example via static factory methods in {@link GradeResult}.
 * </p>
 *
 * @see GradeResult
 * @see Criterion
 * @see Gradable
 */
@ApiStatus.NonExtendable
public interface PointRange {

    /**
     * The minimum number of points in this range.
     *
     * @return The minimum number of points.
     */
    int getMinPoints();

    /**
     * The maximum number of points in this range.
     *
     * @return The maximum number of points.
     */
    int getMaxPoints();

    /**
     * Creates the standard String representation of the given range.
     *
     * <p>
     * If <code>minPoints == maxPoints</code>, returns a String with a single number.
     * </p>
     * <p>
     * Otherwise (if <code>minPoints != maxPoints</code>), returns a String in the format
     * </p>
     * <pre><code>[minPoints, maxPoints]</code></pre>
     *
     * @param range The range to convert to a String
     * @return The standard String representation of the given range.
     */
    static String toString(PointRange range) {
        final int minPoints = range.getMinPoints();
        final int maxPoints = range.getMaxPoints();
        if (minPoints == maxPoints) {
            return Integer.toString(minPoints);
        }
        return String.format("[%d, %d]", minPoints, maxPoints);
    }
}
