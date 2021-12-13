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

package org.sourcegrade.jagr.core.executor

import org.sourcegrade.jagr.api.executor.ExecutionScope
import org.sourcegrade.jagr.api.executor.ExecutionScopeVerifier
import org.opentest4j.AssertionFailedError

object NotRecursiveExecutionScopeVerifier : ExecutionScopeVerifier {

  /**
   * @throws [AssertionFailedError] if [snapshot] is determined to contain recursive elements.
   */
  override fun verify(scope: ExecutionScope) {
    val snapshot = scope.snapshot
    val elements = HashSet<StackTraceElement>(snapshot.stackTrace.size)
    for (element in snapshot.stackTrace) {
      if (element == snapshot.anchor) break
      if (!elements.add(element)) {
        throw AssertionFailedError("Recursive invocation detected @ $element")
      }
    }
  }
}
