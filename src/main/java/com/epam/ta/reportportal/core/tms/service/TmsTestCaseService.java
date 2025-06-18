package com.epam.ta.reportportal.core.tms.service;

import com.epam.ta.reportportal.core.tms.dto.TmsTestCaseRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsTestCaseRS;
import com.epam.ta.reportportal.core.tms.dto.batch.BatchDeleteTestCasesRQ;
import com.epam.ta.reportportal.core.tms.dto.batch.BatchPatchTestCasesRQ;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public interface TmsTestCaseService extends CrudService<TmsTestCaseRQ, TmsTestCaseRS, Long> {

  List<TmsTestCaseRS> getTestCaseByProjectId(long projectId);

  void deleteByTestFolderId(long projectId, long folderId);

  void delete(long projectId, @Valid BatchDeleteTestCasesRQ deleteRequest);

  void patch(long projectId, @Valid BatchPatchTestCasesRQ patchRequest);
}
