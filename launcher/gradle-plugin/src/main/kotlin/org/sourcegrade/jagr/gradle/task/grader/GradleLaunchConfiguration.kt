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

package org.sourcegrade.jagr.gradle.task.grader

import org.apache.logging.log4j.Logger
import org.apache.logging.log4j.core.config.Configurator
import org.sourcegrade.jagr.launcher.env.Config
import org.sourcegrade.jagr.launcher.env.LaunchConfiguration

internal class GradleLaunchConfiguration(override val config: Config) : LaunchConfiguration {
    override val logger: Logger by lazy {
        Configurator.initialize(
            "console-only",
            "log4j2-console-only.xml"
        ).getLogger("jagr")
    }
}
