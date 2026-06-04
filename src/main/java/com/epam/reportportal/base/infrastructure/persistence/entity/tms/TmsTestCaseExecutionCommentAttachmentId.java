package com.epam.reportportal.base.infrastructure.persistence.entity.tms;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Composite primary key for TmsTestCaseExecutionCommentAttachment entity.
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TmsTestCaseExecutionCommentAttachmentId implements Serializable {

  @Column(name = "execution_comment_id")
  private Long executionCommentId;

  @Column(name = "attachment_id")
  private Long attachmentId;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TmsTestCaseExecutionCommentAttachmentId that = (TmsTestCaseExecutionCommentAttachmentId) o;
    return Objects.equals(executionCommentId, that.executionCommentId)
        && Objects.equals(attachmentId, that.attachmentId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(executionCommentId, attachmentId);
  }

  @Override
  public String toString() {
    return "TmsTestCaseExecutionCommentAttachmentId{" +
        "executionCommentId=" + executionCommentId +
        ", attachmentId=" + attachmentId +
        '}';
  }
}
