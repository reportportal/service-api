package com.epam.reportportal.base.core.tms.service;

import static com.epam.reportportal.base.infrastructure.rules.exception.ErrorType.NOT_FOUND;

import com.epam.reportportal.base.core.tms.dto.TmsManualScenarioRQ;
import com.epam.reportportal.base.core.tms.dto.TmsManualScenarioType;
import com.epam.reportportal.base.core.tms.mapper.TmsManualScenarioMapper;
import com.epam.reportportal.base.core.tms.service.factory.TmsManualScenarioImplServiceFactory;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsManualScenarioRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsManualScenario;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestCaseVersion;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TmsManualScenarioServiceImpl implements TmsManualScenarioService {

  private static final String MANUAL_SCENARIO_FOR_VERSION =
      "Manual Scenario for the test case version with id: %d";

  private final TmsManualScenarioRepository tmsManualScenarioRepository;
  private final TmsManualScenarioAttributeService tmsManualScenarioAttributeService;
  private final TmsManualScenarioImplServiceFactory tmsManualScenarioImplServiceFactory;
  private final TmsManualScenarioMapper tmsManualScenarioMapper;
  private final TmsManualScenarioPreconditionsService tmsManualScenarioPreconditionsService;
  private final TmsManualScenarioRequirementService tmsManualScenarioRequirementService;

  @Override
  @Transactional
  public TmsManualScenario createTmsManualScenario(long projectId,
      TmsTestCaseVersion testCaseVersion,
      TmsManualScenarioRQ testCaseManualScenarioRQ) {
    var tmsManualScenario = tmsManualScenarioRepository.save(
        tmsManualScenarioMapper.createTmsManualScenario(testCaseManualScenarioRQ)
    );

    tmsManualScenarioPreconditionsService.createPreconditions(
        projectId, tmsManualScenario, testCaseManualScenarioRQ.getPreconditions()
    );

    tmsManualScenarioRequirementService.createRequirements(
        tmsManualScenario, testCaseManualScenarioRQ.getRequirements()
    );

    tmsManualScenarioAttributeService.createAttributes(projectId, tmsManualScenario,
        testCaseManualScenarioRQ.getAttributes());

    tmsManualScenarioImplServiceFactory
        .getTmsManualScenarioService(testCaseManualScenarioRQ.getManualScenarioType())
        .createTmsManualScenarioImpl(projectId, tmsManualScenario, testCaseManualScenarioRQ);

    testCaseVersion.setManualScenario(tmsManualScenario);
    tmsManualScenario.setTestCaseVersion(testCaseVersion);

    return tmsManualScenario;
  }

  @Override
  @Transactional
  public List<TmsManualScenario> createTmsManualScenariosBatch(long projectId,
      List<TmsTestCaseVersion> testCaseVersions,
      List<TmsManualScenarioRQ> testCaseManualScenarioRQs) {

    // Indices of versions that actually have a manual scenario to create
    var activeIndices = new ArrayList<Integer>();
    var scenarioEntities = new ArrayList<TmsManualScenario>();

    for (int i = 0; i < testCaseManualScenarioRQs.size(); i++) {
      var rq = testCaseManualScenarioRQs.get(i);
      if (rq == null) {
        continue;
      }
      var scenario = tmsManualScenarioMapper.createTmsManualScenario(rq);
      scenario.setTestCaseVersion(testCaseVersions.get(i));
      activeIndices.add(i);
      scenarioEntities.add(scenario);
    }

    var result = new ArrayList<TmsManualScenario>(Collections.nCopies(
        testCaseManualScenarioRQs.size(), null));

    if (scenarioEntities.isEmpty()) {
      return result;
    }

    var savedScenarios = tmsManualScenarioRepository.saveAll(scenarioEntities);
    var activeRQs = activeIndices.stream().map(testCaseManualScenarioRQs::get).toList();

    for (int i = 0; i < savedScenarios.size(); i++) {
      var scenario = savedScenarios.get(i);
      testCaseVersions.get(activeIndices.get(i)).setManualScenario(scenario);
      result.set(activeIndices.get(i), scenario);
    }

    tmsManualScenarioPreconditionsService.createPreconditionsBatch(projectId, savedScenarios,
        activeRQs.stream().map(TmsManualScenarioRQ::getPreconditions).toList());

    tmsManualScenarioRequirementService.createRequirementsBatch(savedScenarios,
        activeRQs.stream().map(TmsManualScenarioRQ::getRequirements).toList());

    for (int i = 0; i < savedScenarios.size(); i++) {
      tmsManualScenarioAttributeService.createAttributes(projectId, savedScenarios.get(i),
          activeRQs.get(i).getAttributes());
    }

    // All scenarios created via a single importTestCases()/CSV-import call share one manual
    // scenario type (CSV only ever produces TEXT), so one factory lookup + one batch call suffices.
    var scenariosByType = new LinkedHashMap<TmsManualScenarioType, List<Integer>>();
    for (int i = 0; i < savedScenarios.size(); i++) {
      scenariosByType
          .computeIfAbsent(activeRQs.get(i).getManualScenarioType(), t -> new ArrayList<>())
          .add(i);
    }

    scenariosByType.forEach((type, indices) -> {
      var scenariosOfType = indices.stream().map(savedScenarios::get).toList();
      var rqsOfType = indices.stream().map(activeRQs::get).toList();
      tmsManualScenarioImplServiceFactory
          .getTmsManualScenarioService(type)
          .createTmsManualScenarioBatch(projectId, scenariosOfType, rqsOfType);
    });

    return result;
  }

  @Override
  @Transactional
  public TmsManualScenario updateTmsManualScenario(long projectId,
      TmsTestCaseVersion testCaseVersion,
      TmsManualScenarioRQ testCaseManualScenarioRQ) {
    var existingManualScenario = testCaseVersion.getManualScenario();
    if (Objects.nonNull(existingManualScenario)) {
      tmsManualScenarioMapper.update(existingManualScenario,
          tmsManualScenarioMapper.createTmsManualScenario(testCaseManualScenarioRQ));

      tmsManualScenarioPreconditionsService.updatePreconditions(
          projectId, existingManualScenario, testCaseManualScenarioRQ.getPreconditions()
      );

      tmsManualScenarioRequirementService.updateRequirements(
          existingManualScenario, testCaseManualScenarioRQ.getRequirements()
      );

      tmsManualScenarioAttributeService.updateAttributes(projectId, existingManualScenario,
          testCaseManualScenarioRQ.getAttributes());

      tmsManualScenarioImplServiceFactory
          .getTmsManualScenarioService(testCaseManualScenarioRQ.getManualScenarioType())
          .updateTmsManualScenarioImpl(projectId, existingManualScenario, testCaseManualScenarioRQ);
      return tmsManualScenarioRepository.save(existingManualScenario);
    } else {
      return createTmsManualScenario(projectId, testCaseVersion, testCaseManualScenarioRQ);
    }
  }

  @Override
  @Transactional
  public TmsManualScenario patchTmsManualScenario(long projectId,
      TmsTestCaseVersion testCaseVersion,
      TmsManualScenarioRQ testCaseManualScenarioRQ) {
    var existingManualScenario = testCaseVersion.getManualScenario();

    if (Objects.nonNull(existingManualScenario)) {
      tmsManualScenarioMapper.patch(existingManualScenario,
          tmsManualScenarioMapper.createTmsManualScenario(testCaseManualScenarioRQ));

      tmsManualScenarioPreconditionsService.patchPreconditions(
          projectId, existingManualScenario, testCaseManualScenarioRQ.getPreconditions()
      );

      tmsManualScenarioRequirementService.patchRequirements(
          existingManualScenario, testCaseManualScenarioRQ.getRequirements()
      );

      tmsManualScenarioAttributeService.updateAttributes(projectId, existingManualScenario,
          testCaseManualScenarioRQ.getAttributes());

      tmsManualScenarioImplServiceFactory
          .getTmsManualScenarioService(testCaseManualScenarioRQ.getManualScenarioType())
          .patchTmsManualScenarioImpl(projectId, existingManualScenario, testCaseManualScenarioRQ);

      return tmsManualScenarioRepository.save(existingManualScenario);
    } else {
      throw new ReportPortalException(
          NOT_FOUND, MANUAL_SCENARIO_FOR_VERSION.formatted(testCaseVersion.getId()));
    }
  }

  @Override
  @Transactional
  public void deleteAllByTestCaseId(Long testCaseId) {
    tmsManualScenarioImplServiceFactory
        .getTmsManualScenarioImplServices()
        .forEach(service -> service.deleteAllByTestCaseId(testCaseId));
    tmsManualScenarioRequirementService.deleteAllByTestCaseId(testCaseId);
    tmsManualScenarioPreconditionsService.deleteAllByTestCaseId(testCaseId);
    tmsManualScenarioAttributeService.deleteAllByTestCaseId(testCaseId);
    tmsManualScenarioRepository.deleteAllByTestCaseId(testCaseId);
  }

  @Override
  @Transactional
  public void deleteAllByTestCaseIds(List<Long> testCaseIds) {
    if (CollectionUtils.isNotEmpty(testCaseIds)) {
      tmsManualScenarioImplServiceFactory
          .getTmsManualScenarioImplServices()
          .forEach(service -> service.deleteAllByTestCaseIds(testCaseIds));
      tmsManualScenarioRequirementService.deleteAllByTestCaseIds(testCaseIds);
      tmsManualScenarioPreconditionsService.deleteAllByTestCaseIds(testCaseIds);
      tmsManualScenarioAttributeService.deleteAllByTestCaseIds(testCaseIds);
      tmsManualScenarioRepository.deleteAllByTestCaseIds(testCaseIds);
    }
  }

  @Override
  @Transactional
  public void deleteAllByTestFolderId(Long projectId, Long folderId) {
    tmsManualScenarioImplServiceFactory
        .getTmsManualScenarioImplServices()
        .forEach(service -> service.deleteAllByTestFolderId(projectId, folderId));
    tmsManualScenarioRequirementService.deleteAllByTestFolderId(projectId, folderId);
    tmsManualScenarioPreconditionsService.deleteAllByTestFolderId(projectId, folderId);
    tmsManualScenarioAttributeService.deleteAllByTestFolderId(projectId, folderId);
    tmsManualScenarioRepository.deleteAllByTestFolderId(projectId, folderId);
  }

  @Override
  @Transactional
  public TmsManualScenario duplicateManualScenario(TmsTestCaseVersion newVersion,
      TmsManualScenario originalScenario) {
    var duplicatedScenario = tmsManualScenarioMapper.duplicateManualScenario(originalScenario,
        newVersion);

    tmsManualScenarioRepository.save(duplicatedScenario);

    if (Objects.nonNull(originalScenario.getPreconditions())) {
      tmsManualScenarioPreconditionsService.duplicatePreconditions(originalScenario,
          duplicatedScenario);
    }

    if (CollectionUtils.isNotEmpty(originalScenario.getAttributes())) {
      tmsManualScenarioAttributeService.duplicateAttributes(originalScenario, duplicatedScenario);
    }

    if (CollectionUtils.isNotEmpty(originalScenario.getRequirements())) {
      tmsManualScenarioRequirementService.duplicateRequirements(originalScenario,
          duplicatedScenario);
    }

    tmsManualScenarioImplServiceFactory
        .getTmsManualScenarioService(originalScenario.getType())
        .duplicateManualScenarioImpl(duplicatedScenario, originalScenario);

    return duplicatedScenario;
  }
}
