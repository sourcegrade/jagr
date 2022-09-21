package org.sourcegrade.jagr.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register
import org.sourcegrade.jagr.gradle.task.GradeTask
import org.sourcegrade.jagr.gradle.task.GraderJarTask
import org.sourcegrade.jagr.gradle.task.GraderLibsTask
import org.sourcegrade.jagr.gradle.task.WriteGraderInfoTask
import org.sourcegrade.submitter.SubmitterPlugin

class JagrGradlePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.apply<SubmitterPlugin>()

        // create extensions
        val jagr = target.extensions.create<JagrExtension>("jagr")
        val grader = jagr.extensions.create<GraderExtension>("grader")

        // create source sets
        target.extensions.getByType<SourceSetContainer>().apply {
            val graderPublic = create("graderPublic") {
                val test: SourceSet = getByName("test")
                it.compileClasspath += test.output + test.compileClasspath
                it.runtimeClasspath += it.output + test.runtimeClasspath
            }
            create("graderPrivate") {
                it.compileClasspath += graderPublic.output + graderPublic.compileClasspath
                it.runtimeClasspath += it.output + graderPublic.runtimeClasspath
            }
        }

        // create tasks
        target.tasks.register<GraderJarTask>("graderPrivateJar") {
            graderName.set(grader.graderName)
            graderSourceSets.set(listOf("graderPrivate", "graderPublic"))
        }
        target.tasks.register<GraderJarTask>("graderPublicJar") {
            graderName.set(grader.graderName)
            graderSourceSets.set(listOf("graderPublic"))
        }
        target.tasks.register<GraderLibsTask>("graderLibs") {
            graderName.set(grader.graderName)
            assignmentId.set(grader.assignmentId)
        }
        target.tasks.register("graderAll") {
            it.group = "build"
            it.dependsOn("graderJar", "graderLibs")
        }
        target.tasks.register<GradeTask>("grade")
        target.tasks.register<WriteGraderInfoTask>("writeGraderInfo") {
            graderName.set(grader.graderName)
            assignmentId.set(grader.assignmentId)
        }

        // add jagr dependency to target project
        target.dependencies.addProvider<_, ExternalModuleDependency>(
            "compileOnly",
            jagr.toolVersion.map { "org.sourcegrade:jagr-launcher:$it" }
        ) {
            it.exclude(mapOf("org.jetbrains" to "annotations"))
        }
    }
}
