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
import kotlin.reflect.full.companionObjectInstance

/**
 * Write/Read from stream. Scoped elements are retrieved from scope and not read/written from/to stream.
 */
interface SerializerFactory<T : Any> {

  fun read(scope: SerializationScope.Input): T

  fun write(obj: T, scope: SerializationScope.Output)

  companion object Factory {
    @Suppress("UNCHECKED_CAST")
    operator fun <T : Any> get(type: KClass<T>): SerializerFactory<T> = when (type) {
      String::class -> StringSerializerFactory
      else -> type.companionObjectInstance
    } as? SerializerFactory<T>
      ?: error("Could not find serializer factory for $type! (searched standard serializable types and companion object)")

    fun <T : Any> getScoped(type: KClass<T>): Scoped<T> = get(type) as Scoped<T>
  }

  /**
   * If target type [T] has scoped elements, write/read those too.
   */
  interface Scoped<T : Any> : SerializerFactory<T> {

    fun readScoped(scope: SerializationScope.Input): T

    fun writeScoped(obj: T, scope: SerializationScope.Output)
  }
}

/* === Reified helper functions === */

inline fun <reified T : Any> SerializerFactory.Factory.get(): SerializerFactory<T> = get(T::class)
inline fun <reified T : Any> SerializerFactory.Factory.getScoped(): SerializerFactory.Scoped<T> = getScoped(T::class)

/* === Nullable serializer adapters === */

fun <T : Any> SerializerFactory<T>.readNullable(scope: SerializationScope.Input): T? {
  return if (scope.input.readNull()) {
    null
  } else {
    read(scope)
  }
}

fun <T : Any> SerializerFactory<T>.writeNullable(obj: T?, scope: SerializationScope.Output) {
  if (obj == null) {
    scope.output.writeNull()
  } else {
    scope.output.writeNotNull()
    write(obj, scope)
  }
}

inline fun <reified T : Any> SerializationScope.Input.readList(): List<T> =
  ListSerializerFactory<T>().read(this)

inline fun <reified T : Any> SerializationScope.Output.writeList(obj: List<T>) =
  ListSerializerFactory<T>().write(obj, this)

inline fun <reified K : Any, reified V : Any> SerializationScope.Input.readMap(): Map<K, V> =
  MapSerializerFactory<K, V>().read(this)

inline fun <reified K : Any, reified V : Any> SerializationScope.Output.writeMap(obj: Map<K, V>) =
  MapSerializerFactory<K, V>().write(obj, this)

/* === Base serializers === */

internal object StringSerializerFactory : SerializerFactory<String> {
  override fun read(scope: SerializationScope.Input): String = scope.input.readUTF()
  override fun write(obj: String, scope: SerializationScope.Output) {
    scope.output.writeUTF(obj)
  }
}

/* === Decomposing serializers === */

class ListSerializerFactory<T : Any>(type: KClass<T>) : SerializerFactory<List<T>> {
  companion object Factory {
    inline operator fun <reified T : Any> invoke() = ListSerializerFactory(T::class)
  }

  /**
   * [SerializerFactory] for list's element type.
   */
  private val elementSerializer = SerializerFactory[type]
  override fun read(scope: SerializationScope.Input): List<T> =
    (0 until scope.input.readInt()).map { elementSerializer.read(scope) }

  override fun write(obj: List<T>, scope: SerializationScope.Output) {
    scope.output.writeInt(obj.size)
    for (element in obj) {
      elementSerializer.write(element, scope)
    }
  }
}

class MapSerializerFactory<K : Any, V : Any>(keyType: KClass<K>, valueType: KClass<V>) : SerializerFactory<Map<K, V>> {
  companion object Factory {
    inline operator fun <reified K : Any, reified V : Any> invoke() = MapSerializerFactory(K::class, V::class)
  }

  /**
   * [SerializerFactory] for map's key type.Í
   */
  private val keySerializer = SerializerFactory[keyType]

  /**
   * [SerializerFactory] for map's value type.
   */
  private val valueSerializer = SerializerFactory[valueType]
  override fun read(scope: SerializationScope.Input): Map<K, V> {
    return (0 until scope.input.readInt()).associate { keySerializer.read(scope) to valueSerializer.read(scope) }
  }

  override fun write(obj: Map<K, V>, scope: SerializationScope.Output) {
    scope.output.writeInt(obj.size)
    for ((key, value) in obj) {
      keySerializer.write(key, scope)
      valueSerializer.write(value, scope)
    }
  }
}
