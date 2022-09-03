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
import org.sourcegrade.jagr.agent.compiler.java.plus

class FallbackRuntimeTester : RuntimeTester {
    override fun createTestCycle(grader: GraderJarImpl, submission: Submission): TestCycle? {
        val info = submission.info
        val rubricProviders = grader.rubricProviders[info.assignmentId] ?: return null
        var resources = grader.container.runtimeResources
        if (submission is JavaSubmission) {
            resources += submission.compileResult.runtimeResources + submission.libraries
        }
        val classLoader = RuntimeClassLoader(resources)
        val notes = listOf(
            "The grading process was forcibly terminated.",
            "Please check if you have an infinite loop or infinite recursion.",
        )
        return FallbackTestCycle(rubricProviders, submission, classLoader, notes)
    }
}
