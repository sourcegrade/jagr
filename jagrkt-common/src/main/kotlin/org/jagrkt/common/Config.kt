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

import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Comment

@ConfigSerializable
class Config {

  @Comment("The locations of the following directories may be configured here")
  val dir: Dir = Dir()
}

@ConfigSerializable
class Dir {

  @Comment("Runtime dependencies for submissions")
  var libs: String = "libs"

  @Comment("Rubrics export directory")
  var rubrics: String = "rubrics"

  @Comment("Submissions ingest directory")
  var submissions: String = "submissions"

  @Comment("Submission export directory")
  var submissionsExport: String = "submissions-export"

  @Comment("Test jar ingest directory")
  var tests: String = "tests"
}
