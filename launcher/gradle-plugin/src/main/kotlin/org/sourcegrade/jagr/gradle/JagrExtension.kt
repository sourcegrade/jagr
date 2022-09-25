package org.sourcegrade.jagr.gradle

import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.container
import javax.inject.Inject

abstract class JagrExtension @Inject constructor(
    project: Project,
) : ExtensionAware {
    abstract val assignmentId: Property<String>
    val graders: NamedDomainObjectContainer<GraderConfiguration> = project.container()
    val submissions: NamedDomainObjectContainer<SubmissionConfiguration> = project.container()
    fun graders(configure: NamedDomainObjectContainer<GraderConfiguration>.() -> Unit) = graders.configure()
    fun submissions(configure: NamedDomainObjectContainer<SubmissionConfiguration>.() -> Unit) = submissions.configure()
}
