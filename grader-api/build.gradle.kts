import org.sourcegrade.jagr.script.JagrPublishPlugin

plugins {
    `java-library`
}

apply<JagrPublishPlugin>()

tasks {
    withType<PublishToMavenRepository> {
        // check if rootProject version ends with .0 or .0-SNAPSHOT
        onlyIf { rootProject.version.toString().matches(".*\\.0(-SNAPSHOT)?\$".toRegex()) }
    }
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
