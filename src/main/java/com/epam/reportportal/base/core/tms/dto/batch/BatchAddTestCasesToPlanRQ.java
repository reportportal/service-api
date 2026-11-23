package com.epam.reportportal.base.core.tms.dto.batch;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchAddTestCasesToPlanRQ {

  private List<Long> testCaseIds;

  @Size(max = 100)
  @Schema(description = "Folders whose whole subtree of test cases is added")
  private List<@NotNull Long> folderIds;

  @JsonIgnore
  @Schema(hidden = true)
  @AssertTrue(message = "Test case IDs or folder IDs must not be empty")
  public boolean isTestCaseIdsOrFolderIdsPresent() {
    return CollectionUtils.isNotEmpty(testCaseIds) || CollectionUtils.isNotEmpty(folderIds);
  }
}
