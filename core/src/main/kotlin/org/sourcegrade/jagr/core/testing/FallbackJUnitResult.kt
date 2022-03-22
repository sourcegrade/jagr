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

package org.sourcegrade.jagr.core.testing

import org.junit.platform.launcher.TestPlan
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder
import org.junit.platform.launcher.core.LauncherFactory
import org.junit.platform.launcher.listeners.SummaryGeneratingListener
import org.sourcegrade.jagr.api.testing.TestCycle
import org.sourcegrade.jagr.api.testing.TestStatusListener

class FallbackJUnitResult : TestCycle.JUnitResult {
    private val testPlan = LauncherFactory.create().discover(LauncherDiscoveryRequestBuilder.request().build())
    private val summaryListener = SummaryGeneratingListener()
    private val statusListener = TestStatusListener { emptyMap() }
    override fun getTestPlan(): TestPlan = testPlan
    override fun getSummaryListener(): SummaryGeneratingListener = summaryListener
    override fun getStatusListener(): TestStatusListener = statusListener
}
