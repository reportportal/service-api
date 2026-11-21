package com.epam.reportportal.base.infrastructure.persistence.dao.aifactory;

import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineStageTrace;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PipelineStageTraceRepository extends JpaRepository<PipelineStageTrace, Long> {

  Optional<PipelineStageTrace> findByIterationIdAndStageKeyAndAttempt(Long iterationId, String stageKey, int attempt);

  List<PipelineStageTrace> findByIterationIdAndStageKeyOrderByAttemptAsc(Long iterationId, String stageKey);
}
