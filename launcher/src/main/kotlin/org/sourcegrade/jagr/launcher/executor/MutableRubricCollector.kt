package org.sourcegrade.jagr.launcher.executor

sealed interface MutableRubricCollector : RubricCollector {
  fun addListener(listener: () -> Unit)
  fun allocate(queue: GradingQueue)
  fun start(request: GradingRequest): GradingJob
}
