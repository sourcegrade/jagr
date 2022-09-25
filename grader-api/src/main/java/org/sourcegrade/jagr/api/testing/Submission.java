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

import java.util.Set;

/**
 * A student submission to be graded.
 */
@ApiStatus.NonExtendable
public interface Submission {

    /**
     * The info of the submission.
     *
     * @return The info of the submission
     */
    String getInfo();

    /**
     * The {@link CompileResult} of the submission.
     *
     * @return The {@link CompileResult} of the submission
     */
    CompileResult getCompileResult();

    /**
     * The {@link SourceFile} for the given file name.
     *
     * @param fileName The name of the file to return
     * @return The {@link SourceFile} for the given file name
     */
    @Nullable SourceFile getSourceFile(String fileName);

    /**
     * <b>Experimental API. May be moved in a future release.</b>
     *
     * @return An immutable set of Java class names from this submission.
     */
    @ApiStatus.Experimental
    Set<String> getClassNames();
}
