plugins {
    `java-library`
    kotlin("jvm")
}

val apiVersion: String by rootProject
version = apiVersion

dependencies {
    api(libs.asm.core)
    api(libs.guice)
    api(libs.logging.api)
    api(libs.bundles.junit)
    implementation(libs.annotations)
}
