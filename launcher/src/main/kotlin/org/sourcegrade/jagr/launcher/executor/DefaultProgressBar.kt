package org.sourcegrade.jagr.launcher.executor

class DefaultProgressBar(
    rubricCollector: RubricCollector,
) : ProgressBarProvider(rubricCollector) {
    override fun adjustProgressBar(sb: StringBuilder): StringBuilder {
        return sb
    }
}
