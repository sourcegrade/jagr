package org.sourcegrade.jagr.gradle.extension

import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Provider
import org.sourcegrade.jagr.gradle.task.TargetSourceSetsTask

internal fun Project.relative(path: String): Project {
    if (path.isEmpty()) return this
    return project.project(path)
}

internal fun TargetSourceSetsTask.resolveBuildFile(
    configurationNameProvider: Provider<String>,
    pathResolver: (configurationName: String) -> String,
): Provider<RegularFile> {
    return configurationNameProvider.flatMap { configurationName ->
        project.layout.buildDirectory.map { it.file(pathResolver(configurationName)) }
    }
}

internal fun TargetSourceSetsTask.resolveBuildDirectory(
    configurationNameProvider: Provider<String>,
    pathResolver: (configurationName: String) -> String,
): Provider<Directory> {
    return configurationNameProvider.flatMap { configurationName ->
        project.layout.buildDirectory.map { it.dir(pathResolver(configurationName)) }
    }
}

internal fun TargetSourceSetsTask.createSubmissionInfoFileProperty(
    configurationNameProvider: Provider<String>,
): RegularFileProperty {
    return project.objects.fileProperty()
        .value(resolveBuildFile(configurationNameProvider) { "resources/jagr/$it/submission-info.json" })
}

internal fun TargetSourceSetsTask.createGraderInfoFileProperty(): RegularFileProperty {
    return project.objects.fileProperty()
        .value(resolveBuildFile(configurationName) { "resources/jagr/$it/grader-info.json" })
}

internal fun TargetSourceSetsTask.createRubricOutputDirectoryProperty(): DirectoryProperty {
    return project.objects.directoryProperty()
        .value(resolveBuildDirectory(configurationName) { "resources/jagr/$it/rubric" })
}
