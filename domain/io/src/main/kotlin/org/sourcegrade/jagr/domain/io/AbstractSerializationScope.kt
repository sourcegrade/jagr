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

package org.sourcegrade.jagr.domain.io

import com.google.inject.Key

internal abstract class AbstractSerializationScope : SerializationScope {
    abstract val parent: SerializationScope?
    protected val backing = mutableMapOf<SerializationScope.Key<*>, Any>()
    private val proxy = mutableMapOf<SerializationScope.Key<*>, SerializationScope.Key<*>>()
    private fun <T : Any> getProxy(key: SerializationScope.Key<T>): SerializationScope.Key<T>? {
        return proxy[key] as SerializationScope.Key<T>?
    }

    private fun <T : Any> getInjected(key: SerializationScope.Key<T>): T? {
        return if (key.name == null) {
            jagr.injector.getExistingBinding(Key.get(key.type.java))?.run { provider.get() }
        } else null
    }

    override fun <T : Any> get(key: SerializationScope.Key<T>): T {
        val fromBacking = backing[key]
        return if (fromBacking == null) {
            parent?.getOrNull(key)
                ?: getInjected(key)
                ?: getProxy(key)?.let { get(it) }
                ?: error("Key $key not found in scope")
        } else {
            fromBacking as T
        }
    }

    override fun <T : Any> getOrNull(key: SerializationScope.Key<T>): T? {
        return backing[key] as T?
    }

    override fun <T : Any> set(key: SerializationScope.Key<T>, obj: T) {
        backing[key] = obj
    }

    override fun <T : Any> proxy(key: SerializationScope.Key<T>, from: SerializationScope.Key<T>) {
        proxy[key] = from
    }
}
