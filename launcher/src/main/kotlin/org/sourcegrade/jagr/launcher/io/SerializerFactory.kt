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

import org.sourcegrade.jagr.launcher.env.Jagr
import org.sourcegrade.jagr.launcher.env.serializerFactoryLocator
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
    operator fun <T : Any> get(type: KClass<T>, locator: Locator? = null): SerializerFactory<T> = when (type) {
      ByteArray::class -> ByteArraySerializerFactory
      String::class -> StringSerializerFactory
      else -> type.companionObjectInstance
    } as? SerializerFactory<T>
    // as a last resort, look in the provided factory locator (usually from Jagr.serializerFactoryLocator)
      ?: locator?.get(type)
      ?: error("Could not find serializer factory for $type! (searched standard serializable types and companion object)")

    fun <T : Any> getScoped(type: KClass<T>, locator: Locator? = null): Scoped<T> = get(type, locator) as Scoped<T>
  }

  /**
   * If target type [T] has scoped elements, write/read those too.
   */
  interface Scoped<T : Any> : SerializerFactory<T> {

    fun readScoped(scope: SerializationScope.Input): T

    fun writeScoped(obj: T, scope: SerializationScope.Output)

    fun putInScope(obj: T, scope: SerializationScope)
  }

  interface Locator {
    operator fun <T : Any> get(type: KClass<T>): SerializerFactory<T>?
    fun <T : Any> getScoped(type: KClass<T>): Scoped<T>? = get(type) as Scoped<T>?
  }
}

operator fun <T : Any> SerializerFactory.Factory.get(type: KClass<T>, jagr: Jagr) = get(type, jagr.serializerFactoryLocator)

/* === Reified helper functions === */

inline fun <reified T : Any> SerializerFactory.Factory.get(): SerializerFactory<T> = get(T::class)
inline fun <reified T : Any> SerializerFactory.Factory.getScoped(): SerializerFactory.Scoped<T> = getScoped(T::class)

inline fun <reified T : Any> SerializerFactory.Factory.get(locator: SerializerFactory.Locator): SerializerFactory<T> {
  return get(T::class, locator)
}

inline fun <reified T : Any> SerializerFactory.Factory.getScoped(locator: SerializerFactory.Locator): SerializerFactory.Scoped<T> {
  return getScoped(T::class, locator)
}

inline fun <reified T : Any> SerializerFactory.Factory.get(jagr: Jagr): SerializerFactory<T> {
  return get(T::class, jagr.serializerFactoryLocator)
}

inline fun <reified T : Any> SerializerFactory.Factory.getScoped(jagr: Jagr): SerializerFactory.Scoped<T> {
  return getScoped(T::class, jagr.serializerFactoryLocator)
}

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

inline fun <reified T : Any> SerializationScope.Input.readList(
  elementSerializer: SerializerFactory<T> = SerializerFactory.get(jagr),
): List<T> = ListSerializerFactory(elementSerializer).read(this)

inline fun <reified T : Any> SerializationScope.Output.writeList(
  obj: List<T>,
  elementSerializer: SerializerFactory<T> = SerializerFactory.get(jagr),
) = ListSerializerFactory(elementSerializer).write(obj, this)

inline fun <reified K : Any, reified V : Any> SerializationScope.Input.readMap(
  keySerializer: SerializerFactory<K> = SerializerFactory.get(jagr),
  valueSerializer: SerializerFactory<V> = SerializerFactory.get(jagr),
): Map<K, V> = MapSerializerFactory(keySerializer, valueSerializer).read(this)

inline fun <reified K : Any, reified V : Any> SerializationScope.Output.writeMap(
  obj: Map<K, V>,
  keySerializer: SerializerFactory<K> = SerializerFactory.get(jagr),
  valueSerializer: SerializerFactory<V> = SerializerFactory.get(jagr),
) = MapSerializerFactory(keySerializer, valueSerializer).write(obj, this)

fun SerializationScope.Input.readDynamicList() = DynamicListSerializerFactory.read(this)

fun SerializationScope.Output.writeDynamicList(obj: List<Any>) = DynamicListSerializerFactory.write(obj, this)

fun SerializationScope.Input.readDynamicMap() = DynamicMapSerializerFactory.read(this)

fun SerializationScope.Output.writeDynamicMap(obj: Map<KClass<out Any>, Any>) = DynamicMapSerializerFactory.write(obj, this)

/* === Base serializers === */

internal object StringSerializerFactory : SerializerFactory<String> {
  override fun read(scope: SerializationScope.Input): String = scope.input.readUTF()
  override fun write(obj: String, scope: SerializationScope.Output) {
    scope.output.writeUTF(obj)
  }
}

internal object ByteArraySerializerFactory : SerializerFactory<ByteArray> {
  override fun read(scope: SerializationScope.Input): ByteArray = scope.input.readByteArray()
  override fun write(obj: ByteArray, scope: SerializationScope.Output) {
    scope.output.writeByteArray(obj)
  }
}

/* === Decomposing serializers === */

class ListSerializerFactory<T : Any>(
  private val elementSerializer: SerializerFactory<T>,
) : SerializerFactory<List<T>> {
  override fun read(scope: SerializationScope.Input): List<T> =
    (0 until scope.input.readInt()).map { elementSerializer.read(scope) }

  override fun write(obj: List<T>, scope: SerializationScope.Output) {
    scope.output.writeInt(obj.size)
    for (element in obj) {
      elementSerializer.write(element, scope)
    }
  }
}

class MapSerializerFactory<K : Any, V : Any>(
  private val keySerializer: SerializerFactory<K>,
  private val valueSerializer: SerializerFactory<V>,
) : SerializerFactory<Map<K, V>> {
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

/*
 * === Dynamic decomposing serializers ===
 *
 * Dynamic decomposing serializers differ from the standard decomposing serializers in that they store their
 * runtime type in the external medium (e.g. Input/Output Stream).
 */

/**
 * The dynamic list serializer stores the runtime type for each element in the list directly before the element itself, and
 * uses the serializer for this type to then deserialize the following object.
 */
object DynamicListSerializerFactory : SerializerFactory<List<Any>> {
  override fun read(scope: SerializationScope.Input): List<Any> =
    (0 until scope.input.readInt()).map { scope.readDynamic().second }

  override fun write(obj: List<Any>, scope: SerializationScope.Output) {
    scope.output.writeInt(obj.size)
    for (element in obj) {
      scope.writeDynamic(obj)
    }
  }
}

/**
 * The dynamic map serializer is similar to the [DynamicListSerializerFactory] with the difference being which type is stored.
 * The list serializer stores the runtime type of each element, whereas this map serializer stores the type in the
 * corresponding entry's key. This stored type may be the same as the runtime type of the element, but may also be a supertype.
 */
object DynamicMapSerializerFactory : SerializerFactory<Map<KClass<out Any>, Any>> {
  override fun read(scope: SerializationScope.Input): Map<KClass<out Any>, Any> =
    (0 until scope.input.readInt()).associate { scope.readDynamic() }

  override fun write(obj: Map<KClass<out Any>, Any>, scope: SerializationScope.Output) {
    scope.output.writeInt(obj.size)
    for ((key, value) in obj) {
      scope.writeDynamic(value, key)
    }
  }
}
