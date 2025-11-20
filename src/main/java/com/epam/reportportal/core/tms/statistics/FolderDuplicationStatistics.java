package com.epam.reportportal.core.tms.statistics;

import com.epam.reportportal.core.tms.dto.batch.BatchFolderOperationError;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;

/**
 * Statistics collector for folder duplication operations.
 * Tracks successful folder duplications and errors that occurred during the process.
 */
@Getter
public class FolderDuplicationStatistics {

  private final List<Long> successFolderIds = new ArrayList<>();
  private final List<BatchFolderOperationError> errors = new ArrayList<>();

  /**
   * Records a successful folder duplication.
   *
   * @param folderId The ID of the successfully duplicated folder
   */
  public void addSuccess(Long folderId) {
    successFolderIds.add(folderId);
  }

  /**
   * Records a folder duplication error.
   *
   * @param folderId     The ID of the folder that failed to duplicate
   * @param errorMessage The error message describing the failure
   */
  public void addError(Long folderId, String errorMessage) {
    errors.add(new BatchFolderOperationError(folderId, errorMessage));
  }

  /**
   * Gets the total count of folder operations (successful + failed).
   *
   * @return The total count
   */
  public int getTotalCount() {
    return successFolderIds.size() + errors.size();
  }

  /**
   * Gets the count of successful folder duplications.
   *
   * @return The success count
   */
  public int getSuccessCount() {
    return successFolderIds.size();
  }

  /**
   * Gets the count of failed folder duplications.
   *
   * @return The failure count
   */
  public int getFailureCount() {
    return errors.size();
  }
}
