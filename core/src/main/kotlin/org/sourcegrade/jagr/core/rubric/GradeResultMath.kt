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

package org.sourcegrade.jagr.core.rubric

import org.sourcegrade.jagr.api.rubric.Gradable
import org.sourcegrade.jagr.api.rubric.GradeResult

operator fun GradeResult.plus(other: GradeResult): GradeResult =
    GradeResultImpl(minPoints + other.minPoints, maxPoints + other.maxPoints, comments + other.comments)

fun Sequence<GradeResult>.sum(): GradeResult = fold(GradeResult.ofNone()) { a, b -> a + b }

fun GradeResult.withComments(comments: Iterable<String>): GradeResult = GradeResult.withComments(this, comments)

fun GradeResult.clamped(gradable: Gradable<*>): GradeResult = GradeResult.clamped(this, gradable)
