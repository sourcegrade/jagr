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

package org.sourcegrade.jagr.launcher.env

import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Comment

@ConfigSerializable
data class Config(
    @field:Comment("The locations of the following directories may be configured here")
    val dir: Dir = Dir(),
    val executor: Executor = Executor(),
    val transformers: Transformers = Transformers(),
)

@ConfigSerializable
data class Dir(
    @field:Comment("Runtime dependencies for submissions")
    var libs: String = "libs",
    @field:Comment("Rubrics export directory")
    var rubrics: String = "rubrics",
    @field:Comment("Submissions ingest directory")
    var submissions: String = "submissions",
    @field:Comment("Submission export directory")
    var submissionsExport: String = "submissions-export",
    @field:Comment("Grader jar ingest directory")
    var graders: String = "graders",
)

@ConfigSerializable
data class Executor(
    @field:Comment(
        """
The maximum amount of concurrency to use for grading.
For a given concurrency n, Jagr will ensure that a maximum of n threads or processes are used concurrently that actively run
submission code.
""",
    )
    val concurrency: Int = 4,
    @field:Comment(
        """
The JVM arguments to use for grading. These arguments are passed to the JVM that runs the grading code.
This only applies to the "process" mode, as the "thread" and "single" modes do not spawn a new JVM.
        """,
    )
    val jvmArgs: List<String> = listOf(),
    @field:Comment(
        """
The executor mode to use. The following options are available:
- "single" ::
  Runs every TestCycle consecutively in the main thread. This mode does not create any extra processes or threads for grading.

- "thread" ::
  Creates a separate thread for every TestCycle. This mode greatly speeds up the grading process, especially with a large
  amount of submissions. The overhead of creating, managing and synchronizing threads is minimal compared to the performance
  benefits. However, this mode has the danger of creating "unkillable" threads (e.g. from certain kinds of infinite loops)
  which dramatically slow down the grading process through resource starvation of the host machine.

  The maximum number of concurrent threads used for grading is defined by the option "concurrency".

- "process" ::
  Creates a separate process for every TestCycle. This mode has the most overhead, but is also the most defensive against
  "badly behaving" code. A certain amount of sandboxing can be achieved in this mode, which is not possible in the other modes
  such as "thread" or "single".

  The maximum number of concurrent child process used for grading is defined by the option "concurrency".
""",
    )
    val mode: String = "process",
    @field:Comment(
        """
The grading thread's maximum permitted elapsed userTime in milliseconds since the last timeout before an
AssertionFailedError is thrown. If a thread's userTime satisfies
(userTime - lastTimeout) > individualTimeout,
the current userTime is stored for comparison later, and an AssertionFailedError is thrown to be caught by JUnit.
""",
    )
    val timeoutIndividual: Long = 10_000L,
    @field:Comment(
        """
The grading thread's maximum permitted elapsed userTime in milliseconds (from thread start) before an
AssertionFailedError is thrown. If a thread's userTime satisfies
((userTime - lastTimeout) > individualTimeout) && (userTime > totalTimeout),
an AssertionFailedError is thrown to be caught by JUnit. Note that lastTimeout is not reset in this case, and all further
invocations of checkTimeout() will result in an AssertionFailedError
""",
    )
    val timeoutTotal: Long = 150_000L,
)

@ConfigSerializable
data class Transformers(
    val timeout: TimeoutTransformer = TimeoutTransformer(),
) {
    interface Transformer {
        val enabled: Boolean
    }

    @ConfigSerializable
    data class TimeoutTransformer(
        override val enabled: Boolean = true,
    ) : Transformer
}
