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

/**
 * A combination of {@link Submission} and grader.
 *
 * <p>
 * For every submission-grader pair, a test cycle is created. The test cycle stores the test execution information
 * and other meta data about the grader such as the rubric provider names.
 * </p>
 *
 * @see Submission
 */
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

    /**
     * The notes of this test cycle.
     *
     * @return The notes of this test cycle
     */
    List<String> getNotes();

    /**
     * The {@link JUnitResult} associated with this test cycle, if present.
     *
     * @return The {@link JUnitResult} associated with this test cycle, if present
     */
    @Nullable JUnitResult getJUnitResult();

    /**
     * Contains information about the JUnit test execution for this test cycle.
     */
    @ApiStatus.NonExtendable
    interface JUnitResult {
        /**
         * The {@link TestPlan} used to execute the tests.
         *
         * @return The {@link TestPlan} used to execute the tests
         */
        TestPlan getTestPlan();

        /**
         * The {@link SummaryGeneratingListener} used to generate the summary of the test execution.
         *
         * @return The {@link SummaryGeneratingListener} used to generate the summary of the test execution
         */
        SummaryGeneratingListener getSummaryListener();

        /**
         * The {@link TestStatusListener} of the test execution.
         *
         * @return The {@link TestStatusListener} of the test execution
         */
        TestStatusListener getStatusListener();
    }
}
