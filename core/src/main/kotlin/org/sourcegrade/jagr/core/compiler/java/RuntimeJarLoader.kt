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

package org.sourcegrade.jagr.core.compiler.java

import com.google.inject.Inject
import org.apache.logging.log4j.Logger
import org.sourcegrade.jagr.core.compiler.ResourceCollectorImpl
import org.sourcegrade.jagr.core.compiler.ResourceExtractor
import org.sourcegrade.jagr.core.compiler.RuntimeContainer
import org.sourcegrade.jagr.core.compiler.readEncoded
import org.sourcegrade.jagr.launcher.io.Resource
import org.sourcegrade.jagr.launcher.io.ResourceContainer
import java.nio.charset.StandardCharsets
import java.util.Locale
import javax.tools.Diagnostic
import javax.tools.DiagnosticCollector
import javax.tools.JavaFileObject
import javax.tools.ToolProvider

class RuntimeJarLoader @Inject constructor(
    private val logger: Logger,
) {

    fun loadCompiled(
        container: ResourceContainer,
        resourceExtractor: ResourceExtractor = ResourceExtractor { _, _, _, _ -> },
    ): RuntimeContainer {
        val resourceCollector = ResourceCollectorImpl()
        val classStorage: MutableMap<String, CompiledClass> = mutableMapOf()
        val resources: MutableMap<String, ByteArray> = mutableMapOf()
        for (resource in container) {
            when {
                resource.name.endsWith(".class") -> {
                    val className = resource.name.replace('/', '.').substring(0, resource.name.length - 6)
                    classStorage[className] = CompiledClass.Existing(className, resource.getInputStream().use { it.readBytes() })
                }
                resource.name.endsWith("MANIFEST.MF") -> { // ignore
                }
                else -> resources[resource.name] = resource.getInputStream().use { it.readAllBytes() }
                    .also { data -> resourceExtractor.extract(container.info, resource, data, resourceCollector) }
            }
        }
        return JavaRuntimeContainer(container.info, resourceCollector, RuntimeResources(classStorage, resources))
    }

    fun loadSources(
        container: ResourceContainer,
        resourceExtractor: ResourceExtractor = ResourceExtractor { _, _, _, _ -> },
    ): JavaSourceContainer {
        val resourceCollector = ResourceCollectorImpl()
        val sourceFiles = mutableMapOf<String, JavaSourceFile>()
        val resources = mutableMapOf<String, ByteArray>()
        fun loadResource(resource: Resource) {
            when {
                resource.name.endsWith(".java") -> {
                    val className = resource.name.replace('/', '.').substring(0, resource.name.length - 5)
                    val content = resource.getInputStream().use { it.readEncoded() }
                    val sourceFile = JavaSourceFile(className, resource.name, content)
                    sourceFiles[resource.name] = sourceFile
                }
                resource.name.endsWith("MANIFEST.MF") -> { // ignore
                }
                else -> resources[resource.name] = resource.getInputStream().use { it.readAllBytes() }
                    .also { data -> resourceExtractor.extract(container.info, resource, data, resourceCollector) }
            }
        }

        val messages = mutableListOf<String>()
        for (resource in container) {
            try {
                loadResource(resource)
            } catch (e: Exception) {
                logger.error("An error occurred loading ${container.info.name} :: ${e.message}")
                e.message?.also(messages::add)
            }
        }
        return JavaSourceContainer(container.info, resourceCollector, sourceFiles, resources, messages)
    }

    fun compileSources(
        source: JavaSourceContainer,
        runtimeResources: RuntimeResources,
    ): JavaCompiledContainer {
        if (source.sourceFiles.isEmpty()) {
            // no source files, skip compilation task
            return JavaCompiledContainer(source, messages = source.messages)
        }
        val compiledClasses: MutableMap<String, CompiledClass> = mutableMapOf()
        val collector = DiagnosticCollector<JavaFileObject>()
        val compiler = ToolProvider.getSystemJavaCompiler()
        val fileManager = ExtendedStandardJavaFileManager(
            compiler.getStandardFileManager(null, null, StandardCharsets.UTF_8),
            runtimeResources.classes,
            compiledClasses,
        )
        val result = compiler.getTask(null, fileManager, collector, null, null, source.sourceFiles.values).call()
        compiledClasses.linkSource(source.sourceFiles)
        val newRuntimeResources = RuntimeResources(compiledClasses, source.resources)
        if (!result || collector.diagnostics.isNotEmpty()) {
            val messages = source.messages.toMutableList()
            var warnings = 0
            var errors = 0
            var other = 0
            for (diag in collector.diagnostics) {
                when (diag.kind) {
                    Diagnostic.Kind.NOTE,
                    Diagnostic.Kind.MANDATORY_WARNING,
                    Diagnostic.Kind.WARNING,
                    -> ++warnings
                    Diagnostic.Kind.ERROR,
                    -> ++errors
                    else -> ++other
                }
                messages += "${diag.source?.name}:${diag.lineNumber} ${diag.kind} :: ${diag.getMessage(Locale.getDefault())}"
            }
            return JavaCompiledContainer(
                source,
                newRuntimeResources,
                messages,
                warnings,
                errors,
                other
            )
        }
        return JavaCompiledContainer(source, newRuntimeResources)
    }
}
