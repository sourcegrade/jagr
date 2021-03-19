package org.jagrkt.api.rubric;

import org.jetbrains.annotations.ApiStatus;

import java.util.List;

/**
 * Something that has criterion children
 */
@ApiStatus.NonExtendable
public interface CriterionHolder<C> {

  List<? extends C> getChildCriteria();
}
