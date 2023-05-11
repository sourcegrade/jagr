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

package org.sourcegrade.jagr.launcher.io

import org.sourcegrade.jagr.api.testing.Submission
import org.sourcegrade.jagr.launcher.executor.GradingQueue

interface SubmissionExporter {
    /**
     * Creates a list of [ResourceContainer] for every combination of [Submission] + [GraderJar].
     *
     * The resulting list has [GraderJar].size + 1 entries. (The first entry is the default export without
     * a combined [GraderJar])
     */
    fun export(graders: List<GraderJar>, submissions: List<Submission>): List<ResourceContainer>
    interface Gradle : SubmissionExporter
    interface Eclipse : SubmissionExporter
}

fun SubmissionExporter.export(queue: GradingQueue) = export(queue.graders, queue.submissions)
