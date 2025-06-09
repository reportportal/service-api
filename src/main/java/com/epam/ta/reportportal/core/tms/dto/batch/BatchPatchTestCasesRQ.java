package com.epam.ta.reportportal.core.tms.dto.batch;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for batch patch operations on test cases.
 * Contains information about what fields should be updated for the selected test cases.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BatchPatchTestCasesRQ {

  /**
   * ID of the folder where the test cases should be moved.
   * This is the primary field used for batch operations to move multiple test cases
   * to a different folder at once.
   */
  @NotNull(message = "Test folder ID is required for batch update operation")
  private Long testFolderId;
}
