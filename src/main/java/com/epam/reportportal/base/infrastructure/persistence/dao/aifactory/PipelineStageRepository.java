package com.epam.reportportal.base.infrastructure.persistence.dao.aifactory;

import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineStage;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PipelineStageRepository extends JpaRepository<PipelineStage, Long> {

  List<PipelineStage> findByIterationIdOrderBySequenceAsc(Long iterationId);

  /**
   * Batched form of {@link #findByIterationIdOrderBySequenceAsc(Long)} — one query for the whole
   * page instead of one query per iteration; the caller groups the result by
   * {@code stage.getIteration().getId()}.
   */
  List<PipelineStage> findByIterationIdInOrderByIterationIdAscSequenceAsc(Collection<Long> iterationIds);

  long countByIterationId(Long iterationId);

  void deleteByIterationId(Long iterationId);

  /**
   * Batched form of {@link #countByIterationId(Long)} — one {@code GROUP BY} query for the whole
   * page instead of one query per iteration.
   */
  @Query("SELECT s.iteration.id AS iterationId, COUNT(s) AS count FROM PipelineStage s "
      + "WHERE s.iteration.id IN :iterationIds GROUP BY s.iteration.id")
  List<PipelineStageCount> countByIterationIdIn(@Param("iterationIds") Collection<Long> iterationIds);

  interface PipelineStageCount {
    Long getIterationId();

    Long getCount();
  }
}
