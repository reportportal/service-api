package com.epam.ta.reportportal.core.tms.service;

import com.epam.ta.reportportal.core.tms.db.entity.TmsTextManualScenario;
import com.epam.ta.reportportal.core.tms.dto.TmsTextManualScenarioRQ;
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
