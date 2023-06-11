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

package org.sourcegrade.jagr.db

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.sourcegrade.kontour.UUID

object Users : UUIDTable("users") {
    val username = varchar("username", 255).uniqueIndex()
}

class DbUser(id: EntityID<UUID>) : UUIDEntity(id) {
    var username: String by Users.username

    companion object : UUIDEntityClass<DbUser>(Users)
}
