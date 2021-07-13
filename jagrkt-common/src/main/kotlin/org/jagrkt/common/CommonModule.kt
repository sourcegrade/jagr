/*
 *   JagrKt - JagrKt.org
 *   Copyright (C) 2021 Alexander Staeding
 *   Copyright (C) 2021 Contributors
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.jagrkt.common

import com.google.inject.AbstractModule
import org.jagrkt.api.executor.*
import org.jagrkt.api.rubric.*
import org.jagrkt.api.testing.extension.*
import org.jagrkt.common.executor.*
import org.jagrkt.common.rubric.*
import org.jagrkt.common.rubric.grader.*
import org.jagrkt.common.testing.*

/**
 * Shared bindings between main and testing guice modules
 */
abstract class CommonModule : AbstractModule() {
  override fun configure() {
    bind(ExecutionSnapshot.Factory::class.java).to(ExecutionContextFactoryImpl::class.java)
    bind(ExecutionScopeVerifier.Factory::class.java).to(ExecutionContextVerifierFactoryImpl::class.java)
    bind(Criterion.Factory::class.java).to(CriterionFactoryImpl::class.java)
    bind(CriterionHolderPointCalculator.Factory::class.java).to(CriterionHolderPointCalculatorFactoryImpl::class.java)
    bind(Grader.Factory::class.java).to(GraderFactoryImpl::class.java)
    bind(GradeResult.Factory::class.java).to(GradeResultFactoryImpl::class.java)
    bind(JUnitTestRef.Factory::class.java).to(JUnitTestRefFactoryImpl::class.java)
    bind(Rubric.Factory::class.java).to(RubricFactoryImpl::class.java)
    bind(TestCycleResolver.Internal::class.java).to(TestCycleParameterResolver::class.java)

    requestStaticInjection(
      ExecutionContextHandler::class.java,
      ExecutionSnapshot.FactoryProvider::class.java,
      ExecutionScopeVerifier.FactoryProvider::class.java,
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
