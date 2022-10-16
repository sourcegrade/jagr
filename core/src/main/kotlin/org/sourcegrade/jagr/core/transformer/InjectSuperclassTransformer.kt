package org.sourcegrade.jagr.core.transformer

import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.sourcegrade.jagr.api.testing.ClassTransformer

class InjectSuperclassTransformer(
    targetName: String,
    newSuperClassName: String,
) : ClassTransformer {
    // normalize names
    private val targetName = targetName.replace('.', '/')
    private val newSuperClassName = newSuperClassName.replace('.', '/')
    private val name = "$targetName-new-super-$newSuperClassName"
    override fun getName(): String = name

    override fun transform(reader: ClassReader, writer: ClassWriter) {
        if (reader.className == targetName) {
            reader.accept(InjectingCV(writer), 0)
        } else {
            reader.accept(writer, 0)
        }
    }

    private inner class InjectingCV(classVisitor: ClassVisitor?) : ClassVisitor(Opcodes.ASM9, classVisitor) {
        lateinit var oldSuperClassName: String
        override fun visit(
            version: Int,
            access: Int,
            name: String,
            signature: String?,
            superName: String,
            interfaces: Array<String>,
        ) {
            oldSuperClassName = superName
            super.visit(version, access, name, signature, newSuperClassName, interfaces)
        }

        override fun visitMethod(
            access: Int,
            name: String,
            descriptor: String?,
            signature: String?,
            exceptions: Array<out String>?,
        ): MethodVisitor = InjectingMV(name, super.visitMethod(access, name, descriptor, signature, exceptions))

        private inner class InjectingMV(
            private val methodName: String,
            methodVisitor: MethodVisitor?,
        ) : MethodVisitor(Opcodes.ASM9, methodVisitor) {

            var visitedSuperConstructor = false

            /**
             * Checks if the insn is a super() call in the constructor of the target class
             */
            private fun isSuperInsn(opcode: Int, owner: String, name: String): Boolean {
                val result = !visitedSuperConstructor &&
                    opcode == Opcodes.INVOKESPECIAL &&
                    methodName == "<init>" &&
                    name == "<init>" &&
                    owner == oldSuperClassName
                if (result) {
                    visitedSuperConstructor = true
                }
                return result
            }

            override fun visitMethodInsn(opcode: Int, owner: String, name: String, descriptor: String?, isInterface: Boolean) {
                if ((owner == oldSuperClassName && opcode == Opcodes.INVOKESPECIAL && name != "<init>") ||
                    isSuperInsn(opcode, owner, name)
                ) {
                    super.visitMethodInsn(opcode, newSuperClassName, name, descriptor, isInterface)
                } else {
                    super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)
                }
            }
        }
    }
}
