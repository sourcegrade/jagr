package org.sourcegrade.jagr.launcher.executor

import org.sourcegrade.jagr.launcher.env.Jagr
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Comment

/**
 * A Data class that stores the Runtime Configuration of jagr.
 */
@ConfigSerializable
data class ParsedGradleTaskConfiguration(

    val jagrVersion: String = Jagr.version,

    /**
     * The Path to the Jagr-Jar
     */
    @field:Comment("the Path to the Jagr-Jar")
    val jagrJar: String,

    /**
     * The Path to the java executable that will be used to run the child processes
     */
    @field:Comment("The Path to the java executable that will be used to run the child processes")
    val javaExecutable: String = "java",

    /**
     * The JVM Arguments that will be passed to the child processes
     */
    @field:Comment("The JVM Arguments that will be passed to the child processes")
    val jvmArgs: String = "",

    /**
     * Whether to enable the download of the Jagr-Jar if it is not found in the [jagrJar] location
     */
    @field:Comment("Whether to enable the download of the Jagr-Jar if it is not found in the [jagrJar] location")
    val downloadIfMissing: Boolean = false,

    /**
     * The URL to download the Jagr-Jar from if it is not found at the [jagrJar] location and [downloadIfMissing] is true
     */
    @field:Comment("The URL to download the Jagr-Jar from if it is not found at the [jagrJar] location and [downloadIfMissing] is true")
    val jagrDownloadURL: String? = null
) {
}
