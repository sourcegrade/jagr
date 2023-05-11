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
import org.sourcegrade.jagr.launcher.env.Jagr
import org.sourcegrade.jagr.launcher.env.serializerFactoryLocator

fun createScope(input: ByteArrayDataInput, jagr: Jagr, parent: SerializationScope.Input? = null): SerializationScope.Input {
    return InputSerializationScopeImpl(input, jagr, parent)
}

inline fun <T> openScope(input: ByteArrayDataInput, jagr: Jagr, block: SerializationScope.Input.() -> T): T {
    return createScope(input, jagr).block()
}

inline fun <T> SerializationScope.Input.openScope(block: SerializationScope.Input.() -> T): T {
    return createScope(input, jagr, this).block()
}

private class InputSerializationScopeImpl(
    override val input: ByteArrayDataInput,
    override val jagr: Jagr,
    override val parent: SerializationScope?,
) : AbstractSerializationScope(), SerializationScope.Input {
    override fun <T : Any> readScoped(key: SerializationScope.Key<T>): T {
        val obj = SerializerFactory[key.type, jagr.serializerFactoryLocator].read(this)
        backing[key] = obj
        return obj
    }
}
