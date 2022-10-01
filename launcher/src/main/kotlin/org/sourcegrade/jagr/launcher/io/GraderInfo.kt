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
    override val assignmentId: String,
    override val jagrVersion: String,
    override val sourceSets: List<SourceSetInfo>,
    override val dependencyConfigurations: Map<String, Set<String>>,
    override val repositoryConfigurations: List<RepositoryConfiguration>,
    val name: String,
    val rubricProviderName: String,
) : AssignmentArtifactInfo {
    companion object Factory : SerializerFactory<GraderInfo> {
        override fun read(scope: SerializationScope.Input) = GraderInfo(
            scope.input.readUTF(),
            scope.input.readUTF(),
            scope.readList(),
            scope.readMap(),
            scope.readList(),
            scope.input.readUTF(),
            scope.input.readUTF(),
        )

        override fun write(obj: GraderInfo, scope: SerializationScope.Output) {
            scope.output.writeUTF(obj.assignmentId)
            scope.output.writeUTF(obj.jagrVersion)
            scope.writeList(obj.sourceSets)
            scope.writeMap(obj.dependencyConfigurations)
            scope.writeList(obj.repositoryConfigurations)
            scope.output.writeUTF(obj.name)
            scope.output.writeUTF(obj.rubricProviderName)
        }
    }
}

val GraderInfo.graderFiles: Set<String> by named("grader")

val GraderInfo.mainFiles: Set<String> by named("main")

val GraderInfo.testFiles: Set<String> by named("test")

val GraderInfo.solutionFiles: Set<String>
    get() = mainFiles + testFiles

private fun named(name: String): ReadOnlyProperty<GraderInfo, Set<String>> =
    ReadOnlyProperty { info, _ -> info.sourceSets.first { it.name == name }.files.flatMapTo(mutableSetOf()) { it.value } }
