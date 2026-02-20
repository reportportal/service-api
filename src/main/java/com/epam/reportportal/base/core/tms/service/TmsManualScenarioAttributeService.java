package com.epam.reportportal.base.core.tms.service;

import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsManualScenario;
import com.epam.reportportal.base.core.tms.dto.TmsManualScenarioAttributeRQ;
import java.util.List;

public interface TmsManualScenarioAttributeService {

  void createAttributes(long projectId, TmsManualScenario tmsManualScenario,
      List<TmsManualScenarioAttributeRQ> attributes);

  void updateAttributes(long projectId, TmsManualScenario tmsManualScenario,
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