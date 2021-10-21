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

import com.google.common.io.ByteArrayDataOutput
import org.sourcegrade.jagr.launcher.env.Jagr
import org.sourcegrade.jagr.launcher.env.serializerFactoryLocator

private class OutputSerializationScopeImpl(
  override val output: ByteArrayDataOutput,
  override val jagr: Jagr,
  override val parent: SerializationScope.Output?,
) : AbstractSerializationScope(), SerializationScope.Output {
  override fun <T : Any> writeScoped(obj: T, key: SerializationScope.Key<T>) {
    SerializerFactory[key.type, jagr.serializerFactoryLocator].write(obj, this)
    backing[key] = obj
  }
}

fun createScope(output: ByteArrayDataOutput, jagr: Jagr, parent: SerializationScope.Output? = null): SerializationScope.Output {
  return OutputSerializationScopeImpl(output, jagr, parent)
}

// fun 1
inline fun <T> openScope(output: ByteArrayDataOutput, jagr: Jagr, block: SerializationScope.Output.() -> T): T {
  return createScope(output, jagr).block()
}

// fun 2
inline fun <T> SerializationScope.Output.openScope(block: SerializationScope.Output.() -> T): T {
  return createScope(output, jagr, this).block()
}
