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

package org.sourcegrade.jagr.api.testing;

import org.jetbrains.annotations.ApiStatus;
import org.objectweb.asm.Type;

import java.util.List;
import java.util.Map;

@ApiStatus.NonExtendable
public interface RubricConfiguration {

    Map<ClassTransformerOrder, List<ClassTransformer>> getTransformers();

    List<String> getFileNameSolutionOverrides();

    RubricConfiguration addTransformer(ClassTransformer transformer, ClassTransformerOrder order);

    default RubricConfiguration addTransformer(ClassTransformer transformer) {
        return addTransformer(transformer, ClassTransformerOrder.DEFAULT);
    }

    /**
     * Adds a file that will be overridden in the submission from the solution.
     *
     * @param fileName The name of the file to override
     */
    RubricConfiguration addFileNameSolutionOverride(String fileName);

    /**
     * Adds a class that will be overridden in the submission from the solution.
     *
     * <p><b>Only works with top level classes!</b></p>
     *
     * @param clazz The name of the file to override
     */
    default RubricConfiguration addFileNameSolutionOverride(Class<?> clazz) {
        return addFileNameSolutionOverride(Type.getInternalName(clazz) + ".java");
    }
}
