package com.epam.ta.reportportal.core.tms.db.repository;

import com.epam.ta.reportportal.core.tms.db.entity.TmsTestFolder;
import com.epam.ta.reportportal.core.tms.db.entity.TmsTestFolderIdWithCountOfTestCases;
import com.epam.ta.reportportal.core.tms.db.entity.TmsTestFolderWithCountOfTestCases;
import com.epam.ta.reportportal.dao.ReportPortalRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing TMS test folders.
 *
 * <p>This repository provides methods for CRUD operations on test folders, including hierarchical
 * operations for managing folder structures, counting test cases, and handling subfolder
 * relationships within the Test Management System.
 * </p>
 *
 * @author Andrei_Varabyeu
 */
@Repository
public interface TmsTestFolderRepository extends ReportPortalRepository<TmsTestFolder, Long> {

  /**
   * Finds all test folders for a given project with their test case counts.
   *
   * <p>This method returns a paginated list of test folders along with the count of test cases
   * contained in each folder. The count includes only direct test cases, not those in subfolders.
   * </p>
   *
   * @param projectId the ID of the project to search folders in
   * @param pageable  pagination information
   * @return a page of test folders with their test case counts
   */
  @Query(
      "SELECT new com.epam.ta.reportportal.core.tms.db.entity."
          + "TmsTestFolderWithCountOfTestCases(tf, COUNT(tc))"
          + "FROM TmsTestFolder tf "
          + "LEFT JOIN tf.testCases tc "
          + "WHERE tf.project.id = :projectId "
          + "GROUP BY tf.id"
  )
  Page<TmsTestFolderWithCountOfTestCases> findAllByProjectIdWithCountOfTestCases(
      @Param("projectId") long projectId, Pageable pageable
  );

  /**
   * Finds all test folders for a given project with their test case counts.
   *
   * <p>This method returns a paginated list of test folders along with the count of test cases
   * contained in each folder. The count includes only direct test cases, not those in subfolders.
   * </p>
   *
   * @param projectId the ID of the project to search folders in
   * @param pageable  pagination information
   * @return a page of test folders with their test case counts
   */
  @Query(
      "SELECT new com.epam.ta.reportportal.core.tms.db.entity." +
          "TmsTestFolderWithCountOfTestCases(tf, COUNT(DISTINCT tc.id)) " +
          "FROM TmsTestFolder tf " +
          "JOIN tf.testCases tc " +
          "JOIN tc.testPlans tp " +
          "WHERE tf.project.id = :projectId AND tp.id = :testPlanId " +
          "GROUP BY tf.id"
  )
  Page<TmsTestFolderWithCountOfTestCases> findAllByProjectIdAndTestPlanIdWithCountOfTestCases(
      @Param("projectId") long projectId,
      @Param("testPlanId") Long testPlanId,
      Pageable pageable
  );

  /**
   * Finds test folders by their IDs and eagerly fetches their subfolders.
   *
   * <p>This method is useful when you need to work with folder hierarchies and want to avoid
   * N+1 query problems by fetching subfolders in a single query.
   * </p>
   *
   * @param folderIds list of folder IDs to retrieve
   * @return list of test folders with their subfolders loaded
   */
  @Query("SELECT tf FROM TmsTestFolder tf LEFT JOIN FETCH tf.subFolders WHERE tf.id IN :folderIds")
  List<TmsTestFolder> findByIdsWithSubFolders(@Param("folderIds") List<Long> folderIds);

  /**
   * Finds all direct subfolders of a given test folder with their test case counts.
   *
   * <p>This method returns only immediate children of the specified parent folder, not nested
   * subfolders at deeper levels. Each folder is returned with its direct test case count.
   * </p>
   *
   * @param projectId      the ID of the project
   * @param parentFolderId the ID of the parent folder
   * @param pageable       pagination information
   * @return a page of direct subfolders with their test case counts
   */
  @Query(
      "SELECT new com.epam.ta.reportportal.core.tms.db.entity."
          + "TmsTestFolderWithCountOfTestCases(tf, COUNT(tc))"
          + "FROM TmsTestFolder tf "
          + "LEFT JOIN tf.testCases tc "
          + "WHERE tf.project.id = :projectId and tf.parentTestFolder.id = :parentFolderId "
          + "GROUP BY tf.id"
  )
  Page<TmsTestFolderWithCountOfTestCases> findAllByParentTestFolderIdWithCountOfTestCases(
      @Param("projectId") long projectId,
      @Param("parentFolderId") Long parentFolderId,
      Pageable pageable
  );

  /**
   * Finds a specific test folder by ID and project ID with its test case count.
   *
   * <p>This method returns a single folder with the count of test cases it directly contains.
   * The count does not include test cases from subfolders.
   * </p>
   *
   * @param projectId the ID of the project
   * @param folderId  the ID of the folder to find
   * @return an optional containing the folder with test case count, or empty if not found
   */
  @Query(
      "SELECT new com.epam.ta.reportportal.core.tms.db.entity."
          + "TmsTestFolderWithCountOfTestCases(tf, COUNT(tc))"
          + "FROM TmsTestFolder tf "
          + "LEFT JOIN tf.testCases tc "
          + "WHERE tf.project.id = :projectId and tf.id = :folderId "
          + "GROUP BY tf.id"
  )
  Optional<TmsTestFolderWithCountOfTestCases> findByIdWithCountOfTestCases(
      @Param("projectId") Long projectId, @Param("folderId") Long folderId);

  /**
   * Returns test folder IDs and their corresponding test case counts.
   *
   * <p>This method is optimized for bulk operations where you only need folder IDs and their
   * test case counts without loading the full folder entities.
   * </p>
   *
   * @param projectId project id
   * @param folderIds list of folder IDs to get counts for
   * @return list of objects containing folder IDs and their test case counts
   */
  @Query("SELECT new com.epam.ta.reportportal.core.tms.db.entity."
      + "TmsTestFolderIdWithCountOfTestCases(tf.id, COUNT(tc)) "
      + "FROM TmsTestFolder tf "
      + "LEFT JOIN tf.testCases tc "
      + "WHERE tf.id IN :folderIds "
      + "AND tf.project.id = :projectId "
      + "GROUP BY tf.id")
  List<TmsTestFolderIdWithCountOfTestCases> findTestCaseCountsByFolderIds(
      @Param("projectId") Long projectId, @Param("folderIds") List<Long> folderIds);

  /**
   * Finds a test folder by ID and eagerly fetches its subfolders.
   *
   * <p>This method is useful when you need to work with a specific folder and its immediate
   * subfolders without triggering additional queries.
   * </p>
   *
   * @param projectId project id
   * @param folderId  the ID of the folder to find
   * @return an optional containing the folder with subfolders loaded, or empty if not found
   */
  @Query("SELECT tf FROM TmsTestFolder "
      + "tf LEFT JOIN FETCH tf.subFolders "
      + "WHERE tf.id = :folderId and tf.project.id = :projectId"
  )
  Optional<TmsTestFolder> findByIdWithSubFolders(
      @Param("projectId") Long projectId,
      @Param("folderId") Long folderId
  );

  /**
   * Finds a folder by given ID and project ID.
   *
   * <p>This method ensures that the folder belongs to the specified project, providing an
   * additional security layer to prevent cross-project data access.
   * </p>
   *
   * @param id        ID of the folder to find
   * @param projectId ID of the project the folder should belong to
   * @return an optional containing the test folder, or empty if not found
   */
  Optional<TmsTestFolder> findByIdAndProjectId(long id, long projectId);

  /**
   * Recursively deletes a test folder and all its subfolders.
   *
   * <p>This method uses a recursive Common Table Expression (CTE) to identify all subfolders
   * at any level of nesting and then deletes them. The deletion is performed in a way that respects
   * the folder hierarchy.
   * </p>
   *
   * <p><strong>Warning:</strong> This method should be used after deleting all related entities
   * (test cases, attributes, etc.) to avoid foreign key constraint violations.
   * </p>
   *
   * <p><strong>Note:</strong> This query is PostgreSQL specific and may not work with other
   * database systems.
   * </p>
   *
   * @param projectId the project ID to ensure folder belongs to the correct project
   * @param folderId  the ID of the folder to delete (along with all its subfolders)
   */
  @Modifying
  @Query(value = "DELETE FROM tms_test_folder "
      + "WHERE id IN ("
      + "  WITH RECURSIVE folder_hierarchy AS ("
      + "    SELECT tf.id FROM tms_test_folder tf "
      + "    JOIN project p ON tf.project_id = p.id "
      + "    WHERE tf.id = :folderId AND p.id = :projectId "
      + "    UNION ALL "
      + "    SELECT tf.id FROM tms_test_folder tf "
      + "    JOIN folder_hierarchy fh ON tf.parent_id = fh.id "
      + "  ) "
      + "  SELECT id FROM folder_hierarchy"
      + ") ",
      nativeQuery = true)
  void deleteTestFolderWithSubfoldersById(@Param("projectId") Long projectId,
      @Param("folderId") Long folderId);

  /**
   * Finds all folder IDs in the hierarchy starting from a given folder.
   *
   * <p>This method uses a recursive CTE to traverse the folder hierarchy and return all folder
   * IDs including the root folder and all its nested subfolders at any level.
   * </p>
   *
   * Note: This query is PostgreSQL specific and may not work with other
   * database systems.
   *
   * @param projectId the ID of the project to ensure security boundaries
   * @param folderId  the ID of the root folder to start the hierarchy traversal from
   * @return list of all folder IDs in the hierarchy (including the root folder)
   */
  @Query(value =
      "WITH RECURSIVE ids AS ("
          + "  SELECT id FROM tms_test_folder WHERE id = :folderId AND project_id = :projectId "
          + "  UNION ALL "
          + "  SELECT tf.id FROM tms_test_folder tf JOIN ids ON tf.parent_id = ids.id"
          + ") "
          + "SELECT id FROM ids",
      nativeQuery = true)
  List<Long> findAllFolderIdsInHierarchy(@Param("projectId") Long projectId,
      @Param("folderId") Long folderId);

  /**
   * This method determines whether a test folder with an id exists in a project.
   *
   * @param projectId project's id
   * @param id test folder's id
   * @return true if exists, false if not
   */
  Boolean existsByIdAndProjectId(long id, long projectId);
}
