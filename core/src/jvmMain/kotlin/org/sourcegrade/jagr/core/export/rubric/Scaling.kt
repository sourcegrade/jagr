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

package org.sourcegrade.jagr.core.export.rubric

import org.sourcegrade.jagr.api.rubric.PointRange

private fun Int.scaled(factor: Double): String {
    return String.format("%.2f", this.toDouble() * factor)
}

internal fun PointRange.scaledRange(): String {
    val scaleFactor = 0.5
    return if (minPoints == maxPoints) {
        minPoints.scaled(scaleFactor)
    } else {
        "[${minPoints.scaled(scaleFactor)}, ${maxPoints.scaled(scaleFactor)}]"
    }
}
