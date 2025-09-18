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
 * Composite primary key for TmsTextManualScenarioAttachment entity.
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TmsTextManualScenarioAttachmentId implements Serializable {

  @Column(name = "text_manual_scenario_id")
  private Long textManualScenarioId;

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
    TmsTextManualScenarioAttachmentId that = (TmsTextManualScenarioAttachmentId) o;
    return Objects.equals(textManualScenarioId, that.textManualScenarioId)
        && Objects.equals(attachmentId, that.attachmentId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(textManualScenarioId, attachmentId);
  }

  @Override
  public String toString() {
    return "TmsTextManualScenarioAttachmentId{" +
        "textManualScenarioId=" + textManualScenarioId +
        ", attachmentId=" + attachmentId +
        '}';
  }
}
