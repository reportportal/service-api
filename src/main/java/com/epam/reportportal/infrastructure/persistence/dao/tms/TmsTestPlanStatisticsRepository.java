package com.epam.reportportal.infrastructure.persistence.dao.tms;

import com.epam.reportportal.infrastructure.persistence.dao.ReportPortalRepository;
import com.epam.reportportal.infrastructure.persistence.entity.tms.TmsTestPlan;
import com.epam.reportportal.infrastructure.persistence.entity.tms.TmsTestPlanExecutionStatistic;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TmsTestPlanStatisticsRepository extends ReportPortalRepository<TmsTestPlan, Long> {

  @Query(value = """
      SELECT 
        COALESCE(total_cases.total, 0) as total,
        COALESCE(covered_cases.covered, 0) as covered
      FROM tms_test_plan tp
      LEFT JOIN (
        SELECT test_plan_id, COUNT(1) as total
        FROM tms_test_plan_test_case
        WHERE test_plan_id = :testPlanId
        GROUP BY test_plan_id
      ) total_cases ON total_cases.test_plan_id = tp.id
      LEFT JOIN (
        SELECT 
          l.test_plan_id,
          COUNT(DISTINCT last_exec.test_case_id) as covered
        FROM launch l
        JOIN (
          SELECT DISTINCT ON (tce.test_case_id, ti.launch_id)
            tce.test_case_id,
            ti.launch_id,
            tir.status
          FROM tms_test_case_execution tce
          JOIN test_item ti ON ti.item_id = tce.test_item_id
          JOIN test_item_results tir ON tir.result_id = ti.item_id
          WHERE ti.launch_id IN (
            SELECT id FROM launch WHERE test_plan_id = :testPlanId
          )
          ORDER BY tce.test_case_id, ti.launch_id, ti.start_time DESC
        ) last_exec ON last_exec.launch_id = l.id
        WHERE l.test_plan_id = :testPlanId
          AND last_exec.status IN ('PASSED', 'FAILED')
        GROUP BY l.test_plan_id
      ) covered_cases ON covered_cases.test_plan_id = tp.id
      WHERE tp.id = :testPlanId
      """, nativeQuery = true)
  TmsTestPlanExecutionStatistic getExecutionStatisticsByTestPlanId(
      @Param("testPlanId") Long testPlanId
  );
}
