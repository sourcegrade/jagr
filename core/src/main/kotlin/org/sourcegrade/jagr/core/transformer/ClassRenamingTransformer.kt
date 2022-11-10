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
import org.objectweb.asm.MethodVisitor
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
            reader.accept(RenamingCV(writer), 0)
        } else {
            reader.accept(writer, 0)
        }
    }

    private inner class RenamingCV(classVisitor: ClassVisitor?) : ClassVisitor(Opcodes.ASM9, classVisitor) {
        override fun visit(
            version: Int,
            access: Int,
            name: String,
            signature: String?,
            superName: String,
            interfaces: Array<String>,
        ) = super.visit(version, access, newName, signature, superName, interfaces)

        override fun visitMethod(
            access: Int,
            name: String?,
            descriptor: String,
            signature: String?,
            exceptions: Array<out String>?,
        ): MethodVisitor {
            val newDescriptor = descriptor.replace(oldName, newName)
            return RenamingMV(super.visitMethod(access, name, newDescriptor, signature, exceptions))
        }

        private inner class RenamingMV(methodVisitor: MethodVisitor?) : MethodVisitor(Opcodes.ASM9, methodVisitor) {
            override fun visitTypeInsn(opcode: Int, type: String?) {
                // type insns targeting the old classname must be renamed
                if (type == oldName) {
                    super.visitTypeInsn(opcode, newName)
                } else {
                    super.visitTypeInsn(opcode, type)
                }
            }

            override fun visitMethodInsn(opcode: Int, owner: String, name: String, descriptor: String, isInterface: Boolean) {
                // insns targeting the old classname must be renamed
                if (owner == oldName) {
                    super.visitMethodInsn(opcode, newName, name, descriptor, isInterface)
                } else {
                    super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)
                }
            }

            override fun visitFieldInsn(opcode: Int, owner: String?, name: String?, descriptor: String?) {
                // GETFIELDs and PUTFIELDs targeting the old classname must be renamed
                if (opcode == Opcodes.GETFIELD || opcode == Opcodes.PUTFIELD) {
                    super.visitFieldInsn(opcode, newName, name, descriptor)
                } else {
                    super.visitFieldInsn(opcode, owner, name, descriptor)
                }
            }

            override fun visitFrame(type: Int, numLocal: Int, local: Array<out Any>, numStack: Int, stack: Array<out Any>) {
                // local and stack frames must be updated to reflect the new classname
                val newLocal = Array(local.size) { i -> local[i].let { if (it == oldName) newName else it } }
                val newStack = Array(stack.size) { i -> stack[i].let { if (it == oldName) newName else it } }
                super.visitFrame(type, numLocal, newLocal, numStack, newStack)
            }
        }
    }
}
