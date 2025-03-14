package com.epam.ta.reportportal.core.tms.service;

import com.epam.ta.reportportal.core.tms.db.entity.TmsDataset;
import com.epam.ta.reportportal.core.tms.dto.TmsDatasetDataRQ;
import java.util.Collection;

public interface TmsDatasetDataService {

  void createDatasetData(TmsDataset tmsDataset, Collection<TmsDatasetDataRQ> tmsDatasetDataRQs);

  void upsertDatasetData(TmsDataset tmsDataset, Collection<TmsDatasetDataRQ> tmsDatasetDataRQs);

  void addDatasetData(TmsDataset tmsDataset, Collection<TmsDatasetDataRQ> tmsDatasetDataRQs);

  void deleteByDatasetId(Long datasetId);
}
