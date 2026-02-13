package com.epam.reportportal.base.core.tms.service;

import com.epam.reportportal.base.core.tms.dto.TmsTestCaseAttributeRQ;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestCase;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.Collection;
import java.util.List;

public interface TmsTestCaseAttributeService {

  void createTestCaseAttributes(long projectId, @NotNull TmsTestCase tmsTestCase,
      @NotEmpty List<TmsTestCaseAttributeRQ> attributes);

  void updateTestCaseAttributes(long projectId, @NotNull TmsTestCase tmsTestCase,
      List<TmsTestCaseAttributeRQ> attributes);

  void patchTestCaseAttributes(long projectId, @NotNull TmsTestCase tmsTestCase,
      List<TmsTestCaseAttributeRQ> attributes);

  void patchTestCaseAttributes(@NotNull @NotEmpty List<TmsTestCase> testCaseIds,
      List<TmsTestCaseAttributeRQ> attributes);

  void deleteAllByTestCaseId(@NotNull Long testCaseId);

  void deleteAllByTestFolderId(@NotNull Long projectId, @NotNull Long testFolderId);

  void deleteAllByTestCaseIds(@NotNull @NotEmpty List<Long> testCaseIds);

  void deleteByTestCaseIdAndAttributeIds(Long testCaseId, List<Long> attributeIds);

  void deleteByTestCaseIdsAndAttributeIds(List<Long> testCaseIds, Collection<Long> attributeIds);

  /**
   * Duplicates test case attributes by linking existing attributes to a new test case.
   *
   * @param originalTestCase The original test case.
   * @param newTestCase      The new test case.
   */
  void duplicateTestCaseAttributes(TmsTestCase originalTestCase, TmsTestCase newTestCase);

  void addAttributesToTestCases(@NotNull @NotEmpty List<Long> testCaseIds,
      @NotNull @NotEmpty Collection<Long> attributeIds);
}
