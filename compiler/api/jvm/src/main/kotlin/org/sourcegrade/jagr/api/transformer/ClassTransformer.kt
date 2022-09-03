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
package org.sourcegrade.jagr.api.transformer

import com.google.inject.Inject
import org.jetbrains.annotations.ApiStatus
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter

/**
 * Used to transform classes in submissions.
 *
 * The transform method is called for each class in the submission.
 */
interface ClassTransformer {

    /**
     * The name of this transformer.
     *
     * @return The name of this transformer
     */
    val name: String

    /**
     * Transforms the bytecode from the provided [ClassReader] into the provided [ClassWriter].
     *
     * @param reader The [ClassReader] from which to read the bytecode
     * @param writer The [ClassWriter] to which to write the bytecode
     */
    fun transform(reader: ClassReader, writer: ClassWriter)

    /**
     * The flags to use in the construction of the [ClassWriter] provided to [.transform].
     *
     * @return The flags to use
     * @see ClassWriter for more information as to which flags are available
     */
    val writerFlags: Int
        get() = 0

    @ApiStatus.Internal
    object FactoryProvider {
        @Inject
        internal lateinit var factory: Factory
    }

    @ApiStatus.Internal
    interface Factory {
        fun replacement(replacement: Class<*>, original: Class<*>): ClassTransformer
    }

    companion object {
        /**
         * Creates a transformer that replaces all method invocations and field accesses targeting `original` with
         * invocations and accesses targeting `replacement`.
         *
         * @param replacement The class with replacement methods and fields
         * @param original    The original class to be replaced
         * @return The transformer
         */
        fun replacement(replacement: Class<*>, original: Class<*>): ClassTransformer {
            return FactoryProvider.factory.replacement(replacement, original)
        }
    }
}
