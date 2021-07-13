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

import org.jagrkt.common.inspect.ExecutionScopeImpl
import org.jagrkt.common.testing.TestCycleParameterResolver

object ExecutionContextHandler {
  fun checkExecutionContext() {
    val callStack = Thread.currentThread().stackTrace
    for (verifier in CONTEXTS.getOrCreateStack()) {
      verifier.verify(callStack)
    }
    (TestCycleParameterResolver.value.executionScopes.peek() as ExecutionScopeImpl?)?.snapshot =
  }
}
