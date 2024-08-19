/*
 *   Jagr - SourceGrade.org
 *   Copyright (C) 2021-2024 Alexander Städing
 *   Copyright (C) 2021-2024 Contributors
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

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * Checks whether Jagr is currently *not* being used and skips the target test if it is.
 * <p>
 * Inverse of {@link JagrExecutionCondition}.
 */
public final class NonJagrExecutionCondition implements ExecutionCondition {
    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        if (TestCycleResolver.Provider.parameterResolver == null) {
            return ConditionEvaluationResult.enabled("Jagr is not present, enabled");
        } else {
            return ConditionEvaluationResult.disabled("Jagr is present, disabled");
        }
    }
}
