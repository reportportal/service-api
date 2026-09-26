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
 * Settings of a pipeline, stored as one JSON object. Behaviour plugged into the pipeline reads its own
 * entries and the core never interprets them.
 * <p>
 * Example: {@code {"autoReady": {"enabled": true, "threshold": 90}}} switches on Auto-Ready for the test
 * cases a test case generation pipeline produces.
 */
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class PipelineSettings extends JsonbUserType<PipelineSettings> implements Serializable {

  private Map<String, Object> values = new LinkedHashMap<>();

  @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
  public PipelineSettings(Map<String, Object> values) {
    this.values = values == null ? new LinkedHashMap<>() : new LinkedHashMap<>(values);
  }

  @JsonValue
  public Map<String, Object> getValues() {
    return values;
  }

  @Override
  public Class<PipelineSettings> returnedClass() {
    return PipelineSettings.class;
  }
}
