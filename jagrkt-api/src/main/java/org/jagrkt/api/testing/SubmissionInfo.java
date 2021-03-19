package org.jagrkt.api.testing;

import org.jetbrains.annotations.ApiStatus;

/**
 * Represents the contents of a submission-info.json file
 */
@ApiStatus.NonExtendable
public interface SubmissionInfo {

  String getAssignmentId();

  String getFirstName();

  String getLastName();
}
