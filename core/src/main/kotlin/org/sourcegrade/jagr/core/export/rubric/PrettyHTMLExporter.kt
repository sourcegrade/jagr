package org.sourcegrade.jagr.core.export.rubric

import com.google.inject.Inject
import org.apache.logging.log4j.Logger
import org.sourcegrade.jagr.api.rubric.GradedCriterion
import org.sourcegrade.jagr.api.rubric.GradedRubric
import org.sourcegrade.jagr.api.rubric.PointRange
import org.sourcegrade.jagr.launcher.io.GradedRubricExporter
import org.sourcegrade.jagr.launcher.io.Resource
import org.sourcegrade.jagr.launcher.io.buildResource

class PrettyHTMLExporter @Inject constructor(
    private val logger: Logger,
) : GradedRubricExporter.Moodle {

    override fun export(gradedRubric: GradedRubric): Resource {
        val table = StringBuilder().writeTable(gradedRubric).toString()
        return buildResource {

            name = "result.html"
            outputStream.bufferedWriter().use { it.write(table) }
        }
    }

    fun path(): String = "index.html"

    private fun StringBuilder.writeTable(gradedRubric: GradedRubric): StringBuilder {
        // Open table

        append("<link rel=\"stylesheet\" href=\"https://cdn.jsdelivr.net/npm/bootstrap@4.0.0/dist/css/bootstrap.min.css\" integrity=\"sha384-Gn5384xqQ1aoWXA+058RXPxPg6fy4IWvTNh0E263XmFcJlSAwiGgFAW/dAiS6JXm\" crossorigin=\"anonymous\">")
        append("<body>")
        append("<div class=\"container\">")
        append("<table class='table'>")
        append("<thead>")

        // Title
        append("<tr>")
        append("<th scope=\"col\">${gradedRubric.rubric.title.escaped()}</th>")
        append("<th></th>")
        append("<th></th>")
        append("<th></th>")
        append("</tr>")
        append("</thead")

        // Headings
        append("<tr>")
        append("<th scope='col'>Kriterium</th>")
        append("<th scope='col'>Möglich</th>")
        append("<th scope='col'>Erzielt</th>")
        append("<th scope='col'>Kommentar</th>")
        append("</tr>")

        // Child criteria
        for (gradedCriterion in gradedRubric.childCriteria) {
            appendCriterion(gradedCriterion, 0)
        }

        val rubric = gradedRubric.rubric
        val grade = gradedRubric.grade
        val comments = grade.comments

        append("<tr>")
        append("<th scope='row'><strong>Gesamt</strong></th>")
        append("<th scope='row'>${rubric.maxPoints}</th>")
        append("<th scope='row'>${PointRange.toString(grade)}</th>")
        append("<th scope='row'>${comments.firstOrNull()?.escaped() ?: ""}</th>")
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
        append("</div>")
        append("</body>")
        return this
    }

    private fun StringBuilder.appendCriterion(gradedCriterion: GradedCriterion, level: Int): StringBuilder {
        val criterion = gradedCriterion.criterion
        val grade = gradedCriterion.grade
        val comments = grade.comments.joinToString("<br />") { it.escaped() }
        val format = if (level == 0) {
            "<th scope='row'>%s</th>"
        } else {
            "<td>%s</td>"
        }
        append("<tr>")
        append(format.format(criterion.shortDescription.escaped()))
        append(format.format(PointRange.toString(criterion)))
        append(format.format(PointRange.toString(grade)))
        append(format.format(comments))
        append("</tr>")
        if (gradedCriterion.childCriteria.isNotEmpty()) {
            for (childGradedCriterion in gradedCriterion.childCriteria) {
                appendCriterion(childGradedCriterion, level + 1)
            }
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
}
