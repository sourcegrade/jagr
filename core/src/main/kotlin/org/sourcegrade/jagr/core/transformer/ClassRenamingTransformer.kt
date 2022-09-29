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

package org.sourcegrade.jagr.core.transformer

import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import org.sourcegrade.jagr.api.testing.ClassTransformer

class ClassRenamingTransformer(
    oldName: String,
    newName: String,
) : ClassTransformer {
    // normalize names
    private val oldName = oldName.replace('.', '/')
    private val newName = newName.replace('.', '/')
    private val name = "$oldName-renamed-to-$newName"
    override fun getName(): String = name
    override fun transform(reader: ClassReader, writer: ClassWriter) {
        if (reader.className == oldName) {
            reader.accept(RenamingVisitor(writer), 0)
        } else {
            reader.accept(writer, 0)
        }
    }
    private inner class RenamingVisitor(classVisitor: ClassVisitor?) : ClassVisitor(Opcodes.ASM9, classVisitor) {
        override fun visit(version: Int, access: Int, name: String, signature: String?, superName: String, interfaces: Array<String>) {
            super.visit(version, access, newName, signature, superName, interfaces)
        }
    }
}
