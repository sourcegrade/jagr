package org.sourcegrade.jagr.launcher.executor

abstract class RotationProgressBar(
  rubricCollector: RubricCollector,
  showElementsIfLessThan: Int = 3,
) : ProgressBarProvider(rubricCollector, showElementsIfLessThan) {
  private val reset = "\u001b[0m"

  // red, purple, blue, cyan, green, yellow
  abstract val rotationColors: Array<String>
  private var startIndex = 0

  override fun adjustProgressBar(sb: StringBuilder): StringBuilder {
    val tmp = StringBuilder(6 * sb.length)
    for (i in sb.indices) {
      tmp.append(rotationColors[(i + rotationColors.size - startIndex) % rotationColors.size])
      tmp.append(sb[i])
      if (sb[i].toString() == ">") {
        tmp.append(reset)
        if (sb.length > i + 1) {
          tmp.append(sb.substring(i + 1))
        }
        break
      }
    }
    startIndex = (startIndex + 1) % rotationColors.size
    return tmp
  }
}
