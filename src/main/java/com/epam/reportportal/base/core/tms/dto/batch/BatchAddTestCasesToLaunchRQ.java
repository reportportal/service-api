package com.epam.reportportal.base.core.tms.dto.batch;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchAddTestCasesToLaunchRQ {

  private List<Long> testCaseIds;

  @Size(max = 100)
  @Schema(description = "Folders whose whole subtree of test cases is added")
  private List<Long> folderIds;
}
