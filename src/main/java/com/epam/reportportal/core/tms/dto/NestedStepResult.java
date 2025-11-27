package com.epam.reportportal.core.tms.dto;

import com.epam.reportportal.infrastructure.persistence.entity.item.TestItem;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NestedStepResult {
  private List<TestItem> nestedSteps;
  private List<Long> tmsStepIds;
}
