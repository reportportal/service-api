package com.epam.ta.reportportal.core.tms.service;

import com.epam.ta.reportportal.core.tms.db.entity.TmsManualScenarioPreconditions;
import com.epam.ta.reportportal.core.tms.dto.TmsManualScenarioPreconditionsRQ;
import java.util.List;

public interface TmsManualScenarioPreconditionsAttachmentService {

  void createAttachments(TmsManualScenarioPreconditions preconditions,
      TmsManualScenarioPreconditionsRQ preconditionsRQ);

  void updateAttachments(TmsManualScenarioPreconditions existingPreconditions,
      TmsManualScenarioPreconditionsRQ tmsManualScenarioPreconditionsRQ);

  void patchAttachments(TmsManualScenarioPreconditions existingPreconditions,
      TmsManualScenarioPreconditionsRQ preconditionsRQ);

  void deleteAllByTestCaseId(Long testCaseId);

  void deleteAllByTestCaseIds(List<Long> testCaseIds);

  void deleteAllByTestFolderId(Long projectId, Long folderId);

  void duplicateAttachments(TmsManualScenarioPreconditions originalPreconditions,
      TmsManualScenarioPreconditions duplicatedPreconditions);
}
