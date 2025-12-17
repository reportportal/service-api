package com.epam.reportportal.core.tms.dto.batch;

import com.epam.reportportal.core.tms.dto.TmsTestCaseRS;
import jakarta.validation.Valid;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Valid
public class BatchDuplicateTestCasesRS {

  private Long testFolderId;

  private List<TmsTestCaseRS> testCases;
}
