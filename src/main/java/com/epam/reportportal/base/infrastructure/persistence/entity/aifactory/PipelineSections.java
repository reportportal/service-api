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
 * Result tables of a stage or stage attempt, stored as a plain JSON array.
 */
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class PipelineSections extends JsonbUserType<PipelineSections> implements Serializable {

  private List<PipelineSection> items = new ArrayList<>();

  @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
  public PipelineSections(List<PipelineSection> items) {
    this.items = items == null ? new ArrayList<>() : new ArrayList<>(items);
  }

  @JsonValue
  public List<PipelineSection> getItems() {
    return items;
  }

  @Override
  public Class<PipelineSections> returnedClass() {
    return PipelineSections.class;
  }
}
