import org.sourcegrade.jagr.script.apiProjects
import org.sourcegrade.jagr.script.runtimeOnlyProjects

plugins {
    kotlin("jvm")
}

dependencies {
    apiProjects(version, "jagr-grader-api")
    runtimeOnlyProjects(version, "jagr-grader-core-jvm", "jagr-grader-core-racket")
}
