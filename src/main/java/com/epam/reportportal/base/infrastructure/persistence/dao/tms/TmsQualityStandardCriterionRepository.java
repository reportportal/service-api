package com.epam.reportportal.base.infrastructure.persistence.dao.tms;

import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsQualityStandardCriterion;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TmsQualityStandardCriterionRepository extends JpaRepository<TmsQualityStandardCriterion, Long> {

  List<TmsQualityStandardCriterion> findByStandardIdOrderBySequenceAsc(Long standardId);

  Optional<TmsQualityStandardCriterion> findByIdAndStandard_ProjectId(Long id, Long projectId);
}
