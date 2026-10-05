package com.epam.reportportal.base.infrastructure.persistence.entity.aifactory;

import com.epam.reportportal.base.infrastructure.persistence.commons.JsonbUserType;
import java.io.Serializable;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Free-form key:value tags (e.g. {@code skill=create-test-cases@0.4}) shown as chips on
 * the iteration detail screen. String-valued, unlike {@link PipelineMetrics} — these are
 * labels, not measured numbers.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class PipelineAttributes extends JsonbUserType<PipelineAttributes> implements Serializable {

  private Map<String, String> attributes;

  @Override
  public Class<PipelineAttributes> returnedClass() {
    return PipelineAttributes.class;
  }
}
