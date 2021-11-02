package org.sourcegrade.jagr.launcher.executor

import org.sourcegrade.jagr.launcher.env.Jagr

sealed interface MutableRubricCollector : RubricCollector {
  fun setListener(listener: (GradingResult) -> Unit)
  fun allocate(queue: GradingQueue)
  fun start(request: GradingRequest): GradingJob
}

fun emptyCollector(jagr: Jagr = Jagr): MutableRubricCollector = RubricCollectorImpl(jagr)
