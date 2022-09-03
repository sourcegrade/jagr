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

package org.sourcegrade.jagr.domain.io

/**
 * Base interface for all services that accept a [ResourceContainer] to process it in some way
 * and return the result.
 *
 * Examples of such services are:
 * // TODO: Add Services
 */
interface ResourceContainerProcessor<in C : ResourceContainer<*>> {
    fun process(resourceContainer: C): Result

    interface Result {

        /**
         * The [ResourceContainerInfo] of the original [ResourceContainer]
         */
        val originalInfo: ResourceContainerInfo
    }

    /**
     * For processors that create a new [ResourceContainer] from the input [ResourceContainer].
     */
    interface ResourceContainerResult<out C : ResourceContainer<*>> : Result {

        /**
         * The [ResourceContainer] that was created
         */
        val resourceContainer: C
    }

}
