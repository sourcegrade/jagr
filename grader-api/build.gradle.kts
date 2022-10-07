import org.sourcegrade.jagr.script.JagrPublishPlugin

plugins {
    `java-library`
}

apply<JagrPublishPlugin>()

val apiVersion: String by rootProject
version = apiVersion

dependencies {
    api(libs.asm.core)
    api(libs.guice)
    api(libs.logging.api)
    api(libs.bundles.junit)
    implementation(libs.annotations)
}
