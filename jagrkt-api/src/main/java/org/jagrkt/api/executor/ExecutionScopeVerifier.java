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

package org.jagrkt.api.executor;

import com.google.inject.Inject;
import org.jetbrains.annotations.ApiStatus;

@FunctionalInterface
public interface ExecutionScopeVerifier {

  static ExecutionScopeVerifier ensureNotRecursive() {
    return FactoryProvider.factory.ensureNotRecursive();
  }

  static ExecutionScopeVerifier ensure(ElementPredicate codeContextPredicate) {
    return FactoryProvider.factory.ensure(codeContextPredicate);
  }

  /**
   * @param scope The {@link ExecutionScope} to verify
   * @throws Error (or subclass) if the provided {@link ExecutionSnapshot} does not pass verification
   */
  void verify(ExecutionScope scope);

  @ApiStatus.Internal
  final class FactoryProvider {
    @Inject
    private static Factory factory;
  }

  @ApiStatus.Internal
  interface Factory {
    ExecutionScopeVerifier ensureNotRecursive();

    ExecutionScopeVerifier ensure(ElementPredicate codeContextPredicate);
  }
}
