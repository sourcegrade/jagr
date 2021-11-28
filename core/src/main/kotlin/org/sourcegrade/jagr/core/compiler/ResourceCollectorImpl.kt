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

package org.sourcegrade.jagr.core.compiler

import org.sourcegrade.jagr.launcher.io.SerializationScope
import org.sourcegrade.jagr.launcher.io.SerializerFactory
import org.sourcegrade.jagr.launcher.io.readDynamicMap
import org.sourcegrade.jagr.launcher.io.writeDynamicMap
import kotlin.reflect.KClass

data class ResourceCollectorImpl(
  private val backing: MutableMap<KClass<out Any>, Any> = mutableMapOf(),
) : MutableResourceCollector {

  override fun addResource(value: Any) {
    backing[value::class] = value
  }

  override fun <T : Any> get(type: KClass<T>): T? = backing[type] as T?

  companion object Factory : SerializerFactory<ResourceCollectorImpl> {
    override fun read(scope: SerializationScope.Input) = ResourceCollectorImpl(scope.readDynamicMap().toMutableMap())
    override fun write(obj: ResourceCollectorImpl, scope: SerializationScope.Output) = scope.writeDynamicMap(obj.backing)
  }
}
