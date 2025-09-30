package com.epam.ta.reportportal.core.tms.service;

import static com.epam.reportportal.rules.exception.ErrorType.NOT_FOUND;

import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.entity.tms.TmsManualScenario;
import com.epam.ta.reportportal.dao.tms.TmsTextManualScenarioRepository;
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
  private final TmsTextManualScenarioAttachmentService tmsTextManualScenarioAttachmentService;

  @Override
  public TmsManualScenarioType getTmsManualScenarioType() {
    return TmsManualScenarioType.TEXT;
  }

  @Override
  @Transactional
  public void createTmsManualScenarioImpl(
      TmsManualScenario tmsManualScenario,
      TmsManualScenarioRQ testCaseManualScenarioRQ) {

    var tmsTextManualScenarioRQ = (TmsTextManualScenarioRQ) testCaseManualScenarioRQ;

    var tmsTextManualScenario = tmsTextManualScenarioMapper.createTmsManualScenario(
        tmsTextManualScenarioRQ);

    tmsTextManualScenarioAttachmentService.createAttachments(tmsTextManualScenario,
        tmsTextManualScenarioRQ);

    tmsManualScenario.setTextScenario(tmsTextManualScenario);
    tmsTextManualScenario.setManualScenario(tmsManualScenario);

    tmsTextManualScenarioRepository.save(tmsTextManualScenario);
  }

  @Override
  @Transactional
  public void updateTmsManualScenarioImpl(TmsManualScenario manualScenario,
      TmsManualScenarioRQ testCaseManualScenarioRQ) {
    var textManualScenario = manualScenario.getTextScenario();
    var tmsTextManualScenarioRQ = (TmsTextManualScenarioRQ) testCaseManualScenarioRQ;

    if (Objects.nonNull(textManualScenario)) {
      tmsTextManualScenarioMapper.updateTmsManualScenario(
          textManualScenario, tmsTextManualScenarioRQ
      );
      tmsTextManualScenarioAttachmentService.updateAttachments(textManualScenario,
          tmsTextManualScenarioRQ);
    } else {
      textManualScenario = tmsTextManualScenarioMapper.createTmsManualScenario(
          tmsTextManualScenarioRQ
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
      var tmsTextManualScenarioRQ = (TmsTextManualScenarioRQ) testCaseManualScenarioRQ;

      tmsTextManualScenarioMapper.patchTmsManualScenario(existingTextManualScenario,
          tmsTextManualScenarioRQ);

      tmsTextManualScenarioAttachmentService.patchAttachments(existingTextManualScenario,
          tmsTextManualScenarioRQ);

      tmsTextManualScenarioRepository.save(existingTextManualScenario);
    } else {
      throw new ReportPortalException(
          NOT_FOUND, TEXT_MANUAL_SCENARIO_FOR_MANUAL_SCENARIO.formatted(manualScenario.getId()));
    }
  }

  @Override
  @Transactional
  public void deleteAllByTestCaseId(Long testCaseId) {
    tmsTextManualScenarioAttachmentService.deleteAllByTestCaseId(testCaseId);
    tmsTextManualScenarioRepository.deleteAllByTestCaseId(testCaseId);
  }

  @Override
  @Transactional
  public void deleteAllByTestCaseIds(List<Long> testCaseIds) {
    if (CollectionUtils.isNotEmpty(testCaseIds)) {
      tmsTextManualScenarioAttachmentService.deleteAllByTestCaseIds(testCaseIds);
      tmsTextManualScenarioRepository.deleteAllByTestCaseIds(testCaseIds);
    }
  }

  @Override
  @Transactional
  public void deleteAllByTestFolderId(Long projectId, Long folderId) {
    tmsTextManualScenarioAttachmentService.deleteAllByTestFolderId(projectId, folderId);
    tmsTextManualScenarioRepository.deleteAllByTestFolderId(projectId, folderId);
  }

  @Override
  @Transactional
  public void duplicateManualScenarioImpl(TmsManualScenario newScenario,
      TmsManualScenario originalScenario) {
    if (Objects.isNull(originalScenario)) {
      return;
    }
    var originalTextScenario = originalScenario.getTextScenario();
    if (Objects.nonNull(originalTextScenario)) {
      var duplicatedTextScenario = tmsTextManualScenarioMapper.duplicateTextScenario(newScenario,
          originalTextScenario);

      duplicatedTextScenario = tmsTextManualScenarioRepository.save(duplicatedTextScenario);

      if (CollectionUtils.isNotEmpty(originalTextScenario.getAttachments())) {
        tmsTextManualScenarioAttachmentService.duplicateAttachments(originalTextScenario,
            duplicatedTextScenario);
      }

      newScenario.setTextScenario(duplicatedTextScenario);
    }
  }
}
