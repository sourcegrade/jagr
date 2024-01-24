# Gradle Setup

## Adding the plugin

The jagr-gradle plugin is a [Gradle](https://gradle.org/) plugin that makes it easier to develop graders and submissions for Jagr.
In order to use it, you must have Gradle set up for your project.
If you do not have Gradle set up, you can follow the
[Gradle Getting Started Guide](https://docs.gradle.org/current/samples/sample_building_java_libraries.html)
or create a new Gradle project using the IntelliJ project creation wizard.
Using the Kotlin DSL is recommended over the Groovy DSL.
The code snippets in this documentation will use the Kotlin DSL.

!!! tip

    Using the IntelliJ project creation wizard is the easiest way to get started with Gradle.

Add the jagr-gradle plugin to your buildscript:

```kotlin title="build.gradle.kts"
plugins {
    id("org.sourcegrade.jagr-gradle") version "0.6.0"
}
```

This will automatically add a dependency on jagr-launcher and jagr-grader-api.

## Configuring the jagr extension

The jagr extension is used to configure the jagr-gradle plugin. It has the following properties:

| Property       | Description                    |
|----------------|--------------------------------|
| `assignmentId` | The id of the assignment       |
| `graders`      | The grader configurations.     |
| `submissions`  | The submission configurations. |

### Minimal Configuration

```kotlin title="build.gradle.kts"
jagr {
    assignmentId.set("h00")
    submissions {
        val main by creating { // (1)!
            studentId.set("ab12cdef")
            firstName.set("John")
            lastName.set("Smith")
        }
    }
    graders {
        val grader by creating {
            graderName.set("FOP-2223-H00")
        }
    }
}
```

1. This method of assigning a variable is called a [delegate property](https://kotlinlang.org/docs/delegated-properties.html).
   The `creating` method returns a delegate which uses the name of the variable to create a new submission configuration.

### Customizing the submission name

Graders must run against a specific submission.
By default, graders assumes a submission name of "main" as in the minimal example.
If you want to use a different name, you must manually specify which submission the grader should use when grading
via the `submission()` method.

```kotlin title="build.gradle.kts"
jagr {
    // ...
    submissions {
        val customSubmission by creating {
            // ...
        }
    }
    graders {
        val grader by creating {
            submission(submissions["customSubmission"])
            // ...
        }
    }
}
```

### Defining custom source sets

Submissions and graders use certain source sets by default.

| Kind       | Source Sets                |
|------------|----------------------------|
| Submission | `main` and `test`          |
| Grader     | Same as configuration name |

The source sets used are defined by the `from()` method.

```kotlin title="build.gradle.kts"
jagr {
    // ...
    submissions {
        val main by creating {
            from("main", "test") // default value, not needed
            // ...
        }
    }
    graders {
        val grader by creating {
            from("grader") // default value, not needed
            // ...
        }
    }
}
```

You may provide any source set name and it will be created (if not already present).

### Compiling against a different submission

Graders must be compiled against a specific submission.
By default, graders are compiled against the submission defined by the `submission()` method
(or the "main" submission if none is explicitly defined for the grader).
To compile against a different submission, use the `solution()` method.

```kotlin title="build.gradle.kts"
jagr {
    // ...
    submissions {
        val correctSubmission by creating {
            // ...
        }
        val incorrectSubmission by creating {
            // ...
        }
    }
    graders {
        val grader by creating {
            // compiled against this submission
            solution(submissions["correctSubmission"])
            // graded against this submission
            submission(submissions["incorrectSubmission"])
            // ...
        }
    }
}
```
