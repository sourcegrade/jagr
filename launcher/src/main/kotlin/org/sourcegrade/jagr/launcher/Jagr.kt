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

package org.sourcegrade.jagr.launcher

import org.sourcegrade.jagr.launcher.env.Environment
import org.sourcegrade.jagr.launcher.env.SystemResourceEnvironmentFactory
import org.sourcegrade.jagr.launcher.env.gradingQueueFactory
import org.sourcegrade.jagr.launcher.executor.Executor
import org.sourcegrade.jagr.launcher.executor.MutableRubricCollector
import org.sourcegrade.jagr.launcher.executor.ProgressBar
import org.sourcegrade.jagr.launcher.executor.RubricCollector
import org.sourcegrade.jagr.launcher.executor.emptyCollector
import org.sourcegrade.jagr.launcher.executor.executorForBatch
import org.sourcegrade.jagr.launcher.io.GradingBatch

/**
 * Represents a Jagr implementation.
 */
open class Jagr(val environment: Environment) {

  companion object Default : Jagr(SystemResourceEnvironmentFactory.create())

  suspend fun schedule(
    batch: GradingBatch,
    collector: MutableRubricCollector = emptyCollector(),
    executorFactory: Executor.Factory = executorForBatch(batch),
  ): RubricCollector {
    val progressBar = ProgressBar(collector)
    collector.setListener {
      progressBar.print()
    }
    val queue = environment.gradingQueueFactory.create(batch)
    collector.allocate(queue)
    val executor = executorFactory.create(environment)
    executor.schedule(queue)
    executor.start(collector)
    return collector // TODO: Maybe different return type?
  }
}

fun Jagr.prepare(builderAction: JagrBuilder.() -> Unit) {
  JagrBuilder(this).builderAction()
}

class JagrBuilder internal constructor(val jagr: Jagr) {
  lateinit var batch: GradingBatch
  var collector: MutableRubricCollector? = null
  var executor: Executor? = null
}

fun JagrBuilder.setExecutorFactory(executorFactory: Executor.Factory) {
  executor = executorFactory.create(jagr.environment)
}
