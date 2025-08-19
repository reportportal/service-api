package com.epam.ta.reportportal.core.tms.service;

import com.epam.ta.reportportal.core.tms.dto.TmsTestCaseRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsTestCaseRS;
import com.epam.ta.reportportal.core.tms.dto.batch.BatchDeleteTestCasesRQ;
import com.epam.ta.reportportal.core.tms.dto.batch.BatchPatchTestCasesRQ;
import com.epam.ta.reportportal.model.Page;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface TmsTestCaseService extends CrudService<TmsTestCaseRQ, TmsTestCaseRS, Long> {

  List<TmsTestCaseRS> getTestCaseByProjectId(long projectId);

  void deleteByTestFolderId(long projectId, long folderId);

  void delete(long projectId, @Valid BatchDeleteTestCasesRQ deleteRequest);

  void patch(long projectId, @Valid BatchPatchTestCasesRQ patchRequest);

  List<TmsTestCaseRS> importFromFile(long projectId, MultipartFile file);

  void exportToFile(Long projectId, List<Long> ids, String format, boolean includeAttachments,
      HttpServletResponse response);

  Page<TmsTestCaseRS> getTestCasesByCriteria(long projectId, String search, Long testFolderId, Pageable pageable);

  /**
   * Deletes specific tags from a test case.
   *
   * @param projectId The project ID.
   * @param testCaseId The test case ID.
   * @param attributeIds List of attribute IDs to delete.
   */
  void deleteTagsFromTestCase(Long projectId, Long testCaseId, List<Long> attributeIds);

  /**
   * Deletes specific tags from multiple test cases.
   *
   * @param projectId The project ID.
   * @param testCaseIds List of test case IDs.
   * @param attributeIds List of attribute IDs to delete.
   */
  void deleteTagsFromTestCases(Long projectId, List<Long> testCaseIds, List<Long> attributeIds);
}
