package com.epam.reportportal.core.tms.controller;

import com.epam.reportportal.core.tms.dto.AddTestCaseToLaunchRQ;
import com.epam.reportportal.core.tms.dto.CreateTmsManualLaunchRQ;
import com.epam.reportportal.core.tms.dto.CreateTmsManualLaunchRS;
import com.epam.reportportal.core.tms.dto.TmsManualLaunchRQ;
import com.epam.reportportal.core.tms.dto.TmsManualLaunchRS;
import com.epam.reportportal.core.tms.dto.TmsTestCaseExecutionCommentRQ;
import com.epam.reportportal.core.tms.dto.TmsTestCaseExecutionCommentRS;
import com.epam.reportportal.core.tms.dto.TmsTestCaseExecutionRQ;
import com.epam.reportportal.core.tms.dto.TmsTestCaseExecutionRS;
import com.epam.reportportal.core.tms.dto.TmsTestFolderRS;
import com.epam.reportportal.core.tms.dto.batch.BatchAddTestCasesToLaunchRQ;
import com.epam.reportportal.core.tms.dto.batch.BatchDeleteManualLaunchesRQ;
import com.epam.reportportal.core.tms.dto.batch.BatchManualLaunchOperationResultRS;
import com.epam.reportportal.core.tms.dto.batch.BatchTestCaseOperationResultRS;
import com.epam.reportportal.core.tms.service.TmsManualLaunchService;
import com.epam.reportportal.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.infrastructure.persistence.commons.querygen.Filter;
import com.epam.reportportal.infrastructure.persistence.entity.launch.Launch;
import com.epam.reportportal.infrastructure.persistence.entity.tms.TmsTestCaseExecution;
import com.epam.reportportal.infrastructure.persistence.entity.tms.TmsTestFolder;
import com.epam.reportportal.model.Page;
import com.epam.reportportal.util.OffsetRequest;
import com.epam.reportportal.util.ProjectExtractor;
import com.epam.reportportal.ws.resolver.FilterFor;
import com.epam.reportportal.ws.resolver.PagingOffset;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for managing TMS Manual Launches.
 */
@RestController
@RequestMapping("/v1/project/{projectKey}/launch/manual")
@RequiredArgsConstructor
@Tag(name = "TMS Manual Launch Controller", description = "Operations for managing TMS Manual Launches")
public class TmsManualLaunchController {

  private final TmsManualLaunchService tmsManualLaunchService;
  private final ProjectExtractor projectExtractor;

  @PostMapping
  @Operation(summary = "Create a new TMS Manual Launch")
  public CreateTmsManualLaunchRS createManualLaunch(
      @Parameter(description = "Project key", required = true)
      @PathVariable String projectKey,
      @Valid @RequestBody CreateTmsManualLaunchRQ request,
      @AuthenticationPrincipal ReportPortalUser user) {
    return tmsManualLaunchService.create(
        projectExtractor
            .extractMembershipDetails(user, projectKey)
            .getProjectId(),
        user,
        request
    );
  }

  @GetMapping
  @Operation(summary = "Get Manual Launches by criteria")
  public Page<TmsManualLaunchRS> getManualLaunches(
      @Parameter(description = "Project key", required = true)
      @PathVariable String projectKey,
      @FilterFor(Launch.class) Filter filter,
      @PagingOffset(sortable = Launch.class) OffsetRequest pageable,
      @AuthenticationPrincipal ReportPortalUser user) {
    return tmsManualLaunchService.getManualLaunches(
        projectExtractor
            .extractMembershipDetails(user, projectKey)
            .getProjectId(),
        filter,
        pageable
    );
  }

  @GetMapping("/{launchId}")
  @Operation(summary = "Get Manual Launch by ID")
  public TmsManualLaunchRS getManualLaunchById(
      @Parameter(description = "Project key", required = true)
      @PathVariable String projectKey,
      @Parameter(description = "Launch ID", required = true)
      @PathVariable Long launchId,
      @AuthenticationPrincipal ReportPortalUser user) {
    return tmsManualLaunchService.getById(
        projectExtractor
            .extractMembershipDetails(user, projectKey)
            .getProjectId(),
        launchId
    );
  }

  @DeleteMapping("/{launchId}")
  @Operation(summary = "Delete Manual Launch by ID")
  public void deleteManualLaunch(
      @Parameter(description = "Project key", required = true)
      @PathVariable String projectKey,
      @Parameter(description = "Launch ID", required = true)
      @PathVariable Long launchId,
      @AuthenticationPrincipal ReportPortalUser user) {
    tmsManualLaunchService.delete(
        projectExtractor
            .extractMembershipDetails(user, projectKey),
        launchId,
        user
    );
  }

  @PatchMapping("/{launchId}")
  @Operation(summary = "Patch Manual Launch")
  public TmsManualLaunchRS patchManualLaunch(
      @Parameter(description = "Project key", required = true)
      @PathVariable String projectKey,
      @Parameter(description = "Launch ID", required = true)
      @PathVariable Long launchId,
      @Valid @RequestBody TmsManualLaunchRQ request,
      @AuthenticationPrincipal ReportPortalUser user) {

    return tmsManualLaunchService.patch(
        projectExtractor
            .extractMembershipDetails(user, projectKey)
            .getProjectId(),
        launchId,
        request
    );
  }

  @DeleteMapping
  @Operation(summary = "Batch delete Manual Launches")
  public BatchManualLaunchOperationResultRS batchDeleteManualLaunches(
      @Parameter(description = "Project key", required = true)
      @PathVariable String projectKey,
      @Parameter(description = "List of Launch IDs to delete", required = true)
      @RequestBody BatchDeleteManualLaunchesRQ batchDeleteManualLaunchesRQ,
      @AuthenticationPrincipal ReportPortalUser user) {
    return tmsManualLaunchService.batchDeleteManualLaunches(
        projectExtractor
            .extractMembershipDetails(user, projectKey),
        batchDeleteManualLaunchesRQ,
        user
    );
  }

  @PostMapping("/{launchId}/test-case")
  @Operation(summary = "Add single test case to Manual Launch")
  public void addTestCaseToLaunch(
      @Parameter(description = "Project key", required = true)
      @PathVariable String projectKey,
      @Parameter(description = "Launch ID", required = true)
      @PathVariable Long launchId,
      @Valid @RequestBody AddTestCaseToLaunchRQ request,
      @AuthenticationPrincipal ReportPortalUser user) {
    tmsManualLaunchService.addTestCaseToLaunch(
        projectExtractor
            .extractMembershipDetails(user, projectKey)
            .getProjectId(),
        launchId,
        request
    );
  }

  @PostMapping("{launchId}/test-case/batch")
  @Operation(summary = "Batch add multiple test cases to Manual Launch")
  public BatchTestCaseOperationResultRS addTestCasesToLaunch(
      @Parameter(description = "Project key", required = true)
      @PathVariable String projectKey,
      @Parameter(description = "Launch ID", required = true)
      @PathVariable Long launchId,
      @Valid @RequestBody BatchAddTestCasesToLaunchRQ request,
      @AuthenticationPrincipal ReportPortalUser user) {
    return tmsManualLaunchService.addTestCasesToLaunch(
        projectExtractor
            .extractMembershipDetails(user, projectKey)
            .getProjectId(),
        launchId,
        request
    );
  }

  @GetMapping("{launchId}/folder")
  @Operation(summary = "Get folders of launch")
  public Page<TmsTestFolderRS> getLaunchFolders(
      @Parameter(description = "Project key", required = true)
      @PathVariable String projectKey,
      @Parameter(description = "Launch ID", required = true)
      @PathVariable Long launchId,
      @PagingOffset(sortable = TmsTestFolder.class) OffsetRequest pageable,
      @AuthenticationPrincipal ReportPortalUser user) {
    return tmsManualLaunchService.getLaunchFolders(
        projectExtractor
            .extractMembershipDetails(user, projectKey)
            .getProjectId(),
        launchId,
        pageable
    );
  }

  @GetMapping("/{launchId}/test-case/execution")
  @Operation(summary = "Get all test case executions of launch")
  public Page<TmsTestCaseExecutionRS> getLaunchTestCaseExecutions(
      @Parameter(description = "Project key", required = true)
      @PathVariable String projectKey,
      @Parameter(description = "Launch ID", required = true)
      @PathVariable Long launchId,
      @FilterFor(TmsTestCaseExecution.class) Filter filter,
      @PagingOffset(sortable = TmsTestCaseExecution.class) OffsetRequest pageable,
      @AuthenticationPrincipal ReportPortalUser user) {
    return tmsManualLaunchService.getLaunchTestCaseExecutions(
        projectExtractor
            .extractMembershipDetails(user, projectKey)
            .getProjectId(),
        launchId,
        filter,
        pageable
    );
  }

  @GetMapping("/{launchId}/test-case/execution/{executionId}")
  @Operation(summary = "Get specific test case execution of launch")
  public TmsTestCaseExecutionRS getTestCaseExecution(
      @Parameter(description = "Project key", required = true)
      @PathVariable String projectKey,
      @Parameter(description = "Launch ID", required = true)
      @PathVariable Long launchId,
      @Parameter(description = "Execution ID", required = true)
      @PathVariable Long executionId,
      @AuthenticationPrincipal ReportPortalUser user) {
    return tmsManualLaunchService.getTestCaseExecution(
        projectExtractor
            .extractMembershipDetails(user, projectKey)
            .getProjectId(),
        launchId,
        executionId
    );
  }

  @DeleteMapping("/{launchId}/test-case/execution/{executionId}")
  @Operation(summary = "Delete specific test case execution from launch")
  public void deleteTestCaseExecution(
      @Parameter(description = "Project key", required = true)
      @PathVariable String projectKey,
      @Parameter(description = "Launch ID", required = true)
      @PathVariable Long launchId,
      @Parameter(description = "Execution ID", required = true)
      @PathVariable Long executionId,
      @AuthenticationPrincipal ReportPortalUser user) {
    tmsManualLaunchService.deleteTestCaseExecution(
        projectExtractor
            .extractMembershipDetails(user, projectKey)
            .getProjectId(),
        launchId,
        executionId
    );
  }

  @GetMapping("/{launchId}/test-case/{testCaseId}/execution")
  @Operation(summary = "Get all executions of specific test case in launch")
  public Page<TmsTestCaseExecutionRS> getTestCaseExecutionsInLaunch(
      @Parameter(description = "Project key", required = true)
      @PathVariable String projectKey,
      @Parameter(description = "Launch ID", required = true)
      @PathVariable Long launchId,
      @Parameter(description = "Test Case ID", required = true)
      @PathVariable Long testCaseId,
      @PagingOffset OffsetRequest offsetRequest,
      @AuthenticationPrincipal ReportPortalUser user) {
    return tmsManualLaunchService.getTestCaseExecutionsInLaunchForTestCase(
        projectExtractor
            .extractMembershipDetails(user, projectKey)
            .getProjectId(),
        launchId,
        testCaseId,
        offsetRequest
    );
  }

  @PatchMapping("/{launchId}/test-case/execution/{executionId}")
  @Operation(summary = "Patch specific test case execution")
  public TmsTestCaseExecutionRS patchTestCaseExecution(
      @Parameter(description = "Project key", required = true)
      @PathVariable String projectKey,
      @Parameter(description = "Launch ID", required = true)
      @PathVariable Long launchId,
      @Parameter(description = "Execution ID", required = true)
      @PathVariable Long executionId,
      @Valid @RequestBody TmsTestCaseExecutionRQ request,
      @AuthenticationPrincipal ReportPortalUser user) {
    return tmsManualLaunchService.patchTestCaseExecution(
        projectExtractor
            .extractMembershipDetails(user, projectKey)
            .getProjectId(),
        launchId,
        executionId,
        request
    );
  }

  @PutMapping("/{launchId}/test-case/execution/{executionId}/comment")
  @Operation(summary = "Put test case execution comment")
  public TmsTestCaseExecutionCommentRS putTestCaseExecutionComment(
      @Parameter(description = "Project key", required = true)
      @PathVariable String projectKey,
      @Parameter(description = "Launch ID", required = true)
      @PathVariable Long launchId,
      @Parameter(description = "Execution ID", required = true)
      @PathVariable Long executionId,
      @Valid @RequestBody TmsTestCaseExecutionCommentRQ request,
      @AuthenticationPrincipal ReportPortalUser user) {
    return tmsManualLaunchService.putTestCaseExecutionComment(
        projectExtractor
            .extractMembershipDetails(user, projectKey)
            .getProjectId(),
        launchId,
        executionId,
        request
    );
  }

  @DeleteMapping("/{launchId}/test-case/execution/{executionId}/comment")
  @Operation(summary = "Delete test case execution comment")
  public void deleteTestCaseExecutionComment(
      @Parameter(description = "Project key", required = true)
      @PathVariable String projectKey,
      @Parameter(description = "Launch ID", required = true)
      @PathVariable Long launchId,
      @Parameter(description = "Execution ID", required = true)
      @PathVariable Long executionId,
      @AuthenticationPrincipal ReportPortalUser user) {
    tmsManualLaunchService.deleteTestCaseExecutionComment(
        projectExtractor
            .extractMembershipDetails(user, projectKey)
            .getProjectId(),
        launchId,
        executionId
    );
  }
}
