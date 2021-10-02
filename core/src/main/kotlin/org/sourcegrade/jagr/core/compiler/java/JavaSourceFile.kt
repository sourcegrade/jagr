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

import com.google.common.io.ByteArrayDataInput
import com.google.common.io.ByteArrayDataOutput
import org.sourcegrade.jagr.api.testing.SourceFile
import org.sourcegrade.jagr.launcher.io.SerializationScope
import org.sourcegrade.jagr.launcher.io.SerializerFactory
import java.net.URI
import javax.tools.JavaFileObject.Kind
import javax.tools.SimpleJavaFileObject

class JavaSourceFile(
  private val className: String,
  private val fileName: String,
  private val content: String,
) : SimpleJavaFileObject(URI.create("string:///$fileName"), Kind.SOURCE), SourceFile {
  companion object Factory : SerializerFactory<JavaSourceFile> {
    override fun read(input: ByteArrayDataInput, scope: SerializationScope): JavaSourceFile =
      JavaSourceFile(input.readUTF(), input.readUTF(), input.readUTF())

    override fun write(obj: JavaSourceFile, output: ByteArrayDataOutput, scope: SerializationScope) {
      output.writeUTF(obj.className)
      output.writeUTF(obj.fileName)
      output.writeUTF(obj.content)
    }
  }

  override fun getFileName(): String = fileName
  override fun getContent(): String = content
  override fun getClassName(): String = className
  override fun getCharContent(ignoreEncodingErrors: Boolean): CharSequence = content
}
