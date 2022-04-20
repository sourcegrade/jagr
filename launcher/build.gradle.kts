plugins {
    kotlin("jvm")
    kotlin("kapt")
    kotlin("plugin.serialization")
    id("com.github.johnrengelman.shadow")
}

val configurateVersion: String by project
val guiceVersion: String by project
val jetbrainsAnnotationsVersion: String by project
val kotlinxCoroutinesVersion: String by project
val kotlinxSerializationVersion: String by project
val log4jVersion: String by project
val slf4jVersion: String by project

dependencies {
    api(project(":jagr-grader-api"))
    api(libs.coroutines)
    implementation(libs.configurate.hocon)
    implementation(libs.annotations)
    implementation(libs.serialization)
    implementation(libs.logging.impl)
    implementation(libs.logging.core)
    kapt(libs.logging.core)
    implementation(kotlin("reflect"))
}
