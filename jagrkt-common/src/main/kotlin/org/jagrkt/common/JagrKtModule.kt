package org.jagrkt.common

import com.google.inject.AbstractModule
import com.google.inject.multibindings.Multibinder
import org.jagrkt.api.rubric.Criterion
import org.jagrkt.api.rubric.CriterionHolderPointCalculator
import org.jagrkt.api.rubric.GradeResult
import org.jagrkt.api.rubric.Grader
import org.jagrkt.api.rubric.JUnitTestRef
import org.jagrkt.api.rubric.Rubric
import org.jagrkt.common.export.rubric.GermanCSVExporter
import org.jagrkt.common.export.rubric.GradedRubricExporter
import org.jagrkt.common.export.submission.EclipseSubmissionExporter
import org.jagrkt.common.export.submission.SubmissionExporter
import org.jagrkt.common.rubric.CriterionFactoryImpl
import org.jagrkt.common.rubric.CriterionHolderPointCalculatorFactoryImpl
import org.jagrkt.common.rubric.GradeResultFactoryImpl
import org.jagrkt.common.rubric.JUnitTestRefFactoryImpl
import org.jagrkt.common.rubric.RubricFactoryImpl
import org.jagrkt.common.rubric.grader.GraderFactoryImpl
import org.jagrkt.common.testing.RuntimeTester
import org.jagrkt.common.testing.JavaRuntimeTester
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class JagrKtModule : AbstractModule() {
  override fun configure() {
    bind(Logger::class.java).toInstance(LoggerFactory.getLogger("JagrKt"))

    bind(Criterion.Factory::class.java).to(CriterionFactoryImpl::class.java)
    bind(CriterionHolderPointCalculator.Factory::class.java).to(CriterionHolderPointCalculatorFactoryImpl::class.java)
    bind(Grader.Factory::class.java).to(GraderFactoryImpl::class.java)
    bind(GradeResult.Factory::class.java).to(GradeResultFactoryImpl::class.java)
    bind(JUnitTestRef.Factory::class.java).to(JUnitTestRefFactoryImpl::class.java)
    bind(Rubric.Factory::class.java).to(RubricFactoryImpl::class.java)

    requestStaticInjection(
      Criterion.FactoryProvider::class.java,
      CriterionHolderPointCalculator.FactoryProvider::class.java,
      Grader.FactoryProvider::class.java,
      GradeResult.FactoryProvider::class.java,
      JUnitTestRef.FactoryProvider::class.java,
      Rubric.FactoryProvider::class.java,
    )

    with(Multibinder.newSetBinder(binder(), GradedRubricExporter::class.java)) {
      addBinding().to(GermanCSVExporter::class.java)
    }
    with(Multibinder.newSetBinder(binder(), SubmissionExporter::class.java)) {
      addBinding().to(EclipseSubmissionExporter::class.java)
    }
    with(Multibinder.newSetBinder(binder(), RuntimeTester::class.java)) {
      addBinding().to(JavaRuntimeTester::class.java)
    }
  }
}
