/*
 *   JagrKt - JagrKt.org
 *   Copyright (C) 2021 Alexander Staeding
 *   Copyright (C) 2021 Contributors
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.jagrkt.common.transformer

import com.google.inject.Inject
import org.jagrkt.common.Config
import org.jagrkt.common.compiler.java.CompiledClass
import org.jagrkt.common.compiler.java.RuntimeJarLoader
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter

class TransformerManager @Inject constructor(
  private val config: Config,
  private val timeoutTransformer: TimeoutTransformer,
) {

  private fun TransformData.tryRunTransformer(condition: Boolean, transformer: Transformer) {
    if (condition) {
      transformer.transform(reader, writer)
    }
  }

  private fun TransformData.transform() {
    tryRunTransformer(config.transformers.timeout.enabled, timeoutTransformer)
  }

  private fun MutableMap<String, CompiledClass>.transform(): MutableMap<String, CompiledClass> {
    for ((className, compiledClass) in this) {
      val reader = compiledClass.reader
      val writer = ClassWriter(reader, 0)
      TransformData(reader, writer).transform()
      this[className] = CompiledClass.Existing(className, writer.toByteArray())
    }
    return this
  }

  fun transform(result: RuntimeJarLoader.CompileJarResult): RuntimeJarLoader.CompileJarResult {
    return result.copyWith(compiledClasses = result.compiledClasses.toMutableMap().transform())
  }

  data class TransformData(
    val reader: ClassReader,
    val writer: ClassWriter,
  )
}
