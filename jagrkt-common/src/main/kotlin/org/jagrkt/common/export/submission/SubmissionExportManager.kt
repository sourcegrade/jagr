package org.jagrkt.common.export.submission

import com.google.inject.Inject
import org.jagrkt.api.testing.Submission
import java.io.File

class SubmissionExportManager @Inject constructor(
  private val exporters: Set<SubmissionExporter>,
) {
  fun export(submission: Submission, directory: File?) {
    if (directory != null) {
      for (exporter in exporters) {
        exporter.export(submission, directory)
      }
    }
  }
}
