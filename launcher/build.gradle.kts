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
    api("com.google.inject:guice:$guiceVersion")
    api("org.slf4j:slf4j-api:$slf4jVersion")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinxCoroutinesVersion")
    api("org.spongepowered:configurate-hocon:$configurateVersion")
    implementation("org.jetbrains:annotations:$jetbrainsAnnotationsVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:$kotlinxSerializationVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core-jvm:$kotlinxSerializationVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinxSerializationVersion")
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:$log4jVersion")
    implementation("org.apache.logging.log4j:log4j-core:$log4jVersion")
    kapt("org.apache.logging.log4j:log4j-core:$log4jVersion")
    implementation(kotlin("reflect"))
}
