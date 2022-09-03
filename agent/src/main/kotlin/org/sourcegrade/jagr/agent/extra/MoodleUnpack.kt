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

package org.sourcegrade.jagr.agent.extra

import com.google.inject.Inject
import org.slf4j.Logger
import org.sourcegrade.jagr.launcher.env.Config
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

class MoodleUnpack @Inject constructor(
    override val config: Config,
    override val logger: Logger,
) : Unpack() {
    private val assignmentIdRegex = config.extras.moodleUnpack.assignmentIdRegex.toRegex()
    override val name: String = "moodle-unpack"
    override fun run() {
        val submissions = File(config.dir.submissions)
        val studentRegex = Regex(config.extras.moodleUnpack.studentRegex)
        val unpackedFiles: MutableList<SubmissionInfoVerification> = mutableListOf()
        for (candidate in submissions.listFiles { _, t -> t.endsWith(".zip") }!!) {
            logger.info("extra($name) :: Discovered candidate zip $candidate")
            val zipFile = ZipFile(candidate)
            // TODO: Fix this hack
            val assignmentId = assignmentIdRegex.matchEntire(candidate.name)
                ?.run { groups["assignmentId"]?.value }
                ?.padStart(length = 2, padChar = '0')
                ?.let { "h$it" }
                ?: "none"
            for (entry in zipFile.entries()) {
                val matcher = studentRegex.matchEntire(entry.name) ?: continue
                try {
                    unpackedFiles += zipFile.unpackEntry(entry, submissions, assignmentId, matcher)
                } catch (e: Throwable) {
                    logger.info("extra($name) :: Unable to unpack entry ${entry.name} in candidate $candidate", e)
                }
            }
        }
        unpackedFiles.verify()
    }

    private fun ZipFile.unpackEntry(
        entry: ZipEntry,
        directory: File,
        assignmentId: String,
        matcher: MatchResult,
    ): SubmissionInfoVerification {
        val studentId = matcher.groups["studentId"]?.value
        val fileName = "$assignmentId-$studentId.jar"
        if (studentId == null) {
            logger.warn("extra(moodle-unpack) :: Unpacking unknown studentId in file $fileName")
        } else {
            logger.info("extra(moodle-unpack) :: Unpacking studentId $studentId in file $fileName")
        }
        val file = directory.resolve(fileName)
        file.outputStream().buffered().use { getInputStream(entry).copyTo(it) }
        return SubmissionInfoVerification(
            file,
            assignmentId = assignmentId,
            studentId = studentId
        )
    }
}
