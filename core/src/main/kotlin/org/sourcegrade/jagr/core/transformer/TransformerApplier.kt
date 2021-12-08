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

import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Type
import org.sourcegrade.jagr.api.testing.ClassTransformer
import org.sourcegrade.jagr.core.compiler.java.CompiledClass
import org.sourcegrade.jagr.core.compiler.java.JavaCompiledContainer
import kotlin.reflect.KFunction
import kotlin.reflect.jvm.javaMethod

fun interface TransformationApplier {
    fun transform(result: JavaCompiledContainer): JavaCompiledContainer
}

operator fun TransformationApplier.plus(other: TransformationApplier): TransformationApplier {
    if (this is NoOpTransformerAppliedImpl) return other
    if (other is NoOpTransformerAppliedImpl) return this
    return TransformationApplier { other.transform(transform(it)) }
}

private object NoOpTransformerAppliedImpl : TransformationApplier {
    override fun transform(result: JavaCompiledContainer): JavaCompiledContainer = result
}

private class TransformerApplierImpl(private val transformer: ClassTransformer) : TransformationApplier {
    override fun transform(result: JavaCompiledContainer): JavaCompiledContainer = result.copy(
        runtimeResources = result.runtimeResources.copy(classes = result.runtimeResources.classes.transform(transformer))
    )
}

private class MultiTransformerApplierImpl(private vararg val transformers: ClassTransformer) : TransformationApplier {
    override fun transform(result: JavaCompiledContainer): JavaCompiledContainer {
        var classes = result.runtimeResources.classes
        for (transformer in transformers) {
            classes = classes.transform(transformer)
        }
        return result.copy(runtimeResources = result.runtimeResources.copy(classes = classes))
    }
}

fun applierOf(vararg transformers: ClassTransformer): TransformationApplier {
    return when (transformers.size) {
        0 -> NoOpTransformerAppliedImpl
        1 -> TransformerApplierImpl(transformers[0])
        else -> MultiTransformerApplierImpl(*transformers)
    }
}

infix fun List<ClassTransformer>.useWhen(predicate: (JavaCompiledContainer) -> Boolean): TransformationApplier {
    val backing = MultiTransformerApplierImpl(*toTypedArray())
    return TransformationApplier { if (predicate(it)) backing.transform(it) else it }
}

fun Map<String, CompiledClass>.transform(transformer: ClassTransformer): Map<String, CompiledClass> {
    return mapValues { (_, compiledClass) ->
        compiledClass.transformed(transformer.transform(compiledClass.bytecode))
    }
}

fun ClassTransformer.transform(byteArray: ByteArray): ByteArray {
    val reader = ClassReader(byteArray)
    val writer = ClassWriter(reader, 0)
    transform(reader, writer)
    return writer.toByteArray()
}

fun MethodVisitor.visitMethodInsn(
    opcode: Int,
    function: KFunction<*>,
    isInterface: Boolean = false,
) {
    val method = requireNotNull(function.javaMethod)
    visitMethodInsn(
        opcode,
        Type.getInternalName(method.declaringClass),
        method.name,
        Type.getMethodDescriptor(method),
        isInterface,
    )
}
