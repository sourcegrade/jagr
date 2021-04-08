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

package org.jagrkt.common.export

import org.jagrkt.common.createIfNotExists
import org.slf4j.Logger
import java.io.File

abstract class ExportManager<E : Exporter> {

  protected abstract val logger: Logger
  protected abstract val exporters: Set<E>

  fun ensureDirs(directory: File?) {
    if (directory != null) {
      for (exporter in exporters) {
        directory.resolve(exporter.name).createIfNotExists(logger)
      }
    }
  }
}
