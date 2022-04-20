plugins {
    `java-library`
    kotlin("jvm")
}

val apiVersion: String by rootProject
version = apiVersion

val asmVersion: String by project
val guiceVersion: String by project
val jetbrainsAnnotationsVersion: String by project
val junitVersion: String by project
val junitPlatformVersion: String by project
val slf4jVersion: String by project

dependencies {
    api(libs.asm.core)
    api(libs.guice)
    api(libs.logging.api)
    api(libs.bundles.junit)
    implementation(libs.annotations)
}
