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

import com.google.common.io.ByteArrayDataInput
import com.google.common.io.ByteArrayDataOutput
import kotlin.reflect.KClass
import kotlin.reflect.full.companionObjectInstance

interface SerializerFactory<T : Any> {
  companion object Factory {
    @Suppress("UNCHECKED_CAST")
    operator fun <T : Any> get(type: KClass<T>): SerializerFactory<T> = when (type) {
      String::class -> StringSerializerFactory
      else -> type.companionObjectInstance
    } as? SerializerFactory<T>
      ?: error("Could not find serializer factory for $type! (searched standard serializable types and companion object)")
  }

  fun read(input: ByteArrayDataInput, scope: SerializationScope): T
  fun write(obj: T, output: ByteArrayDataOutput, scope: SerializationScope)
}

/* Reified adapter functions */

inline fun <reified T : Any> SerializationScope.get() = get(T::class)
inline fun <reified T : Any> SerializationScope.set(obj: T) = set(T::class, obj)
inline fun <reified T : Any> SerializerFactory.Factory.get(): SerializerFactory<T> = get(T::class)

/* === Nullable serializer adapters === */

fun <T : Any> SerializerFactory<T>.readNullable(input: ByteArrayDataInput, context: SerializationScope): T? {
  return if (input.readNull()) {
    null
  } else {
    read(input, context)
  }
}

fun <T : Any> SerializerFactory<T>.writeNullable(obj: T?, output: ByteArrayDataOutput, context: SerializationScope) {
  if (obj == null) {
    output.writeNull()
  } else {
    output.writeNotNull()
    write(obj, output, context)
  }
}

/* === Serializer inference === */

inline fun <reified T : Any> ByteArrayDataInput.read(scope: SerializationScope) =
  SerializerFactory[T::class].read(this, scope)

inline fun <reified T : Any> ByteArrayDataOutput.write(obj: T, scope: SerializationScope) =
  SerializerFactory[T::class].write(obj, this, scope)

inline fun <reified T : Any> ByteArrayDataInput.readNullable(scope: SerializationScope) =
  SerializerFactory[T::class].readNullable(this, scope)

inline fun <reified T : Any> ByteArrayDataOutput.writeNullable(obj: T?, scope: SerializationScope) =
  SerializerFactory[T::class].writeNullable(obj, this, scope)

/* === Base serializers === */

object StringSerializerFactory : SerializerFactory<String> {
  override fun read(input: ByteArrayDataInput, scope: SerializationScope): String = input.readUTF()
  override fun write(obj: String, output: ByteArrayDataOutput, scope: SerializationScope) {
    output.writeUTF(obj)
  }
}

/* === Decomposing serializers === */

class ListSerializerFactory<T : Any>(type: KClass<T>) : SerializerFactory<List<T>> {
  /**
   * [SerializerFactory] for list's element type.
   */
  private val elementSerializer = SerializerFactory[type]
  override fun read(input: ByteArrayDataInput, scope: SerializationScope): List<T> =
    (0 until input.readInt()).map { elementSerializer.read(input, scope) }

  override fun write(obj: List<T>, output: ByteArrayDataOutput, scope: SerializationScope) {
    output.writeInt(obj.size)
    for (element in obj) {
      elementSerializer.write(element, output, scope)
    }
  }
}

class MapSerializerFactory<K : Any, V : Any>(keyType: KClass<K>, valueType: KClass<V>) : SerializerFactory<Map<K, V>> {
  /**
   * [SerializerFactory] for map's key type.
   */
  private val keySerializer = SerializerFactory[keyType]

  /**
   * [SerializerFactory] for map's value type.
   */
  private val valueSerializer = SerializerFactory[valueType]
  override fun read(input: ByteArrayDataInput, scope: SerializationScope): Map<K, V> {
    return (0 until input.readInt()).associate { keySerializer.read(input, scope) to valueSerializer.read(input, scope) }
  }

  override fun write(obj: Map<K, V>, output: ByteArrayDataOutput, scope: SerializationScope) {
    output.writeInt(obj.size)
    for ((key, value) in obj) {
      keySerializer.write(key, output, scope)
      valueSerializer.write(value, output, scope)
    }
  }
}
