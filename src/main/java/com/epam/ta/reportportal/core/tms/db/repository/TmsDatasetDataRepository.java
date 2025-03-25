package com.epam.ta.reportportal.core.tms.db.repository;

import com.epam.ta.reportportal.core.tms.db.entity.TmsDatasetData;
import com.epam.ta.reportportal.dao.ReportPortalRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

@Repository
public interface TmsDatasetDataRepository extends
    ReportPortalRepository<TmsDatasetData, Long> {

  @Modifying
  void deleteAllByDataset_Id(Long datasetId);
}
