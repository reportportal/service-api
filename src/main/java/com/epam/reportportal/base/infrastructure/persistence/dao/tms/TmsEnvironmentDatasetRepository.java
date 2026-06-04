package com.epam.reportportal.base.infrastructure.persistence.dao.tms;

import com.epam.reportportal.base.infrastructure.persistence.dao.ReportPortalRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsEnvironmentDataset;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsEnvironmentDatasetId;
import org.springframework.data.jpa.repository.Modifying;

public interface TmsEnvironmentDatasetRepository extends
    ReportPortalRepository<TmsEnvironmentDataset, TmsEnvironmentDatasetId> {

  @Modifying
  void deleteAllByDataset_Id(Long datasetId);
}
