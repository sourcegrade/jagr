package org.sourcegrade.jagr

import org.sourcegrade.jagr.launcher.env.Jagr
import org.sourcegrade.jagr.launcher.env.config
import org.sourcegrade.jagr.launcher.env.runtimeGrader
import org.sourcegrade.jagr.launcher.io.GradedRubricExporter
import org.sourcegrade.jagr.launcher.io.createResourceContainer
import org.sourcegrade.jagr.launcher.io.writeIn
import java.io.File

fun main() {
    val compiler = Jagr.injector.getInstance(SubmissionTupleCompiler::class.java)

    val submissionTuple = SubmissionTuple(
        createResourceContainer(File(Jagr.config.dir.graders).resolve("FOP-2223-H09-Public-1.0.0.jar")),
        createResourceContainer(File(Jagr.config.dir.submissions).resolve("h09-ab12cdef-sol_first-sol_last-submission.jar")),
        createResourceContainer(File(Jagr.config.dir.libs).resolve("FOP-2223-H09-Private-1.0.0-libs.jar")),
    )

    val compiled = compiler.compile(submissionTuple)

    val gradingSchema = Jagr.runtimeGrader.grade(
        listOf(compiled.grader),
        compiled.submission
    )

    Jagr.injector.getInstance(GradedRubricExporter.HTML::class.java).export(gradingSchema.keys.first()).writeIn(File("result.html"))
}
