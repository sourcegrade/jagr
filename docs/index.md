# Welcome to Jagr

The Jagr AutoGrader is a powerful tool for grading programming assignments,
providing detailed test results in the form of rubrics for each submission.

## Features

- Easy Submission Process for students via [Gradle Plugin](development/getting-started/gradle-setup)
- Fluent API for defining grading rubric
- Write tests in familiar JUnit 5 style
- Access to the submission's source code in tests
- Integration with powerful bytecode manipulation tools such as [ASM](https://asm.ow2.io/)
- Detailed test results in the form of rubrics

## Basic Command-Line Usage

1. Create a [grader](architecture/grader)
2. Create a [submission](architecture/submission)
3. Download the [latest release](https://github.com/sourcegrade/jagr/releases)

    !!! tip

        The [jagr-bin](https://aur.archlinux.org/packages/jagr-bin) package is available on the AUR for Arch Linux users.

4. Create an empty working directory and copy the Jagr jar into it
5. Run `java -jar Jagr-x-x-x.jar`, which should create the following folder structure:

    ```text
    ./graders -- input folder for grader jars (tests and rubric providers)
    ./libs -- for libraries that are required on each submission's classpath
    ./logs -- saved log files
    ./rubrics -- the output folder for graded rubrics
    ./submissions -- input folder for submissions
    ./submissions-export -- output folder for submissions
    ```

6. Prepare the grader and submission for grading
    1. Prepare the grader jar by running the `graderBuildGrader` Gradle task in the grader project
    2. Prepare the submission jar by running the `mainBuildSubmission` Gradle task in the submission project
    3. Locate the respective jars in the `build/libs` folder of the grader and submission projects

7. Copy the grader jar into the `graders` folder and the submission jar into the `submissions` folder.
   If the grader requires any runtime dependencies (that are not already included in Jagr), copy them into the `libs` folder

    !!! tip

        The `graderBuildLibs` gradle task provided by the jagr-gradle plugin can be used to generate a fat jar containing all runtime dependencies.
        This task automatically excludes dependencies already present in the Jagr runtime.

8. Run `java -jar Jagr-x-x-x.jar` again to grade the submission
