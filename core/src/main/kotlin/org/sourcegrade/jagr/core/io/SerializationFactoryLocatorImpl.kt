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

package org.sourcegrade.jagr.core.io

import org.sourcegrade.jagr.api.rubric.Criterion
import org.sourcegrade.jagr.api.rubric.GradeResult
import org.sourcegrade.jagr.api.rubric.GradedCriterion
import org.sourcegrade.jagr.api.rubric.GradedRubric
import org.sourcegrade.jagr.api.rubric.Rubric
import org.sourcegrade.jagr.api.testing.Submission
import org.sourcegrade.jagr.api.testing.SubmissionInfo
import org.sourcegrade.jagr.api.testing.TestCycle
import org.sourcegrade.jagr.core.compiler.ResourceCollector
import org.sourcegrade.jagr.core.compiler.ResourceCollectorImpl
import org.sourcegrade.jagr.core.executor.GradingRequestImpl
import org.sourcegrade.jagr.core.rubric.CriterionImpl
import org.sourcegrade.jagr.core.rubric.GradeResultImpl
import org.sourcegrade.jagr.core.rubric.GradedCriterionImpl
import org.sourcegrade.jagr.core.rubric.GradedRubricImpl
import org.sourcegrade.jagr.core.rubric.RubricImpl
import org.sourcegrade.jagr.core.testing.GraderJarImpl
import org.sourcegrade.jagr.core.testing.JavaSubmission
import org.sourcegrade.jagr.core.testing.JavaTestCycle
import org.sourcegrade.jagr.core.testing.SubmissionInfoImpl
import org.sourcegrade.jagr.launcher.executor.GradingRequest
import org.sourcegrade.jagr.launcher.io.GraderJar
import org.sourcegrade.jagr.launcher.io.SerializerFactory
import kotlin.reflect.KClass

class SerializationFactoryLocatorImpl : SerializerFactory.Locator {
    override fun <T : Any> get(type: KClass<T>): SerializerFactory<T>? = when (type) {
        Criterion::class -> CriterionImpl
        Rubric::class -> RubricImpl
        GradeResult::class -> GradeResultImpl
        GradedCriterion::class -> GradedCriterionImpl
        GradedRubric::class -> GradedRubricImpl
        GraderJar::class -> GraderJarImpl
        GradingRequest::class -> GradingRequestImpl
        ResourceCollector::class -> ResourceCollectorImpl
        Submission::class -> JavaSubmission
        SubmissionInfo::class -> SubmissionInfoImpl
        TestCycle::class -> JavaTestCycle
        else -> null
    } as SerializerFactory<T>?
}
