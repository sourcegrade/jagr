/*
 *   Jagr - SourceGrade.org
 *   Copyright (C) 2021 Alexander Staeding
 *   Copyright (C) 2021 Contributors
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.sourcegrade.jagr.launcher.executor

import com.google.common.io.ByteArrayDataInput
import com.google.common.io.ByteArrayDataOutput
import org.sourcegrade.jagr.api.testing.Submission
import org.sourcegrade.jagr.launcher.io.GraderJar
import org.sourcegrade.jagr.launcher.io.SerializationScope
import org.sourcegrade.jagr.launcher.io.SerializerFactory
import org.sourcegrade.jagr.launcher.io.readList
import org.sourcegrade.jagr.launcher.io.read

data class GradingRequest(
  val submission: Submission,
  val graderJars: List<GraderJar>,
) {
  companion object Factory : SerializerFactory<GradingRequest> {
    override fun read(input: ByteArrayDataInput, scope: SerializationScope): GradingRequest =
      GradingRequest(input.read(scope), input.readList(scope))

    override fun write(obj: GradingRequest, output: ByteArrayDataOutput, scope: SerializationScope) {
      TODO("Not yet implemented")
    }
  }
}
