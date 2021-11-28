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
@file:UseSerializers(serializerClasses = [TrimmingStringSerializer::class])

package org.sourcegrade.jagr.core.testing

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import org.sourcegrade.jagr.api.testing.SubmissionInfo
import org.sourcegrade.jagr.core.compiler.MutableResourceCollector
import org.sourcegrade.jagr.core.compiler.ResourceExtractor
import org.sourcegrade.jagr.launcher.env.Jagr
import org.sourcegrade.jagr.launcher.env.logger
import org.sourcegrade.jagr.launcher.io.Resource
import org.sourcegrade.jagr.launcher.io.ResourceContainerInfo
import org.sourcegrade.jagr.launcher.io.SerializationScope
import org.sourcegrade.jagr.launcher.io.SerializerFactory
import org.sourcegrade.jagr.launcher.io.readList
import org.sourcegrade.jagr.launcher.io.writeList
import java.io.ByteArrayInputStream

/**
 * Represents the contents of a submission-info.json file
 */
@Serializable
data class SubmissionInfoImpl(
  private val assignmentId: String,
  private val studentId: String,
  private val firstName: String,
  private val lastName: String,
  val sourceSets: List<SourceSetInfo>,
) : SubmissionInfo {
  override fun getAssignmentId(): String = assignmentId
  override fun getStudentId(): String = studentId
  override fun getFirstName(): String = firstName
  override fun getLastName(): String = lastName
  override fun toString(): String = "${assignmentId}_${studentId}_${lastName}_$firstName"

  companion object Factory : SerializerFactory<SubmissionInfoImpl> {
    override fun read(scope: SerializationScope.Input) = SubmissionInfoImpl(
      scope.input.readUTF(),
      scope.input.readUTF(),
      scope.input.readUTF(),
      scope.input.readUTF(),
      scope.readList(),
    )

    override fun write(obj: SubmissionInfoImpl, scope: SerializationScope.Output) {
      scope.output.writeUTF(obj.assignmentId)
      scope.output.writeUTF(obj.studentId)
      scope.output.writeUTF(obj.firstName)
      scope.output.writeUTF(obj.lastName)
      scope.writeList(obj.sourceSets)
    }
  }

  object Extractor : ResourceExtractor {
    @OptIn(ExperimentalSerializationApi::class)
    override fun extract(
      containerInfo: ResourceContainerInfo,
      resource: Resource,
      data: ByteArray,
      collector: MutableResourceCollector,
    ) {
      if (resource.name == "submission-info.json") {
        try {
          collector.addResource(Json.decodeFromStream<SubmissionInfoImpl>(ByteArrayInputStream(data)))
        } catch (e: Exception) {
          Jagr.logger.error("${containerInfo.name} has invalid submission-info.json", e)
        }
      }
    }
  }
}

@Serializable
data class SourceSetInfo(
  val name: String,
  val files: List<String>,
) {
  companion object Factory : SerializerFactory<SourceSetInfo> {
    override fun read(scope: SerializationScope.Input) = SourceSetInfo(scope.input.readUTF(), scope.readList())

    override fun write(obj: SourceSetInfo, scope: SerializationScope.Output) {
      scope.output.writeUTF(obj.name)
      scope.writeList(obj.files)
    }
  }
}

private object TrimmingStringSerializer : KSerializer<String> {
  override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("TrimmingString", PrimitiveKind.STRING)
  override fun deserialize(decoder: Decoder): String = decoder.decodeString().trim()
  override fun serialize(encoder: Encoder, value: String) = encoder.encodeString(value.trim())
}
