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
package org.sourcegrade.jagr.api.compiler

import org.jetbrains.annotations.ApiStatus

/**
 * The result from the compilation of a container.
 */
@ApiStatus.NonExtendable
interface CompilationResult {
    /**
     * The messages produced by the compiler.
     *
     * @return The messages produced by the compiler
     */
    val messages: List<String>

    /**
     * The number of warnings that were produced by the compiler.
     *
     * @return The number of warnings that were produced by the compiler
     */
    val warningCount: Int

    /**
     * The number of errors that were produced by the compiler.
     *
     * @return The number of errors that were produced by the compiler
     */
    val errorCount: Int

    /**
     * The number of messages that are neither warnings nor errors produced by the compiler.
     *
     * @return The number of messages that are neither warnings nor errors produced by the compiler
     */
    val otherCount: Int
}
