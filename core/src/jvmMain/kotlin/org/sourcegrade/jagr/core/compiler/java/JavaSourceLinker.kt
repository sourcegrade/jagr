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

class JavaSourceLinker(private val sourceFiles: Map<String, JavaSourceFile>) {
    fun link(compiledClass: CompiledClass) {
        val reader = compiledClass.reader
        val visitor = SourceLinkingClassVisitor(reader.className)
        reader.accept(visitor, ClassReader.SKIP_CODE)
        if (visitor.sourceFileName != null) {
            compiledClass.source = sourceFiles[visitor.sourceFileName]
        }
    }
}

fun Map<String, CompiledClass>.linkSource(sourceFiles: Map<String, JavaSourceFile>) {
    val linker = JavaSourceLinker(sourceFiles)
    for ((_, compiledClass) in this) {
        linker.link(compiledClass)
    }
}
