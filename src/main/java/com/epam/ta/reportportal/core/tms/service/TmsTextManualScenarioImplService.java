package com.epam.ta.reportportal.core.tms.service;

import static com.epam.reportportal.rules.exception.ErrorType.NOT_FOUND;

import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.core.tms.db.entity.TmsManualScenario;
import com.epam.ta.reportportal.core.tms.db.repository.TmsTextManualScenarioRepository;
import com.epam.ta.reportportal.core.tms.dto.TmsManualScenarioRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsManualScenarioType;
import com.epam.ta.reportportal.core.tms.dto.TmsTextManualScenarioRQ;
import com.epam.ta.reportportal.core.tms.mapper.TmsTextManualScenarioMapper;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TmsTextManualScenarioImplService implements TmsManualScenarioImplService {

  private static final String TEXT_MANUAL_SCENARIO_FOR_MANUAL_SCENARIO =
      "Text Manual Scenario for Manual Scenario with id: %d";

  private final TmsTextManualScenarioMapper tmsTextManualScenarioMapper;
  private final TmsTextManualScenarioRepository tmsTextManualScenarioRepository;

  @Override
  public TmsManualScenarioType getTmsManualScenarioType() {
    return TmsManualScenarioType.TEXT;
  }

  @Override
  @Transactional
  public void createTmsManualScenarioImpl(
      TmsManualScenario tmsManualScenario,
      TmsManualScenarioRQ testCaseManualScenarioRQ) {

    var tmsTextManualScenario = tmsTextManualScenarioMapper.createTmsManualScenario(
        (TmsTextManualScenarioRQ) testCaseManualScenarioRQ
    );

    tmsManualScenario.setTextScenario(tmsTextManualScenario);
    tmsTextManualScenario.setManualScenario(tmsManualScenario);

    tmsTextManualScenarioRepository.save(tmsTextManualScenario);
  }

  @Override
  @Transactional
  public void updateTmsManualScenarioImpl(TmsManualScenario manualScenario,
      TmsManualScenarioRQ testCaseManualScenarioRQ) {
    var textManualScenario = manualScenario.getTextScenario();

    if (Objects.nonNull(textManualScenario)) {
      tmsTextManualScenarioMapper.updateTmsManualScenario(
          textManualScenario, (TmsTextManualScenarioRQ) testCaseManualScenarioRQ
      );
    } else {
      textManualScenario = tmsTextManualScenarioMapper.createTmsManualScenario(
          (TmsTextManualScenarioRQ) testCaseManualScenarioRQ
      );
      manualScenario.setTextScenario(textManualScenario);
      textManualScenario.setManualScenario(manualScenario);
    }

    tmsTextManualScenarioRepository.save(textManualScenario);
  }

  @Override
  @Transactional
  public void patchTmsManualScenarioImpl(TmsManualScenario manualScenario,
      TmsManualScenarioRQ testCaseManualScenarioRQ) {
    var existingTextManualScenario = manualScenario.getTextScenario();
    if (Objects.nonNull(existingTextManualScenario)) {

      tmsTextManualScenarioMapper.patchTmsManualScenario(existingTextManualScenario,
          (TmsTextManualScenarioRQ) testCaseManualScenarioRQ);

      tmsTextManualScenarioRepository.save(existingTextManualScenario);
    } else {
      throw new ReportPortalException(
          NOT_FOUND, TEXT_MANUAL_SCENARIO_FOR_MANUAL_SCENARIO.formatted(manualScenario.getId()));
    }
  }

  @Override
  @Transactional
  public void deleteAllByTestCaseId(Long testCaseId) {
    tmsTextManualScenarioRepository.deleteAllByTestCaseId(testCaseId);
  }

  @Override
  @Transactional
  public void deleteAllByTestCaseIds(List<Long> testCaseIds) {
    if (CollectionUtils.isNotEmpty(testCaseIds)) {
      tmsTextManualScenarioRepository.deleteAllByTestCaseIds(testCaseIds);
    }
  }

  @Override
  @Transactional
  public void deleteAllByTestFolderId(Long projectId, Long folderId) {
    tmsTextManualScenarioRepository.deleteAllByTestFolderId(projectId, folderId);
  }
}
