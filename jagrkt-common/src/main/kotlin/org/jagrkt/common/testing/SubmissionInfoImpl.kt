package org.jagrkt.common.testing

import kotlinx.serialization.Serializable
import org.jagrkt.api.testing.SubmissionInfo

/**
 * Represents the contents of a submission-info.json file
 */
@Serializable
data class SubmissionInfoImpl(
  private val assignmentId: String,
  private val firstName: String,
  private val lastName: String,
) : SubmissionInfo {

  override fun getAssignmentId(): String = assignmentId
  override fun getFirstName(): String = firstName
  override fun getLastName(): String = lastName
  override fun toString(): String = assignmentId + "_" + lastName + "_" + firstName
}
