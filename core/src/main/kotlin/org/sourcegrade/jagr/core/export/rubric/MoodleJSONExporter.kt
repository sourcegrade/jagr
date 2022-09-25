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
import org.slf4j.Logger
import org.sourcegrade.jagr.api.rubric.GradedCriterion
import org.sourcegrade.jagr.api.rubric.GradedRubric
import org.sourcegrade.jagr.api.rubric.PointRange
import org.sourcegrade.jagr.core.testing.JavaSubmission
import org.sourcegrade.jagr.launcher.io.GradedRubricExporter
import org.sourcegrade.jagr.launcher.io.Resource
import org.sourcegrade.jagr.launcher.io.SubmissionInfo
import org.sourcegrade.jagr.launcher.io.buildResource

class MoodleJSONExporter @Inject constructor(
    private val logger: Logger,
) : GradedRubricExporter.HTML {
    override fun export(gradedRubric: GradedRubric): Resource {
        val json = MoodleJSON(
            (gradedRubric.testCycle.submission as JavaSubmission).submissionInfo,
            gradedRubric.grade.minPoints,
            StringBuilder().writeTable(gradedRubric).toString(),
        )
        val jsonString = Json.encodeToString(json)
        return buildResource {
            name = "${gradedRubric.testCycle.submission.info}.json"
            outputStream.bufferedWriter().use { it.write(jsonString) }
        }
    }

    private fun StringBuilder.writeTable(gradedRubric: GradedRubric): StringBuilder {
        // Open table
        append("<table border='1' cellpadding='5' cellspacing='0'>")
        append("<tbody>")

        // Title
        append("<tr>")
        append("<td><strong>${gradedRubric.rubric.title.escaped()}</strong></td>")
        append("<td></td>")
        append("<td></td>")
        append("<td></td>")
        append("</tr>")
        appendEmptyHTMLTableRow()

        // Headings
        append("<tr>")
        append("<td><strong>Kriterium</strong></td>")
        append("<td><strong>Möglich</strong></td>")
        append("<td><strong>Erzielt</strong></td>")
        append("<td><strong>Kommentar</strong></td>")
        append("</tr>")
        appendEmptyHTMLTableRow()

        // Child criteria
        for (gradedCriterion in gradedRubric.childCriteria) {
            appendCriterion(gradedCriterion)
            appendEmptyHTMLTableRow()
        }

        val rubric = gradedRubric.rubric
        val grade = gradedRubric.grade
        val comments = grade.comments

        append("<tr>")
        append("<td><strong>Gesamt:</strong></td>")
        append("<td>${rubric.maxPoints}</td>")
        append("<td>${PointRange.toString(grade)}</td>")
        append("<td>${comments.firstOrNull()?.escaped() ?: ""}</td>")
        append("</tr>")

        for (i in 1 until comments.size) {
            append("<tr>")
            append("<td></td>")
            append("<td></td>")
            append("<td></td>")
            append("<td>${comments[i].escaped()}</td>")
            append("</tr>")
        }

        // Close table
        append("</tbody>")
        append("</table>")
        return this
    }

    private fun StringBuilder.appendCriterion(gradedCriterion: GradedCriterion): StringBuilder {
        val criterion = gradedCriterion.criterion
        val grade = gradedCriterion.grade
        val comments = grade.comments.joinToString("<br />") { it.escaped() }
        append("<tr>")
        append("<td>${criterion.shortDescription.escaped()}</td>")
        append("<td>${PointRange.toString(criterion)}</td>")
        append("<td>${PointRange.toString(grade)}</td>")
        append("<td>$comments</td>")
        append("</tr>")
        if (gradedCriterion.childCriteria.isNotEmpty()) {
            for (childGradedCriterion in gradedCriterion.childCriteria) {
                appendCriterion(childGradedCriterion)
            }
            appendEmptyHTMLTableRow()
        }
        return this
    }

    private fun StringBuilder.appendEmptyHTMLTableRow() {
        append("<tr>")
        for (i in 0 until 4) {
            append("<td></td>")
        }
        append("</tr>")
    }

    private fun String.escaped(): String {
        return replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\n", "<br>")
    }

    @Serializable
    data class MoodleJSON(
        val submissionInfo: SubmissionInfo,
        val totalPoints: Int,
        val feedbackComment: String,
    )
}
