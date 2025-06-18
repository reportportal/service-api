package com.epam.ta.reportportal.core.tms.db.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data transfer object that represents a test folder along with the count of test cases associated
 * with that folder in the Test Management System (TMS).
 *
 * <p>This class is primarily used for reporting and analytics purposes when retrieving aggregated
 * data
 * about test folders and their test case counts. It's commonly used in repository queries that need
 * to return both the folder and the number of test cases within that folder in a single result.
 * </p>
 *
 * <p>
 * The class serves as a projection object for database queries that aggregate test case counts
 * grouped by test folder, providing efficient data retrieval for TMS dashboard and reporting
 * features.
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TmsTestFolderWithCountOfTestCases {

  private TmsTestFolder testFolder;

  private Long countOfTestCases;
}
