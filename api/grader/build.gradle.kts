plugins {
    id("jagr-publish")
    id("jagr-sign")
    id("kotlin-jvm.base-conventions")
}

tasks {
    withType<PublishToMavenRepository> {
        // check if rootProject version ends with .0 or .0-SNAPSHOT
        onlyIf { rootProject.version.toString().matches(".*\\.0(-SNAPSHOT)?\$".toRegex()) }
    }
}

val apiVersion: String by rootProject
version = apiVersion

dependencies {
    jvmMainApi(libs.asm.core)
    jvmMainApi(libs.guice)
    jvmMainApi(libs.logging.api)
    jvmMainApi(libs.bundles.junit)
    jvmMainImplementation(libs.annotations)
}
