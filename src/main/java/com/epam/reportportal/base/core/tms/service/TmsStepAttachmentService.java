package com.epam.reportportal.base.core.tms.service;

import com.epam.reportportal.base.core.tms.dto.TmsStepRQ;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsStep;
import java.util.Collection;
import java.util.List;

public interface TmsStepAttachmentService {

  void createAttachments(Long projectId, TmsStep tmsStep, TmsStepRQ stepRQ);

  void deleteAllBySteps(Collection<TmsStep> steps);

  void deleteAllByTestCaseId(Long testCaseId);

  void deleteAllByTestCaseIds(List<Long> testCaseIds);

  void deleteStepsByTestFolderId(Long projectId, Long folderId);

  void duplicateAttachments(TmsStep originalStep, TmsStep duplicatedStep);
}
