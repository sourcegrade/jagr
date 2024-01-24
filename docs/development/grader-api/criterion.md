# Criterion - Overview

## Usage

Create a basic `Criterion`:

```java
public static final Criterion H1_1 = Criterion.builder()
    .shortDescription("Some short description")
    .grader(
        Grader.testAwareBuilder()
            .requirePass(JUnitTestRef.ofMethod(() -> Tests.class.getMethod("testPositiveInts")))
            .requirePass(JUnitTestRef.ofMethod(() -> Tests.class.getMethod("testNegativeInts")))
            .maxPoints(3) // default maxPoints is 1
            .minPoints(-1) // default minPoints is 0
            .pointsPassedMax() // award maximum points if ALL tests passed
            .pointsFailedMin() // award minimum points if ANY test failed
            .build()
    ).build();
```
