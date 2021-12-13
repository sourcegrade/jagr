package org.sourcegrade.jagr.launcher.executor

class XMasProgressBar(
    rubricCollector: RubricCollector,
) : RotationProgressBar(rubricCollector) {
    // red, purple, blue, cyan, green, yellow
    override val rotationColors = arrayOf("\u001b[31m", "\u001B[32m")
}
