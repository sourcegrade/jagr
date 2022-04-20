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
    api(libs.guice)
    api("com.google.inject:guice:$guiceVersion")
    api("org.slf4j:slf4j-api:$slf4jVersion")
    api("org.junit.jupiter:junit-jupiter:$junitVersion")
    api("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
    api("org.junit.platform:junit-platform-launcher:$junitPlatformVersion")
    api("org.ow2.asm:asm:$asmVersion")
    implementation("org.jetbrains:annotations:$jetbrainsAnnotationsVersion")
}
