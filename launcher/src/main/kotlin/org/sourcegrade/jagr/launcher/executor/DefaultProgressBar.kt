package org.sourcegrade.jagr.launcher.executor

class DefaultProgressBar(
  private val rubricCollector: RubricCollector,
  private val showElementsIfLessThan: Int = 3,
): ProgressBarProvider(rubricCollector, showElementsIfLessThan) {
  override fun adjustProgressBar(sb: StringBuilder): StringBuilder {
    return sb
  }

}
