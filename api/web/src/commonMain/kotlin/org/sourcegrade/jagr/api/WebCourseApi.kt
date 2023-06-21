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

package org.sourcegrade.jagr.api

import domain.course.Course
import org.sourcegrade.jagr.api.course.CourseApi
import org.sourcegrade.jagr.api.course.CourseDto
import org.sourcegrade.kontour.DomainEntity
import org.sourcegrade.kontour.Dto
import org.sourcegrade.kontour.UUID
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

class WebCourseApi : CourseApi {
    override suspend fun DomainEntity.Repository<Course>.create(item: Course.CreateDto): Course {
        TODO("Not yet implemented")
    }

    override suspend fun DomainEntity.Repository<Course>.deleteById(id: UUID): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun Course.exists(): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun <D : Dto<Course>> DomainEntity.Repository<Course>.findDtoById(
        id: UUID,
        dtoType: KClass<D>,
    ): D? {
        TODO("Not yet implemented")
    }

    override suspend fun DomainEntity.Repository<Course>.paginate(
        page: Int,
        pageSize: Int,
        sortBy: KProperty1<CourseDto.PaginationElement, Comparable<*>>,
        ascending: Boolean,
    ): List<CourseDto.PaginationElement> {
        TODO("Not yet implemented")
    }
}
