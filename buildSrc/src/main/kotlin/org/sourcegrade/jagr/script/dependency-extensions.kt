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

import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.kotlin.dsl.project

private fun DependencyHandler.addConfiguration(configuration: String, version: Any, vararg paths: String) {
    // force release version of API on release to prevent transitive dependency on snapshot version of API
    if (version.toString().endsWith("SNAPSHOT") || version.toString().endsWith(".0")) {
        for (path in paths) {
            add(configuration, project(":$path"))
        }
    } else {
        for (path in paths) {
            add(configuration, "org.sourcegrade:$path:$version")
        }
    }
}

fun DependencyHandler.apiProjects(version: Any, vararg paths: String) =
    addConfiguration("api", version, *paths)

fun DependencyHandler.implementationProjects(version: Any, vararg paths: String) =
    addConfiguration("implementation", version, *paths)

fun DependencyHandler.runtimeOnlyProjects(version: Any, vararg paths: String) =
    addConfiguration("runtimeOnly", version, *paths)
