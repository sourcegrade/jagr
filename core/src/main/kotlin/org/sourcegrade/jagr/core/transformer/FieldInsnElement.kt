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

package org.sourcegrade.jagr.core.transformer

import org.objectweb.asm.Type
import kotlin.reflect.KClass

internal data class FieldInsnElement(
  val opcode: Int,
  val owner: String,
  val name: String,
  val descriptor: String,
) : BytecodeElement {
  override fun withSurrogate(original: KClass<*>, surrogate: KClass<*>): FieldInsnElement {
    val originalDescriptor = Type.getDescriptor(original.java)
    val surrogateDescriptor = Type.getDescriptor(surrogate.java)
    val descriptor = descriptor.replace(originalDescriptor, surrogateDescriptor)
    return copy(descriptor = descriptor)
  }

  companion object Factory : BytecodeElement.Replacer.Factory<FieldInsnElement> {
    override fun create(original: KClass<*>, surrogate: KClass<*>): BytecodeElement.Replacer<FieldInsnElement> {
      return BytecodeElement.Replacer {
        if (it.descriptor.contains(Type.getDescriptor(original.java))) {
          it.withSurrogate(original, surrogate)
        } else null
      }
    }
  }
}
