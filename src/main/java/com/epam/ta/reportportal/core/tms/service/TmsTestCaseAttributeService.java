package com.epam.ta.reportportal.core.tms.service;

import com.epam.ta.reportportal.core.tms.db.entity.TmsTestCase;
import com.epam.ta.reportportal.core.tms.dto.TmsAttributeRQ;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public interface TmsTestCaseAttributeService {

  void createTestCaseAttributes(@NotNull TmsTestCase tmsTestCase, @NotEmpty List<TmsAttributeRQ> attributes);

  void updateTestCaseAttributes(@NotNull TmsTestCase tmsTestCase, List<TmsAttributeRQ> attributes);

  void patchTestCaseAttributes(@NotNull TmsTestCase tmsTestCase, List<TmsAttributeRQ> attributes);

  void patchTestCaseAttributes(@NotNull @NotEmpty List<TmsTestCase> testCaseIds, List<TmsAttributeRQ> attributes);

  void deleteAllByTestCaseId(@NotNull Long testCaseId);

  void deleteAllByTestFolderId(@NotNull Long projectId, @NotNull Long testFolderId);

  void deleteAllByTestCaseIds(@NotNull @NotEmpty List<Long> testCaseIds);
}
