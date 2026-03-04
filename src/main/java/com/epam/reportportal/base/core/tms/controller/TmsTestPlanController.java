package com.epam.reportportal.base.core.tms.controller;

import com.epam.reportportal.base.core.tms.dto.DuplicateTmsTestPlanRS;
import com.epam.reportportal.base.core.tms.dto.TmsTestCaseInTestPlanRS;
import com.epam.reportportal.base.core.tms.dto.TmsTestFolderRS;
import com.epam.reportportal.base.core.tms.dto.TmsTestPlanRQ;
import com.epam.reportportal.base.core.tms.dto.TmsTestPlanRS;
import com.epam.reportportal.base.core.tms.dto.batch.BatchAddTestCasesToPlanRQ;
import com.epam.reportportal.base.core.tms.dto.batch.BatchTestCaseOperationResultRS;
import com.epam.reportportal.base.core.tms.dto.batch.BatchRemoveTestCasesFromPlanRQ;
import com.epam.reportportal.base.core.tms.service.TmsTestPlanService;
import com.epam.reportportal.base.infrastructure.persistence.commons.EntityUtils;
import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.Filter;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestCase;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestFolder;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestPlan;
import com.epam.reportportal.base.model.Page;
import com.epam.reportportal.base.util.OffsetRequest;
import com.epam.reportportal.base.util.ProjectExtractor;
import com.epam.reportportal.base.ws.resolver.FilterFor;
import com.epam.reportportal.base.ws.resolver.PagingOffset;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for managing test plans within a project. Provides endpoints
 * for creating, retrieving, updating, patching, and deleting test plans.
 * All operations require administrator privileges.
 */
@RestController
@RequestMapping("/v1/project/{projectKey}/tms/test-plan")
@Tag(name = "Test Plan", description = "Test Plan API collection")
@RequiredArgsConstructor
public class TmsTestPlanController {

  private final TmsTestPlanService tmsTestPlanService;
  private final ProjectExtractor projectExtractor;

  /**
   * Creates a new test plan.
   *
   * @param projectKey The key of the project.
   * @param testPlan  The test plan data ({@link TmsTestPlanRQ}) to be created.
   * @return The details of the created test plan ({@link TmsTestPlanRS}).
   */
  @PostMapping
  public TmsTestPlanRS createTestPlan(@PathVariable String projectKey,
      @RequestBody TmsTestPlanRQ testPlan,
      @AuthenticationPrincipal ReportPortalUser user) {
    return tmsTestPlanService.create(
        projectExtractor
            .extractMembershipDetails(user, EntityUtils.normalizeId(projectKey))
            .getProjectId(),
        testPlan);
  }

  /**
   * Retrieves a list of test plans filtered by criteria.
   *
   * @param projectKey      The key of the project.
   * @param filter          Filter (optional).
   * @param offsetRequest   Pagination details.
   * @return Paginated list of test plans matching the criteria ({@link TmsTestPlanRS}).
   */
  @GetMapping
  public Page<TmsTestPlanRS> getTestPlansByCriteria(
      @PathVariable String projectKey,
      @FilterFor(TmsTestPlan.class) Filter filter,
      @PagingOffset(sortable = TmsTestPlan.class) OffsetRequest offsetRequest,
      @AuthenticationPrincipal ReportPortalUser user) {
    return tmsTestPlanService.getByCriteria(
        projectExtractor
            .extractMembershipDetails(user, EntityUtils.normalizeId(projectKey))
            .getProjectId(),
        filter,
        offsetRequest
    );
  }

  /**
   * Updates an existing test plan.
   *
   * @param projectKey The key of the project.
   * @param testPlanId The ID of the test plan to update.
   * @param testPlan   Updated test plan data ({@link TmsTestPlanRQ}).
   * @return Updated test plan details ({@link TmsTestPlanRS}).
   */
  @PutMapping("/{id}")
  @Deprecated
  public TmsTestPlanRS updateTestPlan(@PathVariable String projectKey,
      @PathVariable("id") Long testPlanId,
      @RequestBody TmsTestPlanRQ testPlan,
      @AuthenticationPrincipal ReportPortalUser user) {
    return tmsTestPlanService.update(
        projectExtractor
            .extractMembershipDetails(user, EntityUtils.normalizeId(projectKey))
            .getProjectId(),
        testPlanId,
        testPlan);
  }

  /**
   * Retrieves a specific test plan by its ID.
   *
   * @param projectKey The key of the project.
   * @param testPlanId The ID of the test plan to retrieve.
   * @return The test plan details ({@link TmsTestPlanRS}).
   */
  @GetMapping("/{id}")
  public TmsTestPlanRS getTestPlanById(@PathVariable String projectKey,
      @PathVariable("id") Long testPlanId,
      @AuthenticationPrincipal ReportPortalUser user) {
    return tmsTestPlanService.getById(
        projectExtractor
            .extractMembershipDetails(user, EntityUtils.normalizeId(projectKey))
            .getProjectId(),
        testPlanId);
  }

  /**
   * Deletes a test plan from the specified project.
   *
   * @param projectKey The key of the project.
   * @param testPlanId The ID of the test plan to delete.
   */
  @DeleteMapping("/{id}")
  public void deleteTestPlan(@PathVariable String projectKey,
      @PathVariable("id") Long testPlanId,
      @AuthenticationPrincipal ReportPortalUser user) {
    tmsTestPlanService.delete(
        projectExtractor
            .extractMembershipDetails(user, EntityUtils.normalizeId(projectKey))
            .getProjectId(),
        testPlanId);
  }

  /**
   * Applies partial updates to an existing test plan.
   *
   * @param projectKey The key of the project.
   * @param testPlanId The ID of the test plan to patch.
   * @param updatedTestPlan The patch data ({@link TmsTestPlanRQ}).
   * @return The updated test plan details ({@link TmsTestPlanRS}).
   */
  @PatchMapping("/{id}")
  public TmsTestPlanRS patchTestPlan(@PathVariable String projectKey,
      @PathVariable("id") Long testPlanId,
      @RequestBody TmsTestPlanRQ updatedTestPlan,
      @AuthenticationPrincipal ReportPortalUser user) {
    return tmsTestPlanService.patch(
        projectExtractor
            .extractMembershipDetails(user, EntityUtils.normalizeId(projectKey))
            .getProjectId(),
        testPlanId,
        updatedTestPlan);
  }

  /**
   * Adds multiple test cases to a test plan.
   *
   * @param projectKey The key of the project.
   * @param testPlanId The ID of the test plan to add test cases to.
   * @param addRequest Request containing test case IDs to add to the test plan.
   */
  @PostMapping("/{id}/test-case/batch")
  @Operation(
      summary = "Add test cases to test plan",
      description = "Adds multiple test cases to a test plan by their IDs.",
      tags = {"Batch Operations"}
  )
  @ApiResponse(responseCode = "204", description = "Test cases added to test plan successfully")
  public BatchTestCaseOperationResultRS addTestCasesToPlan(@PathVariable String projectKey,
      @PathVariable("id") Long testPlanId,
      @Valid @RequestBody BatchAddTestCasesToPlanRQ addRequest,
      @AuthenticationPrincipal ReportPortalUser user) {
    return tmsTestPlanService.addTestCasesToPlan(
        projectExtractor
            .extractMembershipDetails(user, EntityUtils.normalizeId(projectKey))
            .getProjectId(),
        testPlanId,
        addRequest.getTestCaseIds());
  }

  /**
   * Removes multiple test cases from a test plan.
   *
   * @param projectKey The key of the project.
   * @param testPlanId The ID of the test plan to remove test cases from.
   * @param removeRequest Request containing test case IDs to remove from the test plan.
   */
  @DeleteMapping("/{id}/test-case/batch")
  @Operation(
      summary = "Remove test cases from test plan",
      description = "Removes multiple test cases from a test plan by their IDs.",
      tags = {"Batch Operations"}
  )
  @ApiResponse(responseCode = "204", description = "Test cases removed from test plan successfully")
  public BatchTestCaseOperationResultRS removeTestCasesFromPlan(@PathVariable String projectKey,
      @PathVariable("id") Long testPlanId,
      @Valid @RequestBody BatchRemoveTestCasesFromPlanRQ removeRequest,
      @AuthenticationPrincipal ReportPortalUser user) {
    return tmsTestPlanService.removeTestCasesFromPlan(
        projectExtractor
            .extractMembershipDetails(user, EntityUtils.normalizeId(projectKey))
            .getProjectId(),
        testPlanId,
        removeRequest.getTestCaseIds());
  }

  /**
   * Retrieves test cases added to a test plan with pagination.
   * Returns test cases with only the last execution.
   *
   * @param projectKey    the project key
   * @param testPlanId    the test plan ID
   * @param offsetRequest pagination details
   * @param user          authenticated user
   * @return paginated list of test cases in the test plan
   */
  @GetMapping("/{testPlanId}/test-case")
  @Operation(
      summary = "Get test cases added to test plan",
      description = "Get test cases added to a test plan by its ID with pagination. "
          + "Returns test cases with only the last execution."
  )
  @ApiResponse(responseCode = "200", description = "Test cases added to test plan")
  public Page<TmsTestCaseInTestPlanRS> getTestCasesAddedToPlan(
      @PathVariable String projectKey,
      @PathVariable("testPlanId") Long testPlanId,
      @RequestParam(value = "testFolderId", required = false) Long testFolderId,
      @PagingOffset(sortable = TmsTestCase.class) OffsetRequest offsetRequest,
      @AuthenticationPrincipal ReportPortalUser user) {
    return tmsTestPlanService.getTestCasesAddedToPlan(
        projectExtractor
            .extractMembershipDetails(user, EntityUtils.normalizeId(projectKey))
            .getProjectId(),
        testPlanId,
        testFolderId,
        offsetRequest
    );
  }

  /**
   * Retrieves a single test case in test plan with full execution history.
   *
   * @param projectKey the project key
   * @param testPlanId the test plan ID
   * @param testCaseId the test case ID
   * @param user       authenticated user
   * @return test case with last execution and all executions
   */
  @GetMapping("/{testPlanId}/test-case/{id}")
  @Operation(
      summary = "Get test case in test plan",
      description = "Get a specific test case in test plan by test case ID. "
          + "Returns test case with both last execution and all executions."
  )
  @ApiResponse(responseCode = "200", description = "Test case in test plan")
  public TmsTestCaseInTestPlanRS getTestCaseInTestPlan(
      @PathVariable String projectKey,
      @PathVariable("testPlanId") Long testPlanId,
      @PathVariable("id") Long testCaseId,
      @AuthenticationPrincipal ReportPortalUser user) {
    return tmsTestPlanService.getTestCaseInTestPlan(
        projectExtractor
            .extractMembershipDetails(user, EntityUtils.normalizeId(projectKey))
            .getProjectId(),
        testPlanId,
        testCaseId
    );
  }

  @GetMapping("/{id}/folder")
  @Operation(
      summary = "Get folders where test cases added to test plan",
      description = "Gets folders where test cases added to a test plan by test plan ID."
  )
  @ApiResponse(responseCode = "200", description = "Test folders from test plan")
  public Page<TmsTestFolderRS> getTestFoldersFromPlan(@PathVariable String projectKey,
      @PathVariable("id") Long testPlanId,
      @PagingOffset(sortable = TmsTestFolder.class) OffsetRequest offsetRequest,
      @AuthenticationPrincipal ReportPortalUser user) {
    return tmsTestPlanService.getTestFoldersFromPlan(
        projectExtractor
            .extractMembershipDetails(user, EntityUtils.normalizeId(projectKey))
            .getProjectId(),
        testPlanId,
        offsetRequest
    );
  }

  @PostMapping("/{id}/duplicate")
  @Operation(
      summary = "Duplicate test plan",
      description = "Duplicates test plan by test plan ID."
  )
  @ApiResponse(responseCode = "200", description = "Duplicated test plan")
  public DuplicateTmsTestPlanRS duplicateTestPlan(@PathVariable String projectKey,
      @PathVariable("id") Long testPlanId,
      @RequestBody TmsTestPlanRQ duplicateTestPlanRQ,
      @AuthenticationPrincipal ReportPortalUser user) {
    return tmsTestPlanService.duplicate(
        projectExtractor
            .extractMembershipDetails(user, EntityUtils.normalizeId(projectKey))
            .getProjectId(),
        testPlanId,
        duplicateTestPlanRQ);
  }
}
