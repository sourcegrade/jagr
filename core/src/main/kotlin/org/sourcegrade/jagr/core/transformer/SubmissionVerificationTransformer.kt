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

class SubmissionVerificationTransformer : ClassTransformer {
    private val name: String = "submission-verification"
    override fun getName(): String = name
    override fun transform(reader: ClassReader, writer: ClassWriter) = reader.accept(SVVisitor(writer), 0)
    private inner class SVVisitor(classVisitor: ClassVisitor?) : ClassVisitor(Opcodes.ASM9, classVisitor) {
        override fun visitMethod(
            access: Int,
            name: String?,
            descriptor: String?,
            signature: String?,
            exceptions: Array<out String>?,
        ) = SVMethodVisitor(super.visitMethod(access, name, descriptor, signature, exceptions))

        private inner class SVMethodVisitor(methodVisitor: MethodVisitor?) : MethodVisitor(Opcodes.ASM9, methodVisitor) {
            override fun visitMethodInsn(opcode: Int, owner: String, name: String, descriptor: String, isInterface: Boolean) {
                val methodInsnElement = MethodInsnElement(opcode, owner, name, descriptor, isInterface)
                if (methodInsnElement.let { baseRules.any { rule -> rule(it) } }) {
                    error("Used illegal instruction $owner.$name$descriptor")
                }
                super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)
            }
        }
    }

    companion object {
        val baseRules = listOf<MethodInsnElement.() -> Boolean>(
            { descriptor.contains("java/lang/reflect") },
            { owner.startsWith("java/lang/reflect") },
            { owner.startsWith("org/sourcegrade") },
            { owner.startsWith("java/lang/Process") },
            { owner.startsWith("java/lang/invoke") },
            { owner == "java/lang/ClassLoader" && !name.startsWith("getResource") },
            { owner == "java/lang/Class" && name == "forName" },
            { owner == "java/lang/System" && name == "exit" },
            { owner == "java/lang/Runtime" },
        )
    }
}
