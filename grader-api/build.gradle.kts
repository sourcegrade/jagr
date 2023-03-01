plugins {
    `java-library`
    id("jagr-publish")
}

apply<JagrPublishPlugin>()
apply<JagrSignPlugin>()

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
