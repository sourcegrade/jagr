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

package org.sourcegrade.jagr

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.prompt
import com.github.ajalt.clikt.parameters.types.choice
import com.github.ajalt.clikt.parameters.types.path
import org.sourcegrade.jagr.launcher.env.Environment
import org.sourcegrade.jagr.launcher.env.Jagr
import org.sourcegrade.jagr.launcher.env.logger
import org.sourcegrade.jagr.launcher.executor.ProcessWorker

fun main(vararg args: String) {
    try {
        MainCommand().main(args)
    } catch (e: Throwable) {
        Jagr.logger.error("A fatal error occurred", e)
        throw e
    }
}

class MainCommand : CliktCommand() {

    /**
     * Command line option to indicate that this process will listen to (via std in) to a grading request
     */
    private val child by option("--child", "-c")
        .flag()
        .help("Waits to receive grading job details via IPC")
    private val noExport by option("--no-export", "-n")
        .flag()
        .help("Do not export submissions")
    private val exportOnly by option("--export-only", "-e")
        .flag()
        .help("Do not grade, only export submissions")
    private val progress by option("--progress")
        .choice("rainbow", "xmas")
        .help("Progress bar style")
    private val createMoodleUnpackConfig: String? by option("--create-moodle-unpack-config")
        .prompt(
            text = "Configuration output path",
            default = "moodle-unpack.conf",
        )
        .help("Creates default moodle unpack config at the requested path and exits")
    private val moodleUnpack by option("--moodle-unpack").path(mustExist = true, canBeFile = true, canBeDir = false)
        .help("Runs a moodle unpack with the given configuration")

    override fun run() {
        if (createMoodleUnpackConfig != null) {

        } else if (child) {
            println(ProcessWorker.MARK_CHILD_BOOT)
            Environment.initializeChildProcess()
            ChildProcGrading().grade()
        } else {
            Environment.initializeMainProcess()
            val startTime = System.currentTimeMillis()
            StandardGrading(progress).grade(noExport, exportOnly)
            Jagr.logger.info("Time taken: ${System.currentTimeMillis() - startTime}ms")
        }
    }
}
