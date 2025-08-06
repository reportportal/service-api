package com.epam.ta.reportportal.core.tms.service;

import com.epam.ta.reportportal.core.tms.db.entity.TmsManualScenario;
import com.epam.ta.reportportal.core.tms.dto.TmsManualScenarioRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsManualScenarioType;
import java.util.List;

public interface TmsManualScenarioImplService {

  TmsManualScenarioType getTmsManualScenarioType();

  void createTmsManualScenarioImpl(TmsManualScenario tmsManualScenario,
      TmsManualScenarioRQ testCaseManualScenarioRQ);

  void updateTmsManualScenarioImpl(TmsManualScenario manualScenario,
      TmsManualScenarioRQ testCaseManualScenarioRQ);

  void patchTmsManualScenarioImpl(TmsManualScenario manualScenario,
      TmsManualScenarioRQ testCaseManualScenarioRQ);

  void deleteAllByTestCaseId(Long testCaseId);

  void deleteAllByTestCaseIds(List<Long> testCaseIds);

  void deleteAllByTestFolderId(Long projectId, Long folderId);
}
