package com.epam.ta.reportportal.core.tms.service;

import com.epam.ta.reportportal.core.tms.db.entity.TmsManualScenario;
import com.epam.ta.reportportal.core.tms.dto.TmsManualScenarioAttributeRQ;
import java.util.List;

public interface TmsManualScenarioAttributeService {

  void createAttributes(TmsManualScenario tmsManualScenario, List<TmsManualScenarioAttributeRQ> attributes);

  void updateAttributes(TmsManualScenario tmsManualScenario, List<TmsManualScenarioAttributeRQ> attributes);

  void patchAttributes(TmsManualScenario tmsManualScenario, List<TmsManualScenarioAttributeRQ> attributes);

  void deleteAllByTestCaseId(Long testCaseId);

  void deleteAllByTestCaseIds(List<Long> testCaseIds);

  void deleteAllByTestFolderId(Long projectId, Long folderId);
}
