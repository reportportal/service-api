package com.epam.reportportal.base.infrastructure.persistence.dao.aifactory;

import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineStageAttempt;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PipelineStageAttemptRepository extends JpaRepository<PipelineStageAttempt, Long> {

  List<PipelineStageAttempt> findByIterationIdOrderByStageKeyAscAttemptAsc(Long iterationId);
}
