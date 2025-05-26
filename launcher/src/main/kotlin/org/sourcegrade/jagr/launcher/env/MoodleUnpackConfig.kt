/*
 *   Jagr - SourceGrade.org
 *   Copyright (C) 2021-2024 Alexander Städing
 *   Copyright (C) 2021-2024 Contributors
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

package org.sourcegrade.jagr.launcher.env

data class MoodleUnpackConfig(
    val moodleZipRegex: String = ".*[.]zip",
    val assignmentIdRegex: String = ".*Abgabe[^0-9]*(?<assignmentId>[0-9]{1,2}).*[.]zip",
    val assignmentIdTransformer: String = "h%id",
    val studentIdRegex: String = ".* - (?<studentId>([a-z]{2}[0-9]{2}[a-z]{4})|([a-z]+_[a-z]+))/submissions/.*[.]jar",
)
