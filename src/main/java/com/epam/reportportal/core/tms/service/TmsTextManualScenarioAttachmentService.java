package com.epam.reportportal.core.tms.service;

import com.epam.reportportal.infrastructure.persistence.entity.tms.TmsTextManualScenario;
import com.epam.reportportal.core.tms.dto.TmsTextManualScenarioRQ;
import java.util.List;

public interface TmsTextManualScenarioAttachmentService {

  void createAttachments(TmsTextManualScenario tmsTextManualScenario,
      TmsTextManualScenarioRQ tmsTextManualScenarioRQ);

  void updateAttachments(TmsTextManualScenario textManualScenario,
      TmsTextManualScenarioRQ tmsTextManualScenarioRQ);

  void patchAttachments(TmsTextManualScenario existingTextManualScenario,
      TmsTextManualScenarioRQ tmsTextManualScenarioRQ);

  void deleteAllByTestCaseId(Long testCaseId);

  void deleteAllByTestCaseIds(List<Long> testCaseIds);

  void deleteAllByTestFolderId(Long projectId, Long folderId);

  void duplicateAttachments(TmsTextManualScenario originalTextScenario,
      TmsTextManualScenario duplicatedTextScenario);
}
