package com.epam.reportportal.base.infrastructure.persistence.dao.aifactory;

import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineIteration;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PipelineIterationRepository extends JpaRepository<PipelineIteration, Long>,
    JpaSpecificationExecutor<PipelineIteration> {

  long countByPipelineId(Long pipelineId);

  /**
   * Locks the iteration row for the rest of the caller's transaction, serializing concurrent
   * reports into the same iteration.
   */
  @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
  @Query("select it from PipelineIteration it where it.id = :id")
  Optional<PipelineIteration> findByIdForUpdate(@Param("id") Long id);

  Optional<PipelineIteration> findFirstByPipelineIdOrderByIterationNumberDesc(Long pipelineId);

  Optional<PipelineIteration> findByPipelineIdAndIdempotencyKey(Long pipelineId, String idempotencyKey);

  /**
   * Batched form of {@link #countByPipelineId(Long)} — one {@code GROUP BY} query for the whole
   * page instead of one query per pipeline.
   */
  @Query("SELECT it.pipeline.id AS pipelineId, COUNT(it) AS count FROM PipelineIteration it "
      + "WHERE it.pipeline.id IN :pipelineIds GROUP BY it.pipeline.id")
  List<PipelineIterationCount> countByPipelineIdIn(@Param("pipelineIds") Collection<Long> pipelineIds);

  /**
   * Batched form of {@link #findFirstByPipelineIdOrderByIterationNumberDesc(Long)} — the latest
   * iteration per pipeline in one query instead of one per pipeline.
   */
  @Query("SELECT it FROM PipelineIteration it WHERE it.pipeline.id IN :pipelineIds "
      + "AND it.iterationNumber = (SELECT MAX(it2.iterationNumber) FROM PipelineIteration it2 "
      + "WHERE it2.pipeline.id = it.pipeline.id)")
  List<PipelineIteration> findLatestByPipelineIdIn(@Param("pipelineIds") Collection<Long> pipelineIds);

  interface PipelineIterationCount {
    Long getPipelineId();

    Long getCount();
  }
}
