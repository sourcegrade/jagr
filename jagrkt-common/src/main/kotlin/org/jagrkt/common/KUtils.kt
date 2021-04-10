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

import org.slf4j.Logger
import java.io.File

fun File.ensure(logger: Logger, logInfo: Boolean = true): Boolean {
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
