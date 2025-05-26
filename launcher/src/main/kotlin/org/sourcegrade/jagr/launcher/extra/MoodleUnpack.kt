/*
 *   Jagr - SourceGrade.org
 *   Copyright (C) 2021-2025 Alexander Städing
 *   Copyright (C) 2021-2025 Contributors
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

package org.sourcegrade.jagr.launcher.extra

import com.google.inject.Inject
import org.apache.logging.log4j.Logger
import org.sourcegrade.jagr.launcher.env.Config
import org.sourcegrade.jagr.launcher.env.MoodleUnpackConfig
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

class MoodleUnpack private constructor(
    override val config: Config,
    override val logger: Logger,
    private val moodleUnpackConfig: MoodleUnpackConfig,
) : Unpack() {
    override fun run() {
        val submissions = File(config.dir.submissions)
        val moodleZipRegex = Regex(moodleUnpackConfig.moodleZipRegex)
        val assignmentIdRegex = Regex(moodleUnpackConfig.assignmentIdRegex)
        val studentRegex = Regex(moodleUnpackConfig.studentRegex)
        val idRegex = Regex("%id%")
        val assignmentIdTransformer = { id: String -> moodleUnpackConfig.assignmentIdTransformer.replace(idRegex, id) }
        val unpackedFiles: MutableList<SubmissionInfoVerification> = mutableListOf()
        for (candidate in submissions.listFiles { _, name -> name.matches(moodleZipRegex) }!!) {
            logger.info("moodle-unpack :: Discovered candidate zip $candidate")
            val zipFile = ZipFile(candidate)
            val assignmentId = assignmentIdRegex.matchEntire(candidate.name)
                ?.run { groups["assignmentId"]?.value }
                ?.padStart(length = 2, padChar = '0')
                ?.let(assignmentIdTransformer)
                ?: "none"
            for (entry in zipFile.entries()) {
                val matcher = studentRegex.matchEntire(entry.name) ?: continue
                try {
                    unpackedFiles += zipFile.unpackEntry(entry, submissions, assignmentId, matcher)
                } catch (e: Throwable) {
                    logger.info("moodle-unpack :: Unable to unpack entry ${entry.name} in candidate $candidate", e)
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
            studentId = studentId,
        )
    }

    class Factory @Inject constructor(
        val config: Config,
        val logger: Logger,
    ) {
        fun create(moodleUnpackConfig: MoodleUnpackConfig): MoodleUnpack = MoodleUnpack(config, logger, moodleUnpackConfig)
    }
}
