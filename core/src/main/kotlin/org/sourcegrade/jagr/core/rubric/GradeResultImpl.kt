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

import org.sourcegrade.jagr.api.rubric.GradeResult
import org.sourcegrade.jagr.launcher.io.SerializationScope
import org.sourcegrade.jagr.launcher.io.SerializerFactory
import org.sourcegrade.jagr.launcher.io.readList
import org.sourcegrade.jagr.launcher.io.writeList

data class GradeResultImpl(
    private val minPoints: Int,
    private val maxPoints: Int,
    private val comments: List<String> = listOf(),
) : GradeResult {
    init {
        require(minPoints <= maxPoints) {
            "minPoints ($minPoints) for grade result may not be greater than maxPoints ($maxPoints)"
        }
    }

    override fun getMinPoints(): Int = minPoints
    override fun getMaxPoints(): Int = maxPoints
    override fun getComments(): List<String> = comments

    companion object Factory : SerializerFactory<GradeResultImpl> {
        override fun read(scope: SerializationScope.Input): GradeResultImpl = GradeResultImpl(
            scope.input.readInt(),
            scope.input.readInt(),
            scope.readList()
        )

        override fun write(obj: GradeResultImpl, scope: SerializationScope.Output) {
            scope.output.writeInt(obj.minPoints)
            scope.output.writeInt(obj.maxPoints)
            scope.writeList(obj.comments)
        }
    }
}
