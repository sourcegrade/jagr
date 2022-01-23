package org.sourcegrade.jagr.core.rubric

import org.sourcegrade.jagr.api.rubric.Gradable
import org.sourcegrade.jagr.api.rubric.GradeResult

fun Sequence<GradeResult>.sum(): GradeResult = fold(GradeResult.ofNone()) { a, b ->
    GradeResult.of(a.minPoints + b.minPoints, a.maxPoints + b.maxPoints)
}

fun GradeResult.withComments(comments: Iterable<String>): GradeResult = GradeResult.withComments(this, comments)

fun GradeResult.clamped(gradable: Gradable<*>): GradeResult = GradeResult.clamped(this, gradable)
