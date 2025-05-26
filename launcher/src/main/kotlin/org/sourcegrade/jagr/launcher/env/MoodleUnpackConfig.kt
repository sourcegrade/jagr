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

import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Comment

@ConfigSerializable
data class MoodleUnpackConfig(

    @field:Comment(
        """
Matches "moodle zip" file names that should be unpacked using this config.
""",
    )
    val moodleZipRegex: String = ".*[.]zip",

    @field:Comment(
        """
The "moodle zip" has a specific path format from which it is usually possible to extract an assignment id.
This is useful for bulk grading where individual submissions may not have correct information.
The format of this regex depends on the name of the submission module in moodle.
""",
    )
    val assignmentIdRegex: String = ".*Abgabe[^0-9]*(?<assignmentId>[0-9]{1,2}).*[.]zip",

    @field:Comment(
        """
The assignment ids extracted from the "moodle zip" are numeric only.
Use this option to transform each numeric assignment id to match the intended full assignment id.
By default, this is "h%id" which prefixes the id with 'h'.
""",
    )
    val assignmentIdTransformer: String = "h%id",

    @field:Comment(
        """
The "moodle zip" contains each submission at a path that includes the student id.
This regex parses and extracts the id.
""",
    )
    val studentRegex: String = ".* - (?<studentId>([a-z]{2}[0-9]{2}[a-z]{4})|([a-z]+_[a-z]+))/submissions/.*[.]jar",
)
