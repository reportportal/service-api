package com.epam.ta.reportportal.core.tms.controller;

import static com.epam.ta.reportportal.auth.permissions.Permissions.IS_ADMIN;

import com.epam.ta.reportportal.core.tms.dto.TmsTestPlanRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsTestPlanRS;
import com.epam.ta.reportportal.core.tms.service.TmsTestPlanService;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
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
@RequestMapping("/project/{projectId}/tms/test-plan")
@Tag(name = "Test Plan", description = "Test Plan API collection")
@RequiredArgsConstructor
public class TmsTestPlanController {

  private final TmsTestPlanService tmsTestPlanService;

  /**
   * Creates a new test plan.
   *
   * @param projectId The ID of the project.
   * @param testPlan  The test plan data ({@link TmsTestPlanRQ}) to be created.
   * @return The details of the created test plan ({@link TmsTestPlanRS}).
   */
  @PostMapping
  @PreAuthorize(IS_ADMIN)
  public TmsTestPlanRS createTestPlan(@PathVariable Long projectId,
      @RequestBody TmsTestPlanRQ testPlan) {
    return tmsTestPlanService.create(projectId, testPlan);
  }

  /**
   * Retrieves a list of test plans filtered by criteria.
   *
   * @param projectId          The ID of the project.
   * @param environmentId      List of environment IDs to filter by.
   * @param productVersionId   List of product version IDs to filter by.
   * @param pageable           Pagination details.
   * @return Paginated list of test plans matching the criteria ({@link TmsTestPlanRS}).
   */
  @GetMapping
  @PreAuthorize(IS_ADMIN)
  public Page<TmsTestPlanRS> getTestPlansByCriteria(
      @PathVariable Long projectId,
      @RequestParam(required = false) List<Long> environmentId,
      @RequestParam(required = false) List<Long> productVersionId,
      Pageable pageable) {
    return tmsTestPlanService.getByCriteria(
        projectId, environmentId, productVersionId, pageable
    );
  }

  /**
   * Updates an existing test plan.
   *
   * @param projectId  The ID of the project.
   * @param testPlanId The ID of the test plan to update.
   * @param testPlan   Updated test plan data ({@link TmsTestPlanRQ}).
   * @return Updated test plan details ({@link TmsTestPlanRS}).
   */
  @PutMapping("/{id}")
  @PreAuthorize(IS_ADMIN)
  public TmsTestPlanRS updateTestPlan(@PathVariable Long projectId,
      @PathVariable("id") Long testPlanId,
      @RequestBody TmsTestPlanRQ testPlan) {
    return tmsTestPlanService.update(projectId, testPlanId, testPlan);
  }

  /**
   * Retrieves a specific test plan by its ID.
   *
   * @param projectId  The ID of the project.
   * @param testPlanId The ID of the test plan to retrieve.
   * @return The test plan details ({@link TmsTestPlanRS}).
   */
  @GetMapping("/{id}")
  @PreAuthorize(IS_ADMIN)
  public TmsTestPlanRS getTestPlanById(@PathVariable Long projectId,
      @PathVariable("id") Long testPlanId) {
    return tmsTestPlanService.getById(projectId, testPlanId);
  }

  /**
   * Deletes a test plan from the specified project.
   *
   * @param projectId  The ID of the project.
   * @param testPlanId The ID of the test plan to delete.
   */
  @DeleteMapping("/{id}")
  @PreAuthorize(IS_ADMIN)
  public void deleteTestPlan(@PathVariable Long projectId, @PathVariable("id") Long testPlanId) {
    tmsTestPlanService.delete(projectId, testPlanId);
  }

  /**
   * Applies partial updates to an existing test plan.
   *
   * @param projectId  The ID of the project.
   * @param testPlanId The ID of the test plan to patch.
   * @param updatedTestPlan The patch data ({@link TmsTestPlanRQ}).
   * @return The updated test plan details ({@link TmsTestPlanRS}).
   */
  @PatchMapping("/{id}")
  @PreAuthorize(IS_ADMIN)
  public TmsTestPlanRS patchTestPlan(@PathVariable Long projectId,
      @PathVariable("id") Long testPlanId,
      @RequestBody TmsTestPlanRQ updatedTestPlan) {
    return tmsTestPlanService.patch(projectId, testPlanId, updatedTestPlan);
  }
}
