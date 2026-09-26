package com.epam.reportportal.base.infrastructure.persistence.entity.aifactory;

import com.epam.reportportal.base.core.aifactory.model.PipelineExecution;
import com.epam.reportportal.base.infrastructure.persistence.commons.JsonbUserType;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.io.Serializable;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Where and how a stage or stage attempt ran, stored as a plain JSON object.
 */
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class PipelineExecutionData extends JsonbUserType<PipelineExecutionData> implements Serializable {

  private PipelineExecution content;

  @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
  public PipelineExecutionData(PipelineExecution content) {
    this.content = content;
  }

  @JsonValue
  public PipelineExecution getContent() {
    return content;
  }

  @Override
  public Class<PipelineExecutionData> returnedClass() {
    return PipelineExecutionData.class;
  }
}
