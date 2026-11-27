package com.epam.reportportal.base.infrastructure.persistence.dao.aifactory;

import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineStageTraceEvent;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PipelineStageTraceEventRepository extends JpaRepository<PipelineStageTraceEvent, Long> {

  List<PipelineStageTraceEvent> findByIterationIdAndStageKeyAndAttemptOrderBySeqAsc(Long iterationId, String stageKey,
      int attempt);

  List<PipelineStageTraceEvent> findByIterationIdAndStageKeyAndAttemptAndSeqIn(Long iterationId, String stageKey,
      int attempt, Collection<Integer> seqs);

  /**
   * Events per attempt of a stage, for the attempt switcher: one {@code GROUP BY} instead of a count per attempt.
   */
  @Query("SELECT e.attempt AS attempt, COUNT(e) AS count FROM PipelineStageTraceEvent e "
      + "WHERE e.iterationId = :iterationId AND e.stageKey = :stageKey GROUP BY e.attempt")
  List<AttemptEventCount> countByAttempt(@Param("iterationId") Long iterationId, @Param("stageKey") String stageKey);

  interface AttemptEventCount {
    Integer getAttempt();

    Long getCount();
  }
}
