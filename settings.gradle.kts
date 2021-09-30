rootProject.name = "Jagr"
include(":jagr-core")
include(":jagr-grader-api")
include(":jagr-launcher")
project(":jagr-core").projectDir = File("core")
project(":jagr-grader-api").projectDir = File("grader-api")
project(":jagr-launcher").projectDir = File("launcher")


pluginManagement {
  plugins {
    val kotlinVersion: String by settings
    val shadowVersion: String by settings
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion
    id("com.github.johnrengelman.shadow") version shadowVersion
  }
}
