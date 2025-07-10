package com.epam.ta.reportportal.core.tms.service;

import static org.apache.commons.collections4.CollectionUtils.isEmpty;

import com.epam.ta.reportportal.core.tms.db.entity.TmsDataset;
import com.epam.ta.reportportal.core.tms.db.repository.TmsDatasetDataRepository;
import com.epam.ta.reportportal.core.tms.dto.TmsDatasetDataRQ;
import com.epam.ta.reportportal.core.tms.mapper.TmsDatasetDataMapper;
import java.util.Collection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TmsDatasetDataServiceImpl implements TmsDatasetDataService {

  private final TmsDatasetDataMapper tmsDatasetDataMapper;
  private final TmsDatasetDataRepository tmsDatasetDataRepository;

  @Override
  @Transactional
  public void createDatasetData(TmsDataset tmsDataset,
      Collection<TmsDatasetDataRQ> tmsDatasetDataRQs) {
    if (isEmpty(tmsDatasetDataRQs)) {
      return;
    }
    var tmsDatasetData = tmsDatasetDataMapper.convertToTmsDatasetData(
        tmsDatasetDataRQs);
    tmsDataset.setData(tmsDatasetData);
    tmsDatasetData.forEach(
        tmsTestPlanAttribute -> tmsTestPlanAttribute.setDataset(tmsDataset));
    tmsDatasetDataRepository.saveAll(tmsDatasetData);
  }

  @Override
  @Transactional
  public void upsertDatasetData(TmsDataset tmsDataset,
      Collection<TmsDatasetDataRQ> tmsDatasetDataRQs) {
    tmsDatasetDataRepository.deleteAllByDataset_Id(tmsDataset.getId());
    createDatasetData(tmsDataset, tmsDatasetDataRQs);
  }

  @Override
  @Transactional
  public void addDatasetData(TmsDataset tmsDataset,
      Collection<TmsDatasetDataRQ> tmsDatasetDataRQs) {
    if (isEmpty(tmsDatasetDataRQs)) {
      return;
    }
    var tmsDatasetData = tmsDatasetDataMapper.convertToTmsDatasetData(
        tmsDatasetDataRQs);
    tmsDataset.getData().addAll(tmsDatasetData);
    tmsDatasetData.forEach(
        tmsTestPlanAttribute -> tmsTestPlanAttribute.setDataset(tmsDataset));
    tmsDatasetDataRepository.saveAll(tmsDatasetData);
  }

  @Override
  @Transactional
  public void deleteByDatasetId(Long datasetId) {
    tmsDatasetDataRepository.deleteAllByDataset_Id(datasetId);
  }
}
