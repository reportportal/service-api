package com.epam.reportportal.infrastructure.persistence.dao.tms;

import com.epam.reportportal.infrastructure.persistence.dao.ReportPortalRepository;
import com.epam.reportportal.infrastructure.persistence.entity.tms.TmsTestPlanTestCase;
import com.epam.reportportal.infrastructure.persistence.entity.tms.TmsTestPlanTestCaseId;
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

  @Modifying
  @Query("DELETE FROM TmsTestPlanTestCase tptc "
      + "WHERE tptc.id.testPlanId = :testPlanId "
      + "AND tptc.id.testCaseId IN :testCaseIds")
  void deleteByTestPlanIdAndTestCaseIds(@Param("testPlanId") Long testPlanId,
      @Param("testCaseIds") List<Long> testCaseIds);

  @Modifying
  @Query("DELETE FROM TmsTestPlanTestCase tptc "
      + "WHERE tptc.id.testCaseId IN :testCaseIds"
  )
  void deleteAllByTestCaseIds(@Param("testCaseIds") List<Long> testCaseIds);

  @Modifying
  @Query(value = """
      INSERT INTO tms_test_plan_test_case (test_plan_id, test_case_id)
      SELECT :testPlanId, t.test_case_id
      FROM (VALUES(:#{#testCaseIds})) AS t(test_case_id)
      """, nativeQuery = true)
  void batchInsertTestPlanTestCases(@Param("testPlanId") Long testPlanId,
      @Param("testCaseIds") List<Long> testCaseIds);

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
   * within the project.
   *
   * @param projectId the ID of the project
   * @param folderId  the ID of the test folder
   */
  @Modifying
  @Query("DELETE FROM TmsTestPlanTestCase tptc "
      + "WHERE tptc.id.testCaseId IN ("
      + "SELECT tc.id FROM TmsTestCase tc "
      + "WHERE tc.testFolder.id = :folderId "
      + "AND tc.testFolder.project.id = :projectId"
      + ")")
  void deleteAllByTestFolderId(@Param("projectId") Long projectId,
      @Param("folderId") Long folderId);

  @Modifying
  @Query(value = "INSERT INTO tms_test_plan_test_case (test_plan_id, test_case_id) " +
      "VALUES (:testPlanId, :testCaseId) " +
      "ON CONFLICT (test_plan_id, test_case_id) DO NOTHING",
      nativeQuery = true)
  int insertTestPlanTestCaseIgnoreConflict(@Param("testPlanId") Long testPlanId,
      @Param("testCaseId") Long testCaseId);

  @Modifying
  @Query("DELETE FROM TmsTestPlanTestCase tptc " +
      "WHERE tptc.id.testPlanId = :testPlanId AND tptc.id.testCaseId = :testCaseId")
  int deleteByTestPlanIdAndTestCaseId(@Param("testPlanId") Long testPlanId,
      @Param("testCaseId") Long testCaseId);
}
