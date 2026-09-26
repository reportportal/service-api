package com.epam.reportportal.base.infrastructure.persistence.entity.aifactory;

import com.epam.reportportal.base.infrastructure.persistence.commons.JsonbUserType;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * JSONB-backed collection of grader results for a pipeline stage.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class PipelineGraders extends JsonbUserType<PipelineGraders> implements Serializable {

  @Builder.Default
  private List<PipelineGrader> graders = new ArrayList<>();

  @Override
  public Class<PipelineGraders> returnedClass() {
    return PipelineGraders.class;
  }
}
