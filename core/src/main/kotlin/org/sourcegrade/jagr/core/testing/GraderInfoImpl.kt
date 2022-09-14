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

package org.sourcegrade.jagr.core.testing

import kotlinx.serialization.Serializable
import org.sourcegrade.jagr.core.compiler.InfoJsonResourceExtractor
import org.sourcegrade.jagr.core.compiler.ResourceExtractor
import org.sourcegrade.jagr.launcher.io.GraderInfo
import org.sourcegrade.jagr.launcher.io.SerializationScope
import org.sourcegrade.jagr.launcher.io.SerializerFactory
import org.sourcegrade.jagr.launcher.io.read
import org.sourcegrade.jagr.launcher.io.readList
import org.sourcegrade.jagr.launcher.io.readMap
import org.sourcegrade.jagr.launcher.io.write
import org.sourcegrade.jagr.launcher.io.writeList
import org.sourcegrade.jagr.launcher.io.writeMap

@Serializable
data class GraderInfoImpl(
    override val name: String,
    override val assignmentId: String,
    override val sourceDescriptors: Map<String, List<String>>,
    override val sourceSets: List<SourceSetInfoImpl>,
) : GraderInfo {

    companion object Factory : SerializerFactory<GraderInfoImpl> {
        override fun read(scope: SerializationScope.Input) = GraderInfoImpl(
            scope.input.readUTF(),
            scope.read(),
            scope.readMap(),
            scope.readList(),
        )

        override fun write(obj: GraderInfoImpl, scope: SerializationScope.Output) {
            scope.output.writeUTF(obj.name)
            scope.write(obj.assignmentId)
            scope.writeMap(obj.sourceDescriptors)
            scope.writeList(obj.sourceSets)
        }
    }

    object Extractor : ResourceExtractor by InfoJsonResourceExtractor<GraderInfoImpl>("grader-info.json")
}
