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

import org.sourcegrade.jagr.api.testing.Submission
import org.sourcegrade.jagr.api.testing.TestCycle
import org.sourcegrade.jagr.agent.compiler.java.RuntimeClassLoader
import org.sourcegrade.jagr.agent.compiler.java.RuntimeResources
import org.sourcegrade.jagr.launcher.io.SerializationScope
import org.sourcegrade.jagr.launcher.io.SerializerFactory
import org.sourcegrade.jagr.launcher.io.get
import org.sourcegrade.jagr.launcher.io.keyOf
import org.sourcegrade.jagr.launcher.io.openScope
import org.sourcegrade.jagr.launcher.io.read
import org.sourcegrade.jagr.launcher.io.readList
import org.sourcegrade.jagr.launcher.io.writeList

data class JavaTestCycle(
    private val rubricProviderClassNames: List<String>,
    private val submission: JavaSubmission,
    private val classLoader: RuntimeClassLoader,
    private var testsSucceededCount: Int = -1,
    private var testsStartedCount: Int = -1,
) : TestCycle {
    private var jUnitResult: TestCycle.JUnitResult? = null
    override fun getRubricProviderClassNames(): List<String> = rubricProviderClassNames
    override fun getClassLoader(): ClassLoader = classLoader
    override fun getSubmission(): JavaSubmission = submission
    override fun getTestsSucceededCount(): Int = testsSucceededCount
    override fun getTestsStartedCount(): Int = testsStartedCount
    override fun getNotes(): List<String> = emptyList()
    override fun getJUnitResult(): TestCycle.JUnitResult? = jUnitResult
    fun setJUnitResult(jUnitResult: TestCycle.JUnitResult?) {
        if (jUnitResult == null) return
        this.jUnitResult = jUnitResult
        testsSucceededCount = jUnitResult.summaryListener.summary.testsSucceededCount.toInt()
        testsStartedCount = jUnitResult.summaryListener.summary.testsStartedCount.toInt()
    }

    companion object Factory : SerializerFactory<JavaTestCycle> {
        override fun read(scope: SerializationScope.Input) = JavaTestCycle(
            scope.readList(),
            scope[Submission::class] as JavaSubmission,
            scope.openScope {
                proxy(keyOf(RuntimeResources::class), RuntimeResources.base)
                read()
            },
            scope.input.readInt(),
            scope.input.readInt(),
        )

        override fun write(obj: JavaTestCycle, scope: SerializationScope.Output) {
            scope.writeList(obj.rubricProviderClassNames)
            scope.output.writeInt(obj.testsSucceededCount)
            scope.output.writeInt(obj.testsStartedCount)
        }
    }
}
