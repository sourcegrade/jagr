plugins {
    id("kotlin-jvm.base-conventions")
    id("kotlin-js.base-conventions")
}

dependencies {
    commonMainApi(project(":jagr-api"))
}
