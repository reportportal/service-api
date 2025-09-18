package com.epam.ta.reportportal.core.tms.db.entity;

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
 * Junction entity for Many-to-Many relationship between TmsManualScenarioPreconditions and
 * TmsAttachment.
 */
@Entity
@Table(name = "tms_manual_scenario_preconditions_attachment")
@Getter
@Setter
public class TmsManualScenarioPreconditionsAttachment {

  @EmbeddedId
  private TmsManualScenarioPreconditionsAttachmentId id;

  @ManyToOne(fetch = FetchType.LAZY)
  @MapsId("preconditionsId")
  @JoinColumn(name = "preconditions_id", nullable = false)
  private TmsManualScenarioPreconditions preconditions;

  @ManyToOne(fetch = FetchType.LAZY)
  @MapsId("attachmentId")
  @JoinColumn(name = "attachment_id", nullable = false)
  private TmsAttachment attachment;

  public TmsManualScenarioPreconditionsAttachment() {
    this.id = new TmsManualScenarioPreconditionsAttachmentId();
  }

  public TmsManualScenarioPreconditionsAttachment(TmsManualScenarioPreconditions preconditions,
      TmsAttachment attachment) {
    this.id = new TmsManualScenarioPreconditionsAttachmentId(preconditions.getId(),
        attachment.getId());
    this.preconditions = preconditions;
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
    TmsManualScenarioPreconditionsAttachment that = (TmsManualScenarioPreconditionsAttachment) o;
    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
