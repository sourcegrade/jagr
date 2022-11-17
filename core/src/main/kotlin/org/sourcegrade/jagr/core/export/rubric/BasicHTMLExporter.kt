package org.sourcegrade.jagr.core.export.rubric

import org.sourcegrade.jagr.api.rubric.GradedCriterion
import org.sourcegrade.jagr.api.rubric.GradedRubric
import org.sourcegrade.jagr.api.rubric.PointRange
import org.sourcegrade.jagr.launcher.io.GradedRubricExporter
import org.sourcegrade.jagr.launcher.io.Resource
import org.sourcegrade.jagr.launcher.io.buildResource

/**
 * An implementation of [GradedRubricExporter] used to export [GradedRubric] to human-readably HTML files.
 */
class BasicHTMLExporter : GradedRubricExporter.HTML {

    private var cellCounter = 0
    private var criterionCounter = 0

    override fun export(gradedRubric: GradedRubric): Resource {
        cellCounter = 0
        criterionCounter = 0
        val builder = StringBuilder()
        builder.pageStart()
        builder.table(gradedRubric)
        builder.pageEnd()
        return buildResource {
            name = "${gradedRubric.testCycle.submission.info}.html"
            outputStream.bufferedWriter().use { it.write(builder.toString()) }
        }
    }

    private fun StringBuilder.table(rubric: GradedRubric) {
        tableStart(classes = listOf("table", "table-hover"))
        tableHeadStart()
        rowStart()
        titleEntry(rubric.rubric.title)
        titleEntry()
        titleEntry()
        titleEntry()
        rowEnd()
        tableHeadEnd()
        tableBodyStart()
        rowStart()
        titleEntry("Kriterium")
        titleEntry("Möglich")
        titleEntry("Erzielt")
        titleEntry("Kommentar")
        rowEnd()
        tableEntries(rubric)
        rowStart()
        titleEntry("Gesamt")
        titleEntry(rubric.rubric.range())
        titleEntry(rubric.grade.range())
        titleEntry(rubric.grade.comments.joinToString("<br>") { "<p>$it</p>" })
        rowEnd()
        tableBodyEnd()
        tableEnd()
    }

    private fun StringBuilder.pageStart() {
        append("<html>")
        append("<head>")
        append("""<meta charset="utf-8">""")
        append("""<link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@4.0.0/dist/css/bootstrap.min.css" integrity="sha384-Gn5384xqQ1aoWXA+058RXPxPg6fy4IWvTNh0E263XmFcJlSAwiGgFAW/dAiS6JXm" crossorigin="anonymous">""")
        append("</head>")
        append("<body>")
        append("""<div class="container">""")
    }

    private fun StringBuilder.pageEnd() {
        append("</div>")
        append("</body>")
        append("</html>")
    }

    private fun StringBuilder.tableEntries(r: GradedRubric) {
        r.childCriteria.forEach { this.tableEntry(it) }
    }

    private fun StringBuilder.tableStart(classes: List<String> = listOf()) {
        append("""<table class="${classes.joinToString(separator = " ")}">""")
    }

    private fun StringBuilder.tableEnd() = append("</table>")

    private fun StringBuilder.tableHeadStart() = append("<thead>")
    private fun StringBuilder.tableHeadEnd() = append("</thead>")

    private fun StringBuilder.tableBodyStart() = append("<tbody>")
    private fun StringBuilder.tableBodyEnd() = append("</tbody>")

    private fun StringBuilder.tableEntry(r: GradedCriterion) {
        val codeTagRegex = "\\[\\[\\[(?<code>.*?)]]]".toRegex()
        val codeTagTransform: (MatchResult) -> CharSequence = { "<code>${it.groups["code"]?.value}</code>" }

        rowStart()
        if (r.hasChildCriteria()) {
            titleEntry(r.description().replace(codeTagRegex, codeTagTransform))
            titleEntry(r.criterion.range())
            titleEntry(r.grade.range())
            titleEntry(r.comments().replace(codeTagRegex, codeTagTransform))
            rowEnd()
            r.childCriteria.forEach { this.tableEntry(it) }
        } else {
            entry("""${badge((++criterionCounter).toString())} ${r.description().replace(codeTagRegex, codeTagTransform)}""")
            entry(r.criterion.range())
            entry(r.grade.range(), classes = r.rowClasses())
            entry(r.comments().replace(codeTagRegex, codeTagTransform))
            rowEnd()
        }
    }

    private fun badge(text: String): String = """<span class="badge">$text</span>"""

    private fun PointRange.range(): String = PointRange.toString(this)

    private fun GradedCriterion.description(): String = criterion.shortDescription.escaped()

    private fun GradedCriterion.comments(): String {
        return grade.comments.filter { it.isNotBlank() }.joinToString(separator = "<br>") { it.escaped() }
    }

    private fun StringBuilder.rowStart(classes: List<String> = listOf()) {
        append("<tr class=\"${classes.joinToString(separator = " ")}\">")
    }

    private fun StringBuilder.rowEnd() {
        append("</tr>")
    }

    private fun StringBuilder.titleEntry(entry: String = "", classes: List<String> = listOf()) {
        append("""<th scope="col" class="${classes.joinToString(separator = " ")}" id="cell-${cellCounter++}">$entry</th>""")
    }

    private fun StringBuilder.entry(entry: String = "", classes: List<String> = listOf()) {
        append("""<td class="${classes.joinToString(separator = " ")}" id="cell-${cellCounter++}">$entry</td>""")
    }

    private fun GradedCriterion.rowClasses(): List<String> {
        return when {
            grade.minPoints == criterion.maxPoints -> listOf("table-success")
            grade.maxPoints == criterion.minPoints -> listOf("table-danger")
            else -> listOf("table-warning")
        }
    }

    private fun GradedCriterion.hasChildCriteria(): Boolean = childCriteria.isNotEmpty()

    private fun String.escaped(): String {
        return this
            .replace(Regex("(?<!\\\\)<"), "&lt;")
            .replace(Regex("(?<!\\\\)>"), "&gt;")
            .replace("\\<", "<")
            .replace("\\>", ">")
            .replace("\n", "<br>")
    }
}
