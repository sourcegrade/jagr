package org.sourcegrade.jagr

import com.google.common.io.ByteStreams
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import org.sourcegrade.jagr.api.rubric.GradedRubric
import org.sourcegrade.jagr.launcher.env.Jagr
import org.sourcegrade.jagr.launcher.env.runtimeGrader
import org.sourcegrade.jagr.launcher.io.*
import java.io.ByteArrayOutputStream
import java.util.*

fun grade(submission: GradingSubmissionTuple): GradedRubric {
    val compiler = Jagr.injector.getInstance(SubmissionTupleCompiler::class.java)
    val compiled = compiler.compile(submission)

    return Jagr.runtimeGrader.grade(
        compiled.grader,
        compiled.submission
    )
}

fun gradeBase64(
    grader: String,
    submission: String,
    lib: String,
): String {
    val gradingSubmission = GradingSubmissionTuple(
        grader = createResourceContainerFromZipBase64("grader", grader),
        submission = createResourceContainerFromZipBase64("submission", submission),
        library = createResourceContainerFromZipBase64("library", lib),
    )

    val graded = grade(
        submission = gradingSubmission,
    )

    val outputStream = ByteArrayOutputStream(8192)
    val output = ByteStreams.newDataOutput(outputStream)

    openScope(output, Jagr) {
        SerializerFactory.get<GradedRubric>().write(
            graded,
            this
        )
    }

    return Base64.getEncoder().encodeToString(outputStream.toByteArray())
}

@Serializable
data class SubmissionTupleInput(
    val grader: String,
    val submission: String,
    val library: String,
)

@Serializable
data class Output(
    val gradedRubric: String,
)

fun main() {
    val input = Json.decodeFromStream<SubmissionTupleInput>(System.`in`)

    val result = gradeBase64(
        grader = input.grader,
        submission = input.submission,
        lib = input.library,
    )

    val output = Output(
        gradedRubric = result,
    )

    Json.encodeToStream(output, System.out)
}
