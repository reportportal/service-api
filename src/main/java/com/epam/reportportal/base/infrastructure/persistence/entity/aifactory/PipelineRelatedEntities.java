package com.epam.reportportal.base.infrastructure.persistence.entity.aifactory;

import com.epam.reportportal.base.core.aifactory.model.PipelineSection;
import com.epam.reportportal.base.infrastructure.persistence.commons.JsonbUserType;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * The things elsewhere in ReportPortal a stage reported it worked on or produced, kept as reported and
 * stored as a plain JSON array.
 * <p>
 * Example: {@code [{"entityType": "TEST_CASE", "entityId": "TC-12"}]}.
 */
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class PipelineRelatedEntities extends JsonbUserType<PipelineRelatedEntities> implements Serializable {

  private List<PipelineSection.EntityReference> items = new ArrayList<>();

  @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
  public PipelineRelatedEntities(List<PipelineSection.EntityReference> items) {
    this.items = items == null ? new ArrayList<>() : new ArrayList<>(items);
  }

  @JsonValue
  public List<PipelineSection.EntityReference> getItems() {
    return items;
  }

  @Override
  public Class<PipelineRelatedEntities> returnedClass() {
    return PipelineRelatedEntities.class;
  }
}
