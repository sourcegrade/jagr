package org.sourcegrade.jagr.launcher.executor

sealed interface MutableRubricCollector : RubricCollector {
  fun setListener(listener: () -> Unit)
  fun allocate(queue: GradingQueue)
  fun start(request: GradingRequest): GradingJob
}

fun emptyCollector(): MutableRubricCollector = RubricCollectorImpl()
