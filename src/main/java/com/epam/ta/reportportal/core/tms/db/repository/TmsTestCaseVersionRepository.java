package com.epam.ta.reportportal.core.tms.db.repository;

import com.epam.ta.reportportal.core.tms.db.entity.TmsTestCaseDefaultVersionTestCaseId;
import com.epam.ta.reportportal.core.tms.db.entity.TmsTestCaseVersion;
import com.epam.ta.reportportal.dao.ReportPortalRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TmsTestCaseVersionRepository extends ReportPortalRepository<TmsTestCaseVersion, Long> {

  @Query("SELECT tcv FROM TmsTestCaseVersion tcv "
      + "JOIN FETCH tcv.manualScenario ms "
      + "LEFT JOIN FETCH ms.textScenario ts "
      + "LEFT JOIN FETCH ms.stepsScenario ss "
      + "LEFT JOIN FETCH ms.attributes t "
      + "WHERE tcv.testCase.id = :testCaseId "
      + "AND tcv.isDefault = true"
  )
  Optional<TmsTestCaseVersion> findDefaultVersionByTestCaseId(@Param("testCaseId") Long testCaseId);

  @Query("SELECT new com.epam.ta.reportportal.core.tms.db.entity."
      + "TmsTestCaseDefaultVersionTestCaseId(tcv, tcv.testCase.id) "
      + "FROM TmsTestCaseVersion tcv "
      + "JOIN FETCH tcv.manualScenario ms "
      + "LEFT JOIN FETCH ms.textScenario ts "
      + "LEFT JOIN FETCH ms.stepsScenario ss "
      + "LEFT JOIN FETCH ms.attributes t "
      + "WHERE tcv.testCase.id IN (:testCaseIds) "
      + "AND tcv.isDefault = true"
  )
  List<TmsTestCaseDefaultVersionTestCaseId> findDefaultVersionsByTestCaseIds(
      @Param("testCaseIds") List<Long> testCaseIds
  );

  @Modifying
  @Query("DELETE FROM TmsTestCaseVersion tcv WHERE tcv.testCase.id = :testCaseId")
  void deleteAllByTestCaseId(@Param("testCaseId") Long testCaseId);

  @Modifying
  @Query("DELETE FROM TmsTestCaseVersion tcv WHERE tcv.testCase.id IN (:testCaseIds)")
  void deleteAllByTestCaseIds(@Param("testCaseIds") List<Long> testCaseIds);

  @Modifying
  @Query(value = "DELETE FROM tms_test_case_version tcv "
      + "WHERE tcv.test_case_id IN ("
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
  void deleteTestCaseVersionsByTestFolderId(@Param("projectId") Long projectId,
      @Param("folderId") Long folderId);
}
