package com.epam.reportportal.base.core.tms.service;

import com.epam.reportportal.base.core.tms.dto.TmsManualScenarioPreconditionsRQ;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsManualScenarioPreconditions;
import java.util.List;

public interface TmsManualScenarioPreconditionsAttachmentService {

  /**
   * Creates attachments for manual scenario preconditions.
   */
  void createAttachments(Long projectId, TmsManualScenarioPreconditions preconditions,
      TmsManualScenarioPreconditionsRQ preconditionsRq);

  /**
   * Updates attachments for manual scenario preconditions (replaces all existing).
   */
  void updateAttachments(Long projectId, TmsManualScenarioPreconditions existingPreconditions,
      TmsManualScenarioPreconditionsRQ tmsManualScenarioPreconditionsRq);

  void deleteAllByTestCaseId(Long testCaseId);

  void deleteAllByTestCaseIds(List<Long> testCaseIds);

  void deleteAllByTestFolderId(Long projectId, Long folderId);

  void duplicateAttachments(TmsManualScenarioPreconditions originalPreconditions,
      TmsManualScenarioPreconditions duplicatedPreconditions);
}
