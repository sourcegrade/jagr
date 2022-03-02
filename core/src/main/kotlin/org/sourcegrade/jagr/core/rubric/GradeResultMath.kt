package org.sourcegrade.jagr.core.rubric

import org.sourcegrade.jagr.api.rubric.Gradable
import org.sourcegrade.jagr.api.rubric.GradeResult

operator fun GradeResult.plus(other: GradeResult): GradeResult =
    GradeResultImpl(minPoints + other.minPoints, maxPoints + other.maxPoints, comments + other.comments)

fun Sequence<GradeResult>.sum(): GradeResult = fold(GradeResult.ofNone()) { a, b -> a + b }

fun GradeResult.withComments(comments: Iterable<String>): GradeResult = GradeResult.withComments(this, comments)

fun GradeResult.clamped(gradable: Gradable<*>): GradeResult = GradeResult.clamped(this, gradable)
