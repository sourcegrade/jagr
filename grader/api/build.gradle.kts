plugins {
    `java-library`
    kotlin("jvm")
}

version = checkNotNull(rootProject.ext["apiVersion"])

dependencies {
    api(libs.guice)
    api(libs.logging.api)
    implementation(libs.annotations)
}
