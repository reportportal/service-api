package com.epam.reportportal.base.core.tms.service;

import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTextManualScenario;
import com.epam.reportportal.base.core.tms.dto.TmsTextManualScenarioRQ;
import java.util.List;

public interface TmsTextManualScenarioAttachmentService {

  void createAttachments(TmsTextManualScenario tmsTextManualScenario,
      TmsTextManualScenarioRQ tmsTextManualScenarioRQ);

  void updateAttachments(TmsTextManualScenario textManualScenario,
      TmsTextManualScenarioRQ tmsTextManualScenarioRQ);

  void deleteAllByTestCaseId(Long testCaseId);

  void deleteAllByTestCaseIds(List<Long> testCaseIds);

  void deleteAllByTestFolderId(Long projectId, Long folderId);

  void duplicateAttachments(TmsTextManualScenario originalTextScenario,
      TmsTextManualScenario duplicatedTextScenario);
}