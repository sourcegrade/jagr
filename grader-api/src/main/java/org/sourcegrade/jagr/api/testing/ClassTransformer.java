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

package org.sourcegrade.jagr.api.testing;

import com.google.inject.Inject;
import org.jetbrains.annotations.ApiStatus;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

/**
 * Used to transform classes in submissions.
 *
 * <p>
 * The transform method is called for each class in the submission.
 * </p>
 */
public interface ClassTransformer {

    /**
     * Transforms the given target class so that it directly extends the provided new superclass.
     *
     * <p>
     * The new superclass should extend the old superclass of the target class.
     * </p>
     *
     * @param targetName The class to transform
     * @param superName  The new superclass
     * @return The transformer
     */
    static ClassTransformer injectSuperclass(String targetName, String superName) {
        return FactoryProvider.factory.injectSuperclass(targetName, superName);
    }

    /**
     * Creates a transformer that replaces all method invocations and field accesses targeting {@code original} with
     * invocations and accesses targeting {@code replacement}.
     *
     * @param replacement The class with replacement methods and fields
     * @param original    The original class to be replaced
     * @return The transformer
     */
    static ClassTransformer replacement(Class<?> replacement, Class<?> original) {
        return FactoryProvider.factory.replacement(replacement, original);
    }

    /**
     * The name of this transformer.
     *
     * @return The name of this transformer
     */
    String getName();

    /**
     * Transforms the bytecode from the provided {@link ClassReader} into the provided {@link ClassWriter}.
     *
     * @param reader The {@link ClassReader} from which to read the bytecode
     * @param writer The {@link ClassWriter} to which to write the bytecode
     */
    void transform(ClassReader reader, ClassWriter writer);

    /**
     * The flags to use in the construction of the {@link ClassWriter} provided to {@link #transform(ClassReader, ClassWriter)}.
     *
     * @return The flags to use
     * @see ClassWriter for more information as to which flags are available
     */
    default int getWriterFlags() {
        return 0;
    }

    @ApiStatus.Internal
    final class FactoryProvider {
        @Inject
        private static Factory factory;
    }

    @ApiStatus.Internal
    interface Factory {
        ClassTransformer injectSuperclass(String targetName, String superName);

        ClassTransformer replacement(Class<?> replacement, Class<?> original);
    }
}
