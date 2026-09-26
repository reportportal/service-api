package com.epam.reportportal.base.infrastructure.persistence.dao.aifactory;

import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineStage;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PipelineStageRepository extends JpaRepository<PipelineStage, Long> {

  List<PipelineStage> findByIterationIdOrderBySequenceAsc(Long iterationId);

  long countByIterationId(Long iterationId);

  void deleteByIterationId(Long iterationId);
}
