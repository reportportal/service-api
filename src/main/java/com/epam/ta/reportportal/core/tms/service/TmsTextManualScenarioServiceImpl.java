package com.epam.ta.reportportal.core.tms.service;

import com.epam.ta.reportportal.core.tms.db.entity.TmsManualScenario;
import com.epam.ta.reportportal.core.tms.db.entity.TmsTestCaseVersion;
import com.epam.ta.reportportal.core.tms.db.repository.TmsManualScenarioRepository;
import com.epam.ta.reportportal.core.tms.dto.TmsManualScenarioRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsManualScenarioRQ.TmsManualScenarioType;
import com.epam.ta.reportportal.core.tms.dto.TmsTextManualScenarioRQ;
import com.epam.ta.reportportal.core.tms.mapper.TmsManualScenarioMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TmsTextManualScenarioServiceImpl implements TmsManualScenarioService {

  private final TmsManualScenarioRepository tmsManualScenarioRepository;
  private final TmsManualScenarioAttributeService tmsManualScenarioAttributeService;
  private final TmsManualScenarioMapper tmsManualScenarioMapper;
  private final TmsStepService tmsStepService;

  @Override
  public TmsManualScenarioType getTmsManualScenarioType() {
    return TmsManualScenarioType.TEXT;
  }

  @Override
  @Transactional
  public TmsManualScenario createTmsManualScenario(TmsTestCaseVersion testCaseVersion,
      TmsManualScenarioRQ testCaseManualScenarioRQ) {
    var tmsManualScenario = tmsManualScenarioMapper.createTmsManualScenario(
        testCaseManualScenarioRQ
    );

    tmsManualScenarioAttributeService.createAttributes(tmsManualScenario,
        testCaseManualScenarioRQ.getAttributes());
    tmsStepService.createStep(tmsManualScenario,
        (TmsTextManualScenarioRQ) testCaseManualScenarioRQ);

    testCaseVersion.setManualScenario(tmsManualScenario);
    tmsManualScenario.setTestCaseVersion(testCaseVersion);

    return tmsManualScenarioRepository.save(tmsManualScenario);
  }

  @Override
  @Transactional
  public TmsManualScenario updateTmsManualScenario(TmsTestCaseVersion testCaseVersion,
      TmsManualScenarioRQ testCaseManualScenarioRQ) {
    var existingManualScenario = testCaseVersion.getManualScenario();

    tmsManualScenarioMapper.update(existingManualScenario,
        tmsManualScenarioMapper.createTmsManualScenario(testCaseManualScenarioRQ));

    tmsManualScenarioAttributeService.updateAttributes(existingManualScenario,
        testCaseManualScenarioRQ.getAttributes());

    tmsStepService.updateStep(existingManualScenario,
        (TmsTextManualScenarioRQ) testCaseManualScenarioRQ);

    return tmsManualScenarioRepository.save(existingManualScenario);
  }

  @Override
  @Transactional
  public TmsManualScenario patchTmsManualScenario(TmsTestCaseVersion testCaseVersion,
      TmsManualScenarioRQ testCaseManualScenarioRQ) {
    var existingManualScenario = testCaseVersion.getManualScenario();

    tmsManualScenarioMapper.patch(existingManualScenario,
        tmsManualScenarioMapper.createTmsManualScenario(testCaseManualScenarioRQ));

    tmsManualScenarioAttributeService.patchAttributes(existingManualScenario,
        testCaseManualScenarioRQ.getAttributes());

    tmsStepService.patchStep(existingManualScenario,
        (TmsTextManualScenarioRQ) testCaseManualScenarioRQ);

    return tmsManualScenarioRepository.save(existingManualScenario);
  }

  @Override
  @Transactional
  public void deleteAllByTestCaseId(Long testCaseId) {
    tmsStepService.deleteAllByTestCaseId(testCaseId);
    tmsManualScenarioAttributeService.deleteAllByTestCaseId(testCaseId);
    tmsManualScenarioRepository.deleteAllByTestCaseId(testCaseId);
  }

  @Override
  @Transactional
  public void deleteAllByTestCaseIds(List<Long> testCaseIds) {
    if (testCaseIds != null && !testCaseIds.isEmpty()) {
      tmsStepService.deleteAllByTestCaseIds(testCaseIds);
      tmsManualScenarioAttributeService.deleteAllByTestCaseIds(testCaseIds);
      tmsManualScenarioRepository.deleteAllByTestCaseIds(testCaseIds);
    }
  }

  @Override
  @Transactional
  public void deleteAllByTestFolderId(Long projectId, Long folderId) {
    tmsStepService.deleteAllByTestFolderId(projectId, folderId);
    tmsManualScenarioAttributeService.deleteAllByTestFolderId(projectId, folderId);
    tmsManualScenarioRepository.deleteManualScenariosByTestFolderId(projectId, folderId);
  }
}
