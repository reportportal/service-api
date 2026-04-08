package com.epam.reportportal.base.core.tms.service;

import com.epam.reportportal.base.core.tms.dto.TmsTestCaseInTestPlanRS;
import com.epam.reportportal.base.core.tms.dto.TmsTestCaseRQ;
import com.epam.reportportal.base.core.tms.dto.TmsTestCaseRS;
import com.epam.reportportal.base.core.tms.dto.TmsTestFolderRS;
import com.epam.reportportal.base.core.tms.dto.batch.BatchDeleteTestCasesRQ;
import com.epam.reportportal.base.core.tms.dto.batch.BatchDuplicateTestCasesRQ;
import com.epam.reportportal.base.core.tms.dto.batch.BatchDuplicateTestCasesRS;
import com.epam.reportportal.base.core.tms.dto.batch.BatchPatchTestCaseAttributesRQ;
import com.epam.reportportal.base.core.tms.dto.batch.BatchPatchTestCasesRQ;
import com.epam.reportportal.base.core.tms.dto.batch.BatchPatchTestCasesRS;
import com.epam.reportportal.base.core.tms.dto.batch.BatchTestCaseOperationResultRS;
import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.Filter;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.MembershipDetails;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestCase;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestFolder;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.base.model.Page;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

public interface TmsTestCaseService {

  List<TmsTestCaseRS> getTestCaseByProjectId(long projectId);

  TmsTestCaseRS getById(long projectId, Long testCaseId);

  TmsTestCaseRS create(
      MembershipDetails membershipDetails,
      ReportPortalUser user,
      TmsTestCaseRQ tmsTestCaseRQ
  );

  TmsTestCaseRS update(MembershipDetails membershipDetails,
      ReportPortalUser user, Long testCaseId, TmsTestCaseRQ tmsTestCaseRQ);

  TmsTestCaseRS patch(MembershipDetails membershipDetails,
      ReportPortalUser user, Long testCaseId, TmsTestCaseRQ tmsTestCaseRQ);

  void delete(MembershipDetails membershipDetails,
      ReportPortalUser user, Long testCaseId);

  void deleteByTestFolderId(MembershipDetails membershipDetails,
      ReportPortalUser user, long folderId);

  void delete(MembershipDetails membershipDetails,
      ReportPortalUser user, @Valid BatchDeleteTestCasesRQ deleteRequest);

  BatchPatchTestCasesRS patch(long projectId, @Valid BatchPatchTestCasesRQ patchRequest);

  List<TmsTestFolderRS> importFromFile(
      long projectId,
      Long testFolderId,
      String testFolderName,
      MultipartFile file
  );

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
   * @param patchRequest Patch request.
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
  BatchDuplicateTestCasesRS duplicate(long projectId, BatchDuplicateTestCasesRQ duplicateRequest);

  BatchTestCaseOperationResultRS duplicateTestCases(long projectId, List<Long> originalTestCaseIds);

  BatchTestCaseOperationResultRS duplicateTestCases(long projectId, TmsTestFolder targetFolder,
      List<Long> originalTestCaseIds);

  /**
   * Retrieves test cases added to a test plan with pagination. Returns test cases with last
   * execution only (without full execution history).
   *
   * @param projectId    the project ID
   * @param testPlanId   the test plan ID
   * @param testFolderId test folder ID
   * @param filter       the filter parameters
   * @param pageable     pagination parameters
   * @return page of test cases added to the test plan
   */
  Page<TmsTestCaseInTestPlanRS> getTestCasesInTestPlan(Long projectId, Long testPlanId,
      Filter filter, Pageable pageable);

  /**
   * Retrieves a single test case in test plan with full execution history.
   *
   * @param projectId  the project ID
   * @param testPlanId the test plan ID
   * @param testCaseId the test case ID
   * @return test case with last execution and all executions
   */
  TmsTestCaseInTestPlanRS getTestCaseInTestPlan(Long projectId, Long testPlanId, Long testCaseId);

  /**
   * Gets test case entity by ID (for internal service usage).
   *
   * @param testCaseId test case ID
   * @return test case entity
   * @throws ReportPortalException if test case not found
   */
  TmsTestCase getEntityById(Long testCaseId);

  List<TmsTestCaseRS> getByIds(long projectId, List<Long> testCaseIds);

  List<Long> getTestCaseIdsInTestPlan(long projectId, Long testPlanId);
}
