package com.epam.reportportal.base.infrastructure.persistence.entity.aifactory;

import com.epam.reportportal.base.infrastructure.persistence.commons.JsonbUserType;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * The raw output a stage reported, kept exactly as received (e.g. the grader's output) next to the
 * reference of the schema it was validated against.
 */
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class PipelineRawOutput extends JsonbUserType<PipelineRawOutput> implements Serializable {

  private Map<String, Object> content = new LinkedHashMap<>();

  @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
  public PipelineRawOutput(Map<String, Object> content) {
    this.content = content == null ? new LinkedHashMap<>() : new LinkedHashMap<>(content);
  }

  @JsonValue
  public Map<String, Object> getContent() {
    return content;
  }

  @Override
  public Class<PipelineRawOutput> returnedClass() {
    return PipelineRawOutput.class;
  }
}
