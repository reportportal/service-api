package com.epam.ta.reportportal.core.tms.db.entity;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Entity representing a TMS test folder with its subfolders and their respective test case counts.
 *
 * <p>This class encapsulates information about a test folder hierarchy, including the main folder
 * with its total test case count and a mapping of subfolder IDs to their individual test case
 * counts. It provides convenient access to test case statistics for both the main folder and its
 * subfolders.
 * </p>
 */
@Data
@AllArgsConstructor
public class TmsTestFolderWithSubfoldersTestCaseCount {

  /**
   * The main test folder containing information about the folder itself and its total test case
   * count. This includes both direct test cases in the folder and aggregated counts from
   * subfolders.
   */
  private TmsTestFolderWithCountOfTestCases folderWithSubfolders;

  /**
   * A mapping of subfolder IDs to their respective test case counts.
   *
   * <p>Key: Subfolder ID (Long)<br>
   * Value: Number of test cases in that subfolder (Long)
   * </p>
   */
  private Map<Long, Long> subFolderTestCaseCounts;

  /**
   * Retrieves the test case count for a specific subfolder.
   *
   * <p>If the specified folder ID is not found in the subfolder test case counts map,
   * this method returns 0 as the default count.
   * </p>
   *
   * @param folderId the ID of the subfolder for which to retrieve the test case count
   * @return the number of test cases in the specified subfolder, or 0 if the folder ID is not found
   * @throws NullPointerException if folderId is null and the map implementation doesn't support
   *                              null keys
   */
  public Long getSubFolderTestCaseCount(Long folderId) {
    return subFolderTestCaseCounts.getOrDefault(folderId, 0L);
  }
}
