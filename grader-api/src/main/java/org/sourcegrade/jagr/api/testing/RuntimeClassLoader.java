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

import java.util.Set;

/**
 * A {@link ClassLoader} used to load classes for each {@link Submission}.
 */
public interface RuntimeClassLoader {

    /**
     * Loads the class with the specified name.
     *
     * @param name The name of the class
     * @return The loaded class
     */
    Class<?> loadClass(String name);

    /**
     * Loads and transforms the class with the specified name with the provided transformers.
     *
     * <p>
     * The resulting class is defined from the bytecode resulting from the chain of transformations.
     * </p>
     *
     * @param name         The name of the class to load
     * @param transformers The transformers to apply to the class
     * @return The transformed class
     */
    Class<?> loadClass(String name, ClassTransformer... transformers);

    /**
     * Loads and transforms the class with the specified name with the provided transformers.
     *
     * <p>
     * The resulting class is defined from the bytecode resulting from the chain of transformations.
     * </p>
     *
     * @param name         The name of the class to load
     * @param transformers The transformers to apply to the class
     * @return The transformed class
     */
    Class<?> loadClass(String name, Iterable<? extends ClassTransformer> transformers);

    /**
     * The names of all classes in this submission.
     *
     * @return The names of all classes in this submission
     */
    Set<String> getClassNames();

    /**
     * The names of all resources (no classes) in this submission.
     *
     * @return The names of all resources (no classes) in this submission
     */
    Set<String> getResourceNames();
}
