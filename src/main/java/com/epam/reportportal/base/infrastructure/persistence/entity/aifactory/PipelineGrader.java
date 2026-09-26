package com.epam.reportportal.base.infrastructure.persistence.entity.aifactory;

import com.epam.reportportal.base.core.aifactory.enums.PipelineGraderType;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A single grader check result reported for a pipeline stage.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PipelineGrader implements Serializable {
  private String name;
  private PipelineGraderType type;
  private String result;
  private boolean pass;
}
