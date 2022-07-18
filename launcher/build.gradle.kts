plugins {
    kotlin("jvm")
    kotlin("kapt")
    kotlin("plugin.serialization")
    id("com.github.johnrengelman.shadow")
}

dependencies {
    // force release version of API on release to prevent transitive dependency on snapshot version of API
    if (version.toString().endsWith("SNAPSHOT") || version.toString().endsWith(".0")) {
        api(project(":jagr-grader-api"))
    } else {
        api("org.sourcegrade:jagr-grader-api:${rootProject.extra["apiVersion"]}")
    }
    api(libs.coroutines)
    implementation(libs.configurate.hocon)
    implementation(libs.annotations)
    implementation(libs.serialization)
    implementation(libs.logging.impl)
    implementation(libs.logging.core)
    kapt(libs.logging.core)
    implementation(kotlin("reflect"))
}
