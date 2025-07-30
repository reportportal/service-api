package com.epam.ta.reportportal.core.tms.service;

import static com.epam.reportportal.rules.exception.ErrorType.NOT_FOUND;

import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.core.tms.db.repository.TmsDatasetRepository;
import com.epam.ta.reportportal.core.tms.dto.TmsDatasetRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsDatasetRS;
import com.epam.ta.reportportal.core.tms.mapper.TmsDatasetMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class TmsDatasetServiceImpl implements TmsDatasetService {

  private static final String TMS_DATASET_NOT_FOUND_BY_ID = "TMS dataset with id: %d for project: %d";
  private static final String TMS_DATASET_NOT_FOUND_BY_PROJECT_ID = "TMS datasets for project: %d";

  private final TmsDatasetRepository tmsDatasetRepository;
  private final TmsDatasetMapper tmsDatasetMapper;
  private final TmsDatasetDataService tmsDatasetDataService;
  private final TmsEnvironmentDatasetService tmsEnvironmentDatasetService;

  @Override
  @Transactional
  public TmsDatasetRS create(long projectId, TmsDatasetRQ tmsDatasetRQ) {
    var tmsDataset = tmsDatasetMapper.convertFromRQ(projectId, tmsDatasetRQ);

    tmsDatasetRepository.save(tmsDataset);

    tmsDatasetDataService.createDatasetData(tmsDataset,
        tmsDatasetRQ.getAttributes());
    tmsEnvironmentDatasetService.createEnvironmentDataset(
        tmsDataset, tmsDatasetRQ.getEnvironmentAttachments()
    );

    return tmsDatasetMapper.convertToRS(tmsDataset);
  }

  @Override
  @Transactional
  public TmsDatasetRS update(long projectId, Long datasetId, TmsDatasetRQ tmsDatasetRQ) {
    return tmsDatasetRepository.findByIdAndProjectId(datasetId, projectId)
        .map((var existingDataset) -> {
          tmsDatasetMapper.update(existingDataset,
              tmsDatasetMapper.convertFromRQ(projectId, tmsDatasetRQ)
          );

          tmsDatasetDataService.upsertDatasetData(existingDataset,
              tmsDatasetRQ.getAttributes());
          tmsEnvironmentDatasetService.upsertEnvironmentDataset(
              existingDataset, tmsDatasetRQ.getEnvironmentAttachments()
          );
          return tmsDatasetMapper.convertToRS(existingDataset);
        })
        .orElseGet(() -> create(projectId, tmsDatasetRQ));
  }

  @Override
  @Transactional
  public TmsDatasetRS patch(long projectId, Long datasetId, TmsDatasetRQ tmsDatasetRQ) {
    return tmsDatasetRepository.findByIdAndProjectId(datasetId, projectId)
        .map((var existingDataset) -> {
          tmsDatasetMapper.patch(existingDataset,
              tmsDatasetMapper.convertFromRQ(projectId, tmsDatasetRQ)
          );

          tmsDatasetDataService.addDatasetData(existingDataset,
              tmsDatasetRQ.getAttributes());
          tmsEnvironmentDatasetService.addEnvironmentDataset(
              existingDataset, tmsDatasetRQ.getEnvironmentAttachments()
          );
          return tmsDatasetMapper.convertToRS(existingDataset);
        })
        .orElseThrow(() -> new ReportPortalException(
            NOT_FOUND, TMS_DATASET_NOT_FOUND_BY_ID.formatted(datasetId, projectId))
        );
  }

  @Override
  @Transactional
  public void delete(long projectId, Long datasetId) {
    tmsDatasetDataService.deleteByDatasetId(datasetId);
    tmsEnvironmentDatasetService.deleteByDatasetId(datasetId);
    tmsDatasetRepository.deleteByIdAndProject_Id(datasetId, projectId);
  }

  @Override
  @Transactional(readOnly = true)
  public TmsDatasetRS getById(long projectId, Long datasetId) {
    return tmsDatasetRepository.findByIdAndProjectId(datasetId, projectId)
        .map(tmsDatasetMapper::convertToRS)
        .orElseThrow(() -> new ReportPortalException(
            NOT_FOUND, TMS_DATASET_NOT_FOUND_BY_ID.formatted(datasetId, projectId))
        );
  }

  @Override
  @Transactional(readOnly = true)
  public List<TmsDatasetRS> getByProjectId(Long projectId) {
    var datasets = tmsDatasetRepository.findAllByProject_Id(projectId);
    if (CollectionUtils.isNotEmpty(datasets)) {
      return tmsDatasetMapper.convertToRS(datasets);
    } else {
      throw new ReportPortalException(
          NOT_FOUND, TMS_DATASET_NOT_FOUND_BY_PROJECT_ID.formatted(projectId)
      );
    }
  }

  @Override
  @Transactional
  public List<TmsDatasetRS> uploadFromFile(Long projectId, MultipartFile file) {
    return tmsDatasetMapper
        .convertToRQ(file)
        .stream()
        .map(tmsDatasetRQ -> create(projectId, tmsDatasetRQ))
        .toList();
  }
}
