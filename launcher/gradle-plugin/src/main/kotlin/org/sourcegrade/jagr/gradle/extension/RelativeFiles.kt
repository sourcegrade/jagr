package org.sourcegrade.jagr.gradle.extension

import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.property
import org.sourcegrade.jagr.gradle.task.TargetSourceSetsTask
import java.io.File

fun Project.relative(path: String): Project {
    if (path.isEmpty()) return this
    return project.project(path)
}

fun TargetSourceSetsTask.resolveInBuildDirectory(
    configurationNameProvider: Provider<String>,
    pathResolver: (configurationName: String) -> String,
): Provider<File> {
    return configurationNameProvider.flatMap { configurationName ->
        project.layout.buildDirectory.map { it.file(pathResolver(configurationName)).asFile }
    }
}

fun TargetSourceSetsTask.createSubmissionInfoFileProperty(
    configurationNameProvider: Provider<String>,
): Property<File> {
    return project.objects.property<File>()
        .value(resolveInBuildDirectory(configurationNameProvider) { "resources/jagr/$it/submission-info.json" })
}

fun TargetSourceSetsTask.createGraderInfoFileProperty(): Property<File> {
    return project.objects.property<File>()
        .value(resolveInBuildDirectory(configurationName) { "resources/jagr/$it/grader-info.json" })
}
