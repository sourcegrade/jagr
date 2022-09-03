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
package org.sourcegrade.jagr.api.rubric

interface TestRef<T> {
    /**
     * Searches in the given [Map] for a [TestRef] that matches the given [T].
     *
     * @param testResults The [Map] to search in
     * @return A [Result] that matches the given [T]
     */
    operator fun get(testResults: Map<T, Result>): Result

    class Result private constructor(val status: Status, val throwable: Throwable?) {
        enum class Status {
            SUCCESSFUL, ABORTED, FAILED
        }

        companion object {
            private val SUCCESSFUL_RESULT by lazy { Result(Status.SUCCESSFUL, null) }
            fun successful() = SUCCESSFUL_RESULT
            fun aborted(throwable: Throwable) = Result(Status.ABORTED, throwable)
            fun failed(throwable: Throwable) = Result(Status.FAILED, throwable)
        }
    }
}
