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

package org.sourcegrade.jagr.core.compiler.java

import org.slf4j.Logger
import org.sourcegrade.jagr.core.parallelMapNotNull
import org.sourcegrade.jagr.core.transformer.TransformationApplier
import org.sourcegrade.jagr.launcher.io.ResourceContainer
import org.sourcegrade.jagr.launcher.io.SerializationScope
import org.sourcegrade.jagr.launcher.io.SerializerFactory
import org.sourcegrade.jagr.launcher.io.keyOf
import org.sourcegrade.jagr.launcher.io.nameWithoutExtension
import org.sourcegrade.jagr.launcher.io.readMap
import org.sourcegrade.jagr.launcher.io.writeMap
import java.io.File
import java.util.jar.JarEntry
import java.util.jar.JarOutputStream

data class RuntimeResources(
  val classes: Map<String, CompiledClass> = mapOf(),
  val resources: Map<String, ByteArray> = mapOf(),
) {
  companion object Factory : SerializerFactory<RuntimeResources> {
    val base = keyOf<RuntimeResources>("base")
    val grader = keyOf<RuntimeResources>("grader")
    override fun read(scope: SerializationScope.Input) = RuntimeResources(scope.readMap(), scope.readMap())

    override fun write(obj: RuntimeResources, scope: SerializationScope.Output) {
      scope.writeMap(obj.classes)
      scope.writeMap(obj.resources)
    }
  }
}

infix fun Map<String, CompiledClass>.rr(resources: Map<String, ByteArray>) = RuntimeResources(this, resources)
operator fun RuntimeResources.plus(other: RuntimeResources) = classes + other.classes rr resources + other.resources

fun RuntimeJarLoader.loadCompiled(containers: Sequence<ResourceContainer>): RuntimeResources {
  return containers
    .map { loadCompiled(it).runtimeResources }
    .ifEmpty { sequenceOf(RuntimeResources()) }
    .reduce { a, b -> a + b }
}

fun <T> Sequence<ResourceContainer>.compile(
  logger: Logger,
  transformerApplier: TransformationApplier,
  runtimeJarLoader: RuntimeJarLoader,
  graderRuntimeLibraries: RuntimeResources,
  containerType: String,
  constructor: JavaCompileResult.() -> T?
): List<T> = toList().parallelMapNotNull {
  val original = runtimeJarLoader.loadSources(it, graderRuntimeLibraries)
  val transformed = try {
    transformerApplier.transform(original)
  } catch (e: Exception) {
    logger.error("Failed to apply transformations for ${original.submissionInfo} :: ${e.message}")
    return@parallelMapNotNull null
  }
  original.exportCompilationResult("${it.info.nameWithoutExtension}-original.jar")
  transformed.exportCompilationResult("${it.info.nameWithoutExtension}-transformed.jar")
  with(transformed) {
    printMessages(
      logger,
      { "$containerType ${container.name} has $warnings warnings and $errors errors!" },
      { "$containerType ${container.name} has $warnings warnings!" },
    )
    constructor()?.apply { logger.info("Loaded $containerType $it") }
      ?: run { logger.error("Failed to load $containerType $it"); null }
  }
}

fun JavaCompileResult.exportCompilationResult(name: String) {
  val file = File("compilation/$name")
  file.parentFile.mkdirs()
  JarOutputStream(file.outputStream().buffered()).use { jar ->
    for ((className, compiledClass) in runtimeResources.classes) {
      val entry = JarEntry("${className.replace(".", "/")}.class")
      entry.time = System.currentTimeMillis()
      jar.putNextEntry(entry)
      jar.write(compiledClass.byteArray)
      jar.closeEntry()
    }
  }
}
