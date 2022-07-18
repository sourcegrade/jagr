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

package org.sourcegrade.jagr.api.rubric;

import org.jetbrains.annotations.ApiStatus;

import java.util.List;

/**
 * Something that has criterion children.
 *
 * @see Criterion
 * @see Rubric
 */
@ApiStatus.NonExtendable
public interface CriterionHolder<C> {

    /**
     * The criterion children of this CriterionHolder.
     *
     * @return The criterion children of this CriterionHolder
     */
    List<? extends C> getChildCriteria();
}
