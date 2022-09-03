import org.sourcegrade.jagr.script.apiProjects

plugins {
    kotlin("jvm")
}

dependencies {
    apiProjects(version, "jagr-compiler-api", "jagr-domain-io")
    api(libs.serialization)
    implementation(libs.juniversalchardet)
    implementation(kotlin("reflect"))
}
