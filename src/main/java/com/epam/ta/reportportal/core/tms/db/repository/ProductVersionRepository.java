package com.epam.ta.reportportal.core.tms.db.repository;

import com.epam.ta.reportportal.core.tms.db.entity.TmsProductVersion;
import com.epam.ta.reportportal.dao.ReportPortalRepository;
import java.util.Optional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductVersionRepository extends ReportPortalRepository<TmsProductVersion, Long> {

  Optional<TmsProductVersion> findByProjectIdAndId(Long projectId, Long id);

  @Modifying
  void deleteByIdAndProjectId(Long id, Long projectId);

}
