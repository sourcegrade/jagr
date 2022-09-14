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

package org.sourcegrade.jagr.launcher.io

import kotlin.properties.ReadOnlyProperty

/**
 * Represents the contents of a grader-info.json file.
 */
interface GraderInfo {
    val name: String
    val assignmentId: String
    val sourceDescriptors: Map<String, List<String>>
    val sourceSets: List<SourceSetInfo>
}

val GraderInfo.graderFiles: List<String> by withDescriptor("grader")

val GraderInfo.solutionFiles: List<String> by withDescriptor("solution")

private fun withDescriptor(name: String): ReadOnlyProperty<GraderInfo, List<String>> = ReadOnlyProperty { info, _ ->
    val descriptors = checkNotNull(info.sourceDescriptors[name]) { "No source descriptor named $name" }
    info.sourceSets.filter { it.name in descriptors }.flatMap { it.files }
}
