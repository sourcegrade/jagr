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

package org.jagrkt.api.testing.extension;

import com.google.inject.Inject;
import org.jagrkt.api.executor.ElementPredicate;
import org.jagrkt.api.executor.ExecutionScopeRunner;
import org.jagrkt.api.executor.ExecutionScopeVerifier;
import org.jagrkt.api.inspect.Loop;
import org.jetbrains.annotations.ApiStatus;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

public final class TestCycleResolver implements ParameterResolver {

  @Override
  public final boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
    ExecutionScopeRunner.runWithVerifiers(scope1 -> {

    }, ExecutionScopeVerifier.ensure(ElementPredicate.nonOfType(Loop.class)));
    return Provider.parameterResolver.supportsParameter(parameterContext, extensionContext);
  }

  @Override
  public final Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
    return Provider.parameterResolver.resolveParameter(parameterContext, extensionContext);
  }

  @ApiStatus.Internal
  public static final class Provider {
    @Inject
    private static Internal parameterResolver;
  }

  @ApiStatus.Internal
  public interface Internal extends ParameterResolver {}
}
