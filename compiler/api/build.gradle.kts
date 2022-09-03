plugins {
    kotlin("jvm")
}

dependencies {
    api(libs.guice) // TODO: Abstract away in the API
    implementation(libs.annotations)
}
