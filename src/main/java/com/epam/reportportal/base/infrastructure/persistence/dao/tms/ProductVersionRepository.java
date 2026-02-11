package com.epam.reportportal.base.infrastructure.persistence.dao.tms;

import com.epam.reportportal.base.infrastructure.persistence.dao.ReportPortalRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsProductVersion;
import java.util.Optional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductVersionRepository extends ReportPortalRepository<TmsProductVersion, Long> {

  Optional<TmsProductVersion> findByProjectIdAndId(Long projectId, Long id);

  @Modifying
  void deleteByIdAndProjectId(Long id, Long projectId);

}
