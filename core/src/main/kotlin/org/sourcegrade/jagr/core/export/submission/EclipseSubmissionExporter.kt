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

package org.sourcegrade.jagr.core.export.submission

import com.google.inject.Inject
import org.slf4j.Logger
import org.sourcegrade.jagr.api.testing.Submission
import org.sourcegrade.jagr.core.testing.JavaSubmission
import org.sourcegrade.jagr.launcher.io.GraderJar
import org.sourcegrade.jagr.launcher.io.ResourceContainer
import org.sourcegrade.jagr.launcher.io.SubmissionExporter
import org.sourcegrade.jagr.launcher.io.addResource
import org.sourcegrade.jagr.launcher.io.buildResourceContainer
import org.sourcegrade.jagr.launcher.io.buildResourceContainerInfo
import java.io.PrintWriter

class EclipseSubmissionExporter @Inject constructor(
  private val logger: Logger,
) : SubmissionExporter.Eclipse {
  companion object {
    const val DEFAULT_EXPORT_NAME = "default"
  }

  override fun export(graders: List<GraderJar>, submissions: List<Submission>): List<ResourceContainer> {
    return submissions.map { export(it) }
  }

  private fun export(submission: Submission) = buildResourceContainer {
    submission as JavaSubmission
    info = buildResourceContainerInfo {
      name = DEFAULT_EXPORT_NAME
    }
    writeProjectFile(submission)
    writeClasspathFile()
    for ((fileName, sourceFile) in submission.compileResult.sourceFiles) {
      addResource {
        name = "src$fileName"
        outputStream.writer().write(sourceFile.content)
      }
    }
    for ((fileName, resource) in submission.compileResult.runtimeResources.resources) {
      addResource {
        name = "res$fileName"
        outputStream.writeBytes(resource)
      }
    }
  }

  private fun ResourceContainer.Builder.writeProjectFile(submission: JavaSubmission) = addResource {
    name = ".project"
    val writer = PrintWriter(outputStream, false, Charsets.UTF_8)
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

  private fun ResourceContainer.Builder.writeClasspathFile() = addResource {
    name = ".classpath"
    val writer = PrintWriter(outputStream, false, Charsets.UTF_8)
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
}
