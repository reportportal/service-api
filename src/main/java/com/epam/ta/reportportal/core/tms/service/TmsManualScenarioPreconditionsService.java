package com.epam.ta.reportportal.core.tms.service;

import com.epam.ta.reportportal.core.tms.db.entity.TmsManualScenario;
import com.epam.ta.reportportal.core.tms.dto.TmsManualScenarioPreconditionsRQ;
import java.util.List;

public interface TmsManualScenarioPreconditionsService {

  void createPreconditions(TmsManualScenario tmsManualScenario,
      TmsManualScenarioPreconditionsRQ preconditions);

  void updatePreconditions(TmsManualScenario manualScenario,
      TmsManualScenarioPreconditionsRQ preconditions);

  void patchPreconditions(TmsManualScenario existingManualScenario,
      TmsManualScenarioPreconditionsRQ preconditions);

  void deleteAllByTestCaseId(Long testCaseId);

  void deleteAllByTestCaseIds(List<Long> testCaseIds);

  void deleteAllByTestFolderId(Long projectId, Long folderId);

  void duplicatePreconditions(TmsManualScenario originalScenario,
      TmsManualScenario duplicatedScenario);
}
