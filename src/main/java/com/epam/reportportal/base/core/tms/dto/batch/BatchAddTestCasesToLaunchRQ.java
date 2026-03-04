package com.epam.reportportal.base.core.tms.dto.batch;

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
}
