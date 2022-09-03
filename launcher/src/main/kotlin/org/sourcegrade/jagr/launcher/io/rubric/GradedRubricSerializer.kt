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

package org.sourcegrade.jagr.launcher.io.rubric

import org.sourcegrade.jagr.core.rubric.GradedRubricImpl
import org.sourcegrade.jagr.launcher.io.SerializationScope
import org.sourcegrade.jagr.launcher.io.SerializerFactory
import org.sourcegrade.jagr.launcher.io.read
import org.sourcegrade.jagr.launcher.io.readList
import org.sourcegrade.jagr.launcher.io.readScoped
import org.sourcegrade.jagr.launcher.io.write
import org.sourcegrade.jagr.launcher.io.writeList
import org.sourcegrade.jagr.launcher.io.writeScoped

object GradedRubricSerializer : SerializerFactory<GradedRubricImpl> {
    override fun read(scope: SerializationScope.Input) = GradedRubricImpl(
        scope.readScoped(),
        scope.read(),
        scope.read(),
        scope.readList(),
    )

    override fun write(obj: GradedRubricImpl, scope: SerializationScope.Output) {
        scope.writeScoped(obj.testCycle)
        scope.write(obj.grade)
        scope.write(obj.rubric)
        scope.writeList(obj.childCriteria)
    }
}
