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

import org.sourcegrade.jagr.api.testing.SourceFile
import org.sourcegrade.jagr.api.testing.Submission
import org.sourcegrade.jagr.api.testing.SubmissionInfo
import org.sourcegrade.jagr.core.compiler.java.JavaCompiledContainer
import org.sourcegrade.jagr.core.compiler.java.RuntimeResources
import org.sourcegrade.jagr.launcher.io.SerializationScope
import org.sourcegrade.jagr.launcher.io.SerializerFactory
import org.sourcegrade.jagr.launcher.io.read
import org.sourcegrade.jagr.launcher.io.readScoped
import org.sourcegrade.jagr.launcher.io.write
import org.sourcegrade.jagr.launcher.io.writeScoped
import java.util.Collections

data class JavaSubmission(
    private val info: SubmissionInfo,
    private val compileResult: JavaCompiledContainer,
    val libraries: RuntimeResources,
) : Submission {
    override fun getInfo(): SubmissionInfo = info
    override fun getCompileResult(): JavaCompiledContainer = compileResult
    override fun getSourceFile(fileName: String): SourceFile? = compileResult.source.sourceFiles[fileName]
    override fun getClassNames(): Set<String> = Collections.unmodifiableSet(compileResult.runtimeResources.classes.keys)

    override fun toString(): String = "$info(${compileResult.info.name})"

    companion object Factory : SerializerFactory<JavaSubmission> {
        override fun read(scope: SerializationScope.Input): JavaSubmission =
            JavaSubmission(scope.readScoped(), scope.read(), scope[RuntimeResources.base])

        override fun write(obj: JavaSubmission, scope: SerializationScope.Output) {
            scope.writeScoped(obj.info)
            scope.write(obj.compileResult)
        }
    }
}
