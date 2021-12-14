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

package org.sourcegrade.jagr.core.rubric

import org.sourcegrade.jagr.api.rubric.Criterion
import org.sourcegrade.jagr.api.rubric.Gradable
import org.sourcegrade.jagr.api.rubric.GradeResult

class GradeResultFactoryImpl : GradeResult.Factory {
    private val none = GradeResultImpl(0, 0)
    override fun ofCorrect(points: Int): GradeResult = GradeResultImpl(0, points)
    override fun ofNone(): GradeResult = none
    override fun of(minReachedPoints: Int, maxReachedPoints: Int): GradeResult =
        GradeResultImpl(maxReachedPoints, minReachedPoints)

    override fun of(minReachedPoints: Int, maxReachedPoints: Int, comment: String): GradeResult =
        GradeResultImpl(maxReachedPoints, minReachedPoints, listOf(comment))

    override fun of(grade: GradeResult, vararg otherGrades: GradeResult): GradeResult = of(grade, otherGrades.asIterable())

    override fun of(grade: GradeResult, otherGrades: Iterable<GradeResult>): GradeResult {
        var correctPoints = grade.maxPoints
        var incorrectPoints = grade.minPoints
        for (otherGrade in otherGrades) {
            correctPoints += otherGrade.maxPoints
            incorrectPoints += otherGrade.minPoints
        }
        return GradeResultImpl(incorrectPoints, correctPoints)
    }

    override fun ofMax(criterion: Criterion): GradeResult = ofCorrect(criterion.maxPoints - criterion.minPoints)

    override fun withComments(grade: GradeResult, comments: Iterable<String>): GradeResult {
        return GradeResultImpl(grade.minPoints, grade.maxPoints, grade.comments + comments)
    }

    override fun clamped(grade: GradeResult, gradable: Gradable<*>): GradeResult {
        if (grade.minPoints >= gradable.minPoints && grade.maxPoints <= gradable.maxPoints) {
            return grade
        }
        return GradeResult.of(
            grade.minPoints.coerceAtLeast(gradable.minPoints),
            grade.maxPoints.coerceAtMost(gradable.maxPoints),
        )
    }
}
