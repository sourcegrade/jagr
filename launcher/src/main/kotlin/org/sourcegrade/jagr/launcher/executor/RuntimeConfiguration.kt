package org.sourcegrade.jagr.launcher.executor

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.File

/**
 * An interface that can be used to configure the Runtime of jagr. This is especially useful for gradle tasks.
 */
interface RuntimeConfiguration {
    /**
     * The [GradleTaskConfiguration] that is used to configure the Runtime of jagr.
     */
    val config: ParsedGradleTaskConfiguration

    /**
     * The [Logger] that is used to log messages.
     */
    val logger: Logger

    /**
     * A [RuntimeConfiguration] that is generated from the .jagrrc file in the current working directory.
     */
    object Standard : RuntimeConfiguration {
        override val config: ParsedGradleTaskConfiguration by lazy {
            val configFile = File(".jagrrc")
            if (!configFile.exists()) {
                configFile.createNewFile()
            }
            val options = configFile.readLines()
                .map { it.trim().replace("#.*".toRegex(), "") }
                .filter { it.isNotBlank() }
                // split after the first '='
                .map { it.split("=", limit = 2) }
                .associate { it[0].trim() to it[1].trim() }
            val defaultConfiguration = GradleTaskConfiguration()
            val result: ParsedGradleTaskConfiguration = GradleTaskConfiguration(
                jagrVersion = options["jagrVersion"] ?: defaultConfiguration.jagrVersion,
                jagrJar = options["jagrJar"] ?: defaultConfiguration.jagrJar,
                javaExecutable = options["javaExecutable"] ?: defaultConfiguration.javaExecutable,
                jvmArgs = options["jvmArgs"] ?: defaultConfiguration.jvmArgs,
                downloadIfMissing = options["downloadIfMissing"]?.toBoolean() ?: defaultConfiguration.downloadIfMissing,
                jagrDownloadURL = options["jagrDownloadURL"] ?: defaultConfiguration.jagrDownloadURL,
            ).parsed()
            checkNotNull(result) { "Failed to load gradle task configuration" }
        }

        /**
         * The [Logger] that is used to log messages.
         */
        override val logger: Logger by lazy {
            LogManager.getLogger("Jagr")
        }
    }
}
