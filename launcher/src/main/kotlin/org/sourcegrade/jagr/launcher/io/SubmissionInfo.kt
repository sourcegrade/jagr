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

import kotlinx.serialization.Serializable

/**
 * Represents the contents of a submission-info.json file
 */
@Serializable
data class SubmissionInfo(
    val assignmentId: String,
    val studentId: String,
    val firstName: String,
    val lastName: String,
    val sourceSets: List<SourceSetInfo>,
) {
    companion object Factory : SerializerFactory<SubmissionInfo> {
        override fun read(scope: SerializationScope.Input) = SubmissionInfo(
            scope.input.readUTF(),
            scope.input.readUTF(),
            scope.input.readUTF(),
            scope.input.readUTF(),
            scope.readList(),
        )

        override fun write(obj: SubmissionInfo, scope: SerializationScope.Output) {
            scope.output.writeUTF(obj.assignmentId)
            scope.output.writeUTF(obj.studentId)
            scope.output.writeUTF(obj.firstName)
            scope.output.writeUTF(obj.lastName)
            scope.writeList(obj.sourceSets)
        }
    }
}
