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

package org.sourcegrade.jagr.script

import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.kotlin.dsl.extra
import org.gradle.kotlin.dsl.project

/**
 * Add a x.y versioned project api dependency to a x.y.z versioned project.
 */
fun DependencyHandler.apiProject(project: Project, path: String) =
    addConfiguration("api", project, path)

/**
 * Add a x.y versioned project implementation dependency to a x.y.z versioned project.
 */
fun DependencyHandler.implementationProject(project: Project, path: String) =
    addConfiguration("implementation", project, path)

/**
 * Add a x.y versioned runtimeOnly dependency to a x.y.z versioned project.
 */
fun DependencyHandler.runtimeOnlyProject(project: Project, path: String) =
    addConfiguration("runtimeOnly", project, path)

/**
 * Add a x.y versioned project dependency to a x.y.z versioned project.
 */
private fun DependencyHandler.addConfiguration(configuration: String, project: Project, path: String) {
    // force release version of API on release to prevent transitive dependency on snapshot version of API
    val version = project.version.toString()
    val apiVersion = project.rootProject.extra["apiVersion"] as String
    if (version.endsWith("SNAPSHOT") || version.endsWith(".0")) {
        add(configuration, project(":$path"))
    } else {
        add(configuration, "org.sourcegrade:$path:$apiVersion")
    }
}
