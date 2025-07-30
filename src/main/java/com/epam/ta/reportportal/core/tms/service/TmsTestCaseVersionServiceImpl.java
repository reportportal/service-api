package com.epam.ta.reportportal.core.tms.service;

import static com.epam.reportportal.rules.exception.ErrorType.NOT_FOUND;

import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.core.tms.db.entity.TmsTestCase;
import com.epam.ta.reportportal.core.tms.db.entity.TmsTestCaseVersion;
import com.epam.ta.reportportal.core.tms.db.repository.TmsTestCaseVersionRepository;
import com.epam.ta.reportportal.core.tms.dto.TmsTestCaseDefaultVersionRQ;
import com.epam.ta.reportportal.core.tms.mapper.TmsTestCaseVersionMapper;
import com.epam.ta.reportportal.core.tms.service.factory.TmsManualScenarioServiceFactory;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TmsTestCaseVersionServiceImpl implements TmsTestCaseVersionService {

  private static final String DEFAULT_TEST_CASE_VERSION_NOT_FOUND =
      "Default test case version for test case: %d";

  private final TmsTestCaseVersionMapper tmsTestCaseVersionMapper;
  private final TmsManualScenarioServiceFactory tmsManualScenarioServiceFactory;
  private final TmsTestCaseVersionRepository tmsTestCaseVersionRepository;

  @Override
  @Transactional
  public void createDefaultTestCaseVersion(TmsTestCase tmsTestCase,
      TmsTestCaseDefaultVersionRQ defaultTestCaseVersionRQ) {
    var defaultTestCaseVersion = tmsTestCaseVersionMapper.createDefaultTestCaseVersion(
        defaultTestCaseVersionRQ
    );

    if (Objects.nonNull(defaultTestCaseVersionRQ.getManualScenario())) {
      var tmsManualScenario = tmsManualScenarioServiceFactory
          .getTmsManualScenarioService(
              defaultTestCaseVersionRQ.getManualScenario().getManualScenarioType())
          .createTmsManualScenario(defaultTestCaseVersion, defaultTestCaseVersionRQ.getManualScenario()
          );

      defaultTestCaseVersion.setManualScenario(tmsManualScenario);

      tmsManualScenario.setTestCaseVersion(defaultTestCaseVersion);
    }

    tmsTestCase.setVersions(Collections.singleton(defaultTestCaseVersion));
    defaultTestCaseVersion.setTestCase(tmsTestCase);

    tmsTestCaseVersionRepository.save(defaultTestCaseVersion);
  }

  @Override
  @Transactional
  public void updateDefaultTestCaseVersion(TmsTestCase tmsTestCase,
      TmsTestCaseDefaultVersionRQ defaultTestCaseVersionRQ) {
    if (defaultTestCaseVersionRQ == null) {
      return;
    }

    var existingDefaultVersion = Optional
        .ofNullable(tmsTestCase.getVersions())
        .orElse(Collections.emptySet())
        .stream()
        .filter(TmsTestCaseVersion::isDefault)
        .findFirst()
        .orElse(null);

    if (existingDefaultVersion != null) {
      tmsTestCaseVersionMapper.update(existingDefaultVersion,
          tmsTestCaseVersionMapper.createDefaultTestCaseVersion(defaultTestCaseVersionRQ));

      if (defaultTestCaseVersionRQ.getManualScenario() != null) {
        var updatedManualScenario = tmsManualScenarioServiceFactory
            .getTmsManualScenarioService(defaultTestCaseVersionRQ.getManualScenario().getManualScenarioType())
            .updateTmsManualScenario(existingDefaultVersion, defaultTestCaseVersionRQ.getManualScenario());

        existingDefaultVersion.setManualScenario(updatedManualScenario);
      }

      tmsTestCaseVersionRepository.save(existingDefaultVersion);
    } else {
      createDefaultTestCaseVersion(tmsTestCase, defaultTestCaseVersionRQ);
    }
  }

  @Override
  @Transactional
  public void patchDefaultTestCaseVersion(TmsTestCase tmsTestCase,
      TmsTestCaseDefaultVersionRQ defaultTestCaseVersionRQ) {
    if (defaultTestCaseVersionRQ == null) {
      return;
    }

    var existingDefaultVersion = tmsTestCase.getVersions()
        .stream()
        .filter(TmsTestCaseVersion::isDefault)
        .findFirst()
        .orElseThrow(() -> new ReportPortalException(
            NOT_FOUND, DEFAULT_TEST_CASE_VERSION_NOT_FOUND.formatted(tmsTestCase.getId()))
        );

    tmsTestCaseVersionMapper.patch(existingDefaultVersion,
        tmsTestCaseVersionMapper.createDefaultTestCaseVersion(defaultTestCaseVersionRQ));

    if (defaultTestCaseVersionRQ.getManualScenario() != null) {
      var patchedManualScenario = tmsManualScenarioServiceFactory
          .getTmsManualScenarioService(defaultTestCaseVersionRQ.getManualScenario().getManualScenarioType())
          .patchTmsManualScenario(existingDefaultVersion, defaultTestCaseVersionRQ.getManualScenario());

      existingDefaultVersion.setManualScenario(patchedManualScenario);
    }

    tmsTestCaseVersionRepository.save(existingDefaultVersion);
  }

  @Override
  @Transactional
  public void deleteAllByTestCaseId(Long testCaseId) {
    tmsManualScenarioServiceFactory
        .getTmsManualScenarioServices()
        .forEach(manualScenarioService -> manualScenarioService.deleteAllByTestCaseId(testCaseId));
    tmsTestCaseVersionRepository.deleteAllByTestCaseId(testCaseId);
  }

  @Override
  @Transactional
  public void deleteAllByTestCaseIds(List<Long> testCaseIds) {
    if (testCaseIds != null && !testCaseIds.isEmpty()) {
      tmsManualScenarioServiceFactory
          .getTmsManualScenarioServices()
          .forEach(manualScenarioService -> manualScenarioService.deleteAllByTestCaseIds(testCaseIds));
      tmsTestCaseVersionRepository.deleteAllByTestCaseIds(testCaseIds);
    }
  }

  @Override
  @Transactional
  public void deleteAllByTestFolderId(Long projectId, Long folderId) {
    tmsManualScenarioServiceFactory
        .getTmsManualScenarioServices()
        .forEach(manualScenarioService -> manualScenarioService.deleteAllByTestFolderId(projectId, folderId));
    tmsTestCaseVersionRepository.deleteTestCaseVersionsByTestFolderId(projectId, folderId);
  }
}
