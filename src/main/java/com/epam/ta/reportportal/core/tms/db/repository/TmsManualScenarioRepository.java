package com.epam.ta.reportportal.core.tms.db.repository;

import com.epam.ta.reportportal.core.tms.db.entity.TmsManualScenario;
import com.epam.ta.reportportal.dao.ReportPortalRepository;
import java.util.List;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TmsManualScenarioRepository extends ReportPortalRepository<TmsManualScenario, Long> {

  @Modifying
  @Query("DELETE FROM TmsManualScenario ms WHERE ms.testCaseVersion.id IN ("
      + "SELECT tcv.id FROM TmsTestCaseVersion tcv WHERE tcv.testCase.id = :testCaseId)")
  void deleteAllByTestCaseId(@Param("testCaseId") Long testCaseId);

  @Modifying
  @Query("DELETE FROM TmsManualScenario ms WHERE ms.testCaseVersion.id IN ("
      + "SELECT tcv.id FROM TmsTestCaseVersion tcv WHERE tcv.testCase.id IN (:testCaseIds))")
  void deleteAllByTestCaseIds(@Param("testCaseIds") List<Long> testCaseIds);

  @Modifying
  @Query(value = "DELETE FROM tms_manual_scenario ms "
      + "WHERE ms.test_case_version_id IN ("
      + "  SELECT tcv.id FROM tms_test_case_version tcv "
      + "  WHERE tcv.test_case_id IN ("
      + "    SELECT tc.id FROM tms_test_case tc "
      + "    WHERE tc.test_folder_id IN ("
      + "      WITH RECURSIVE folder_hierarchy AS ("
      + "        SELECT tf.id FROM tms_test_folder tf "
      + "        JOIN project p ON tf.project_id = p.id "
      + "        WHERE tf.id = :folderId AND p.id = :projectId "
      + "        UNION ALL "
      + "        SELECT tf.id FROM tms_test_folder tf "
      + "        JOIN folder_hierarchy fh ON tf.parent_id = fh.id "
      + "      ) "
      + "      SELECT id FROM folder_hierarchy "
      + "    ) "
      + "  ) "
      + ") ",
      nativeQuery = true)
  void deleteAllByTestFolderId(@Param("projectId") Long projectId,
      @Param("folderId") Long folderId);
}
