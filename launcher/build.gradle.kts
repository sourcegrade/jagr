plugins {
  kotlin("jvm")
  kotlin("kapt")
  kotlin("plugin.serialization")
  id("com.github.johnrengelman.shadow")
}

version = "0.2.1-SNAPSHOT"

val configurateVersion: String by project
val guiceVersion: String by project
val jetbrainsAnnotationsVersion: String by project
val kotlinxSerializationVersion: String by project
val log4jVersion: String by project
val slf4jVersion: String by project

dependencies {
  api(project(":jagr-grader-api"))
  api("com.google.inject:guice:$guiceVersion")
  api("org.slf4j:slf4j-api:$slf4jVersion")
  api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.0")
  api("org.spongepowered:configurate-hocon:$configurateVersion")
  implementation("org.jetbrains:annotations:$jetbrainsAnnotationsVersion")
  implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:$kotlinxSerializationVersion")
  implementation("org.jetbrains.kotlinx:kotlinx-serialization-core-jvm:$kotlinxSerializationVersion")
  implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinxSerializationVersion")
  implementation("org.apache.logging.log4j:log4j-slf4j-impl:$log4jVersion")
  implementation("org.apache.logging.log4j:log4j-core:$log4jVersion")
  kapt("org.apache.logging.log4j:log4j-core:$log4jVersion")
  implementation("org.fusesource.jansi:jansi:2.3.1")
  implementation("com.github.albfernandez:juniversalchardet:2.4.0")
  implementation(kotlin("reflect"))
}
