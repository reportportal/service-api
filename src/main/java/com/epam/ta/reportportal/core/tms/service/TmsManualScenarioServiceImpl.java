package com.epam.ta.reportportal.core.tms.service;

import static com.epam.reportportal.rules.exception.ErrorType.NOT_FOUND;

import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.core.tms.db.entity.TmsManualScenario;
import com.epam.ta.reportportal.core.tms.db.entity.TmsTestCaseVersion;
import com.epam.ta.reportportal.core.tms.db.repository.TmsManualScenarioRepository;
import com.epam.ta.reportportal.core.tms.dto.TmsManualScenarioRQ;
import com.epam.ta.reportportal.core.tms.mapper.TmsManualScenarioMapper;
import com.epam.ta.reportportal.core.tms.service.factory.TmsManualScenarioImplServiceFactory;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TmsManualScenarioServiceImpl implements TmsManualScenarioService {

  private static final String MANUAL_SCENARIO_FOR_VERSION =
      "Manual Scenario for the test case version with id: %d";

  private final TmsManualScenarioRepository tmsManualScenarioRepository;
  private final TmsManualScenarioAttributeService tmsManualScenarioAttributeService;
  private final TmsManualScenarioImplServiceFactory tmsManualScenarioImplServiceFactory;
  private final TmsManualScenarioMapper tmsManualScenarioMapper;

  @Override
  public TmsManualScenario createTmsManualScenario(TmsTestCaseVersion testCaseVersion,
      TmsManualScenarioRQ testCaseManualScenarioRQ) {
    var tmsManualScenario = tmsManualScenarioMapper.createTmsManualScenario(
        testCaseManualScenarioRQ
    );

    tmsManualScenarioAttributeService.createAttributes(tmsManualScenario,
        testCaseManualScenarioRQ.getTags());

    testCaseVersion.setManualScenario(tmsManualScenario);
    tmsManualScenario.setTestCaseVersion(testCaseVersion);

    tmsManualScenarioImplServiceFactory
        .getTmsManualScenarioService(testCaseManualScenarioRQ.getManualScenarioType())
        .createTmsManualScenarioImpl(tmsManualScenario, testCaseManualScenarioRQ);

    return tmsManualScenarioRepository.save(tmsManualScenario);
  }

  @Override
  public TmsManualScenario updateTmsManualScenario(TmsTestCaseVersion testCaseVersion,
      TmsManualScenarioRQ testCaseManualScenarioRQ) {
    var manualScenario = testCaseVersion.getManualScenario();
    if (Objects.nonNull(manualScenario)) {
      tmsManualScenarioMapper.update(manualScenario,
          tmsManualScenarioMapper.createTmsManualScenario(testCaseManualScenarioRQ));

      tmsManualScenarioAttributeService.updateAttributes(manualScenario,
          testCaseManualScenarioRQ.getTags());

      tmsManualScenarioImplServiceFactory
          .getTmsManualScenarioService(testCaseManualScenarioRQ.getManualScenarioType())
          .updateTmsManualScenarioImpl(manualScenario, testCaseManualScenarioRQ);
      return tmsManualScenarioRepository.save(manualScenario);
    } else {
     return createTmsManualScenario(testCaseVersion, testCaseManualScenarioRQ);
    }
  }

  @Override
  public TmsManualScenario patchTmsManualScenario(TmsTestCaseVersion testCaseVersion,
      TmsManualScenarioRQ testCaseManualScenarioRQ) {
    var existingManualScenario = testCaseVersion.getManualScenario();

    if (Objects.nonNull(existingManualScenario)) {
      tmsManualScenarioMapper.patch(existingManualScenario,
          tmsManualScenarioMapper.createTmsManualScenario(testCaseManualScenarioRQ));

      tmsManualScenarioAttributeService.patchAttributes(existingManualScenario,
          testCaseManualScenarioRQ.getTags());

      tmsManualScenarioImplServiceFactory
          .getTmsManualScenarioService(testCaseManualScenarioRQ.getManualScenarioType())
          .patchTmsManualScenarioImpl(existingManualScenario, testCaseManualScenarioRQ);

      return tmsManualScenarioRepository.save(existingManualScenario);
    } else {
      throw new ReportPortalException(
          NOT_FOUND, MANUAL_SCENARIO_FOR_VERSION.formatted(testCaseVersion.getId()));
    }
  }

  @Override
  public void deleteAllByTestCaseId(Long testCaseId) {
    tmsManualScenarioImplServiceFactory
        .getTmsManualScenarioImplServices()
        .forEach(service -> service.deleteAllByTestCaseId(testCaseId));
    tmsManualScenarioAttributeService.deleteAllByTestCaseId(testCaseId);
    tmsManualScenarioRepository.deleteAllByTestCaseId(testCaseId);
  }

  @Override
  public void deleteAllByTestCaseIds(List<Long> testCaseIds) {
    if (CollectionUtils.isNotEmpty(testCaseIds)) {
      tmsManualScenarioImplServiceFactory
          .getTmsManualScenarioImplServices()
          .forEach(service -> service.deleteAllByTestCaseIds(testCaseIds));
      tmsManualScenarioAttributeService.deleteAllByTestCaseIds(testCaseIds);
      tmsManualScenarioRepository.deleteAllByTestCaseIds(testCaseIds);
    }
  }

  @Override
  public void deleteAllByTestFolderId(Long projectId, Long folderId) {
    tmsManualScenarioImplServiceFactory
        .getTmsManualScenarioImplServices()
        .forEach(service -> service.deleteAllByTestFolderId(projectId, folderId));
    tmsManualScenarioAttributeService.deleteAllByTestFolderId(projectId, folderId);
    tmsManualScenarioRepository.deleteAllByTestFolderId(projectId, folderId);
  }
}
