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

package org.sourcegrade.jagr.core.export.rubric

import com.google.inject.Inject
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.sourcegrade.jagr.api.rubric.GradedRubric
import org.sourcegrade.jagr.core.testing.JavaSubmission
import org.sourcegrade.jagr.launcher.io.GradedRubricExporter
import org.sourcegrade.jagr.launcher.io.Resource
import org.sourcegrade.jagr.launcher.io.SubmissionInfo
import org.sourcegrade.jagr.launcher.io.buildResource

class MoodleJSONExporter @Inject constructor(
    private val exporterHTML: GradedRubricExporter.HTML,
) : GradedRubricExporter.Moodle {

    override fun export(gradedRubric: GradedRubric): Resource {
        val json = MoodleJSON(
            (gradedRubric.testCycle.submission as JavaSubmission).submissionInfo,
            gradedRubric.grade.minPoints,
            exporterHTML.export(gradedRubric).getInputStream().bufferedReader().readText(),
        )
        val jsonString = Json.encodeToString(json)
        return buildResource {
            name = "${gradedRubric.testCycle.submission.info}.json"
            outputStream.bufferedWriter().use { it.write(jsonString) }
        }
    }

    @Serializable
    data class MoodleJSON(
        val submissionInfo: SubmissionInfo,
        val totalPoints: Int,
        val feedbackComment: String,
    )
}
