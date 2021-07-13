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

package org.jagrkt.common.executor

import org.jagrkt.api.executor.ExecutionScopeVerifier

interface StackTraceVerifier {
  fun verify(stackTrace: Array<out StackTraceElement>)
}

private object AnchoredEmptyExecutionContextVerifier : StackTraceVerifier {
  override fun verify(stackTrace: Array<out StackTraceElement>) {}
}

private class AnchoredSingleExecutionContextVerifier(
  private val snapshot: LightExecutionSnapshot,
  private val verifier: ExecutionScopeVerifier,
) : StackTraceVerifier {
  override fun verify(stackTrace: Array<out StackTraceElement>) = verifier.verify(snapshot.withStackTrace(stackTrace))
}

private class AnchoredManyExecutionContextVerifier(
  private val snapshot: LightExecutionSnapshot,
  private val verifiers: Array<out ExecutionScopeVerifier>,
) : StackTraceVerifier {
  override fun verify(stackTrace: Array<out StackTraceElement>) {
    val context = snapshot.withStackTrace(stackTrace)
    for (verifier in verifiers) {
      verifier.verify(context)
    }
  }
}

fun Array<out ExecutionScopeVerifier>.withAnchor(snapshot: LightExecutionSnapshot): StackTraceVerifier {
  return when (size) {
    0 -> AnchoredEmptyExecutionContextVerifier
    1 -> AnchoredSingleExecutionContextVerifier(snapshot, this[0])
    else -> AnchoredManyExecutionContextVerifier(snapshot, this)
  }
}
