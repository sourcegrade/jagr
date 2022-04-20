plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}
repositories {
    mavenCentral()
    maven("https://repo.spongepowered.org/repository/maven-public/")
}

val asmVersion: String by project
val configurateVersion: String by project
val jansiVersion: String by project
val juniversalchardetVersion: String by project
val kotlinxSerializationVersion: String by project
val log4jVersion: String by project

dependencies {
    api(project(":jagr-grader-api"))
    api(project(":jagr-launcher"))
    implementation(libs.csv)
    implementation(libs.asm.util)
    implementation(libs.serialization)
    implementation(libs.logging.impl)
    implementation(libs.jansi)
    implementation(libs.juniversalchardet)
    implementation(libs.configurate.core)
    implementation(kotlin("reflect"))
    implementation(files("../gradle/wrapper/gradle-wrapper.jar"))
    runtimeOnly("org.apiguardian:apiguardian-api:1.1.2")
}
tasks {
    test {
        useJUnitPlatform()
    }
}
