package com.epam.reportportal.base.infrastructure.persistence.dao.tms;

import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestCaseAutomationRun;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TmsTestCaseAutomationRunRepository extends JpaRepository<TmsTestCaseAutomationRun, Long> {

  /**
   * All runs of the given test cases, the latest first; a caller keeps the first one it meets per case.
   * <p>
   * Example: cases 42 and 43 with runs 5 (case 42), 9 (case 43) and 12 (case 42) come back as 12, 9, 5.
   */
  @Query("SELECT r FROM TmsTestCaseAutomationRun r WHERE r.testCaseId IN :testCaseIds ORDER BY r.id DESC")
  List<TmsTestCaseAutomationRun> findAllByTestCaseIdsLatestFirst(@Param("testCaseIds") Collection<Long> testCaseIds);

  List<TmsTestCaseAutomationRun> findByIterationId(Long iterationId);
}
