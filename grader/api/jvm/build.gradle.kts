import org.sourcegrade.jagr.script.apiProjects

plugins {
    kotlin("jvm")
}

version = checkNotNull(rootProject.ext["apiVersion"])

dependencies {
    apiProjects(version, "jagr-grader-api")
    api(libs.bundles.junit)
    implementation(libs.annotations)
}
