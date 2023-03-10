package org.sourcegrade.jagr

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import org.sourcegrade.jagr.launcher.env.Jagr
import org.sourcegrade.jagr.launcher.env.config
import org.sourcegrade.jagr.launcher.env.runtimeGrader
import org.sourcegrade.jagr.launcher.io.*
import java.io.File

fun grade(
    grader: ResourceContainer,
    submission: ResourceContainer,
    lib: ResourceContainer
): String {
    val compiler = Jagr.injector.getInstance(SubmissionTupleCompiler::class.java)

    val submissionTuple = SubmissionTuple(
        grader = grader,
        submission = submission,
        library = lib,
    )

    val compiled = compiler.compile(submissionTuple)

    val gradingSchema = Jagr.runtimeGrader.grade(
        listOf(compiled.grader),
        compiled.submission
    )

    return Jagr.injector.getInstance(GradedRubricExporter.HTML::class.java).export(gradingSchema.keys.first())
        .getInputStream().reader().readText()
}

fun gradeBase64(
    grader: String,
    submission: String,
    lib: String,
): String {
    return grade(
        grader = createResourceContainerFromZipBase64("grader", grader),
        submission = createResourceContainerFromZipBase64("submission", submission),
        lib = createResourceContainerFromZipBase64("library", lib),
    )
}

@Serializable
data class SubmissionTupleInput(
    val grader: String,
    val submission: String,
    val library: String,
)

@Serializable
data class Output(
    val result: String,
)

fun main() {
    val input = Json.decodeFromStream<SubmissionTupleInput>(System.`in`)
    val output = Output(
        result = gradeBase64(
            grader = input.grader,
            submission = input.submission,
            lib = input.library,
        ),
    )
    Json.encodeToStream(output, System.out)

    /*gradeBase64(
        grader = File("./worker/mount/grader.bin").readText(),
        submission = File("./worker/mount/submission.bin").readText(),
        lib = File("./worker/mount/library.bin").readText(),
    )*/
}
