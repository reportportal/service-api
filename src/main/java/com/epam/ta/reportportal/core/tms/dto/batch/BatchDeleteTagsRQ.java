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
public class BatchDeleteTagsRQ {

  @NotEmpty(message = "Test case IDs cannot be empty")
  private List<Long> testCaseIds;

  @NotEmpty(message = "Tag IDs cannot be empty")
  private List<Long> tagIds;
}
