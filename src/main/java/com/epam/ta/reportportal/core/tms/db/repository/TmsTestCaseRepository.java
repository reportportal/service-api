package com.epam.ta.reportportal.core.tms.db.repository;

import com.epam.ta.reportportal.core.tms.db.entity.TmsTestCase;
import com.epam.ta.reportportal.dao.ReportPortalRepository;
import java.util.List;
import java.util.Optional;
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
}
