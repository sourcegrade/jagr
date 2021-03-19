package org.jagrkt.common.export.rubric

import org.jagrkt.api.rubric.GradedRubric
import java.io.File

interface GradedRubricExporter {
  /**
   * Assumes that [directory] exists.
   */
  fun export(gradedRubric: GradedRubric, directory: File, fileName: String)
}
