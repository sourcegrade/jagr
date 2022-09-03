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

package org.sourcegrade.jagr.agent

import com.google.inject.AbstractModule
import com.google.inject.multibindings.Multibinder
import org.slf4j.Logger
import org.sourcegrade.jagr.api.rubric.Criterion
import org.sourcegrade.jagr.api.rubric.CriterionHolderPointCalculator
import org.sourcegrade.jagr.api.rubric.GradeResult
import org.sourcegrade.jagr.api.rubric.Grader
import org.sourcegrade.jagr.api.rubric.JUnitTestRef
import org.sourcegrade.jagr.api.rubric.Rubric
import org.sourcegrade.jagr.api.testing.ClassTransformer
import org.sourcegrade.jagr.api.testing.extension.TestCycleResolver
import org.sourcegrade.jagr.agent.executor.GradingQueueFactoryImpl
import org.sourcegrade.jagr.agent.executor.TimeoutHandler
import org.sourcegrade.jagr.agent.export.rubric.GermanCSVExporter
import org.sourcegrade.jagr.agent.export.rubric.MoodleJSONExporter
import org.sourcegrade.jagr.agent.export.submission.EclipseSubmissionExporter
import org.sourcegrade.jagr.agent.export.submission.GradleSubmissionExporter
import org.sourcegrade.jagr.agent.extra.ExtrasManagerImpl
import org.sourcegrade.jagr.agent.io.SerializationFactoryLocatorImpl
import org.sourcegrade.jagr.agent.rubric.CriterionFactoryImpl
import org.sourcegrade.jagr.agent.rubric.CriterionHolderPointCalculatorFactoryImpl
import org.sourcegrade.jagr.agent.rubric.GradeResultFactoryImpl
import org.sourcegrade.jagr.agent.rubric.JUnitTestRefFactoryImpl
import org.sourcegrade.jagr.agent.rubric.RubricFactoryImpl
import org.sourcegrade.jagr.agent.rubric.grader.GraderFactoryImpl
import org.sourcegrade.jagr.agent.testing.JavaRuntimeTester
import org.sourcegrade.jagr.agent.testing.RuntimeGraderImpl
import org.sourcegrade.jagr.agent.testing.RuntimeTester
import org.sourcegrade.jagr.agent.testing.TestCycleParameterResolver
import org.sourcegrade.jagr.agent.transformer.ClassTransformerFactoryImpl
import org.sourcegrade.jagr.launcher.env.Config
import org.sourcegrade.jagr.launcher.env.LaunchConfiguration
import org.sourcegrade.jagr.launcher.env.ModuleFactory
import org.sourcegrade.jagr.launcher.executor.GradingQueue
import org.sourcegrade.jagr.launcher.executor.RuntimeGrader
import org.sourcegrade.jagr.launcher.io.ExtrasManager
import org.sourcegrade.jagr.launcher.io.GradedRubricExporter
import org.sourcegrade.jagr.launcher.io.SerializerFactory
import org.sourcegrade.jagr.launcher.io.SubmissionExporter

/**
 * Shared bindings between main and testing guice modules
 */
@Suppress("unused") // src/main/resources/jagr.json
class CommonModule(private val configuration: LaunchConfiguration) : AbstractModule() {
    object Factory : ModuleFactory {
        override fun create(configuration: LaunchConfiguration) = CommonModule(configuration)
    }

    override fun configure() {
        bind(ClassTransformer.Factory::class.java).to(ClassTransformerFactoryImpl::class.java)
        bind(Criterion.Factory::class.java).to(CriterionFactoryImpl::class.java)
        bind(CriterionHolderPointCalculator.Factory::class.java).to(CriterionHolderPointCalculatorFactoryImpl::class.java)
        bind(ExtrasManager::class.java).to(ExtrasManagerImpl::class.java)
        bind(GradedRubricExporter.CSV::class.java).to(GermanCSVExporter::class.java)
        bind(GradedRubricExporter.HTML::class.java).to(MoodleJSONExporter::class.java)
        bind(Grader.Factory::class.java).to(GraderFactoryImpl::class.java)
        bind(GradeResult.Factory::class.java).to(GradeResultFactoryImpl::class.java)
        bind(GradingQueue.Factory::class.java).to(GradingQueueFactoryImpl::class.java)
        bind(JUnitTestRef.Factory::class.java).to(JUnitTestRefFactoryImpl::class.java)
        bind(Logger::class.java).toInstance(configuration.logger)
        bind(Rubric.Factory::class.java).to(RubricFactoryImpl::class.java)
        bind(RuntimeGrader::class.java).to(RuntimeGraderImpl::class.java)
        with(Multibinder.newSetBinder(binder(), RuntimeTester::class.java)) {
            addBinding().to(org.sourcegrade.jagr.agent.testing.JavaRuntimeTester::class.java)
        }
        bind(SerializerFactory.Locator::class.java).to(SerializationFactoryLocatorImpl::class.java)
        bind(SubmissionExporter.Eclipse::class.java).to(EclipseSubmissionExporter::class.java)
        bind(SubmissionExporter.Gradle::class.java).to(GradleSubmissionExporter::class.java)
        bind(TestCycleResolver.Internal::class.java).to(TestCycleParameterResolver::class.java)

        with(configuration.configurationLoader) {
            load().let { root ->
                if (root.empty()) Config().also { root.set(it).also(::save) }
                else root[Config::class.java]
            }.also(bind(Config::class.java)::toInstance)
        }

        requestStaticInjection(
            ClassTransformer.FactoryProvider::class.java,
            Criterion.FactoryProvider::class.java,
            CriterionHolderPointCalculator.FactoryProvider::class.java,
            Grader.FactoryProvider::class.java,
            GradeResult.FactoryProvider::class.java,
            JUnitTestRef.FactoryProvider::class.java,
            Rubric.FactoryProvider::class.java,
            TestCycleResolver.Provider::class.java,
            TimeoutHandler::class.java,
        )
    }
}
