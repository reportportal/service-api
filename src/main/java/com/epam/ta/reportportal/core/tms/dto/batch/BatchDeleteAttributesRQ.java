package com.epam.ta.reportportal.core.tms.dto.batch;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchDeleteAttributesRQ {

  @NotEmpty(message = "Test case IDs cannot be empty")
  private List<Long> testCaseIds;

  @NotEmpty(message = "Attribute IDs cannot be empty")
  private List<Long> attributeIds;
}
