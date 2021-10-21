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

package org.sourcegrade.jagr.core.testing

import com.google.common.base.MoreObjects
import org.slf4j.Logger
import org.sourcegrade.jagr.api.rubric.RubricForSubmission
import org.sourcegrade.jagr.api.rubric.RubricProvider
import org.sourcegrade.jagr.api.rubric.TestForSubmission
import org.sourcegrade.jagr.core.compiler.java.JavaCompileResult
import org.sourcegrade.jagr.core.compiler.java.RuntimeClassLoader
import org.sourcegrade.jagr.core.compiler.java.RuntimeResources
import org.sourcegrade.jagr.core.compiler.java.plus
import org.sourcegrade.jagr.launcher.io.GraderJar
import org.sourcegrade.jagr.launcher.io.SerializationScope
import org.sourcegrade.jagr.launcher.io.SerializerFactory
import org.sourcegrade.jagr.launcher.io.get
import org.sourcegrade.jagr.launcher.io.nameWithoutExtension
import org.sourcegrade.jagr.launcher.io.read
import org.sourcegrade.jagr.launcher.io.write

class GraderJarImpl(
  private val logger: Logger,
  val compileResult: JavaCompileResult,
  runtimeLibraries: RuntimeResources,
) : GraderJar {

  companion object Factory : SerializerFactory<GraderJarImpl> {
    override fun read(scope: SerializationScope.Input): GraderJarImpl =
      GraderJarImpl(scope.get(), scope.read(), scope[RuntimeResources.grader])

    override fun write(obj: GraderJarImpl, scope: SerializationScope.Output) {
      scope.write(obj.compileResult)
    }
  }

  private val container = compileResult.container

  override val name: String = container.nameWithoutExtension

  /**
   * A map of assignments ids to classes of rubric providers (in the base classloader).
   *
   * Classes in this map are guaranteed to have an accessible no-args constructor.
   */
  override val rubricProviders: Map<String, List<String>>

  /**
   * A map of assignment ids to JUnit test classes
   */
  override val testProviders: Map<String, List<String>>

  init {
    val rubricProviders: MutableMap<String, MutableList<String>> = mutableMapOf()
    val testProviders: MutableMap<String, MutableList<String>> = mutableMapOf()
    val baseClassLoader = RuntimeClassLoader(compileResult.runtimeResources + runtimeLibraries)
    for (className in compileResult.runtimeResources.classes.keys) {
      val clazz = baseClassLoader.loadClass(className)
      rubricProviders.putIfRubric(clazz)
      testProviders.putIfTest(clazz)
    }
    this.rubricProviders = rubricProviders
    this.testProviders = testProviders
  }

  private fun MutableMap<String, MutableList<String>>.putIfRubric(clazz: Class<*>) {
    val filter = clazz.getAnnotation(RubricForSubmission::class.java) ?: return
    val asRubricProvider = try {
      clazz.asSubclass(RubricProvider::class.java)
    } catch (e: ClassCastException) {
      logger.error("Class annotated with @RubricForSubmission does not implement RubricProvider! Ignoring...")
      return
    }
    val assignmentId = filter.value
    val className = clazz.name
    try {
      clazz.getConstructor()
    } catch (e: NoSuchMethodException) {
      logger.error("Rubric provider $className in grader ${container.name} must have a no-args public constructor!")
      return
    }
    logger.info("${container.name} Discovered rubric provider $className for assignment $assignmentId")
    computeIfAbsent(filter.value) { mutableListOf() }.add(asRubricProvider.name)
  }

  private fun MutableMap<String, MutableList<String>>.putIfTest(clazz: Class<*>) {
    val filter = clazz.getAnnotation(TestForSubmission::class.java) ?: return
    computeIfAbsent(filter.value) { mutableListOf() }.add(clazz.name)
  }

  private val stringRep: String by lazy {
    MoreObjects.toStringHelper(this)
      .add("container", container)
      .add("applicableSubmissions", rubricProviders.keys.joinToString(", "))
      .toString()
  }

  override fun toString(): String = stringRep
}
