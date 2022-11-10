package org.sourcegrade.jagr.core.rubric

import org.junit.platform.engine.TestExecutionResult
import org.opentest4j.AssertionFailedError
import java.lang.reflect.InvocationTargetException

internal val TestExecutionResult.message: String?
    get() = throwable.orElse(null)?.run {
        when (this) {
            is AssertionFailedError,
            -> message.toString()
            // students should not see an invocation target exception
            // it's better to show the actual exception thrown from their code
            is InvocationTargetException,
            -> cause?.prettyMessage
            else -> prettyMessage
        }
    }

internal val Throwable.prettyMessage
    get() = "${this::class.simpleName}: $message @ ${stackTrace.firstOrNull()}"
