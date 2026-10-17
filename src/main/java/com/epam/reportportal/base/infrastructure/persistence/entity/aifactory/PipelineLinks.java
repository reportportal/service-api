package com.epam.reportportal.base.infrastructure.persistence.entity.aifactory;

import com.epam.reportportal.base.core.aifactory.model.PipelineLink;
import com.epam.reportportal.base.infrastructure.persistence.commons.JsonbUserType;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Links of a stage (to a CI job, to something in ReportPortal, ...), stored as a plain JSON array.
 */
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class PipelineLinks extends JsonbUserType<PipelineLinks> implements Serializable {

  private List<PipelineLink> items = new ArrayList<>();

  @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
  public PipelineLinks(List<PipelineLink> items) {
    this.items = items == null ? new ArrayList<>() : new ArrayList<>(items);
  }

  @JsonValue
  public List<PipelineLink> getItems() {
    return items;
  }

  @Override
  public Class<PipelineLinks> returnedClass() {
    return PipelineLinks.class;
  }
}
