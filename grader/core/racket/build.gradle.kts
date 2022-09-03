import org.sourcegrade.jagr.script.apiProjects

plugins {
    kotlin("jvm")
}

dependencies {
    apiProjects(version, "jagr-grader-api-racket")
    apiProjects(version, "jagr-grader-core")
}
