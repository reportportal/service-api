package com.epam.reportportal.base.infrastructure.persistence.dao.tms;

import com.epam.reportportal.base.infrastructure.persistence.dao.ReportPortalRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestPlan;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.projection.TmsTestPlanStatisticByTestPlan;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TmsTestPlanStatisticsRepository extends ReportPortalRepository<TmsTestPlan, Long> {

  /**
   * Calculates execution statistics for several test plans in one query.
   * <p>
   * total - number of test cases in the plan; covered - number of test cases of the plan whose
   * latest execution in at least one launch of the plan is PASSED or FAILED. Test cases that were
   * executed in a launch of the plan but are not (or no longer) members of the plan are not
   * counted, so covered never exceeds total.
   *
   * @param testPlanIds test plan IDs
   * @return one row per existing test plan
   */
  @Query(value = """
      SELECT
        tp.id AS "testPlanId",
        COALESCE(total_cases.total, 0) AS "total",
        COALESCE(covered_cases.covered, 0) AS "covered"
      FROM tms_test_plan tp
      LEFT JOIN (
        SELECT test_plan_id, COUNT(1) AS total
        FROM tms_test_plan_test_case
        WHERE test_plan_id IN (:testPlanIds)
        GROUP BY test_plan_id
      ) total_cases ON total_cases.test_plan_id = tp.id
      LEFT JOIN (
        SELECT
          last_exec.test_plan_id,
          COUNT(DISTINCT last_exec.test_case_id) AS covered
        FROM (
          SELECT DISTINCT ON (tce.test_case_id, ti.launch_id)
            l.test_plan_id,
            tce.test_case_id,
            tir.status
          FROM launch l
          JOIN test_item ti ON ti.launch_id = l.id
          JOIN tms_test_case_execution tce ON tce.test_item_id = ti.item_id
          JOIN test_item_results tir ON tir.result_id = ti.item_id
          WHERE l.test_plan_id IN (:testPlanIds)
          ORDER BY tce.test_case_id, ti.launch_id, ti.start_time DESC
        ) last_exec
        JOIN tms_test_plan_test_case tptc
          ON tptc.test_plan_id = last_exec.test_plan_id
          AND tptc.test_case_id = last_exec.test_case_id
        WHERE last_exec.status IN ('PASSED', 'FAILED')
        GROUP BY last_exec.test_plan_id
      ) covered_cases ON covered_cases.test_plan_id = tp.id
      WHERE tp.id IN (:testPlanIds)
      """, nativeQuery = true)
  List<TmsTestPlanStatisticByTestPlan> getExecutionStatisticsByTestPlanIds(
      @Param("testPlanIds") Collection<Long> testPlanIds
  );
}
