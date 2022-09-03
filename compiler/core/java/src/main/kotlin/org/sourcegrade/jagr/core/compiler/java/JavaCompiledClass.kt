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

package org.sourcegrade.jagr.core.compiler.java

import org.objectweb.asm.ClassReader
import org.sourcegrade.jagr.core.compiler.jvm.JVMCompiledClass
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.net.URI
import javax.tools.JavaFileObject
import javax.tools.SimpleJavaFileObject

sealed class JavaCompiledClass(
    override val className: String,
    override var sourceFile: JavaSourceFile? = null,
) : SimpleJavaFileObject(URI(className), JavaFileObject.Kind.CLASS), JVMCompiledClass {

    val reader: ClassReader by lazy { ClassReader(bytecode) }

    fun transformed(bytecode: ByteArray): JavaCompiledClass = Existing(className, bytecode, sourceFile)

    class Runtime(className: String) : JavaCompiledClass(className) {
        private val outputStream = ByteArrayOutputStream()
        override val bytecode: ByteArray get() = outputStream.toByteArray()
        override fun openOutputStream(): OutputStream = outputStream
    }

    class Existing(
        className: String,
        override val bytecode: ByteArray,
        source: JavaSourceFile? = null,
    ) : JavaCompiledClass(className, source) {
        override fun openInputStream(): InputStream = ByteArrayInputStream(bytecode)
    }
}
