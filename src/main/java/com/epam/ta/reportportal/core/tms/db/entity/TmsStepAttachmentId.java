package com.epam.ta.reportportal.core.tms.db.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Composite primary key for TmsStepAttachment entity.
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TmsStepAttachmentId implements Serializable {

  @Column(name = "step_id")
  private Long stepId;

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
    TmsStepAttachmentId that = (TmsStepAttachmentId) o;
    return Objects.equals(stepId, that.stepId) && Objects.equals(attachmentId, that.attachmentId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(stepId, attachmentId);
  }

  @Override
  public String toString() {
    return "TmsStepAttachmentId{" +
        "stepId=" + stepId +
        ", attachmentId=" + attachmentId +
        '}';
  }
}
