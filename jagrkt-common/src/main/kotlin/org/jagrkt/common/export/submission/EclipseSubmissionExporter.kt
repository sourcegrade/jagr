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

package org.jagrkt.common.export.submission

import com.google.inject.Inject
import org.jagrkt.api.testing.Submission
import org.jagrkt.common.createIfNotExists
import org.jagrkt.common.testing.JavaSubmission
import org.slf4j.Logger
import java.io.File
import java.io.PrintWriter

class EclipseSubmissionExporter @Inject constructor(
  private val logger: Logger,
) : SubmissionExporter {
  override fun export(submission: Submission, directory: File) {
    if (submission !is JavaSubmission) return
    val file = directory.resolve(submission.info.toString())
    if (file.createIfNotExists(logger, false)) {
      return
    }
    val src = file.resolve("src")
    if (src.createIfNotExists(logger, false)) {
      return
    }
    writeProjectFile(submission, file.resolve(".project"))
    writeClasspathFile(submission, file.resolve(".classpath"))
    for ((_, sourceFile) in submission.sourceFiles) {
      writeFile(sourceFile.content, src.resolve(".${sourceFile.name}"))
    }
  }

  private fun writeProjectFile(submission: JavaSubmission, file: File) {
    val writer = PrintWriter(file, Charsets.UTF_8)
    writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
    writer.println("<projectDescription>")
    writer.println("\t<name>${submission.info}</name>")
    writer.println("<buildSpec>")
    writer.println("\t\t<buildCommand>")
    writer.println("\t\t\t<name>org.eclipse.jdt.core.javabuilder</name>")
    writer.println("\t\t</buildCommand>")
    writer.println("\t</buildSpec>")
    writer.println("\t<natures>")
    writer.println("\t\t<nature>org.eclipse.jdt.core.javanature</nature>")
    writer.println("\t</natures>")
    writer.println("</projectDescription>")
    writer.flush()
  }

  private fun writeClasspathFile(submission: JavaSubmission, file: File) {
    val writer = PrintWriter(file, Charsets.UTF_8)
    writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
    writer.println("<classpath>")
    writer.println("\t<classpathentry kind=\"src\" path=\"src\"/>")
    writer.println("\t<classpathentry kind=\"con\" path=\"org.eclipse.jdt.launching.JRE_CONTAINER/org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType/JavaSE-11\">\n")
    writer.println("\t\t<attributes>\n")
    writer.println("\t\t\t<attribute name=\"module\" value=\"true\"/>")
    writer.println("\t\t</attributes>")
    writer.println("\t</classpathentry>")
    writer.println("\t<classpathentry kind=\"con\" path=\"org.eclipse.jdt.junit.JUNIT_CONTAINER/5\"/>")
    writer.println("\t<classpathentry kind=\"output\" path=\"bin\"/>")
    writer.println("</classpath>")
    writer.flush()
  }

  private fun writeFile(content: String, file: File) {
    file.parentFile.createIfNotExists(logger, false)
    val writer = PrintWriter(file, Charsets.UTF_8)
    writer.write(content)
    writer.flush()
  }
}
