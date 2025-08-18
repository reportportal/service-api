package com.epam.ta.reportportal.core.tms.service;

import com.epam.ta.reportportal.core.tms.db.entity.TmsManualScenario;
import com.epam.ta.reportportal.core.tms.dto.TmsAttributeRQ;
import java.util.List;

public interface TmsManualScenarioAttributeService {

  void createAttributes(TmsManualScenario tmsManualScenario, List<TmsAttributeRQ> attributes);

  void updateAttributes(TmsManualScenario tmsManualScenario, List<TmsAttributeRQ> attributes);

  void patchAttributes(TmsManualScenario tmsManualScenario, List<TmsAttributeRQ> attributes);

  void deleteAllByTestCaseId(Long testCaseId);

  void deleteAllByTestCaseIds(List<Long> testCaseIds);

  void deleteAllByTestFolderId(Long projectId, Long folderId);
}
