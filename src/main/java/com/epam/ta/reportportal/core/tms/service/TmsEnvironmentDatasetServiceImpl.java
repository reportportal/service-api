package com.epam.ta.reportportal.core.tms.service;

import static org.apache.commons.collections4.CollectionUtils.isEmpty;

import com.epam.ta.reportportal.core.tms.db.entity.TmsDataset;
import com.epam.ta.reportportal.core.tms.db.repository.TmsEnvironmentDatasetRepository;
import com.epam.ta.reportportal.core.tms.dto.TmsEnvironmentDatasetRQ;
import com.epam.ta.reportportal.core.tms.mapper.TmsEnvironmentDatasetMapper;
import java.util.Collection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TmsEnvironmentDatasetServiceImpl implements TmsEnvironmentDatasetService {

  private final TmsEnvironmentDatasetRepository tmsEnvironmentDatasetRepository;

  private final TmsEnvironmentDatasetMapper tmsEnvironmentDatasetMapper;

  @Override
  @Transactional
  public void createEnvironmentDataset(TmsDataset tmsDataset,
      Collection<TmsEnvironmentDatasetRQ> environmentDatasetRQs) {
    if (isEmpty(environmentDatasetRQs)) {
      return;
    }
    var tmsEnvironmentDatasets = tmsEnvironmentDatasetMapper.convertToEnvironmentDatasets(
        tmsDataset, environmentDatasetRQs
    );
    tmsDataset.setEnvironmentDatasets(tmsEnvironmentDatasets);
    tmsEnvironmentDatasetRepository.saveAll(tmsEnvironmentDatasets);
  }

  @Override
  @Transactional
  public void upsertEnvironmentDataset(TmsDataset tmsDataset,
      Collection<TmsEnvironmentDatasetRQ> environmentDatasetRQs) {
    tmsEnvironmentDatasetRepository.deleteAllByDataset_Id(tmsDataset.getId());
    createEnvironmentDataset(tmsDataset, environmentDatasetRQs);
  }

  @Override
  @Transactional
  public void addEnvironmentDataset(TmsDataset tmsDataset,
      Collection<TmsEnvironmentDatasetRQ> environmentDatasetRQs) {
    if (isEmpty(environmentDatasetRQs)) {
      return;
    }
    var tmsEnvironmentDatasets = tmsEnvironmentDatasetMapper.convertToEnvironmentDatasets(
        tmsDataset, environmentDatasetRQs
    );
    tmsDataset
        .getEnvironmentDatasets()
        .addAll(tmsEnvironmentDatasets);
    tmsEnvironmentDatasetRepository.saveAll(tmsEnvironmentDatasets);
  }

  @Override
  @Transactional
  public void deleteByDatasetId(Long datasetId) {
    tmsEnvironmentDatasetRepository.deleteAllByDataset_Id(datasetId);
  }
}
