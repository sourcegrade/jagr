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

import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import kotlin.reflect.KClass
import kotlin.reflect.full.functions
import kotlin.reflect.jvm.javaMethod

internal data class MethodInsnElement(
  val opcode: Int,
  val owner: String,
  val name: String,
  val descriptor: String,
  val isInterface: Boolean,
) : BytecodeElement {
  override fun withSurrogate(original: KClass<*>, surrogate: KClass<*>): MethodInsnElement {
    val surrogateOwner = Type.getInternalName(surrogate.java)
    val descriptor = descriptor.replace(owner, surrogateOwner)
    return copy(owner = surrogateOwner, descriptor = descriptor)
  }

  companion object Factory : BytecodeElement.Replacer.Factory<MethodInsnElement> {
    override fun create(original: KClass<*>, surrogate: KClass<*>): BytecodeElement.Replacer<MethodInsnElement> {
      val replacementInsns = surrogate.functions.asSequence()
        .mapNotNull { it.javaMethod }
        .map { it.toMethodInsn(original) to it.toMethodInsn() }
        .toMap()
      return BytecodeElement.Replacer {
        replacementInsns[it] ?: error("Could not find method in $surrogate matching ${it.withSurrogate(original, surrogate)}")
      }
    }
  }
}

private fun Method.toMethodInsn(surrogate: KClass<*>? = null): MethodInsnElement {
  // not 100% correct, but it will work in most cases
  // e.g. INVOKESPECIAL via superclass method invocation from base class not covered here
  val opcode = when {
    Modifier.isStatic(modifiers) -> Opcodes.INVOKESTATIC
    Modifier.isPrivate(modifiers) -> Opcodes.INVOKESPECIAL
    declaringClass.isInterface -> Opcodes.INVOKEINTERFACE
    else -> Opcodes.INVOKEVIRTUAL
  }
  var owner = Type.getInternalName(declaringClass)
  var descriptor = Type.getMethodDescriptor(this)
  if (surrogate != null) {
    val surrogateOwner = Type.getInternalName(surrogate.java)
    descriptor = descriptor.replace(owner, surrogateOwner)
    owner = surrogateOwner
  }
  return MethodInsnElement(opcode, owner, name, descriptor, surrogate?.java?.isInterface ?: declaringClass.isInterface)
}
