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

package org.sourcegrade.jagr.core.compiler.java

import org.objectweb.asm.ClassReader
import org.sourcegrade.jagr.launcher.io.SerializationScope
import org.sourcegrade.jagr.launcher.io.SerializerFactory
import org.sourcegrade.jagr.launcher.io.readByteArray
import org.sourcegrade.jagr.launcher.io.readNullable
import org.sourcegrade.jagr.launcher.io.writeByteArray
import org.sourcegrade.jagr.launcher.io.writeNullable
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.net.URI
import javax.tools.JavaFileObject
import javax.tools.SimpleJavaFileObject

sealed class CompiledClass(
  val className: String,
  var source: JavaSourceFile? = null,
) : SimpleJavaFileObject(URI(className), JavaFileObject.Kind.CLASS) {

  abstract val bytecode: ByteArray
  val reader: ClassReader by lazy { ClassReader(bytecode) }

  fun transformed(bytecode: ByteArray): CompiledClass = Existing(className, bytecode, source)

  class Runtime(className: String) : CompiledClass(className) {
    private val outputStream = ByteArrayOutputStream()
    override val bytecode: ByteArray get() = outputStream.toByteArray()
    override fun openOutputStream(): OutputStream = outputStream
  }

  class Existing(
    className: String,
    override val bytecode: ByteArray,
    source: JavaSourceFile? = null,
  ) : CompiledClass(className, source) {
    override fun openInputStream(): InputStream = ByteArrayInputStream(bytecode)
  }

  companion object Factory : SerializerFactory<CompiledClass> {
    override fun read(scope: SerializationScope.Input) = Existing(
      scope.input.readUTF(),
      scope.input.readByteArray(),
      scope.readNullable(),
    )

    override fun write(obj: CompiledClass, scope: SerializationScope.Output) {
      scope.output.writeUTF(obj.className)
      scope.output.writeByteArray(obj.bytecode)
      scope.writeNullable(obj.source)
    }
  }
}
