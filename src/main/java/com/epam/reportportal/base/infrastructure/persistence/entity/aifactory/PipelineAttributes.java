package com.epam.reportportal.base.infrastructure.persistence.entity.aifactory;

import com.epam.reportportal.base.core.aifactory.model.PipelineAttribute;
import com.epam.reportportal.base.infrastructure.persistence.commons.JsonbUserType;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Typed attributes of a pipeline, iteration, stage or stage attempt, stored as a plain JSON array.
 */
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class PipelineAttributes extends JsonbUserType<PipelineAttributes> implements Serializable {

  private List<PipelineAttribute> items = new ArrayList<>();

  @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
  public PipelineAttributes(List<PipelineAttribute> items) {
    this.items = items == null ? new ArrayList<>() : new ArrayList<>(items);
  }

  @JsonValue
  public List<PipelineAttribute> getItems() {
    return items;
  }

  @Override
  public Class<PipelineAttributes> returnedClass() {
    return PipelineAttributes.class;
  }
}
