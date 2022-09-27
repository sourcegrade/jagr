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

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Type;
import org.sourcegrade.jagr.api.rubric.RubricProvider;

import java.util.List;
import java.util.Map;

/**
 * The configuration of a rubric provider via {@link RubricProvider#configure(RubricConfiguration)}.
 *
 * @see RubricProvider
 */
@ApiStatus.NonExtendable
public interface RubricConfiguration {

    /**
     * The transformers to apply to every matching submission.
     *
     * @return The transformers to apply to every matching submission
     */
    Map<ClassTransformerOrder, List<ClassTransformer>> getTransformers();

    /**
     * The files that will be overridden in the submission from the solution.
     *
     * <p>
     * All files from this list will be taken from the solution, overwriting files with the same name from the submission.
     * </p>
     *
     * @return The files that will be overridden in the submission from the solution
     */
    List<String> getFileNameSolutionOverrides();

    /**
     * The path to the custom export build script relative to the grader resource directory.
     *
     * @return the path to the custom export build script relative to the grader resource directory
     */
    String getExportBuildScriptPath();

    /**
     * Adds a transformer to the list of transformers to apply to every matching submission.
     *
     * @param transformer The {@link ClassTransformer} to add
     * @param order       The {@link ClassTransformerOrder} in which to run the provided transformer
     * @return {@code this}
     */
    RubricConfiguration addTransformer(ClassTransformer transformer, ClassTransformerOrder order);

    /**
     * Adds a transformer to the list of transformers to apply to every matching submission.
     *
     * <p>
     * Same as {@link #addTransformer(ClassTransformer, ClassTransformerOrder)} but uses\
     * order {@link ClassTransformerOrder#DEFAULT}.
     * </p>
     *
     * @param transformer The {@link ClassTransformer} to add
     * @return {@code this}
     */
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

    /**
     * Sets the path to the custom export build script, relative to the grader resource directory.
     * If set to {@code null} or left unset, the default build script will be used.
     *
     * @param path The path to the build script, relative to the grader resource directory
     */
    RubricConfiguration setExportBuildScriptPath(@Nullable String path);
}
