package org.sourcegrade.jagr.gradle.task.grader

import org.gradle.api.Project
import org.gradle.api.artifacts.ResolvedArtifact
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.file.FileTree
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.getByType
import org.sourcegrade.jagr.gradle.extension.GraderConfiguration
import org.sourcegrade.jagr.gradle.extension.JagrExtension
import org.sourcegrade.jagr.gradle.task.JagrTaskFactory
import org.sourcegrade.jagr.launcher.env.Jagr

@Suppress("LeakingThis")
abstract class GraderLibsTask : Jar(), GraderTask {

    init {
        group = "build"
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        archiveFileName.set(graderName.map { "$it-${project.version}-libs.jar" })
        from(
            configurationName.map {
                project.extensions.getByType<JagrExtension>().graders[it]
                    .getRuntimeDependenciesRecursive().toList()
            },
        )
    }

    private fun GraderConfiguration.getRuntimeDependenciesRecursive(): Sequence<FileTree> = sequence {
        val jagrArtifacts: Set<ResolvedArtifact> = sourceSets.asSequence()
            .map { project.configurations[it.runtimeClasspathConfigurationName] }
            .flatMap { it.resolvedConfiguration.firstLevelModuleDependencies }
            .first {
                it.moduleGroup == "org.sourcegrade" &&
                    it.moduleName == "jagr-launcher" &&
                    it.moduleVersion == Jagr.version
            }.allModuleArtifacts

        (sourceSets.asSequence() + solutionConfiguration.get().sourceSets).flatMap {
            project.configurations[it.runtimeClasspathConfigurationName].resolvedConfiguration.resolvedArtifacts
        }.filter { artifact ->
            // TODO: This is not the best way to exclude modules from the project itself from the libs jar
            project.name != artifact.moduleVersion.id.group
        }.forEach { artifact ->
            val matchingJagrArtifact: ResolvedArtifact? = jagrArtifacts.firstOrNull { jagrArtifact ->
                artifact.moduleVersion.id.group == jagrArtifact.moduleVersion.id.group &&
                    artifact.moduleVersion.id.name == jagrArtifact.moduleVersion.id.name
            }
            if (matchingJagrArtifact == null) {
                yield(project.zipTree(artifact.file))
            } else {
                if (artifact.moduleVersion.id.version != matchingJagrArtifact.moduleVersion.id.version) {
                    logger.error(
                        "WARNING: Version mismatch for ${artifact.moduleVersion.id.group}:${artifact.moduleVersion.id.name} " +
                            "(local ${artifact.moduleVersion.id.version} != Jagr's ${matchingJagrArtifact.moduleVersion.id.version}). " +
                            "This dependency will not be included in the libs jar but will instead be used from Jagr at runtime.",
                    )
                }
            }
        }
        if (parentConfiguration.isPresent) {
            yieldAll(parentConfiguration.get().getRuntimeDependenciesRecursive())
        }
    }

    internal object Factory : JagrTaskFactory<GraderLibsTask, GraderConfiguration> {
        override fun determineTaskName(name: String) = "${name}BuildLibs"
        override fun configureTask(task: GraderLibsTask, project: Project, configuration: GraderConfiguration) {
            task.description = "Builds the grader libs jar for ${task.sourceSetNames.get()}"
        }
    }
}
