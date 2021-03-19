package org.jagrkt.common.export.rubric

import com.google.inject.Inject
import org.jagrkt.api.rubric.GradedRubric
import org.slf4j.Logger
import java.io.File

class GradedRubricExportManager @Inject constructor(
  private val logger: Logger,
  private val exporters: Set<GradedRubricExporter>,
) {
  fun export(gradedRubric: GradedRubric, directory: File?, fileName: String) {
    val rubric = gradedRubric.rubric
    val grade = gradedRubric.grade
    val listener = gradedRubric.testCycle.jUnitResult?.summaryListener
    val succeeded = listener?.summary?.testsSucceededCount
    val total = listener?.summary?.testsStartedCount
    logger.info(
      "${gradedRubric.testCycle.submission} ::"
        + if (listener == null) "" else " ($succeeded / $total tests)"
        + " points=${grade.correctPoints} -points=${grade.incorrectPoints} maxPoints=${rubric.maxPoints}"
        + " from '${gradedRubric.rubric.title}'"
    )
    if (directory != null) {
      for (exporter in exporters) {
        exporter.export(gradedRubric, directory, fileName)
      }
    }
  }
}
