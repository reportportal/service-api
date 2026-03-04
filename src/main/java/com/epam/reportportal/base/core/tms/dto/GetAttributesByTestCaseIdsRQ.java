package com.epam.reportportal.base.core.tms.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetAttributesByTestCaseIdsRQ {

  @NotEmpty(message = "Test case IDs must not be empty")
  private List<Long> testCaseIds;
}
