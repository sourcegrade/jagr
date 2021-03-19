package org.jagrkt.common

import org.slf4j.Logger
import java.io.File

fun File.createIfNotExists(logger: Logger, logInfo: Boolean = true): Boolean {
  if (!exists()) {
    if (logInfo) {
      logger.info("No $this dir! Creating...")
    }
    try {
      if (!mkdirs()) {
        logger.error("Unable to create $absolutePath dir")
        return true
      }
    } catch (e: SecurityException) {
      logger.error("Unable to create $absolutePath dir", e)
      return true
    }
  }
  return false
}
