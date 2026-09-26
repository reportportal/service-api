package com.epam.reportportal.base.infrastructure.persistence.dao.aifactory;

import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineStage;
import jakarta.persistence.LockModeType;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PipelineStageRepository extends JpaRepository<PipelineStage, Long> {

  List<PipelineStage> findByIterationIdOrderBySequenceAsc(Long iterationId);

  Optional<PipelineStage> findByIterationIdAndStageKey(Long iterationId, String stageKey);

  /**
   * Locks the stage row for the rest of the caller's transaction, so concurrent retries of the
   * same stage are serialized and the second one sees the first one's PENDING status.
   */
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select s from PipelineStage s where s.id = :id")
  Optional<PipelineStage> findByIdForUpdate(@Param("id") Long id);

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
