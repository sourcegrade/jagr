/*
 *   Jagr - SourceGrade.org
 *   Copyright (C) 2021 Alexander Staeding
 *   Copyright (C) 2021 Contributors
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

import com.google.inject.Inject
import org.slf4j.Logger
import org.sourcegrade.jagr.launcher.env.Config
import org.sourcegrade.jagr.launcher.io.Resource
import org.sourcegrade.jagr.launcher.io.ResourceContainer

class MoodleUnpack @Inject constructor(
  override val config: Config,
  override val logger: Logger,
) : Unpack() {
  private val assignmentIdRegex = Regex(".*Abgabe zu Hausübung (?<assignmentId>[0-9]+) .*")
  private val studentIdRegex = Regex(config.extras.moodleUnpack.studentIdRegex)
  override val name: String = "moodle-unpack"
  override fun unpack(container: ResourceContainer): List<ResourceContainer> {
    logger.info("extra($name) :: Discovered candidate zip $container")
    // TODO: Fix this hack
    val assignmentId = container.extractAssignmentId()
    return container.asSequence()
      .mapNotNull {
        if (!it.name.endsWith(".jar")) return@mapNotNull null
        try {
          it.toInfoVerification(assignmentId)
        } catch (e: Exception) {
          logger.info("extra($name) :: Unable to unpack entry $name in candidate ${container.info.name}", e)
          null
        }
      }.toList().verify()
  }

  private fun Resource.toInfoVerification(assignmentId: String): SubmissionInfoVerification {
    val path = name.split("/")
    val studentId = path[0].split(" - ").run { this[size - 1] }.takeIf { studentIdRegex.matches(it) }
    val fileName = "$studentId-${path[path.size - 1]}"
    if (studentId == null) {
      logger.warn("extra(moodle-unpack) :: Unpacking unknown studentId in file $fileName")
    } else {
      logger.info("extra(moodle-unpack) :: Unpacking studentId $studentId in file $fileName")
    }
    return SubmissionInfoVerification(this, assignmentId = assignmentId, studentId = studentId)
  }

  private fun ResourceContainer.extractAssignmentId(): String {
    return assignmentIdRegex.matchEntire(info.name)
      ?.run { groups["assignmentId"]?.value }
      ?.padStart(length = 2, padChar = '0')
      ?.let { "h$it" }
      ?: "none"
  }
}
