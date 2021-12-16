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

package org.sourcegrade.jagr.api.testing.extension;

import com.google.inject.Inject;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.sourcegrade.jagr.api.testing.TestCycle;

public final class TestCycleResolver implements ParameterResolver {

    /**
     * <b>Experimental API. May be moved in a future release.</b>
     *
     * @return The current {@link TestCycle}
     */
    @ApiStatus.Experimental
    public static @Nullable TestCycle getTestCycle() {
        if (Provider.parameterResolver == null) {
            return null;
        } else {
            return Provider.parameterResolver.getInternalValue();
        }
    }

    @Override
    public boolean supportsParameter(
        ParameterContext parameterContext,
        ExtensionContext extensionContext
    ) throws ParameterResolutionException {
        return Provider.parameterResolver.supportsParameter(parameterContext, extensionContext);
    }

    @Override
    public Object resolveParameter(
        ParameterContext parameterContext,
        ExtensionContext extensionContext
    ) throws ParameterResolutionException {
        return Provider.parameterResolver.resolveParameter(parameterContext, extensionContext);
    }

    @ApiStatus.Internal
    public static final class Provider {
        @Inject
        static Internal parameterResolver;
    }

    @ApiStatus.Internal
    public interface Internal extends ParameterResolver {
        @Nullable TestCycle getInternalValue();
    }
}
