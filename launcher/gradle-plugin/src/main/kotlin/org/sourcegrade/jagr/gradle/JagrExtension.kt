package org.sourcegrade.jagr.gradle

import org.gradle.api.model.ObjectFactory
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.property
import javax.inject.Inject

abstract class JagrExtension @Inject constructor(
    objectFactory: ObjectFactory,
) : ExtensionAware {
}
