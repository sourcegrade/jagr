pluginManagement {
    plugins {
        val kotlinVersion: String by settings
        val shadowVersion: String by settings
        val styleVersion: String by settings
        kotlin("jvm") version kotlinVersion
        kotlin("kapt") version kotlinVersion
        kotlin("plugin.serialization") version kotlinVersion
        id("com.github.johnrengelman.shadow") version shadowVersion
        id("org.sourcegrade.style") version styleVersion
    }
}

rootProject.name = "Jagr"

sequenceOf(
    "core",
    "grader-api",
    "launcher",
).forEach {
    val project = ":jagr-$it"
    include(project)
    project(project).projectDir = file(it)
}
