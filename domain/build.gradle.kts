import org.sourcegrade.jagr.script.apiProjects

plugins {
    kotlin("jvm")
}

dependencies {
    apiProjects(version, "jagr-domain-io", "jagr-domain-scheduler")
    implementation(libs.annotations)
}
