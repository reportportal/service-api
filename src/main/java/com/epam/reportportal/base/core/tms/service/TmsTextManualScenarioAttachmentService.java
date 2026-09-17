package com.epam.reportportal.base.core.tms.service;

import com.epam.reportportal.base.core.tms.dto.TmsTextManualScenarioRQ;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTextManualScenario;
import java.util.List;

public interface TmsTextManualScenarioAttachmentService {

  /**
   * Creates attachments for text-based manual scenario.
   */
  void createAttachments(Long projectId, TmsTextManualScenario tmsTextManualScenario,
      TmsTextManualScenarioRQ tmsTextManualScenarioRq);

  /**
   * Updates attachments for text-based manual scenario (replaces all existing).
   */
  void updateAttachments(Long projectId, TmsTextManualScenario textManualScenario,
      TmsTextManualScenarioRQ tmsTextManualScenarioRq);

  void deleteAllByTestCaseId(Long testCaseId);

  void deleteAllByTestCaseIds(List<Long> testCaseIds);

  void deleteAllByTestFolderId(Long projectId, Long folderId);

  void duplicateAttachments(TmsTextManualScenario originalTextScenario,
      TmsTextManualScenario duplicatedTextScenario);
}
