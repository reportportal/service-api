package com.epam.ta.reportportal.core.tms.db.repository;

import com.epam.ta.reportportal.core.tms.db.entity.TmsTestCaseAttribute;
import com.epam.ta.reportportal.core.tms.db.entity.TmsTestCaseAttributeId;
import com.epam.ta.reportportal.dao.ReportPortalRepository;
import java.util.List;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TmsTestCaseAttributeRepository extends
    ReportPortalRepository<TmsTestCaseAttribute, TmsTestCaseAttributeId> {

  @Modifying
  @Query(value = "DELETE FROM TmsTestCaseAttribute tca "
      + "WHERE tca.id.testCaseId = :testCaseId")
  void deleteAllByTestCaseId(@Param("testCaseId") Long testCaseId);

  @Modifying
  @Query(value = "DELETE FROM TmsTestCaseAttribute tca "
      + "WHERE tca.id.testCaseId IN (:testCaseIds)")
  void deleteAllByTestCaseIds(@Param("testCaseIds") List<Long> testCaseIds);

  /**
   * Deletes all test case attributes associated with test cases that belong to the specified folder
   * or any of its subfolders. Uses a recursive Common Table Expression (CTE) to identify all
   * subfolders at any level of nesting and then deletes attributes associated with test cases in
   * those folders.
   *
   * @param folderId  The ID of the folder
   * @param projectId The project ID to ensure folder belongs to the correct project
   */
  @Modifying
  @Query(value = "DELETE FROM tms_test_case_attribute tca "
      + "WHERE tca.test_case_id IN ("
      + "  SELECT tc.id FROM tms_test_case tc "
      + "  WHERE tc.test_folder_id IN ("
      + "    WITH RECURSIVE folder_hierarchy AS ("
      + "      SELECT tf.id FROM tms_test_folder tf "
      + "      JOIN project p ON tf.project_id = p.id "
      + "      WHERE tf.id = :folderId AND p.id = :projectId "
      + "      UNION ALL "
      + "      SELECT tf.id FROM tms_test_folder tf "
      + "      JOIN folder_hierarchy fh ON tf.parent_id = fh.id "
      + "    ) "
      + "    SELECT id FROM folder_hierarchy "
      + "  ) "
      + ") ",
      nativeQuery = true)
  void deleteTestCaseAttributesByTestFolderId(@Param("projectId") Long projectId,
      @Param("folderId") Long folderId);

}
