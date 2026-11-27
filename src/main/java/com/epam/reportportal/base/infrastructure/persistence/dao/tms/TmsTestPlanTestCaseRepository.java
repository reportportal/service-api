package com.epam.reportportal.base.infrastructure.persistence.dao.tms;

import com.epam.reportportal.base.infrastructure.persistence.dao.ReportPortalRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestPlanTestCase;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestPlanTestCaseId;
import java.util.List;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TmsTestPlanTestCaseRepository extends
    ReportPortalRepository<TmsTestPlanTestCase, TmsTestPlanTestCaseId> {

  @Query("SELECT tptc.id.testCaseId "
      + "FROM TmsTestPlanTestCase tptc "
      + "WHERE tptc.id.testPlanId = :testPlanId")
  List<Long> findTestCaseIdsByTestPlanId(@Param("testPlanId") Long testPlanId);

  /**
   * Deletes the test cases from the test plan in one statement.
   *
   * @param testPlanId  the test plan ID
   * @param testCaseIds the test case IDs
   * @return IDs of the test cases that were actually deleted from the plan
   */
  @Query(value = """
      DELETE FROM tms_test_plan_test_case
      WHERE test_plan_id = :testPlanId AND test_case_id IN (:testCaseIds)
      RETURNING test_case_id
      """, nativeQuery = true)
  List<Long> deleteByTestPlanIdAndTestCaseIds(@Param("testPlanId") Long testPlanId,
      @Param("testCaseIds") List<Long> testCaseIds);

  @Modifying
  @Query("DELETE FROM TmsTestPlanTestCase tptc "
      + "WHERE tptc.id.testCaseId IN :testCaseIds"
  )
  void deleteAllByTestCaseIds(@Param("testCaseIds") List<Long> testCaseIds);

  /**
   * Adds the test cases of the project to the test plan in one statement. Test cases of other
   * projects and test cases already in the plan are skipped. The caller must verify that the test
   * plan belongs to the project.
   *
   * @param projectId   the project ID
   * @param testPlanId  the test plan ID
   * @param testCaseIds the test case IDs
   * @return IDs of the test cases that were actually inserted
   */
  @Query(value = """
      INSERT INTO tms_test_plan_test_case (test_plan_id, test_case_id)
      SELECT :testPlanId, tc.id
      FROM tms_test_case tc
      WHERE tc.id IN (:testCaseIds) AND tc.project_id = :projectId
      ON CONFLICT (test_plan_id, test_case_id) DO NOTHING
      RETURNING test_case_id
      """, nativeQuery = true)
  List<Long> insertTestPlanTestCasesInProject(@Param("projectId") Long projectId,
      @Param("testPlanId") Long testPlanId, @Param("testCaseIds") List<Long> testCaseIds);

  /**
   * Deletes all test plan-test case associations for the specified test case.
   *
   * @param testCaseId the ID of the test case
   */
  @Modifying
  @Query("DELETE FROM TmsTestPlanTestCase tptc "
      + "WHERE tptc.id.testCaseId = :testCaseId")
  void deleteAllByTestCaseId(@Param("testCaseId") Long testCaseId);

  /**
   * Deletes all test plan-test case associations for test cases that belong to the specified folder
   * or any of its subfolders within the project.
   *
   * @param projectId the ID of the project
   * @param folderId  the ID of the test folder
   */
  @Modifying
  @Query(value = "DELETE FROM tms_test_plan_test_case tptc "
      + "WHERE tptc.test_case_id IN ("
      + "  SELECT tc.id FROM tms_test_case tc "
      + "  WHERE tc.project_id = :projectId "
      + "  AND tc.test_folder_id IN ("
      + "    WITH RECURSIVE folder_hierarchy AS ("
      + "      SELECT tf.id FROM tms_test_folder tf "
      + "      JOIN project p ON tf.project_id = p.id "
      + "      WHERE tf.id = :folderId AND p.id = :projectId "
      + "      UNION ALL "
      + "      SELECT tf.id FROM tms_test_folder tf "
      + "      JOIN folder_hierarchy fh ON tf.parent_id = fh.id "
      + "      WHERE tf.project_id = :projectId "
      + "    ) "
      + "    SELECT id FROM folder_hierarchy "
      + "  ) "
      + ") ",
      nativeQuery = true)
  void deleteAllByTestFolderId(@Param("projectId") Long projectId,
      @Param("folderId") Long folderId);

  @Modifying
  @Query(value = "INSERT INTO tms_test_plan_test_case (test_plan_id, test_case_id) " +
      "VALUES (:testPlanId, :testCaseId) " +
      "ON CONFLICT (test_plan_id, test_case_id) DO NOTHING",
      nativeQuery = true)
  int insertTestPlanTestCaseIgnoreConflict(@Param("testPlanId") Long testPlanId,
      @Param("testCaseId") Long testCaseId);

  /**
   * Inserts a test plan - test case association only if the test plan and the test case both exist
   * and belong to the project. Never raises a foreign key or unique violation.
   *
   * @param projectId  the project ID
   * @param testPlanId the test plan ID
   * @param testCaseId the test case ID
   * @return number of inserted rows (0 or 1)
   */
  @Modifying
  @Query(value = """
      INSERT INTO tms_test_plan_test_case (test_plan_id, test_case_id)
      SELECT tp.id, tc.id
      FROM tms_test_plan tp
      JOIN tms_test_case tc ON tc.project_id = tp.project_id
      WHERE tp.id = :testPlanId AND tc.id = :testCaseId AND tp.project_id = :projectId
      ON CONFLICT (test_plan_id, test_case_id) DO NOTHING
      """, nativeQuery = true)
  int insertTestPlanTestCaseInProject(@Param("projectId") Long projectId,
      @Param("testPlanId") Long testPlanId, @Param("testCaseId") Long testCaseId);

  @Modifying
  @Query("DELETE FROM TmsTestPlanTestCase tptc " +
      "WHERE tptc.id.testPlanId = :testPlanId AND tptc.id.testCaseId = :testCaseId")
  int deleteByTestPlanIdAndTestCaseId(@Param("testPlanId") Long testPlanId,
      @Param("testCaseId") Long testCaseId);
}