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

package org.sourcegrade.jagr.launcher.io

import kotlin.reflect.KClass

interface SerializationScope {
  interface Key<T : Any> {
    val type: KClass<T>
    val name: String?
  }

  operator fun <T : Any> get(key: Key<T>): T
  operator fun <T : Any> get(type: KClass<T>): T = get(keyOf(type))
  operator fun <T : Any> set(key: Key<T>, obj: T)
  operator fun <T : Any> set(type: KClass<T>, obj: T) = set(keyOf(type), obj)
}

private class SerializationScopeImpl(private val parent: SerializationScope?) : SerializationScope {
  val backing = mutableMapOf<SerializationScope.Key<*>, Any>()
  override fun <T : Any> get(key: SerializationScope.Key<T>): T {
    val fromBacking = backing[key]
    return if (fromBacking == null) {
      if (parent == null) {
        error("Key $key not found in scope")
      } else {
        parent[key]
      }
    } else {
      fromBacking as T
    }
  }

  override fun <T : Any> set(key: SerializationScope.Key<T>, obj: T) {
    backing[key] = obj
  }
}

fun emptyScope(parent: SerializationScope? = null): SerializationScope = SerializationScopeImpl(parent)

inline fun <T> openScope(block: SerializationScope.() -> T): T = emptyScope().block()
inline fun <T> SerializationScope.openScope(block: SerializationScope.() -> T): T = emptyScope(this).block()

private data class KeyImpl<T : Any>(override val type: KClass<T>, override val name: String?) : SerializationScope.Key<T>

fun <T : Any> keyOf(type: KClass<T>, name: String? = null): SerializationScope.Key<T> = KeyImpl(type, name)
inline fun <reified T : Any> keyOf(name: String? = null): SerializationScope.Key<T> = keyOf(T::class, name)
