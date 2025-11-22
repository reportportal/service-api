package com.epam.reportportal.infrastructure.persistence.entity.tms;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;

/**
 * Junction entity for Many-to-Many relationship between TmsTestCaseExecutionComment and
 * TmsAttachment.
 */
@Entity
@Table(name = "tms_test_case_execution_comment_attachment")
@Getter
@Setter
public class TmsTestCaseExecutionCommentAttachment {

  @EmbeddedId
  private TmsTestCaseExecutionCommentAttachmentId id;

  @ManyToOne(fetch = FetchType.LAZY)
  @MapsId("executionCommentId")
  @JoinColumn(name = "execution_comment_id", nullable = false)
  private TmsTestCaseExecutionComment executionComment;

  @ManyToOne(fetch = FetchType.LAZY)
  @MapsId("attachmentId")
  @JoinColumn(name = "attachment_id", nullable = false)
  private TmsAttachment attachment;

  public TmsTestCaseExecutionCommentAttachment() {
    this.id = new TmsTestCaseExecutionCommentAttachmentId();
  }

  public TmsTestCaseExecutionCommentAttachment(TmsTestCaseExecutionComment executionComment,
      TmsAttachment attachment) {
    this.id = new TmsTestCaseExecutionCommentAttachmentId(executionComment.getId(),
        attachment.getId());
    this.executionComment = executionComment;
    this.attachment = attachment;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TmsTestCaseExecutionCommentAttachment that = (TmsTestCaseExecutionCommentAttachment) o;
    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
