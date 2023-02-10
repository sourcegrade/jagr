package org.sourcegrade.jagr

import org.sourcegrade.jagr.api.testing.Submission
import org.sourcegrade.jagr.core.compiler.RuntimeContainer
import org.sourcegrade.jagr.launcher.io.GraderJar
import org.sourcegrade.jagr.launcher.io.ResourceContainer

class SubmissionTuple(
    val grader: ResourceContainer,
    val submission: ResourceContainer,
    val library: ResourceContainer,
)

class CompiledSubmissionTuple(
    val submissionTuple: SubmissionTuple,
    val grader: GraderJar,
    val submission: Submission,
    val library: RuntimeContainer,
)
