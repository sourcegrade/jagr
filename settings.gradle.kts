rootProject.name = "Jagr"
include(":jagr-grader-api")
include(":jagr-core")
project(":jagr-grader-api").projectDir = File("grader-api")
project(":jagr-core").projectDir = File("core")


pluginManagement {
  plugins {
    val kotlinVersion: String by settings
    val shadowVersion: String by settings
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion
    id("com.github.johnrengelman.shadow") version shadowVersion
  }
}
