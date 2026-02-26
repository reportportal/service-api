package com.epam.reportportal.base.core.tms.service;

import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsDataset;
import com.epam.reportportal.base.core.tms.dto.TmsEnvironmentDatasetRQ;
import java.util.Collection;

public interface TmsEnvironmentDatasetService {

  void createEnvironmentDataset(TmsDataset tmsDataset,
      Collection<TmsEnvironmentDatasetRQ> environmentDatasetRQs);

  void upsertEnvironmentDataset(TmsDataset tmsDataset,
      Collection<TmsEnvironmentDatasetRQ> environmentDatasetRQs);

  void addEnvironmentDataset(TmsDataset tmsDataset,
      Collection<TmsEnvironmentDatasetRQ> environmentDatasetRQs);

  void deleteByDatasetId(Long datasetId);
}
