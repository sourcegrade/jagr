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
import org.gradle.kotlin.dsl.setProperty
import java.util.Locale

abstract class AbstractConfiguration(
    val name: String,
    private val project: Project,
    private val sourceSetNamesConvention: Set<String>,
) {

    private val dependencyConfiguration = DependencyConfiguration()

    private val _sourceSets: MutableList<SourceSet> = mutableListOf()
    val sourceSets: List<SourceSet>
        get() = _sourceSets

    val sourceSetNames: SetProperty<ProjectSourceSetTuple> = project.objects.setProperty<ProjectSourceSetTuple>()
        .convention(ProjectSourceSetTuple.fromSourceSetNames("", sourceSetNamesConvention))

    init {
        project.afterEvaluate { proj ->
            sourceSetNames.get().forEach { (projectPath, name) ->
                val sourceSet = proj.relative(projectPath).sourceSetContainer.maybeCreate(name)
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
                val configurationName = if (name == "main") {
                    suffix
                } else {
                    "$name${suffix.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ENGLISH) else it.toString() }}"
                }
                for (dependencyNotation in dependencyNotations) {
                    add(configurationName, dependencyNotation)
                }
            }
        }
    }

    private fun getConfiguration(configurationName: String, nameOverride: String?): Pair<String, Set<String>>? {
        return project.configurations.findByName(configurationName)?.let { configuration ->
            val dependencies = configuration.dependencies.mapTo(mutableSetOf()) { "${it.group}:${it.name}:${it.version}" }
                .takeIf { it.isNotEmpty() } ?: return null
            (nameOverride ?: configurationName) to dependencies
        }
    }

    @JvmOverloads
    internal fun getAllDependencies(nameOverride: String? = null): Map<String, Set<String>> {
        return sourceSets.flatMap { sourceSet ->
            setOfNotNull(
                getConfiguration(sourceSet.apiConfigurationName, nameOverride?.let { "${it}Api" }),
                getConfiguration(sourceSet.compileOnlyConfigurationName, nameOverride?.let { "${it}CompileOnly" }),
                getConfiguration(sourceSet.compileOnlyApiConfigurationName, nameOverride?.let { "${it}CompileOnlyApi" }),
                getConfiguration(sourceSet.implementationConfigurationName, nameOverride?.let { "${it}Implementation" }),
                getConfiguration(sourceSet.runtimeOnlyConfigurationName, nameOverride?.let { "${it}RuntimeOnly" }),
            )
        }.toMap()
    }

    fun from(vararg sourceSetNames: String) {
        this.sourceSetNames.addAll(ProjectSourceSetTuple.fromSourceSetNames("", sourceSetNames.asSequence()))
    }

    /**
     * Adds the default source sets from the given project for the given configuration type.
     *
     * For example, using this method from a submission configuration will add the `main` and `test` source sets from the given project.
     */
    fun from(otherProject: Project) {
        this.sourceSetNames.addAll(ProjectSourceSetTuple.fromSourceSetNames(otherProject.path, sourceSetNamesConvention))
    }

    /**
     * Adds the given source sets from the given project.
     */
    fun from(otherProject: Project, vararg sourceSetNames: String) {
        this.sourceSetNames.addAll(ProjectSourceSetTuple.fromSourceSetNames(otherProject.path, sourceSetNames.asSequence()))
    }

    fun configureDependencies(block: DependencyConfiguration.() -> Unit) = dependencyConfiguration.block()
}
