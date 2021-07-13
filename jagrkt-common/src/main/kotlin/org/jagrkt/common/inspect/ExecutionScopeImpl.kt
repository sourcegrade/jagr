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

package org.jagrkt.common.inspect

import org.jagrkt.api.executor.ElementPredicate
import org.jagrkt.api.executor.ExecutionScope
import org.jagrkt.api.executor.ExecutionSnapshot
import org.jagrkt.api.executor.ScopeVerificationException
import org.jagrkt.api.inspect.Element
import java.util.ArrayDeque
import java.util.Deque

data class ExecutionScopeImpl(
  val elementTable: Array<Element>,
  val predicate: ElementPredicate,
) : ExecutionScope {

  private val elementIds: Deque<Int> = ArrayDeque()
  private val elements: Deque<Element> = ArrayDeque()
  private val idVerifier = ElementIdVerifier(predicate, elementTable)
  private lateinit var snapshot: ExecutionSnapshot

  fun pushElement(id: Int) {
    if (idVerifier.isNotAllowed(id)) {
      throw ScopeVerificationException()
    }
    elementIds += id
  }

  override fun pushElement(element: Element) {
    pushElement(element.id)
    elements += element
  }

  fun setSnapshot(snapshot: ExecutionSnapshot) {
    this.snapshot = snapshot
  }
  override fun getSnapshot(): ExecutionSnapshot = snapshot
}
