/*
 *   Jagr - SourceGrade.org
 *   Copyright (C) 2021-2023 Alexander Städing
 *   Copyright (C) 2021-2023 Contributors
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

package org.sourcegrade.jagr.domain.course

import org.sourcegrade.kontour.Creates
import org.sourcegrade.kontour.DomainEntity
import org.sourcegrade.kontour.UUID
import org.sourcegrade.kontour.scope.CrudScope

class CourseUser(override val id: UUID) : DomainEntity {

    data class CreateDto(
        val courseId: UUID,
        val userId: UUID,
    ) : Creates<CourseUser>

    interface DbScope : CrudScope<CourseUser, CreateDto> {
        suspend fun CourseUser.getRoles(): List<CourseRole>

        suspend fun DomainEntity.Repository<CourseUser>.findByCourseId(courseId: UUID): List<CourseUser>

        suspend fun DomainEntity.Repository<CourseUser>.findByUserId(userId: UUID): List<CourseUser>
    }

    companion object Repository : DomainEntity.Repository<CourseUser>
}
