plugins {
  application
  kotlin("jvm")
  kotlin("plugin.serialization")
  id("com.github.johnrengelman.shadow")
}
repositories {
  mavenCentral()
  maven("https://repo.spongepowered.org/repository/maven-public/")
}
dependencies {
  api(project(":jagrkt-api"))
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.3")
  val kotlinxSerializationVersion = "1.1.0"
  implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:$kotlinxSerializationVersion")
  implementation("org.jetbrains.kotlinx:kotlinx-serialization-core-jvm:$kotlinxSerializationVersion")
  implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinxSerializationVersion")
  implementation("org.apache.logging.log4j:log4j-slf4j-impl:2.14.0")
  implementation("org.fusesource.jansi:jansi:2.3.1")
  implementation("org.ow2.asm:asm:9.1")
  implementation(kotlin("reflect"))
}
application {
  mainClassName = "org.jagrkt.common.MainKt"
}
tasks.shadowJar {
  archiveFileName.set("JagrKt-${project.version}.jar")
}
