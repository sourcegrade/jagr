package org.jagrkt.common.export.rubric

import com.google.inject.Inject
import org.jagrkt.api.rubric.GradedRubric
import org.slf4j.Logger
import java.io.File

class RubricExporterManager @Inject constructor(
  private val logger: Logger,
  private val rubricExporters: Set<RubricExporter>,
) : RubricExporter {
  override fun export(gradedRubric: GradedRubric, file: File) {
    TODO("Not yet implemented")
  }
}
