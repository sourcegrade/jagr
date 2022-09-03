/*
 *   Jagr - SourceGrade.org
 *   Copyright (C) 2021-2022 Alexander Staeding
 *   Copyright (C) 2021-2022 Contributors
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

package org.sourcegrade.jagr.api.rubric;

import com.google.inject.Inject;
import org.jetbrains.annotations.ApiStatus;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.launcher.TestIdentifier;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Represents a JUnit test.
 */
@FunctionalInterface
public interface JUnitTestRef extends TestRef<TestIdentifier> {

    /**
     * Creates a {@link JUnitTestRef} from a JUnit {@link Class}.
     *
     * @param clazz The JUnit {@link Class} to create a {@link JUnitTestRef} from
     * @return A {@link JUnitTestRef} from a JUnit {@link Class}
     */
    static JUnitTestRef ofClass(Class<?> clazz) {
        return FactoryProvider.factory.ofClass(clazz);
    }

    /**
     * Creates a {@link JUnitTestRef} from a JUnit {@link Class}.
     *
     * @param clazzSupplier A {@link Callable} that returns the JUnit {@link Class} to create a {@link JUnitTestRef} from
     * @return A {@link JUnitTestRef} from a JUnit {@link Class}
     */
    static JUnitTestRef ofClass(Callable<Class<?>> clazzSupplier) {
        return FactoryProvider.factory.ofClass(clazzSupplier);
    }

    /**
     * Creates a {@link JUnitTestRef} from a JUnit {@link Method}.
     *
     * @param method The JUnit {@link Method} to create the {@link JUnitTestRef} from
     * @return A {@link JUnitTestRef} from the given {@link Method}
     */
    static JUnitTestRef ofMethod(Method method) {
        return FactoryProvider.factory.ofMethod(method);
    }

    /**
     * Creates a {@link JUnitTestRef} from a JUnit {@link Method}.
     *
     * @param methodSupplier A {@link Callable} that returns the JUnit {@link Method} to create the {@link JUnitTestRef} from
     * @return A {@link JUnitTestRef} from the given {@link Method}
     */
    static JUnitTestRef ofMethod(Callable<Method> methodSupplier) {
        return FactoryProvider.factory.ofMethod(methodSupplier);
    }

    /**
     * Combines the provided {@link JUnitTestRef}s into a single {@link JUnitTestRef} that passes only if
     * all the provided {@link JUnitTestRef}s pass.
     *
     * @param testRefs The {@link JUnitTestRef}s to combine
     * @return A {@link JUnitTestRef} that passes only if all the provided {@link JUnitTestRef}s pass
     */
    static JUnitTestRef and(JUnitTestRef... testRefs) {
        return FactoryProvider.factory.and(testRefs);
    }

    /**
     * Combines the provided {@link JUnitTestRef}s into a single {@link JUnitTestRef} that passes only if
     * at least one of the provided {@link JUnitTestRef}s pass.
     *
     * @param testRefs The {@link JUnitTestRef}s to combine
     * @return A {@link JUnitTestRef} that passes only if at least one of the provided {@link JUnitTestRef}s pass
     */
    static JUnitTestRef or(JUnitTestRef... testRefs) {
        return FactoryProvider.factory.or(testRefs);
    }

    /**
     * Negates the provided {@link JUnitTestRef}.
     *
     * @param testRef The {@link JUnitTestRef} to negate
     * @return A {@link JUnitTestRef} that passes iff the provided {@link JUnitTestRef} fails
     */
    static JUnitTestRef not(JUnitTestRef testRef) {
        return FactoryProvider.factory.not(testRef);
    }

    @ApiStatus.Internal
    final class FactoryProvider {
        @Inject
        private static Factory factory;
    }

    @ApiStatus.Internal
    interface Factory {
        JUnitTestRef ofClass(Class<?> clazz);

        JUnitTestRef ofClass(Callable<Class<?>> clazzSupplier);

        JUnitTestRef ofMethod(Method method);

        JUnitTestRef ofMethod(Callable<Method> methodSupplier);

        JUnitTestRef and(JUnitTestRef... testRefs);

        JUnitTestRef or(JUnitTestRef... testRefs);

        JUnitTestRef not(JUnitTestRef testRef);
    }
}
