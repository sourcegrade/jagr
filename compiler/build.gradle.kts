import org.sourcegrade.jagr.script.apiProjects
import org.sourcegrade.jagr.script.runtimeOnlyProjects

plugins {
    kotlin("jvm")
}

dependencies {
    apiProjects(version, "jagr-compiler-api")
    runtimeOnlyProjects(version, "jagr-compiler-core-jvm", "jagr-compiler-core-racket")
}
