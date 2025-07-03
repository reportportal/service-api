package com.epam.ta.reportportal.core.tms.db.repository;

import com.epam.ta.reportportal.core.tms.db.entity.TmsTestCase;
import com.epam.ta.reportportal.dao.ReportPortalRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TmsTestCaseRepository extends ReportPortalRepository<TmsTestCase, Long> {

  @Query("SELECT tc FROM TmsTestCase tc " +
      "JOIN FETCH tc.testFolder tf " +
      "LEFT JOIN FETCH tc.dataset ds " +
      "LEFT JOIN FETCH tc.tags t " +
      "LEFT JOIN FETCH tc.versions v " +
      "WHERE tf.project.id = :projectId"
  )
  List<TmsTestCase> findByTestFolder_ProjectId(Long projectId);

  @Query("SELECT tc FROM TmsTestCase tc " +
      "JOIN FETCH tc.testFolder tf " +
      "LEFT JOIN FETCH tc.dataset ds " +
      "LEFT JOIN FETCH tc.tags t " +
      "LEFT JOIN FETCH tc.versions v " +
      "WHERE tf.project.id = :projectId AND tc.id = :id"
  )
  Optional<TmsTestCase> findByIdAndProjectId(Long id, Long projectId);

  /**
   * Finds test cases by project with optional search and folder filtering, supporting pagination.
   *
   * @param projectId The project ID
   * @param search Optional search term for full-text search in name and description
   * @param testFolderId Optional test folder ID to filter by specific folder
   * @param pageable Pagination parameters
   * @return Page of test cases matching the criteria
   */
  @Query("SELECT tc FROM TmsTestCase tc " +
      "JOIN FETCH tc.testFolder tf " +
      "LEFT JOIN FETCH tc.tags t " +
      "LEFT JOIN FETCH tc.versions v " +
      "WHERE tf.project.id = :projectId " +
//      "AND (:search IS NULL OR " + TODO add
//      "     LOWER(tc.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
//      "     LOWER(tc.description) LIKE LOWER(CONCAT('%', :search, '%'))) " +
      "AND (:testFolderId IS NULL OR tf.id = :testFolderId)"
  )
  Page<TmsTestCase> findByCriteria(@Param("projectId") Long projectId,
      @Param("search") String search,
      @Param("testFolderId") Long testFolderId,
      Pageable pageable);

  /**
   * Deletes all test cases that belong to the specified folder or any of its subfolders. Uses a
   * recursive Common Table Expression (CTE) to identify all subfolders at any level of nesting and
   * then deletes test cases associated with those folders.
   *
   * @param folderId  The ID of the folder
   * @param projectId The project ID to ensure folder belongs to the correct project
   */
  @Modifying
  @Query(value = "DELETE FROM tms_test_case tc " +
      "WHERE tc.test_folder_id IN (" +
      "  WITH RECURSIVE folder_hierarchy AS (" +
      "    SELECT tf.id FROM tms_test_folder tf " +
      "    JOIN project p ON tf.project_id = p.id " +
      "    WHERE tf.id = :folderId AND p.id = :projectId " +
      "    UNION ALL " +
      "    SELECT tf.id FROM tms_test_folder tf " +
      "    JOIN folder_hierarchy fh ON tf.parent_id = fh.id " +
      "  ) " +
      "  SELECT id FROM folder_hierarchy" +
      ")",
      nativeQuery = true)
  void deleteTestCasesByFolderId(@Param("projectId") Long projectId,
      @Param("folderId") Long folderId);

  /**
   * Deletes multiple test cases by their IDs in a single batch operation.
   *
   * <p>This method performs a bulk delete operation which is more efficient than deleting test cases
   * one by one. If any of the provided IDs don't exist, they will be silently ignored without
   * throwing an exception.
   * </p>
   *
   * @param testCaseIds the list of test case IDs to delete. Cannot be null, but can be empty. If
   *                    empty list is provided, no deletion will be performed.
   */
  @Modifying
  @Query(value = "DELETE FROM TmsTestCase WHERE id IN (:testCaseIds)")
  void deleteAllByTestCaseIds(@Param("testCaseIds") List<Long> testCaseIds);

  /**
   * Updates multiple test cases with new field values in a single batch operation.
   *
   * <p>This method performs a conditional update using CASE statements to only update fields when new
   * values are provided (not null). Currently supports updating the test folder ID, but is designed
   * to be extensible for additional fields in the future.
   * </p>
   * The update logic:
   * <ul>
   *   <li>If testFolderId is not null, updates the test_folder_id field</li>
   *   <li>If testFolderId is null, leaves the current test_folder_id unchanged</li>
   *   <li>Only test cases with IDs in the provided list will be updated</li>
   *   <li>Non-existent IDs will be silently ignored</li>
   * </ul>
   * <p>
   * <strong>Future extensibility:</strong> Additional fields can be added by uncommenting
   * and modifying the template CASE statements in the query.
   * </p>
   *
   * @param projectId    the project ID for additional context/validation (currently not used in
   *                     query but may be used for future security/validation purposes)
   * @param testCaseIds  the list of test case IDs to update. Cannot be null, but can be empty. If
   *                     empty, no updates will be performed.
   * @param testFolderId the new test folder ID to assign. If null, the current test folder
   *                     assignment will remain unchanged.
   */
  @Modifying
  @Query(value = "UPDATE tms_test_case "
      + "SET test_folder_id = CASE "
      + "    WHEN :testFolderId IS NOT NULL THEN :testFolderId "
      + "    ELSE test_folder_id "
      + "END "
      // here we can add another fields in the future:
      // + ", description = CASE "
      // + "    WHEN :description IS NOT NULL THEN :description "
      // + "    ELSE description "
      // + "END "
      + "WHERE id IN (:testCaseIds)",
      nativeQuery = true)
  void patch(
      @Param("projectId") Long projectId,
      @Param("testCaseIds") List<Long> testCaseIds,
      @Param("testFolderId") Long testFolderId);
}
