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

fun TargetSourceSetsTask.resolveInBuildDirectory(pathResolver: (configurationName: String) -> String): Provider<File> {
    return configurationName.flatMap { configurationName ->
        project.layout.buildDirectory.map { it.file(pathResolver(configurationName)).asFile }
    }
}

fun TargetSourceSetsTask.createSubmissionInfoFileProperty(): Property<File> {
    return project.objects.property<File>()
        .value(resolveInBuildDirectory { "resources/jagr/$it/submission-info.json" })
}

fun TargetSourceSetsTask.createGraderInfoFileProperty(): Property<File> {
    return project.objects.property<File>()
        .value(resolveInBuildDirectory { "resources/jagr/$it/grader-info.json" })
}
