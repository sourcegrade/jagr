package org.jagrkt.api.rubric;

import org.jetbrains.annotations.ApiStatus;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@ApiStatus.NonExtendable
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface RubricForSubmission {

  String value();
}
