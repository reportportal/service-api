package com.epam.reportportal.base.infrastructure.persistence.dao.tms;

import com.epam.reportportal.base.infrastructure.persistence.entity.tms.PipelineStageTestCase;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestCase;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.enums.TmsTestCaseStatus;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PipelineStageTestCaseRepository extends JpaRepository<PipelineStageTestCase, Long> {

  List<PipelineStageTestCase> findByStage_Iteration_Id(Long iterationId);

  void deleteByStage_Id(Long stageId);

  /**
   * The test cases linked to the given stage, e.g. the ones an Upload stage created.
   */
  @Query("SELECT l.testCase FROM PipelineStageTestCase l WHERE l.stage.id = :stageId")
  List<TmsTestCase> findTestCasesByStageId(@Param("stageId") Long stageId);

  /**
   * Ids of the iterations that have any of the given test cases linked to one of their stages.
   */
  @Query("SELECT DISTINCT l.stage.iteration.id FROM PipelineStageTestCase l WHERE l.testCase.id IN :testCaseIds")
  List<Long> findIterationIdsByTestCaseIdIn(@Param("testCaseIds") Collection<Long> testCaseIds);

  /**
   * Batched per-iteration test case counts: distinct linked test cases and how many of them are
   * currently {@code READY}. Iterations without links are absent from the result.
   */
  @Query("SELECT l.stage.iteration.id AS iterationId, COUNT(DISTINCT l.testCase.id) AS testCasesCount, "
      + "COUNT(DISTINCT CASE WHEN l.testCase.status = :readyStatus THEN l.testCase.id END) AS readyCount "
      + "FROM PipelineStageTestCase l WHERE l.stage.iteration.id IN :iterationIds "
      + "GROUP BY l.stage.iteration.id")
  List<IterationTestCaseCounts> countTestCasesByIterationIdIn(
      @Param("iterationIds") Collection<Long> iterationIds,
      @Param("readyStatus") TmsTestCaseStatus readyStatus);

  interface IterationTestCaseCounts {
    Long getIterationId();

    Long getTestCasesCount();

    Long getReadyCount();
  }
}
