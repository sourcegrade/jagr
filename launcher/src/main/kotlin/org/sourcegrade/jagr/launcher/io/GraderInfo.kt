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
@file:UseSerializers(serializerClasses = [SafeStringSerializer::class])

package org.sourcegrade.jagr.launcher.io

import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlin.properties.ReadOnlyProperty

/**
 * Represents the contents of a grader-info.json file.
 */
@Serializable
data class GraderInfo(
    val assignmentId: String,
    val jagrVersion: String,
    val name: String,
    val sourceSets: List<SourceSetInfo>,
) {
    companion object Factory : SerializerFactory<GraderInfo> {
        override fun read(scope: SerializationScope.Input) = GraderInfo(
            scope.input.readUTF(),
            scope.input.readUTF(),
            scope.input.readUTF(),
            scope.readList(),
        )

        override fun write(obj: GraderInfo, scope: SerializationScope.Output) {
            scope.output.writeUTF(obj.assignmentId)
            scope.output.writeUTF(obj.jagrVersion)
            scope.output.writeUTF(obj.name)
            scope.writeList(obj.sourceSets)
        }
    }
}

val GraderInfo.graderFiles: List<String> by named("grader")

val GraderInfo.solutionFiles: List<String> by named("solution")

private fun named(name: String): ReadOnlyProperty<GraderInfo, List<String>> =
    ReadOnlyProperty { info, _ -> info.sourceSets.first { it.name == name }.files }
