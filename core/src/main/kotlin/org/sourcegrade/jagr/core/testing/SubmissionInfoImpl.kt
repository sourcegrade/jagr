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

package org.sourcegrade.jagr.core.testing

import kotlinx.serialization.Serializable
import org.sourcegrade.jagr.api.testing.SubmissionInfo
import org.sourcegrade.jagr.launcher.io.SerializationScope
import org.sourcegrade.jagr.launcher.io.SerializerFactory

/**
 * Represents the contents of a submission-info.json file
 */
@Serializable
data class SubmissionInfoImpl(
  private val assignmentId: String,
  private val studentId: String,
  private val firstName: String,
  private val lastName: String,
) : SubmissionInfo {
  companion object Factory : SerializerFactory<SubmissionInfoImpl> {
    override fun read(scope: SerializationScope.Input): SubmissionInfoImpl =
      SubmissionInfoImpl(scope.input.readUTF(), scope.input.readUTF(), scope.input.readUTF(), scope.input.readUTF())

    override fun write(obj: SubmissionInfoImpl, scope: SerializationScope.Output) {
      scope.output.writeUTF(obj.assignmentId)
      scope.output.writeUTF(obj.studentId)
      scope.output.writeUTF(obj.firstName)
      scope.output.writeUTF(obj.lastName)
    }
  }

  override fun getAssignmentId(): String = assignmentId
  override fun getStudentId(): String = studentId
  override fun getFirstName(): String = firstName
  override fun getLastName(): String = lastName
  override fun toString(): String = "${assignmentId}_${studentId}_${lastName}_$firstName"
}
