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

val asmVersion: String by project
val configurateVersion: String by project
val jansiVersion: String by project
val juniversalchardetVersion: String by project
val kotlinCoroutinesVersion: String by project
val kotlinxSerializationVersion: String by project
val log4jVersion: String by project

dependencies {
  api(project(":jagr-grader-api"))
  api(project(":jagr-launcher"))
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinCoroutinesVersion")
  implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:$kotlinxSerializationVersion")
  implementation("org.jetbrains.kotlinx:kotlinx-serialization-core-jvm:$kotlinxSerializationVersion")
  implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinxSerializationVersion")
  implementation("org.apache.logging.log4j:log4j-slf4j-impl:$log4jVersion")
  implementation("org.fusesource.jansi:jansi:$jansiVersion")
  implementation("org.ow2.asm:asm:$asmVersion")
  implementation("com.github.albfernandez:juniversalchardet:$juniversalchardetVersion")
  implementation("org.spongepowered:configurate-hocon:$configurateVersion")
  implementation("org.spongepowered:configurate-extra-kotlin:$configurateVersion")
  implementation(kotlin("reflect"))
  implementation(files("../gradle/wrapper/gradle-wrapper.jar"))
}
application {
  mainClass.set("org.sourcegrade.jagr.core.MainKt")
}
tasks {
  shadowJar {
    from("../gradlew") {
      into("org/gradle")
    }
    from("../gradlew.bat") {
      into("org/gradle")
    }
    from("../gradle/wrapper/gradle-wrapper.properties") {
      into("org/gradle")
    }
    archiveFileName.set("Jagr-${project.version}.jar")
  }
  test {
    useJUnitPlatform()
  }
}
