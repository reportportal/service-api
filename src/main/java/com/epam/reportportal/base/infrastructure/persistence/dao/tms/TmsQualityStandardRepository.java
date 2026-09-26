package com.epam.reportportal.base.infrastructure.persistence.dao.tms;

import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsQualityStandard;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TmsQualityStandardRepository extends JpaRepository<TmsQualityStandard, Long> {

  Optional<TmsQualityStandard> findByProjectId(Long projectId);

  boolean existsByProjectId(Long projectId);

  void deleteByProjectId(Long projectId);
}
