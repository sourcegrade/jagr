package org.jagrkt.common.export.rubric

import org.jagrkt.api.rubric.GradedCriterion
import org.jagrkt.api.rubric.GradedRubric
import java.io.File
import java.io.PrintWriter

class GermanCSVExporter : GradedRubricExporter {
  override fun export(gradedRubric: GradedRubric, directory: File, fileName: String) {
    val rubric = gradedRubric.rubric
    val grade = gradedRubric.grade
    val writer = PrintWriter(directory.resolve("$fileName.csv"), Charsets.UTF_8)
    writer.println("${rubric.title},,,,")
    writer.println("Kriterium,Möglich,Erzielt,Kommentar,Extra")
    writer.appendEmptyLine()
    for (gradedCriterion in gradedRubric.childCriteria) {
      writer.appendCriterion(gradedCriterion)
      writer.appendEmptyLine()
    }
    writer.println("Zusätzlich möglicher Punktabzug,,,,")
    writer.println("Pro fehlendem Java Doc 1 Punkt abzug (nur bei Methoden bei denen mind. 1 Punkt erreicht wurde),-3,,,")
    writer.appendEmptyLine()
    writer.println("Gesamt,${rubric.maxPoints},${grade.correctPoints},,")
    writer.flush()
  }

  private fun PrintWriter.appendCriterion(gradedCriterion: GradedCriterion): PrintWriter {
    val criterion = gradedCriterion.criterion
    val grade = gradedCriterion.grade
    val receivedPoints = if (grade.correctPoints == 0) {
      if (grade.incorrectPoints == 0) "" else (criterion.maxPoints - grade.incorrectPoints).toString()
    } else grade.correctPoints.toString()
    if (gradedCriterion.childCriteria.isEmpty()) {
      println("${criterion.shortDescription},${criterion.maxPoints},$receivedPoints,,${criterion.hiddenNotes ?: ""}")
    } else {
      println("${criterion.shortDescription},,,,${criterion.hiddenNotes ?: ""}")
      for (childGradedCriterion in gradedCriterion.childCriteria) {
        appendCriterion(childGradedCriterion)
      }
      appendEmptyLine()
    }
    return this
  }

  private fun PrintWriter.appendEmptyLine(): PrintWriter = append(",,,,\n")
}
