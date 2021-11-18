# Jagr

**Java AutoGrader, implemented in Kotlin**

## Setup

This project's compiled, source and javadoc jars are hosted on [sonatype](https://s01.oss.sonatype.org).
Releases are available from `mavenCentral()` and snapshots from the maven repository in the following code block.

To depend on Jagr in your project, use the following lines in your gradle build script:

**build.gradle (Groovy):**
```groovy
repositories {
  mavenCentral()
  maven { url "https://s01.oss.sonatype.org/content/repositories/snapshots" } // only needed for snapshot versions
}
dependencies {
  implementation("org.sourcegrade:jagr-grader-api:$version")
}
```

**build.gradle.kts (Kotlin DSL):**
```kotlin
repositories {
  mavenCentral()
  maven("https://s01.oss.sonatype.org/content/repositories/snapshots") // only needed for snapshot versions
}
dependencies {
  implementation("org.sourcegrade:jagr-grader-api:$version")
}
```

Where `$version` is in the [semver](https://semver.org/) format, optionally suffixed with `-SNAPSHOT` for snapshots versions.
E.g: `0.1.0` or `0.1.0-SNAPSHOT`

## Usage

Create a basic `Criterion`:

```java
public static final Criterion H1_1 = Criterion.builder()
  .shortDescription("Some short description")
  .maxPoints(3) // default maxPoints is 1
  .minPoints(-1) // default minPoints is 0
  .grader(
    Grader.testAwareBuilder()
    .requirePass(JUnitTestRef.ofMethod(() -> Tests.class.getMethod("testPositiveInts")))
    .requirePass(JUnitTestRef.ofMethod(() -> Tests.class.getMethod("testNegativeInts")))
    .pointsPassedMax() // award maximum points if ALL tests passed
    .pointsFailedMin() // award minimum points if ANY test failed
    .build()
  ).build();
```

Make sure your JUnit test classes are annotated as follows, or they wont run:

```java
@TestForSubmission("H03")
public class Test {
```

A `Criterion` may be nested as follows:

```java
public static final Criterion H1_1 = Criterion.builder()....build();
public static final Criterion H1_2 = Criterion.builder()....build();

public static final Criterion H1 = Criterion.builder()
  .shortDescription("I have two child criteria!")
  .addChildCriteria(H1_1, H1_2) // maxPoints and minPoints and grading is inferred from child criteria
  .build();
```

Finally, create a `Rubric` and implement `RubricProvider`:

```java
@RubricForSubmission("H03")
public class H03_RubricProvider implements RubricProvider {
  public static final Criterion H1_1 = Criterion.builder()....build();
  public static final Criterion H1_2 = Criterion.builder()....build();
  public static final Criterion H1 = Criterion.builder()....build();

  public static final Rubric RUBRIC = Rubric.builder()
    .title("My example rubric")
    .addChildCriteria(H1)
    .build();

  @Override
  public Rubric getRubric() {
    return RUBRIC;
  }
}
```

The values of `@TestForSubmission` and `@RubricForSubmission` must match the assignmentId in the `submission-info.json` of the
submission that you wish to grade with that test and rubric. See
[submission-info.json](https://github.com/SourceGrade/SubmissionTemplate/blob/master/src/main/resources/submission-info.json)
for an example.

## Running Jagr

To run Jagr, download and place the desired compiled release of Jagr from
[releases](https://github.com/SourceGrade/Jagr/releases) in a (preferably empty) directory. Then either run the following command
in a terminal of your choice (or write a batch/bash script that you can double-click)
```bash
java -jar Jagr-VERSION.jar
```

Alternatively, you may run Jagr in-IDE via the Gradle `runShadow` task (the standard `run` task does not work).
The working directory used is `build/run`.

The following directories should be created:
```java
./graders // input folder for grader jars (tests + rubric providers)
./libs // for libraries that are required on each submission's classpath
./logs // saved log files
./rubrics // the output folder for graded rubrics
./submissions // input folder for submissions
./submissions-export // output folder for submissions
```

Place your grader jar(s) (tests + rubric providers) in `./graders` and the submission(s) you want to test in `./submissions` and
rerun Jagr.
