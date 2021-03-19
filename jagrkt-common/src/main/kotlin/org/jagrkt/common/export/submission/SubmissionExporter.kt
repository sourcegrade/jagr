package org.jagrkt.common.export.submission

import org.jagrkt.api.testing.Submission
import java.io.File

interface SubmissionExporter {
  fun export(submission: Submission, directory: File)
}
