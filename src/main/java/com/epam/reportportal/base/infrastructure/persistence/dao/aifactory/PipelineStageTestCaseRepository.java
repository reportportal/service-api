package com.epam.reportportal.base.infrastructure.persistence.dao.aifactory;

import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineStageTestCase;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PipelineStageTestCaseRepository extends JpaRepository<PipelineStageTestCase, Long> {

  List<PipelineStageTestCase> findByStage_Iteration_Id(Long iterationId);
}
