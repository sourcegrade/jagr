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

import org.sourcegrade.jagr.api.rubric.Criterion
import org.sourcegrade.jagr.api.rubric.CriterionHolderPointCalculator
import org.sourcegrade.jagr.core.rubric.CriterionImpl
import org.sourcegrade.jagr.launcher.io.SerializationScope
import org.sourcegrade.jagr.launcher.io.SerializerFactory
import org.sourcegrade.jagr.launcher.io.readList
import org.sourcegrade.jagr.launcher.io.readNullable
import org.sourcegrade.jagr.launcher.io.writeList
import org.sourcegrade.jagr.launcher.io.writeNullable

internal object CriterionSerializer : SerializerFactory<Criterion> {
    override fun read(scope: SerializationScope.Input) = CriterionImpl(
        scope.input.readUTF(),
        scope.readNullable(),
        // The next line is *technically* incorrect, but it won't be used anyways so this is ok.
        // Serializing a grader would require cooperation from the implementer. This doesn't really make any
        // sense though, as it won't be used after serialization (which happens after rubrics have been graded).
        null,
        CriterionHolderPointCalculator.fixed(scope.input.readInt()),
        CriterionHolderPointCalculator.fixed(scope.input.readInt()),
        scope.readList(),
    )

    override fun write(obj: Criterion, scope: SerializationScope.Output) {
        scope.output.writeUTF(obj.shortDescription)
        scope.writeNullable(obj.hiddenNotes)
        scope.output.writeInt(obj.maxPoints)
        scope.output.writeInt(obj.minPoints)
        scope.writeList(obj.childCriteria)
    }
}
