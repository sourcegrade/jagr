package org.jagrkt.common.testing

import com.google.common.base.MoreObjects
import org.jagrkt.api.rubric.RubricForSubmission
import org.jagrkt.api.rubric.RubricProvider
import org.jagrkt.api.rubric.TestForSubmission
import org.jagrkt.common.compiler.java.CompiledClass
import org.jagrkt.common.compiler.java.RuntimeClassLoader
import org.slf4j.Logger
import java.io.File

class TestJar(
  private val logger: Logger,
  private val file: File,
  val classes: Map<String, CompiledClass>,
) {

  /**
   * A map of assignments ids to classes of rubric providers (in the base classloader).
   *
   * Classes in this map are guaranteed to have an accessible no-args constructor.
   */
  val rubricProviders: Map<String, List<String>>

  /**
   * A map of assignment ids to JUnit test classes
   */
  val testProviders: Map<String, List<String>>

  init {
    val rubricProviders: MutableMap<String, MutableList<String>> = mutableMapOf()
    val testProviders: MutableMap<String, MutableList<String>> = mutableMapOf()
    val baseClassLoader = RuntimeClassLoader(classes)
    for (className in classes.keys) {
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
      logger.error("Rubric provider $className in file $file must have a no-args public constructor!")
      return
    }
    logger.info("${file.name} Discovered rubric provider $className for assignment $assignmentId")
    computeIfAbsent(filter.value) { mutableListOf() }.add(asRubricProvider.name)
  }

  private fun MutableMap<String, MutableList<String>>.putIfTest(clazz: Class<*>) {
    val filter = clazz.getAnnotation(TestForSubmission::class.java) ?: return
    computeIfAbsent(filter.value) { mutableListOf() }.add(clazz.name)
  }

  private val stringRep: String by lazy {
    MoreObjects.toStringHelper(this)
      .add("file", file)
      .add("applicableSubmissions", rubricProviders.keys.joinToString(", "))
      .toString()
  }

  override fun toString(): String = stringRep
}
