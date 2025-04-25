package com.epam.ta.reportportal.core.tms.db.repository;

import com.epam.ta.reportportal.core.tms.db.entity.TmsTestFolder;
import com.epam.ta.reportportal.core.tms.db.entity.TmsTestFolderWithCountOfSubfolders;
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
 * @author Andrei_Varabyeu
 */
@Repository
public interface TmsTestFolderRepository extends ReportPortalRepository<TmsTestFolder, Long> {

  @Query(
      "SELECT new com.epam.ta.reportportal.core.tms.db.entity.TmsTestFolderWithCountOfSubfolders(tf, "
          + "(SELECT COUNT(sf) FROM TmsTestFolder sf WHERE sf.parentTestFolder.id = tf.id)) "
          + "FROM TmsTestFolder tf WHERE tf.project.id = :projectId"
  )
  Page<TmsTestFolderWithCountOfSubfolders> findAllByProjectIdWithCountOfSubfolders(
      @Param("projectId") long projectId, Pageable pageable
  );

  /**
   * Finds all direct subfolders of a given test folder
   *
   * @param projectId      The id of the project
   * @param parentFolderId The ID of the parent folder
   * @return List of subfolders
   */
  @Query(
      "SELECT new com.epam.ta.reportportal.core.tms.db.entity.TmsTestFolderWithCountOfSubfolders(tf, "
          + "(SELECT COUNT(sf) FROM TmsTestFolder sf WHERE sf.parentTestFolder.id = tf.id)) "
          + "FROM TmsTestFolder tf WHERE tf.project.id = :projectId and tf.parentTestFolder.id = :parentFolderId"
  )
  Page<TmsTestFolderWithCountOfSubfolders> findAllByParentTestFolderIdWithCountOfSubfolders(
      @Param("projectId") long projectId,
      @Param("parentFolderId") Long parentFolderId,
      Pageable pageable
  );

  @Query(
      "SELECT new com.epam.ta.reportportal.core.tms.db.entity.TmsTestFolderWithCountOfSubfolders(tf, "
          + "(SELECT COUNT(sf) FROM TmsTestFolder sf WHERE sf.parentTestFolder.id = tf.id)) "
          + "FROM TmsTestFolder tf WHERE tf.project.id = :projectId and tf.id = :folderId"
  )
  Optional<TmsTestFolderWithCountOfSubfolders> findByIdWithCountOfSubfolders(
      @Param("projectId") Long projectId, @Param("folderId") Long folderId);

  /**
   * Finds a folder by given ID and project ID
   *
   * @param id        ID of folder
   * @param projectId ID of project
   * @return Test Folder
   */
  Optional<TmsTestFolder> findByIdAndProjectId(long id, long projectId);

  /**
   * Counts all subfolders of a given test folder
   *
   * @param parentFolderId The ID of the parent folder
   * @return The count of all subfolders
   */
  @Query("SELECT COUNT(tf) FROM TmsTestFolder tf WHERE tf.parentTestFolder.id = :parentFolderId")
  Long countSubfoldersByParentId(@Param("parentFolderId") Long parentFolderId);

  /**
   * Recursively counts all subfolders (including nested subfolders) of a given test folder. Note:
   * This uses a Common Table Expression (CTE) which is PostgreSQL specific.
   *
   * @param rootFolderId The ID of the root folder
   * @return The count of all subfolders including nested ones
   */
  @Query(value =
      "WITH RECURSIVE folder_tree AS ("
          + "  SELECT id, parent_id FROM tms_test_folder WHERE id = :rootFolderId "
          + "  UNION ALL "
          + "  SELECT f.id, f.parent_id FROM tms_test_folder f "
          + "  JOIN folder_tree ft ON f.parent_id = ft.id "
          + ") "
          + "SELECT COUNT(*) - 1 FROM folder_tree", // Subtract 1 to exclude the root folder itself
      nativeQuery = true)
  Integer countAllNestedSubfolders(@Param("rootFolderId") Long rootFolderId);

  /**
   * Recursively deletes a test folder and all its subfolders. This method uses a recursive Common
   * Table Expression (CTE) to identify all subfolders at any level of nesting and then deletes
   * them. Note: This method should be used after deleting all related entities (test cases,
   * attributes, etc.) to avoid foreign key constraint violations.
   *
   * @param folderId  The ID of the folder to delete
   * @param projectId The project ID to ensure folder belongs to the correct project
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

}
