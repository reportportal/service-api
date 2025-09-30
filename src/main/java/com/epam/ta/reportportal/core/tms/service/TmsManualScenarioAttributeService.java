package com.epam.ta.reportportal.core.tms.service;

import com.epam.ta.reportportal.entity.tms.TmsManualScenario;
import com.epam.ta.reportportal.core.tms.dto.TmsManualScenarioAttributeRQ;
import java.util.List;

public interface TmsManualScenarioAttributeService {

  void createAttributes(TmsManualScenario tmsManualScenario,
      List<TmsManualScenarioAttributeRQ> attributes);

  void updateAttributes(TmsManualScenario tmsManualScenario,
      List<TmsManualScenarioAttributeRQ> attributes);

  void patchAttributes(TmsManualScenario tmsManualScenario,
      List<TmsManualScenarioAttributeRQ> attributes);

  void deleteAllByTestCaseId(Long testCaseId);

  void deleteAllByTestCaseIds(List<Long> testCaseIds);

  void deleteAllByTestFolderId(Long projectId, Long folderId);

  /**
   * Duplicates attributes from original scenario to new scenario. Uses existing TmsAttribute
   * entities but creates new TmsManualScenarioAttribute associations.
   *
   * @param originalScenario The original scenario with attributes to duplicate.
   * @param newScenario      The new scenario to attach duplicated attributes to.
   */
  void duplicateAttributes(TmsManualScenario originalScenario, TmsManualScenario newScenario);
}
