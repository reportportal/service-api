package com.epam.reportportal.base.core.tms.service;

import static org.apache.commons.collections4.CollectionUtils.isEmpty;

import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsDataset;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsEnvironmentDatasetRepository;
import com.epam.reportportal.base.core.tms.dto.TmsEnvironmentDatasetRQ;
import com.epam.reportportal.base.core.tms.mapper.TmsEnvironmentDatasetMapper;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;
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
    verifyEnvironmentsBelongToProject(tmsDataset, environmentDatasetRQs);
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
    verifyEnvironmentsBelongToProject(tmsDataset, environmentDatasetRQs);
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

  private void verifyEnvironmentsBelongToProject(TmsDataset tmsDataset,
      Collection<TmsEnvironmentDatasetRQ> environmentDatasetRQs) {
    var environmentIds = environmentDatasetRQs.stream()
        .map(TmsEnvironmentDatasetRQ::getEnvironmentId)
        .filter(Objects::nonNull)
        .map(id -> {
          try {
            return Long.valueOf(id);
          } catch (NumberFormatException e) {
            throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR,
                "Invalid environment id: " + id);
          }
        })
        .collect(Collectors.toSet());
    if (environmentIds.isEmpty()) {
      return;
    }
    var projectId = tmsDataset.getProject().getId();
    if (tmsEnvironmentDatasetRepository.countEnvironmentsByIdsAndProjectId(environmentIds,
        projectId) != environmentIds.size()) {
      throw new ReportPortalException(ErrorType.NOT_FOUND,
          "Environments with ids: %s for project: %d".formatted(environmentIds, projectId));
    }
  }
}
