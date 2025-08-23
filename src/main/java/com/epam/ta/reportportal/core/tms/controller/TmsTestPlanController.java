package com.epam.ta.reportportal.core.tms.controller;

import com.epam.ta.reportportal.commons.EntityUtils;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.tms.dto.TmsTestPlanRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsTestPlanRS;
import com.epam.ta.reportportal.core.tms.dto.batch.BatchAddTestCasesToPlanRQ;
import com.epam.ta.reportportal.core.tms.dto.batch.BatchRemoveTestCasesFromPlanRQ;
import com.epam.ta.reportportal.core.tms.service.TmsTestPlanService;
import com.epam.ta.reportportal.util.ProjectExtractor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
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
   * @param projectKey         The key of the project.
   * @param pageable           Pagination details.
   * @return Paginated list of test plans matching the criteria ({@link TmsTestPlanRS}).
   */
  @GetMapping
  public Page<TmsTestPlanRS> getTestPlansByCriteria(
      @PathVariable String projectKey,
      Pageable pageable,
      @AuthenticationPrincipal ReportPortalUser user) {
    return tmsTestPlanService.getByCriteria(
        projectExtractor
            .extractMembershipDetails(user, EntityUtils.normalizeId(projectKey))
            .getProjectId(),
        pageable
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
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(
      summary = "Add test cases to test plan",
      description = "Adds multiple test cases to a test plan by their IDs.",
      tags = {"Batch Operations"}
  )
  @ApiResponse(responseCode = "204", description = "Test cases added to test plan successfully")
  public void addTestCasesToPlan(@PathVariable String projectKey,
      @PathVariable("id") Long testPlanId,
      @Valid @RequestBody BatchAddTestCasesToPlanRQ addRequest,
      @AuthenticationPrincipal ReportPortalUser user) {
    tmsTestPlanService.addTestCasesToPlan(
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
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(
      summary = "Remove test cases from test plan",
      description = "Removes multiple test cases from a test plan by their IDs.",
      tags = {"Batch Operations"}
  )
  @ApiResponse(responseCode = "204", description = "Test cases removed from test plan successfully")
  public void removeTestCasesFromPlan(@PathVariable String projectKey,
      @PathVariable("id") Long testPlanId,
      @Valid @RequestBody BatchRemoveTestCasesFromPlanRQ removeRequest,
      @AuthenticationPrincipal ReportPortalUser user) {
    tmsTestPlanService.removeTestCasesFromPlan(
        projectExtractor
            .extractMembershipDetails(user, EntityUtils.normalizeId(projectKey))
            .getProjectId(),
        testPlanId,
        removeRequest.getTestCaseIds());
  }
}
