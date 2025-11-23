package com.epam.reportportal.core.tms.service;

import static com.epam.reportportal.infrastructure.rules.exception.ErrorType.NOT_FOUND;

import com.epam.reportportal.core.tms.dto.TmsManualScenarioRQ;
import com.epam.reportportal.core.tms.mapper.TmsTestCaseVersionMapper;
import com.epam.reportportal.infrastructure.persistence.dao.tms.TmsTestCaseVersionRepository;
import com.epam.reportportal.infrastructure.persistence.entity.tms.TmsTestCase;
import com.epam.reportportal.infrastructure.persistence.entity.tms.TmsTestCaseDefaultVersionTestCaseId;
import com.epam.reportportal.infrastructure.persistence.entity.tms.TmsTestCaseVersion;
import com.epam.reportportal.infrastructure.rules.exception.ReportPortalException;
import jakarta.validation.Valid;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service implementation for managing TMS Test Case Versions.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TmsTestCaseVersionServiceImpl implements TmsTestCaseVersionService {

  private static final String DEFAULT_TEST_CASE_VERSION_NOT_FOUND =
      "Default test case version for test case: %d";

  private final TmsTestCaseVersionMapper tmsTestCaseVersionMapper;
  private final TmsManualScenarioService tmsManualScenarioService;
  private final TmsTestCaseVersionRepository tmsTestCaseVersionRepository;

  @Override
  @Transactional
  public TmsTestCaseVersion createDefaultTestCaseVersion(TmsTestCase tmsTestCase,
      @Valid TmsManualScenarioRQ tmsManualScenarioRQ) {
    var defaultTestCaseVersion = tmsTestCaseVersionMapper.createDefaultTestCaseVersion();

    if (Objects.nonNull(tmsManualScenarioRQ)) {
      var tmsManualScenario = tmsManualScenarioService
          .createTmsManualScenario(defaultTestCaseVersion, tmsManualScenarioRQ);

      defaultTestCaseVersion.setManualScenario(tmsManualScenario);

      tmsManualScenario.setTestCaseVersion(defaultTestCaseVersion);
    }

    tmsTestCase.setVersions(Collections.singleton(defaultTestCaseVersion));
    defaultTestCaseVersion.setTestCase(tmsTestCase);

    return tmsTestCaseVersionRepository.save(defaultTestCaseVersion);
  }

  @Override
  @Transactional
  public TmsTestCaseVersion updateDefaultTestCaseVersion(TmsTestCase tmsTestCase,
      @Valid TmsManualScenarioRQ tmsManualScenarioRQ) {
    return tmsTestCaseVersionRepository
        .findDefaultVersionByTestCaseId(tmsTestCase.getId())
        .map(
            existingDefaultVersion -> {
              if (Objects.nonNull(tmsManualScenarioRQ)) {
                var updatedManualScenario = tmsManualScenarioService
                    .updateTmsManualScenario(existingDefaultVersion, tmsManualScenarioRQ);

                existingDefaultVersion.setManualScenario(updatedManualScenario);

                tmsTestCaseVersionRepository.save(existingDefaultVersion);
              }
              return existingDefaultVersion;
            })
        .orElseGet(() -> createDefaultTestCaseVersion(tmsTestCase, tmsManualScenarioRQ));
  }

  @Override
  @Transactional
  public TmsTestCaseVersion patchDefaultTestCaseVersion(TmsTestCase tmsTestCase,
      @Valid TmsManualScenarioRQ tmsManualScenarioRQ) {
    var existingDefaultVersion = getDefaultVersion(tmsTestCase.getId());
    if (Objects.isNull(tmsManualScenarioRQ)) {
      return existingDefaultVersion;
    }
    var patchedManualScenario = tmsManualScenarioService
        .patchTmsManualScenario(existingDefaultVersion, tmsManualScenarioRQ);

    existingDefaultVersion.setManualScenario(patchedManualScenario);

    return tmsTestCaseVersionRepository.save(existingDefaultVersion);
  }

  @Override
  @Transactional
  public void deleteAllByTestCaseId(Long testCaseId) {
    tmsManualScenarioService.deleteAllByTestCaseId(testCaseId);
    tmsTestCaseVersionRepository.deleteAllByTestCaseId(testCaseId);
  }

  @Override
  @Transactional
  public void deleteAllByTestCaseIds(List<Long> testCaseIds) {
    if (CollectionUtils.isNotEmpty(testCaseIds)) {
      tmsManualScenarioService.deleteAllByTestCaseIds(testCaseIds);
      tmsTestCaseVersionRepository.deleteAllByTestCaseIds(testCaseIds);
    }
  }

  @Override
  @Transactional
  public void deleteAllByTestFolderId(Long projectId, Long folderId) {
    tmsManualScenarioService.deleteAllByTestFolderId(projectId, folderId);
    tmsTestCaseVersionRepository.deleteTestCaseVersionsByTestFolderId(projectId, folderId);
  }

  @Override
  @Transactional(readOnly = true)
  public TmsTestCaseVersion getDefaultVersion(Long testCaseId) {
    return tmsTestCaseVersionRepository
        .findDefaultVersionByTestCaseId(testCaseId)
        .orElseThrow(() -> new ReportPortalException(
            NOT_FOUND, DEFAULT_TEST_CASE_VERSION_NOT_FOUND.formatted(testCaseId))
        );
  }

  @Override
  @Transactional(readOnly = true)
  public Map<Long, TmsTestCaseVersion> getDefaultVersions(List<Long> testCaseIds) {
    return Optional
        .ofNullable(tmsTestCaseVersionRepository.findDefaultVersionsByTestCaseIds(testCaseIds))
        .orElse(Collections.emptyList())
        .stream()
        .collect(
            Collectors.toMap(
                TmsTestCaseDefaultVersionTestCaseId::getTestCaseId,
                TmsTestCaseDefaultVersionTestCaseId::getTestCaseVersion
            )
        );
  }

  @Override
  @Transactional
  public TmsTestCaseVersion duplicateDefaultVersion(TmsTestCase newTestCase,
      TmsTestCaseVersion originalVersion) {
    var duplicatedVersion = tmsTestCaseVersionRepository.save(
        tmsTestCaseVersionMapper.duplicateDefaultTestCaseVersion(
            originalVersion, newTestCase)
    );

    if (Objects.nonNull(originalVersion.getManualScenario())) {
      var duplicatedManualScenario = tmsManualScenarioService
          .duplicateManualScenario(duplicatedVersion, originalVersion.getManualScenario());
      duplicatedVersion.setManualScenario(duplicatedManualScenario);
    }

    newTestCase.setVersions(Collections.singleton(duplicatedVersion));

    return duplicatedVersion;
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<TmsTestCaseVersion> findDefaultByTestCaseId(Long testCaseId) {
    log.debug("Finding default version for test case: {}", testCaseId);
    return tmsTestCaseVersionRepository.findDefaultVersionByTestCaseId(testCaseId);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<Long> findDefaultVersionIdByTestCaseId(Long testCaseId) {
    log.debug("Finding default version ID for test case: {}", testCaseId);
    return tmsTestCaseVersionRepository.findDefaultVersionByTestCaseId(testCaseId)
        .map(TmsTestCaseVersion::getId);
  }
}
