package com.epam.reportportal.base.core.tms.dto.batch;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for moving test cases inside the custom order of their folder. The test cases move
 * as one block, in their current relative order, before or after the anchor test case. Exactly one
 * of {@code beforeTestCaseId} and {@code afterTestCaseId} must be set.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BatchReorderTestCasesRQ {

  @NotNull
  @NotEmpty
  private List<Long> testCaseIds;

  /**
   * Put the test cases right before this test case of the same folder.
   */
  private Long beforeTestCaseId;

  /**
   * Put the test cases right after this test case of the same folder.
   */
  private Long afterTestCaseId;
}
