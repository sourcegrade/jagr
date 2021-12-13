/*
 *   Jagr - SourceGrade.org
 *   Copyright (C) 2021 Alexander Staeding
 *   Copyright (C) 2021 Contributors
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.sourcegrade.jagr.core.testing

import org.sourcegrade.jagr.api.testing.JavaCompiledProgram
import spoon.Launcher
import spoon.reflect.CtModel
import spoon.support.compiler.VirtualFile

lateinit var program: JavaCompiledProgram
fun JavaSubmissionImpl.buildModel(): CtModel {
  val launcher = Launcher()
  for ((_, sourceFile) in compileResult.sourceFiles) {
    launcher.addInputResource(VirtualFile(sourceFile.content, sourceFile.name))
  }
  program.compileResult.otherCount
  launcher.buildModel()
  return launcher.model
}
