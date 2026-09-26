package com.epam.reportportal.base.infrastructure.persistence.dao.aifactory;

import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineIteration;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PipelineIterationRepository extends JpaRepository<PipelineIteration, Long> {

  Page<PipelineIteration> findByPipelineId(Long pipelineId, Pageable pageable);

  long countByPipelineId(Long pipelineId);

  Optional<PipelineIteration> findFirstByPipelineIdOrderByIterationNumberDesc(Long pipelineId);
}
