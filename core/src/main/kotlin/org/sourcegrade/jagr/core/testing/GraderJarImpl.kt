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

package org.sourcegrade.jagr.core.testing

import org.apache.logging.log4j.Logger
import org.sourcegrade.jagr.api.rubric.RubricProvider
import org.sourcegrade.jagr.api.rubric.TestForSubmission
import org.sourcegrade.jagr.core.compiler.graderInfo
import org.sourcegrade.jagr.core.compiler.java.JavaCompiledContainer
import org.sourcegrade.jagr.core.compiler.java.RuntimeClassLoaderImpl
import org.sourcegrade.jagr.core.compiler.java.RuntimeResources
import org.sourcegrade.jagr.core.compiler.java.plus
import org.sourcegrade.jagr.launcher.io.GraderJar
import org.sourcegrade.jagr.launcher.io.SerializationScope
import org.sourcegrade.jagr.launcher.io.SerializerFactory
import org.sourcegrade.jagr.launcher.io.get
import org.sourcegrade.jagr.launcher.io.graderFiles
import org.sourcegrade.jagr.launcher.io.read
import org.sourcegrade.jagr.launcher.io.solutionFiles
import org.sourcegrade.jagr.launcher.io.write

class GraderJarImpl constructor(
    private val logger: Logger,
    val container: JavaCompiledContainer,
    libraries: RuntimeResources,
) : GraderJar {
    override val info = requireNotNull(container.graderInfo) { "Grader container ${container.info.name} is missing graderInfo" }

    override val configuration = RubricConfigurationImpl()

    /**
     * A map of assignment ids to JUnit test classes
     */
    override val testClassNames: List<String>

    private val graderFiles = info.graderFiles.toSet()
    private val solutionFiles = info.solutionFiles.toSet()

    val containerWithoutSolution = container.copy(
        source = container.source.copy(
            sourceFiles = container.source.sourceFiles.filterKeys { it in graderFiles },
            resources = container.source.resources.filterKeys { it in graderFiles },
        ),
        runtimeResources = container.runtimeResources.copy(
            // whitelist file from grader
            classes = container.runtimeResources.classes.filter { it.value.source?.fileName in graderFiles },
            resources = container.runtimeResources.resources.filterKeys { it in graderFiles },
        )
    )

    init {
        for ((fileName, _) in container.source.sourceFiles) {
            if (!graderFiles.contains(fileName) && !solutionFiles.contains(fileName)) {
                error("Grader ${info.name} file $fileName is not declared in the grader-info.json")
            }
        }
        val testClasses = mutableListOf<String>()
        val baseClassLoader = RuntimeClassLoaderImpl(container.runtimeResources + libraries)
        var foundRubricProvider = false
        for (className in container.runtimeResources.classes.keys) {
            val clazz = baseClassLoader.loadClass(className)
            if (clazz.name == info.rubricClassName) {
                checkRubricProvider(clazz)
                foundRubricProvider = true
            }
            testClasses.addIfTest(clazz)
        }
        if (!foundRubricProvider) {
            error("Grader ${info.name} is missing rubric provider class ${info.rubricClassName}")
        }
        logger.info(
            "Grader ${info.name} discovered " +
                "rubric provider ${info.rubricClassName} and " +
                "${testClasses.size} test class${if (testClasses.size == 1) "" else "es"}"
        )
        this.testClassNames = testClasses
    }

    private fun checkRubricProvider(clazz: Class<*>) {
        val asRubricProvider = try {
            clazz.asSubclass(RubricProvider::class.java)
        } catch (e: ClassCastException) {
            throw IllegalStateException("Grader ${info.name} class declared as rubric provider does not implement RubricProvider", e)
        }

        val rubricProvider = try {
            checkNotNull(asRubricProvider.getConstructor().newInstance())
        } catch (e: NoSuchMethodException) {
            throw IllegalStateException("Grader ${info.name} rubric provider ${clazz.name} must have an accessible no-args constructor!", e)
        }
        rubricProvider.configure(configuration)
    }

    @Suppress("DEPRECATION")
    private fun MutableList<String>.addIfTest(clazz: Class<*>) {
        val annotation = clazz.getAnnotation(TestForSubmission::class.java) ?: return
        add(clazz.name)
        if (annotation.value.isNotBlank() && annotation.value != clazz.name) {
            logger.warn(
                "Grader ${info.name} test class ${clazz.name} " +
                    "has a non-blank value ${annotation.value} in @TestForSubmission and it does not match " +
                    "the grader's assignmentId ${info.assignmentId}"
            )
            logger.warn("Providing a value to @TestForSubmission is deprecated and will be removed in a future version")
        }
        logger.debug("Grader ${info.name} discovered test provider ${clazz.name} for assignment ${annotation.value}")
    }

    override fun toString(): String = info.name

    companion object Factory : SerializerFactory<GraderJarImpl> {
        override fun read(scope: SerializationScope.Input): GraderJarImpl =
            GraderJarImpl(scope.get(), scope.read(), scope[RuntimeResources.base])

        override fun write(obj: GraderJarImpl, scope: SerializationScope.Output) {
            scope.write(obj.container)
        }
    }
}
