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

import kotlin.reflect.KClass

data class BytecodeReplacement(
  val field: BytecodeElement.Replacer<FieldElement>,
  val fieldInsn: BytecodeElement.Replacer<FieldInsnElement>,
  val methodInsn: BytecodeElement.Replacer<MethodInsnElement>,
) {
  constructor(
    fieldFactory: BytecodeElement.Replacer.Factory<FieldElement>,
    fieldInsnFactory: BytecodeElement.Replacer.Factory<FieldInsnElement>,
    methodFactory: BytecodeElement.Replacer.Factory<MethodInsnElement>,
    original: KClass<*>,
    surrogate: KClass<*>,
  ) : this(
    fieldFactory.create(original, surrogate),
    fieldInsnFactory.create(original, surrogate),
    methodFactory.create(original, surrogate),
  )
}

interface BytecodeElement {
  fun withSurrogate(original: KClass<*>, surrogate: KClass<*>): BytecodeElement
  fun interface Replacer<T : BytecodeElement> {
    fun replace(element: T): T?
    interface Factory<T : BytecodeElement> {
      fun create(original: KClass<*>, surrogate: KClass<*>): Replacer<T>
    }
  }
}

infix fun KClass<*>.replaces(originalType: KClass<*>): BytecodeReplacement {
  return BytecodeReplacement(FieldElement, FieldInsnElement, MethodInsnElement, originalType, this)
}

inline fun <reified T : BytecodeElement, R> T.replace(replacement: BytecodeElement.Replacer<T>, block: (T) -> R): R {
  return block(replacement.replace(this) ?: this)
}
