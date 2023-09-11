/*
 *   Jagr - SourceGrade.org
 *   Copyright (C) 2021-2022 Alexander Staeding
 *   Copyright (C) 2021-2022 Contributors
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
import org.sourcegrade.jagr.launcher.env.Jagr
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

/**
 * A serialization scope stores [SerializationScope.Key]-value pairs that are global in the context of a
 * single serialization/deserialization.
 *
 * Serialization scopes may be nested. A nested scope checks its own backing map before falling back to the parent scope.
 */
interface SerializationScope {

    val jagr: Jagr

    /**
     * Gets the value for the provided [Key] in this scope. If this scope has no value for the provided [Key], and there is a
     * parents scope, the value of the parent scope's [get] method will be returned. Otherwise, an [IllegalStateException] is
     * thrown.
     */
    operator fun <T : Any> get(key: Key<T>): T

    fun <T : Any> getOrNull(key: Key<T>): T?

    operator fun <T : Any> set(key: Key<T>, obj: T)

    /**
     * Redirects scope access to [key] to [from].
     */
    fun <T : Any> proxy(key: Key<T>, from: Key<T>)

    interface Input : SerializationScope {

        val input: ByteArrayDataInput

        /**
         * Reads the value for the provided [Key] from the current [input][ByteArrayDataInput].
         *
         * Note: invoking this method will modify the current input's reader index.
         */
        fun <T : Any> readScoped(key: Key<T>): T
    }

    interface Output : SerializationScope {

        val output: ByteArrayDataOutput

        /**
         * Writes the value for the provided [Key] to the current [output][ByteArrayDataOutput].
         *
         * Note: invoking this method will modify the current output's writer index.
         */
        fun <T : Any> writeScoped(obj: T, key: Key<T>)
    }

    /**
     * Represents a single entry in this scope's data.
     */
    interface Key<T : Any> {

        val type: KClass<T>

        val name: String?

        companion object KeySerializerFactory : SerializerFactory<Key<*>> {
            /**
             * Fake constructor hack because of generics.
             */
            operator fun <T : Any> invoke(): SerializerFactory<Key<T>> = this as SerializerFactory<Key<T>>

            override fun read(scope: Input): Key<*> =
                KeyImpl(scope.input.readKClass(), scope.readNullable())

            override fun write(obj: Key<*>, scope: Output) {
                scope.output.writeKClass(obj.type)
                scope.writeNullable(obj.name)
            }
        }
    }
}

/* === Reified helper functions === */

inline operator fun <reified T : Any> SerializationScope.get(type: KClass<T>, name: String? = null) = get(keyOf(type, name))
inline fun <reified T : Any> SerializationScope.get(name: String? = null) = get(T::class, name)

inline fun <reified T : Any> SerializationScope.Input.read(factory: SerializerFactory<T> = SerializerFactory.get(jagr)): T {
    return factory.read(this)
}

inline fun <reified T : Any> SerializationScope.Output.write(
    obj: T,
    factory: SerializerFactory<T> = SerializerFactory.get(jagr),
) = factory.write(obj, this)

fun SerializationScope.Input.readDynamic(): Pair<KClass<out Any>, Any> {
    val type = input.readKClass<Any>()
    return type to read(SerializerFactory[type, jagr])
}

@Suppress("UNCHECKED_CAST")
fun SerializationScope.Output.writeDynamic(obj: Any, type: KClass<out Any> = obj::class) {
    require(obj::class.isSubclassOf(type)) { "${obj::class} is not a subtype of $type" }
    output.writeKClass(type)
    write(obj, SerializerFactory[type, jagr] as SerializerFactory<Any>)
}

inline fun <reified T : Any> SerializationScope.Input.readNullable(
    factory: SerializerFactory<T> = SerializerFactory.get(jagr),
): T? = factory.readNullable(this)

inline fun <reified T : Any> SerializationScope.Output.writeNullable(
    obj: T?,
    factory: SerializerFactory<T> = SerializerFactory.get(jagr),
) = factory.writeNullable(obj, this)

inline fun <reified T : Any> SerializationScope.Input.readScoped() = readScoped(keyOf(T::class))
inline fun <reified T : Any> SerializationScope.Output.writeScoped(obj: T) = writeScoped(obj, keyOf(T::class))

inline fun <reified T : Any> keyOf(name: String? = null): SerializationScope.Key<T> = keyOf(T::class, name)
fun <T : Any> keyOf(type: KClass<T>, name: String? = null): SerializationScope.Key<T> = KeyImpl(type, name)
private data class KeyImpl<T : Any>(override val type: KClass<T>, override val name: String?) : SerializationScope.Key<T>
