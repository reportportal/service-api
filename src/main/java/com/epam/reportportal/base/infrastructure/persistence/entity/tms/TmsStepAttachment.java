package com.epam.reportportal.base.infrastructure.persistence.entity.tms;

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
 * Junction entity for Many-to-Many relationship between TmsStep and TmsAttachment.
 */
@Entity
@Table(name = "tms_step_attachment")
@Getter
@Setter
public class TmsStepAttachment {

  @EmbeddedId
  private TmsStepAttachmentId id;

  @ManyToOne(fetch = FetchType.LAZY)
  @MapsId("stepId")
  @JoinColumn(name = "step_id", nullable = false)
  private TmsStep step;

  @ManyToOne(fetch = FetchType.LAZY)
  @MapsId("attachmentId")
  @JoinColumn(name = "attachment_id", nullable = false)
  private TmsAttachment attachment;

  public TmsStepAttachment() {
    this.id = new TmsStepAttachmentId();
  }

  public TmsStepAttachment(TmsStep step, TmsAttachment attachment) {
    this.id = new TmsStepAttachmentId(step.getId(), attachment.getId());
    this.step = step;
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
    TmsStepAttachment that = (TmsStepAttachment) o;
    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
