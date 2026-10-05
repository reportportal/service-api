package com.epam.reportportal.base.infrastructure.persistence.entity.aifactory;

import com.epam.reportportal.base.infrastructure.persistence.commons.JsonbUserType;
import java.io.Serializable;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Free-form, agent-reported measurements (score, cost, tokens, ...) for a pipeline
 * iteration or stage. Deliberately untyped so new keys need no schema change.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class PipelineMetrics extends JsonbUserType<PipelineMetrics> implements Serializable {

  private Map<String, Object> metrics;

  @Override
  public Class<PipelineMetrics> returnedClass() {
    return PipelineMetrics.class;
  }
}
