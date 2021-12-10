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
import org.sourcegrade.jagr.api.testing.CompileResult
import org.sourcegrade.jagr.api.testing.RuntimeResources
import org.sourcegrade.jagr.core.compiler.ResourceCollector
import org.sourcegrade.jagr.core.compiler.RuntimeContainer
import org.sourcegrade.jagr.launcher.io.ResourceContainerInfo
import org.sourcegrade.jagr.launcher.io.SerializationScope
import org.sourcegrade.jagr.launcher.io.SerializerFactory
import org.sourcegrade.jagr.launcher.io.read
import org.sourcegrade.jagr.launcher.io.readList
import org.sourcegrade.jagr.launcher.io.write
import org.sourcegrade.jagr.launcher.io.writeList

data class JavaCompiledContainer(
    override val source: JavaSourceContainer,
    override val runtimeResources: JavaRuntimeResources = JavaRuntimeResources(),
    private val messages: List<String> = listOf(),
    val warnings: Int = 0,
    val errors: Int = 0,
    val other: Int = 0,
) : CompileResult, RuntimeContainer {
    override val info: ResourceContainerInfo get() = source.info
    override val resourceCollector: ResourceCollector get() = source.resourceCollector
    override fun getMessages(): List<String> = messages
    override fun getWarningCount(): Int = warnings
    override fun getErrorCount(): Int = errors
    override fun getOtherCount(): Int = other
    override fun getRuntimeResources(): RuntimeResources = runtimeResources

    fun printMessages(logger: Logger, lazyError: () -> String, lazyWarning: () -> String) {
        when {
            errors > 0 -> with(logger) {
                error(lazyError())
                messages.forEach(::error)
            }
            warnings > 0 -> with(logger) {
                warn(lazyWarning())
                messages.forEach(::warn)
            }
        }
    }

    companion object Factory : SerializerFactory<JavaCompiledContainer> {
        override fun read(scope: SerializationScope.Input): JavaCompiledContainer = JavaCompiledContainer(
            scope.read(),
            scope.read(),
            scope.readList(),
            scope.input.readInt(),
            scope.input.readInt(),
            scope.input.readInt(),
        )

        override fun write(obj: JavaCompiledContainer, scope: SerializationScope.Output) {
            scope.write(obj.source)
            scope.write(obj.runtimeResources)
            scope.writeList(obj.messages)
            scope.output.writeInt(obj.warnings)
            scope.output.writeInt(obj.errors)
            scope.output.writeInt(obj.other)
        }
    }
}
