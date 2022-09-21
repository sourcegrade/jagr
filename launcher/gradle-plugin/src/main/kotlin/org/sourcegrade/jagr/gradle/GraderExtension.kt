package org.sourcegrade.jagr.gradle

import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property

abstract class GraderExtension : ExtensionAware {
    abstract val graderName: Property<String>
    abstract val assignmentId: Property<String>
    abstract val sourceSets: ListProperty<String>
}
