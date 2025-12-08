package com.epam.reportportal.infrastructure.persistence.entity.tms;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Composite primary key for TmsManualScenarioPreconditionsAttachment entity.
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TmsManualScenarioPreconditionsAttachmentId implements Serializable {

  @Column(name = "preconditions_id")
  private Long preconditionsId;

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
    TmsManualScenarioPreconditionsAttachmentId that = (TmsManualScenarioPreconditionsAttachmentId) o;
    return Objects.equals(preconditionsId, that.preconditionsId)
        && Objects.equals(attachmentId, that.attachmentId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(preconditionsId, attachmentId);
  }

  @Override
  public String toString() {
    return "TmsManualScenarioPreconditionsAttachmentId{" +
        "preconditionsId=" + preconditionsId +
        ", attachmentId=" + attachmentId +
        '}';
  }
}
