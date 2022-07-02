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

package org.sourcegrade.jagr.api.testing;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.junit.platform.launcher.TestPlan;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;

import java.util.List;

@ApiStatus.NonExtendable
public interface TestCycle {

    /**
     * The class names of every rubric provider in this test cycle.
     *
     * @return An immutable set of class names
     */
    List<String> getRubricProviderClassNames();

    /**
     * Every test cycle uses a unique {@link ClassLoader} that loads the grader jar's classes and the {@link Submission}'s
     * classes if it is a java submission.
     *
     * @return The {@link ClassLoader} used in this test cycle
     */
    ClassLoader getClassLoader();

    /**
     * The {@link Submission} used in this test cycle.
     *
     * @return The {@link Submission} used in this test cycle
     */
    Submission getSubmission();

    /**
     * The number of tests that succeeded.
     *
     * @return The number of tests that succeeded
     */
    int getTestsSucceededCount();

    /**
     * The number of tests that began execution.
     *
     * @return The number of tests that began execution
     */
    int getTestsStartedCount();

    List<String> getNotes();

    @Nullable JUnitResult getJUnitResult();

    @ApiStatus.NonExtendable
    interface JUnitResult {
        TestPlan getTestPlan();

        SummaryGeneratingListener getSummaryListener();

        TestStatusListener getStatusListener();
    }
}
