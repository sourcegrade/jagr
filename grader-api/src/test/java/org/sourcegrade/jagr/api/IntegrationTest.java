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

package org.sourcegrade.jagr.api;

import org.sourcegrade.jagr.api.inspect.ContextResolver;
import org.sourcegrade.jagr.api.inspect.JavaMethodContext;
import org.sourcegrade.jagr.api.testing.TestCycle;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class IntegrationTest {

  @Test
  @DisplayName("Fib Iterative Correct")
  public void fibIterative(TestCycle testCycle) {
    JavaMethodContext ctx = ContextResolver.ofJavaMethod(() -> Solution.class.getMethod("bar", int.class))
      .resolve(testCycle);
    assertTrue(ctx.modified());
    assertTrue(ctx.getModifiedSource().contains("for"));
  }
}
