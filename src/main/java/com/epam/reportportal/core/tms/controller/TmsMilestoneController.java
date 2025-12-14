package com.epam.reportportal.core.tms.controller;

import com.epam.reportportal.core.tms.dto.DuplicateTmsMilestoneRS;
import com.epam.reportportal.core.tms.dto.TmsMilestoneRQ;
import com.epam.reportportal.core.tms.dto.TmsMilestoneRS;
import com.epam.reportportal.core.tms.service.TmsMilestoneService;
import com.epam.reportportal.infrastructure.persistence.commons.EntityUtils;
import com.epam.reportportal.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.infrastructure.persistence.entity.tms.TmsMilestone;
import com.epam.reportportal.model.Page;
import com.epam.reportportal.util.OffsetRequest;
import com.epam.reportportal.util.ProjectExtractor;
import com.epam.reportportal.ws.resolver.PagingOffset;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for managing milestones within a project. Provides endpoints
 * for creating, retrieving, updating, patching, and deleting milestones.
 * All operations require administrator privileges.
 */
@RestController
@RequestMapping("/v1/project/{projectKey}/tms/milestone")
@Tag(name = "Milestone", description = "Milestone API collection")
@RequiredArgsConstructor
public class TmsMilestoneController {

  private final TmsMilestoneService tmsMilestoneService;
  private final ProjectExtractor projectExtractor;

  /**
   * Creates a new milestone.
   *
   * @param projectKey  the key of the project
   * @param milestoneRQ the milestone data ({@link TmsMilestoneRQ}) to be created
   * @param user        authenticated user
   * @return the details of the created milestone ({@link TmsMilestoneRS})
   */
  @PostMapping
  @Operation(
      summary = "Create milestone",
      description = "Creates a new milestone in the project."
  )
  @ApiResponse(responseCode = "200", description = "Milestone created successfully")
  public TmsMilestoneRS createMilestone(@PathVariable String projectKey,
      @Valid @RequestBody TmsMilestoneRQ milestoneRQ,
      @AuthenticationPrincipal ReportPortalUser user) {
    var projectId = projectExtractor
        .extractMembershipDetails(user, EntityUtils.normalizeId(projectKey))
        .getProjectId();
    return tmsMilestoneService.create(projectId, milestoneRQ);
  }

  /**
   * Retrieves a specific milestone by its ID.
   *
   * @param projectKey  the key of the project
   * @param milestoneId the ID of the milestone to retrieve
   * @param user        authenticated user
   * @return the milestone details ({@link TmsMilestoneRS})
   */
  @GetMapping("/{id}")
  @Operation(
      summary = "Get milestone by ID",
      description = "Retrieves a specific milestone by its ID."
  )
  @ApiResponse(responseCode = "200", description = "Milestone retrieved successfully")
  public TmsMilestoneRS getMilestoneById(@PathVariable String projectKey,
      @PathVariable("id") Long milestoneId,
      @AuthenticationPrincipal ReportPortalUser user) {
    var projectId = projectExtractor
        .extractMembershipDetails(user, EntityUtils.normalizeId(projectKey))
        .getProjectId();
    return tmsMilestoneService.getById(projectId, milestoneId);
  }

  /**
   * Retrieves a list of milestones with pagination.
   *
   * @param projectKey    the key of the project
   * @param offsetRequest pagination details
   * @param user          authenticated user
   * @return paginated list of milestones ({@link TmsMilestoneRS})
   */
  @GetMapping
  @Operation(
      summary = "Get milestones by criteria",
      description = "Retrieves a list of milestones with pagination."
  )
  @ApiResponse(responseCode = "200", description = "Milestones retrieved successfully")
  public Page<TmsMilestoneRS> getMilestonesByCriteria(
      @PathVariable String projectKey,
      @PagingOffset(sortable = TmsMilestone.class) OffsetRequest offsetRequest,
      @AuthenticationPrincipal ReportPortalUser user) {
    var projectId = projectExtractor
        .extractMembershipDetails(user, EntityUtils.normalizeId(projectKey))
        .getProjectId();
    return tmsMilestoneService.getAll(projectId, offsetRequest);
  }

  /**
   * Applies partial updates to an existing milestone.
   *
   * @param projectKey  the key of the project
   * @param milestoneId the ID of the milestone to patch
   * @param milestoneRQ the patch data ({@link TmsMilestoneRQ})
   * @param user        authenticated user
   * @return the updated milestone details ({@link TmsMilestoneRS})
   */
  @PatchMapping("/{id}")
  @Operation(
      summary = "Patch milestone",
      description = "Applies partial updates to an existing milestone."
  )
  @ApiResponse(responseCode = "200", description = "Milestone updated successfully")
  public TmsMilestoneRS patchMilestone(@PathVariable String projectKey,
      @PathVariable("id") Long milestoneId,
      @Valid @RequestBody TmsMilestoneRQ milestoneRQ,
      @AuthenticationPrincipal ReportPortalUser user) {
    var projectId = projectExtractor
        .extractMembershipDetails(user, EntityUtils.normalizeId(projectKey))
        .getProjectId();
    return tmsMilestoneService.patch(projectId, milestoneId, milestoneRQ);
  }

  /**
   * Deletes a milestone from the specified project.
   *
   * @param projectKey  the key of the project
   * @param milestoneId the ID of the milestone to delete
   * @param user        authenticated user
   */
  @DeleteMapping("/{id}")
  @Operation(
      summary = "Delete milestone",
      description = "Deletes a milestone from the specified project."
  )
  @ApiResponse(responseCode = "204", description = "Milestone deleted successfully")
  public void deleteMilestone(@PathVariable String projectKey,
      @PathVariable("id") Long milestoneId,
      @AuthenticationPrincipal ReportPortalUser user) {
    var projectId = projectExtractor
        .extractMembershipDetails(user, EntityUtils.normalizeId(projectKey))
        .getProjectId();
    tmsMilestoneService.delete(projectId, milestoneId);
  }

  /**
   * Add a test plan to a milestone.
   *
   * @param projectKey  the key of the project
   * @param milestoneId the ID of the milestone
   * @param testPlanId  the ID of the test plan to remove
   * @param user        authenticated user
   */
  @PostMapping("/{id}/test-plan/{testPlanId}")
  @Operation(
      summary = "Add test plan to milestone",
      description = "Adds a test plan to a milestone by their IDs."
  )
  @ApiResponse(responseCode = "204", description = "Test plan added to milestone successfully")
  public void addTestPlanToMilestone(@PathVariable String projectKey,
      @PathVariable("id") Long milestoneId,
      @PathVariable("testPlanId") Long testPlanId,
      @AuthenticationPrincipal ReportPortalUser user) {
    var projectId = projectExtractor
        .extractMembershipDetails(user, EntityUtils.normalizeId(projectKey))
        .getProjectId();
    tmsMilestoneService.addTestPlanToMilestone(projectId, milestoneId, testPlanId);
  }

  /**
   * Removes a test plan from a milestone.
   *
   * @param projectKey  the key of the project
   * @param milestoneId the ID of the milestone
   * @param testPlanId  the ID of the test plan to remove
   * @param user        authenticated user
   */
  @DeleteMapping("/{id}/test-plan/{testPlanId}")
  @Operation(
      summary = "Remove test plan from milestone",
      description = "Removes a test plan from a milestone by their IDs."
  )
  @ApiResponse(responseCode = "204", description = "Test plan removed from milestone successfully")
  public void removeTestPlanFromMilestone(@PathVariable String projectKey,
      @PathVariable("id") Long milestoneId,
      @PathVariable("testPlanId") Long testPlanId,
      @AuthenticationPrincipal ReportPortalUser user) {
    var projectId = projectExtractor
        .extractMembershipDetails(user, EntityUtils.normalizeId(projectKey))
        .getProjectId();
    tmsMilestoneService.removeTestPlanFromMilestone(projectId, milestoneId, testPlanId);
  }

  /**
   * Duplicates an existing milestone.
   *
   * @param projectKey           the key of the project
   * @param milestoneId          the ID of the milestone to duplicate
   * @param duplicateMilestoneRQ the duplicate milestone data ({@link TmsMilestoneRQ})
   * @param user                 authenticated user
   * @return the duplicated milestone details ({@link TmsMilestoneRS})
   */
  @PostMapping("/{id}/duplicate")
  @Operation(
      summary = "Duplicate milestone",
      description = "Duplicates an existing milestone by its ID."
  )
  @ApiResponse(responseCode = "200", description = "Milestone duplicated successfully")
  public DuplicateTmsMilestoneRS duplicateMilestone(@PathVariable String projectKey,
      @PathVariable("id") Long milestoneId,
      @Valid @RequestBody TmsMilestoneRQ duplicateMilestoneRQ,
      @AuthenticationPrincipal ReportPortalUser user) {
    var projectId = projectExtractor
        .extractMembershipDetails(user, EntityUtils.normalizeId(projectKey))
        .getProjectId();
    return tmsMilestoneService.duplicate(projectId, milestoneId, duplicateMilestoneRQ);
  }
}
