A grader is a [Gradle](https://gradle.org/) project that contains the JUnit tests and rubric definitions for a specific assignment.
To get started, create a new Gradle project and add the [jagr-gradle](/development/getting-started/gradle-setup)
plugin to your buildscript.

In general, a simple grader is a single-module Gradle project with the following source sets:

- `grader` - The JUnit tests and rubric definitions
- `main` - The solution source code (analogous to the `main` source set the student's submission)

It is possible to customize the source sets used by the grader, for example by separating public and private test
(public tests being tests that are distributed to the students for local execution before submission).

- `graderPrivate` - The JUnit tests and rubric definitions that are kept private
- `graderPublic` - The JUnit tests and rubric definitions that are distributed to the students
- `main` - The solution source code (analogous to the `main` source set the student's submission)

In this case, executing the `graderPrivate` will also execute the `graderPublic` tests.
