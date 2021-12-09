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

package org.sourcegrade.jagr.launcher.io

import java.io.File
import java.io.FilenameFilter

interface GradingBatch {

    /**
     * The graders in this batch.
     *
     * When run via commandline, the jars in the "graders" directory are loaded into here.
     */
    val graders: Sequence<ResourceContainer>

    /**
     * The submissions in this batch.
     *
     * When run via commandline, the jars in the "submissions" directory are loaded into here.
     */
    val submissions: Sequence<ResourceContainer>

    /**
     * Libraries that should always be included on the classpath
     * (unlike solutions that should generally only be present during grader compilation).
     *
     * When run via commandline, the jars in the "libs" directory are loaded into here.
     */
    val libraries: Sequence<ResourceContainer>

    /**
     * The expected number of submissions in this grading batch.
     */
    val expectedSubmissions: Int
}

fun buildGradingBatch(block: GradingBatchBuilder.() -> Unit): GradingBatch = GradingBatchBuilder().apply(block).build()

@Suppress("MemberVisibilityCanBePrivate", "unused")
class GradingBatchBuilder internal constructor() {
    private val graders = mutableListOf<ResourceContainer>()
    private val submissions = mutableListOf<ResourceContainer>()
    private val libraries = mutableListOf<ResourceContainer>()
    private var totalExpectedCount: Int = 0

    private fun discover(dir: File, filter: FilenameFilter?, list: MutableList<ResourceContainer>) {
        check(dir.exists() || dir.mkdirs()) { "Unable to create directory $dir" }
        for (candidate in checkNotNull(dir.listFiles(filter)) { "Could not find $dir" }) {
            list += createResourceContainer(candidate)
        }
    }

    fun discoverGraders(dir: File, filter: FilenameFilter? = null) = discover(dir, filter, graders)
    fun discoverGraders(dir: String, filter: FilenameFilter? = null) = discoverGraders(File(dir), filter)

    fun discoverSubmissions(dir: File, filter: FilenameFilter? = null) {
        check(dir.exists() || dir.mkdirs()) { "Unable to create directory $dir" }
        for (candidate in checkNotNull(dir.listFiles(filter)) { "Could not find $dir" }) {
            submissions += createResourceContainer(candidate)
            ++totalExpectedCount
        }
    }

    fun discoverSubmissions(dir: String, filter: FilenameFilter? = null) = discoverSubmissions(File(dir), filter)
    fun discoverLibraries(dir: File, filter: FilenameFilter? = null) = discover(dir, filter, libraries)
    fun discoverLibraries(dir: String, filter: FilenameFilter? = null) = discoverLibraries(File(dir), filter)

    fun addGrader(container: ResourceContainer): Boolean = graders.add(container)

    fun addSubmission(container: ResourceContainer): Boolean {
        ++totalExpectedCount
        return submissions.add(container)
    }

    fun addLibrary(container: ResourceContainer): Boolean = libraries.add(container)

    fun build(): GradingBatch = GradingBatchImpl(graders, submissions, libraries, totalExpectedCount)
}

private data class GradingBatchImpl(
    private val _graders: List<ResourceContainer>,
    private val _submissions: List<ResourceContainer>,
    private val _libraries: List<ResourceContainer>,
    override val expectedSubmissions: Int,
) : GradingBatch {
    override val graders: Sequence<ResourceContainer>
        get() = _graders.asSequence()
    override val submissions: Sequence<ResourceContainer>
        get() = _submissions.asSequence()
    override val libraries: Sequence<ResourceContainer>
        get() = _libraries.asSequence()
}
