package com.epam.ta.reportportal.core.tms.service;

import com.epam.ta.reportportal.core.tms.db.entity.TmsStep;
import com.epam.ta.reportportal.core.tms.dto.TmsStepRQ;
import java.util.Collection;
import java.util.List;

public interface TmsStepAttachmentService {

  void createAttachments(TmsStep tmsStep, TmsStepRQ stepRQ);

  void deleteAllBySteps(Collection<TmsStep> steps);

  void deleteAllByTestCaseId(Long testCaseId);

  void deleteAllByTestCaseIds(List<Long> testCaseIds);

  void deleteStepsByTestFolderId(Long projectId, Long folderId);

  void duplicateAttachments(TmsStep originalStep, TmsStep duplicatedStep);
}
