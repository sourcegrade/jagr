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

package org.sourcegrade.jagr.core.extra

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.apache.logging.log4j.Logger
import org.sourcegrade.jagr.launcher.env.Config
import org.sourcegrade.jagr.launcher.env.Jagr
import org.sourcegrade.jagr.launcher.io.SubmissionInfo
import java.io.File
import java.nio.file.FileSystems
import kotlin.io.path.bufferedReader
import kotlin.io.path.bufferedWriter

abstract class Unpack : Extra {

    protected abstract val config: Config
    protected abstract val logger: Logger

    private fun SubmissionInfoVerification.verify() {
        if (assignmentId == null && studentId == null && firstName == null && lastName == null) return
        try {
            FileSystems.newFileSystem(file.toPath(), null as ClassLoader?)
        } catch (e: Exception) {
            logger.error("Could not open zip for $file :: ${e.message}")
            return
        }.use { fs ->
            val submissionInfoPath = fs.getPath("submission-info.json")
            val replacedSubmissionInfo = try {
                submissionInfoPath.bufferedReader()
            } catch (e: Throwable) {
                logger.error("Unable to read submission-info for $this")
                null
            }?.use useReader@{ reader ->
                val submissionInfo = try {
                    Json.decodeFromString<SubmissionInfo>(reader.readText())
                } catch (e: SerializationException) {
                    return@useReader null
                }
                val replaceAssignmentId = assignmentId != null && assignmentId != submissionInfo.assignmentId
                val replaceStudentId = studentId != null && studentId != submissionInfo.studentId
                val replaceFirstName = firstName != null && firstName != submissionInfo.firstName
                val replaceLastName = lastName != null && lastName != submissionInfo.lastName
                if (replaceAssignmentId || replaceStudentId || replaceFirstName || replaceLastName) {
                    logger.warn(
                        StringBuilder().apply {
                            append("$submissionInfo has incorrect submission-info! Replacing:")
                            if (replaceAssignmentId) append(" assignmentId(${submissionInfo.assignmentId} -> $assignmentId)")
                            if (replaceStudentId) append(" studentId(${submissionInfo.studentId} -> $studentId)")
                            if (replaceFirstName) append(" firstName(${submissionInfo.firstName} -> $firstName)")
                            if (replaceLastName) append(" lastName(${submissionInfo.lastName} -> $lastName)")
                        }.toString(),
                    )
                    SubmissionInfo(
                        if (replaceAssignmentId) checkNotNull(assignmentId) else submissionInfo.assignmentId,
                        Jagr.version,
                        submissionInfo.sourceSets,
                        submissionInfo.dependencyConfigurations,
                        submissionInfo.repositoryConfigurations,
                        if (replaceStudentId) checkNotNull(studentId) else submissionInfo.studentId,
                        if (replaceFirstName) checkNotNull(firstName) else submissionInfo.firstName,
                        if (replaceLastName) checkNotNull(lastName) else submissionInfo.lastName,
                    )
                } else {
                    return
                }
            } ?: SubmissionInfo(
                assignmentId = assignmentId ?: "none",
                jagrVersion = Jagr.version,
                sourceSets = listOf(),
                dependencyConfigurations = emptyMap(),
                repositoryConfigurations = emptyList(),
                studentId = studentId ?: "none",
                firstName = firstName ?: "none",
                lastName = lastName ?: "none",
            )
            submissionInfoPath.bufferedWriter().use { writer ->
                writer.write(Json.encodeToString(replacedSubmissionInfo))
            }
        }
    }

    fun List<SubmissionInfoVerification>.verify() {
        runBlocking {
            with(GlobalScope) {
                asSequence().map {
                    async { it.verify() }
                }
            }.forEach { it.await() }
        }
    }

    data class SubmissionInfoVerification(
        val file: File,
        val assignmentId: String? = null,
        val studentId: String? = null,
        val firstName: String? = null,
        val lastName: String? = null,
    )
}
