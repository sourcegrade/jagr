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

public interface ClassTransformer {

    static ClassTransformer replacement(Class<?> replacement, Class<?> original) {
        return FactoryProvider.factory.replacement(replacement, original);
    }

    String getName();

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
        ClassTransformer replacement(Class<?> replacement, Class<?> original);
    }
}
