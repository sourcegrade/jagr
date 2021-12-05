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

package org.sourcegrade.jagr.core.compiler.java

import org.sourcegrade.jagr.api.testing.ResourceInfo
import org.sourcegrade.jagr.launcher.io.ResourceContainer
import org.sourcegrade.jagr.launcher.io.SerializationScope
import org.sourcegrade.jagr.launcher.io.SerializerFactory
import org.sourcegrade.jagr.launcher.io.keyOf
import org.sourcegrade.jagr.launcher.io.readMap
import org.sourcegrade.jagr.launcher.io.writeMap
import java.util.HashSet

data class RuntimeResources(
  val classes: Map<String, CompiledClass> = mapOf(),
  val resources: Map<String, ByteArray> = mapOf(),
) : ResourceInfo {
  companion object Factory : SerializerFactory<RuntimeResources> {
    val base = keyOf<RuntimeResources>("base")
    override fun read(scope: SerializationScope.Input) = RuntimeResources(scope.readMap(), scope.readMap())

    override fun write(obj: RuntimeResources, scope: SerializationScope.Output) {
      scope.writeMap(obj.classes)
      scope.writeMap(obj.resources)
    }
  }

  override fun getClasses(): MutableSet<String> = HashSet(classes.keys)

  override fun getResources(): MutableSet<String> = HashSet(resources.keys)
}

operator fun RuntimeResources.plus(other: RuntimeResources) =
  RuntimeResources(classes + other.classes, resources + other.resources)

fun RuntimeJarLoader.loadCompiled(containers: Sequence<ResourceContainer>): RuntimeResources {
  return containers
    .map { loadCompiled(it).runtimeResources }
    .ifEmpty { sequenceOf(RuntimeResources()) }
    .reduce { a, b -> a + b }
}
