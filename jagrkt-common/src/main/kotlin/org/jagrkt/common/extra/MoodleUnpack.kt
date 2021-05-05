/*
 *   JagrKt - JagrKt.org
 *   Copyright (C) 2021 Alexander Staeding
 *   Copyright (C) 2021 Contributors
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.jagrkt.common.extra

import com.google.inject.Inject
import org.jagrkt.common.Config
import org.jagrkt.common.writeStream
import org.slf4j.Logger
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

class MoodleUnpack @Inject constructor(
  override val config: Config,
  override val logger: Logger,
) : Unpack() {
  override val name: String = "moodle-unpack"
  override fun run() {
    val submissions = File(config.dir.submissions)
    val studentIdRegex = Regex(config.extras.moodleUnpack.studentIdRegex)
    val unpackedFiles: MutableList<SubmissionInfoVerification> = mutableListOf()
    for (candidate in submissions.listFiles { _, t -> t.endsWith(".zip") }!!) {
      logger.info("extra($name) :: Discovered candidate zip $candidate")
      val zipFile = ZipFile(candidate)
      for (entry in zipFile.entries()) {
        if (!entry.name.endsWith(".jar")) continue
        try {
          unpackedFiles += zipFile.unpackEntry(entry.name.split("/"), entry, submissions, studentIdRegex)
        } catch (e: Throwable) {
          logger.info("extra($name) :: Unable to unpack entry ${entry.name} in candidate $candidate", e)
        }
      }
    }
    unpackedFiles.verify()
  }

  private fun ZipFile.unpackEntry(
    path: List<String>,
    entry: ZipEntry,
    directory: File,
    studentIdRegex: Regex,
  ): SubmissionInfoVerification {
    val studentId = path[0].split(" - ").run { this[size - 1] }.takeIf { studentIdRegex.matches(it) }
    val fileName = path[path.size - 1]
    if (studentId == null) {
      logger.warn("extra(moodle-unpack) :: Unpacking unknown studentId in file $fileName")
    } else {
      logger.info("extra(moodle-unpack) :: Unpacking studentId $studentId in file $fileName")
    }
    val file = directory.resolve(fileName).writeStream { getInputStream(entry) }
    return SubmissionInfoVerification(
      file,
      studentId = studentId
    )
  }
}
