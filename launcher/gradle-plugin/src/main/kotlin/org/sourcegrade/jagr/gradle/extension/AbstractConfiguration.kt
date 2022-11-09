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

package org.sourcegrade.jagr.gradle.extension

import org.gradle.api.Project
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import java.util.Locale

abstract class AbstractConfiguration(
    val name: String,
    private val project: Project,
) {

    private val dependencyConfiguration = DependencyConfiguration()
    private val _sourceSets: MutableList<SourceSet> = mutableListOf()
    val sourceSets: List<SourceSet>
        get() = _sourceSets

    abstract val sourceSetNames: SetProperty<String>

    init {
        project.afterEvaluate { proj ->
            for ((projectName, sourceSetName) in sourceSetNames.get().associate { it.split(":").zipWithNext().single() }) {
                val sourceSet = proj.sourceSetContainer.maybeCreate(sourceSetName)
                _sourceSets.add(sourceSet)
            }
            initialize(proj)
        }
    }

    private val Project.sourceSetContainer: SourceSetContainer
        get() = extensions.getByType()

    private fun initialize(project: Project) {
        project.dependencies {
            for ((suffix, dependencyNotations) in dependencyConfiguration.dependencies) {
                val configurationName = if (name == "main") suffix else {
                    "$name${suffix.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ENGLISH) else it.toString() }}"
                }
                for (dependencyNotation in dependencyNotations) {
                    add(configurationName, dependencyNotation)
                }
            }
        }
    }

    private fun getConfiguration(configurationName: String, normalizedName: String?): Pair<String, Set<String>>? {
        return project.configurations.findByName(configurationName)?.let { configuration ->
            val dependencies = configuration.dependencies.mapTo(mutableSetOf()) { "${it.group}:${it.name}:${it.version}" }
                .takeIf { it.isNotEmpty() } ?: return null
            (normalizedName ?: configurationName) to dependencies
        }
    }

    @JvmOverloads
    internal fun getAllDependencies(normalizedName: String? = null): Map<String, Set<String>> {
        return sourceSets.flatMap { sourceSet ->
            setOfNotNull(
                getConfiguration(sourceSet.apiConfigurationName, normalizedName?.let { "${it}Api" }),
                getConfiguration(sourceSet.compileOnlyConfigurationName, normalizedName?.let { "${it}CompileOnly" }),
                getConfiguration(sourceSet.compileOnlyApiConfigurationName, normalizedName?.let { "${it}CompileOnlyApi" }),
                getConfiguration(sourceSet.implementationConfigurationName, normalizedName?.let { "${it}Implementation" }),
                getConfiguration(sourceSet.runtimeOnlyConfigurationName, normalizedName?.let { "${it}RuntimeOnly" }),
            )
        }.toMap()
    }

    fun from(vararg sourceSetNames: String) {
        for (sourceSetName in sourceSetNames) {
            this.sourceSetNames.add(":$sourceSetName")
        }
    }

    fun fromProject(path: String, vararg sourceSetNames: String) {
        val project = project.project(path)
        for (sourceSetName in sourceSetNames) {
            this.sourceSetNames.add("${project.path}:$sourceSetName")
        }
    }

    fun configureDependencies(block: DependencyConfiguration.() -> Unit) = dependencyConfiguration.block()
}
