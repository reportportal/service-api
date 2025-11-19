package com.epam.reportportal.infrastructure.persistence.dao.tms;

import com.epam.reportportal.infrastructure.persistence.dao.ReportPortalRepository;
import com.epam.reportportal.infrastructure.persistence.entity.tms.TmsDatasetData;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

@Repository
public interface TmsDatasetDataRepository extends
    ReportPortalRepository<TmsDatasetData, Long> {

  @Modifying
  void deleteAllByDataset_Id(Long datasetId);
}
