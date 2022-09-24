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

package org.sourcegrade.jagr.gradle

import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.provider.Property
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.exclude
import org.gradle.kotlin.dsl.getByType
import org.sourcegrade.jagr.launcher.VersionProvider

abstract class GraderSourceSetConfiguration(
    name: String,
    project: Project,
) : AbstractSourceSetConfiguration(name, project, SourceSet.TEST_SOURCE_SET_NAME) {
    abstract val graderName: Property<String>
    private val sourceSetContainer: SourceSetContainer = project.extensions.getByType()
    private val compileClasspath: ConfigurableFileCollection = project.objects.fileCollection()
    private val runtimeClasspath: ConfigurableFileCollection = project.objects.fileCollection()
    val sourceSet: SourceSet

    init {
        val testSourceSet = sourceSetContainer.getByName(SourceSet.TEST_SOURCE_SET_NAME)
        compileClasspath.from(testSourceSet.output)
        compileClasspath.from(testSourceSet.compileClasspath)
        runtimeClasspath.from(testSourceSet.output)
        runtimeClasspath.from(testSourceSet.runtimeClasspath)

        // create own source set
        sourceSet = sourceSetContainer.create(name) { sourceSet ->
            sourceSet.compileClasspath += compileClasspath
            sourceSet.runtimeClasspath += runtimeClasspath
        }

        // add jagr dependencies
        project.dependencies {
            sourceSet.implementationConfigurationName("org.sourcegrade:jagr-launcher:${VersionProvider.version}") {
                exclude("org.jetbrains", "annotations")
            }
        }
    }

    fun dependsOn(vararg sourceSets: SourceSet) {
        // clear compile and runtime classpath
        compileClasspath.setFrom()
        runtimeClasspath.setFrom()

        // add all dependencies
        for (sourceSet in sourceSets) {
            compileClasspath.from(sourceSet.output)
            compileClasspath.from(sourceSet.compileClasspath)
            runtimeClasspath.from(sourceSet.output)
            runtimeClasspath.from(sourceSet.runtimeClasspath)
        }
    }
}
