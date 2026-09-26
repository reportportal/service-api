package com.epam.reportportal.base.infrastructure.persistence.dao.aifactory;

import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.Pipeline;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PipelineRepository extends JpaRepository<Pipeline, Long> {

  Page<Pipeline> findByProjectId(Long projectId, Pageable pageable);

  Optional<Pipeline> findByProjectIdAndName(Long projectId, String name);
}
