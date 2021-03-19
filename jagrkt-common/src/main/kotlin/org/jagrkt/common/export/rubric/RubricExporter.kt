package org.jagrkt.common.export.rubric

import org.jagrkt.api.rubric.GradedRubric
import java.io.File

interface RubricExporter {
  fun export(gradedRubric: GradedRubric, file: File)
}
