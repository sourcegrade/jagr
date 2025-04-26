## Rubric - The basics

A rubric defines the way a submission should be graded.
In the form of a table, a rubric may look something like this:

### Recursive Fibonacci rubric

| Criterion                            | Max points | Min points |
|--------------------------------------|------------|------------|
| The method has javadoc               | 0          | -1         |
| The method is recursive              | 1          | 0          |
| The method works for n = 0 and n = 1 | 1          | 0          |
| The method works for n >= 0          | 1          | 0          |

Every row in the above table represents a [criterion](criterion), which may be applied to a
submission. This process is called grading and produces a grader rubric composed of
graded criteria. The result may look something like this:

### Recursive Fibonacci graded rubric

| Criterion                            | Max points | Min points | Actual points |
|--------------------------------------|------------|------------|---------------|
| The method has javadoc               | 0          | -1         | 0             |
| The method is recursive              | 1          | 0          | 1             |
| The method works for n = 0 and n = 1 | 1          | 0          | 1             |
| The method works for n >= 0          | 1          | 0          | 1             |
| **Total**                            | -          | -          | 3             |

## Writing a rubric

To get started writing a rubric, create a class that implements `RubricProvider` and annotate it with `@RubricForSubmission`

```java
import org.sourcegrade.jagr.api.rubric.*;

@RubricForSubmission("h00")
public class H00_RubricProvider implements RubricProvider {
}
```

The next step is to create a very basic rubric. This may be done like this with the [rubric builder](rubric_builder).

```java
public static final Rubric RUBRIC = Rubric.builder()
    .title("My awesome rubric")
    .build();
```

Finally, create a `RUBRIC` field and implement `getRubric()`. The end result is:

```java
import org.sourcegrade.jagr.api.rubric.*;

@RubricForSubmission("h00")
public class H00_RubricProvider implements RubricProvider {

    public static final Rubric RUBRIC = Rubric.builder()
        .title("My awesome rubric")
        .build();

    @Override
    public Rubric getRubric() {
        return RUBRIC;
    }
}
```

This will create an empty rubric for the assignment `h00`.
