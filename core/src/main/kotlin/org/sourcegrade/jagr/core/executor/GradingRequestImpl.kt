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

package org.sourcegrade.jagr.core.executor

import org.sourcegrade.jagr.api.testing.Submission
import org.sourcegrade.jagr.core.compiler.java.RuntimeResources
import org.sourcegrade.jagr.launcher.executor.GradingRequest
import org.sourcegrade.jagr.launcher.io.GraderJar
import org.sourcegrade.jagr.launcher.io.SerializationScope
import org.sourcegrade.jagr.launcher.io.SerializerFactory
import org.sourcegrade.jagr.launcher.io.readList
import org.sourcegrade.jagr.launcher.io.readScoped
import org.sourcegrade.jagr.launcher.io.writeList
import org.sourcegrade.jagr.launcher.io.writeScoped

data class GradingRequestImpl(
  override val submission: Submission,
  override val graderJars: List<GraderJar>,
  val baseRuntimeLibraries: RuntimeResources,
  val graderRuntimeLibraries: RuntimeResources,
) : GradingRequest {
  companion object Factory : SerializerFactory.Scoped<GradingRequestImpl> {
    override fun read(scope: SerializationScope.Input): GradingRequestImpl {
      return GradingRequestImpl(
        scope.readScoped(),
        scope.readList(),
        scope[RuntimeResources.base],
        scope[RuntimeResources.grader]
      )
    }

    override fun write(obj: GradingRequestImpl, scope: SerializationScope.Output) {
      scope.writeScoped(obj.submission)
      scope.writeList(obj.graderJars)
    }

    override fun readScoped(scope: SerializationScope.Input): GradingRequestImpl {
      scope.readScoped(RuntimeResources.base)
      scope.readScoped(RuntimeResources.grader)
      return read(scope)
    }

    override fun writeScoped(obj: GradingRequestImpl, scope: SerializationScope.Output) {
      scope.writeScoped(obj.baseRuntimeLibraries, RuntimeResources.base)
      scope.writeScoped(obj.graderRuntimeLibraries, RuntimeResources.grader)
      write(obj, scope)
    }
  }
}
