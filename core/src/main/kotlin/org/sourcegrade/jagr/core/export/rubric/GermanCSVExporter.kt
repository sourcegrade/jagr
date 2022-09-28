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
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import org.apache.logging.log4j.Logger
import org.sourcegrade.jagr.api.rubric.GradedCriterion
import org.sourcegrade.jagr.api.rubric.GradedRubric
import org.sourcegrade.jagr.api.rubric.PointRange
import org.sourcegrade.jagr.launcher.io.GradedRubricExporter
import org.sourcegrade.jagr.launcher.io.Resource
import org.sourcegrade.jagr.launcher.io.buildResource

class GermanCSVExporter @Inject constructor(
    private val logger: Logger,
) : GradedRubricExporter.CSV {
    override fun export(gradedRubric: GradedRubric): Resource {
        val rubric = gradedRubric.rubric
        val grade = gradedRubric.grade
        return buildResource {
            name = "${gradedRubric.testCycle.submission.info}.csv"
            outputStream.bufferedWriter().use {
                it.append('\ufeff') // UTF-8 Byte Order Mark
                CSVPrinter(it, CSVFormat.EXCEL).use { csv ->
                    csv.printRecord(rubric.title)
                    csv.printRecord("Kriterium", "Möglich", "Erzielt", "Kommentar", "Extra")
                    for (gradedCriterion in gradedRubric.childCriteria) {
                        csv.printCriterion(gradedCriterion)
                    }
                    csv.printRecord(
                        "Gesamt",
                        rubric.maxPoints.toString(),
                        PointRange.toString(grade),
                        grade.comments.firstOrNull(),
                    )
                    grade.comments.asSequence().drop(1).forEach { comment ->
                        csv.printRecord(null, null, null, comment)
                    }
                }
            }
        }
    }

    private fun CSVPrinter.printCriterion(gradedCriterion: GradedCriterion) {
        val criterion = gradedCriterion.criterion
        val grade = gradedCriterion.grade
        val comments = grade.comments.joinToString("; ")
        printRecord(
            criterion.shortDescription,
            PointRange.toString(criterion),
            PointRange.toString(grade),
            comments,
            criterion.hiddenNotes,
        )
        if (gradedCriterion.childCriteria.isNotEmpty()) {
            for (childGradedCriterion in gradedCriterion.childCriteria) {
                printCriterion(childGradedCriterion)
            }
            this.println()
        }
    }
}
