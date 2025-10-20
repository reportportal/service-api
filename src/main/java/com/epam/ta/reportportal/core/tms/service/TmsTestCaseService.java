package com.epam.ta.reportportal.core.tms.service;

import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.core.tms.dto.TmsTestCaseRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsTestCaseRS;
import com.epam.ta.reportportal.core.tms.dto.batch.BatchDeleteTestCasesRQ;
import com.epam.ta.reportportal.core.tms.dto.batch.BatchDuplicateTestCasesRQ;
import com.epam.ta.reportportal.core.tms.dto.batch.BatchTestCaseOperationResultRS;
import com.epam.ta.reportportal.core.tms.dto.batch.BatchPatchTestCaseAttributesRQ;
import com.epam.ta.reportportal.core.tms.dto.batch.BatchPatchTestCasesRQ;
import com.epam.ta.reportportal.model.Page;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

public interface TmsTestCaseService extends CrudService<TmsTestCaseRQ, TmsTestCaseRS, Long> {

  List<TmsTestCaseRS> getTestCaseByProjectId(long projectId);

  void deleteByTestFolderId(long projectId, long folderId);

  void delete(long projectId, @Valid BatchDeleteTestCasesRQ deleteRequest);

  void patch(long projectId, @Valid BatchPatchTestCasesRQ patchRequest);

  List<TmsTestCaseRS> importFromFile(long projectId, Long testFolderId, String testFolderName,
      MultipartFile file);

  void exportToFile(Long projectId, List<Long> ids, String format, boolean includeAttachments,
      HttpServletResponse response);

  Page<TmsTestCaseRS> getTestCasesByCriteria(long projectId, Filter filter, Pageable pageable);

  /**
   * Deletes specific attributes from a test case.
   *
   * @param projectId    The project ID.
   * @param testCaseId   The test case ID.
   * @param attributeIds List of attribute IDs to delete.
   */
  void deleteAttributesFromTestCase(Long projectId, Long testCaseId, List<Long> attributeIds);

  /**
   * Patch specific attributes from multiple test cases.
   *
   * @param projectId    The project ID.
   * @param patchRequest  Patch request.
   */
  void patchTestCaseAttributes(Long projectId, BatchPatchTestCaseAttributesRQ patchRequest);

  @Transactional(readOnly = true)
  boolean existsById(Long projectId, Long testCaseId);

  @Transactional(readOnly = true)
  List<Long> getExistingTestCaseIds(Long projectId, List<Long> testCaseIds);

  /**
   * Verifies if test cases exist by provided ids in the project
   *
   * @param projectId   project id associated with a test cases
   * @param testCaseIds target test case ids to be existed
   */
  void validateTestCasesExist(Long projectId, @NotEmpty List<Long> testCaseIds);

  /**
   * Duplicates multiple test cases with all their related data.
   *
   * @param projectId        The ID of the project.
   * @param duplicateRequest Request containing test case IDs to duplicate.
   * @return A list of data transfer objects containing details of the duplicated test cases.
   */
  List<TmsTestCaseRS> duplicate(long projectId, BatchDuplicateTestCasesRQ duplicateRequest);

  BatchTestCaseOperationResultRS duplicateTestCases(long projectId, List<Long> originalTestCaseIds);
}
