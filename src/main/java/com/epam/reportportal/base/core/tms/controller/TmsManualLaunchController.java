package com.epam.reportportal.base.core.tms.controller;

import com.epam.reportportal.base.core.tms.dto.AddTestCaseToLaunchRQ;
import com.epam.reportportal.base.core.tms.dto.TmsManualLaunchRQ;
import com.epam.reportportal.base.core.tms.dto.TmsManualLaunchRS;
import com.epam.reportportal.base.core.tms.dto.TmsTestCaseExecutionRQ;
import com.epam.reportportal.base.core.tms.dto.TmsTestCaseExecutionRS;
import com.epam.reportportal.base.core.tms.dto.TmsTestFolderRS;
import com.epam.reportportal.base.core.tms.dto.batch.BatchAddTestCasesToLaunchRQ;
import com.epam.reportportal.base.core.tms.dto.batch.BatchTestCaseOperationResultRS;
import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.reporting.OperationCompletionRS;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/v1/project/{projectKey}/tms")
@RequiredArgsConstructor
@Tag(name = "TMS Manual Launch Controller", description = "Operations for managing TMS Manual Launches")
public class TmsManualLaunchController {

  // Manual Launch CRUD operations
  @PostMapping("/launch")
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Create a new TMS Manual Launch")
  public TmsManualLaunchRS createManualLaunch(
      @Parameter(description = "Project key", required = true)
      @PathVariable String projectKey,
      @Valid @RequestBody TmsManualLaunchRQ request,
      @AuthenticationPrincipal ReportPortalUser user) {

    // TODO: Implement
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @GetMapping("/launch/manual")
  @Operation(summary = "Get Manual Launches by criteria")
  public Page<TmsManualLaunchRS> getManualLaunches(
      @Parameter(description = "Project key", required = true)
      @PathVariable String projectKey,
      Pageable pageable,
      @AuthenticationPrincipal ReportPortalUser user) {

    // TODO: Implement
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @GetMapping("/launch/{launchId}")
  @Operation(summary = "Get Manual Launch by ID")
  public TmsManualLaunchRS getManualLaunchById(
      @Parameter(description = "Project key", required = true)
      @PathVariable String projectKey,
      @Parameter(description = "Launch ID", required = true)
      @PathVariable Long launchId,
      @AuthenticationPrincipal ReportPortalUser user) {

    // TODO: Implement
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @DeleteMapping("/launch/{launchId}")
  @Operation(summary = "Delete Manual Launch by ID")
  public OperationCompletionRS deleteManualLaunch(
      @Parameter(description = "Project key", required = true)
      @PathVariable String projectKey,
      @Parameter(description = "Launch ID", required = true)
      @PathVariable Long launchId,
      @AuthenticationPrincipal ReportPortalUser user) {

    // TODO: Implement
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @PatchMapping("/launch/{launchId}")
  @Operation(summary = "Patch Manual Launch")
  public TmsManualLaunchRS patchManualLaunch(
      @Parameter(description = "Project key", required = true)
      @PathVariable String projectKey,
      @Parameter(description = "Launch ID", required = true)
      @PathVariable Long launchId,
      @Valid @RequestBody TmsManualLaunchRQ request,
      @AuthenticationPrincipal ReportPortalUser user) {

    // TODO: Implement
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @DeleteMapping("/launch/manual")
  @Operation(summary = "Batch delete Manual Launches")
  public OperationCompletionRS batchDeleteManualLaunches(
      @Parameter(description = "Project key", required = true)
      @PathVariable String projectKey,
      @Parameter(description = "List of Launch IDs to delete", required = true)
      @RequestBody List<Long> launchIds,
      @AuthenticationPrincipal ReportPortalUser user) {

    // TODO: Implement
    throw new UnsupportedOperationException("Not implemented yet");
  }

  // Test Case management in Launch
  @PostMapping("/launch/{launchId}/test-case")
  @Operation(summary = "Add single test case to Manual Launch")
  public BatchTestCaseOperationResultRS addTestCaseToLaunch(
      @Parameter(description = "Project key", required = true)
      @PathVariable String projectKey,
      @Parameter(description = "Launch ID", required = true)
      @PathVariable Long launchId,
      @Valid @RequestBody AddTestCaseToLaunchRQ request,
      @AuthenticationPrincipal ReportPortalUser user) {

    // TODO: Implement
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @PostMapping("/launch/{launchId}/test-case/batch")
  @Operation(summary = "Add multiple test cases to Manual Launch")
  public BatchTestCaseOperationResultRS addTestCasesToLaunch(
      @Parameter(description = "Project key", required = true)
      @PathVariable String projectKey,
      @Parameter(description = "Launch ID", required = true)
      @PathVariable Long launchId,
      @Valid @RequestBody BatchAddTestCasesToLaunchRQ request,
      @AuthenticationPrincipal ReportPortalUser user) {

    // TODO: Implement
    throw new UnsupportedOperationException("Not implemented yet");
  }

  // Launch folders
  @GetMapping("/launch/{launchId}/folder")
  @Operation(summary = "Get folders of launch")
  public List<TmsTestFolderRS> getLaunchFolders(
      @Parameter(description = "Project key", required = true)
      @PathVariable String projectKey,
      @Parameter(description = "Launch ID", required = true)
      @PathVariable Long launchId,
      @AuthenticationPrincipal ReportPortalUser user) {

    // TODO: Implement
    throw new UnsupportedOperationException("Not implemented yet");
  }

  // Test Case Execution operations
  @GetMapping("/launch/{launchId}/test-case/execution")
  @Operation(summary = "Get all test case executions of launch")
  public Page<TmsTestCaseExecutionRS> getLaunchTestCaseExecutions(
      @Parameter(description = "Project key", required = true)
      @PathVariable String projectKey,
      @Parameter(description = "Launch ID", required = true)
      @PathVariable Long launchId,
      Pageable pageable,
      @AuthenticationPrincipal ReportPortalUser user) {

    // TODO: Implement
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @GetMapping("/launch/{launchId}/test-case/execution/{executionId}")
  @Operation(summary = "Get specific test case execution of launch")
  public TmsTestCaseExecutionRS getTestCaseExecution(
      @Parameter(description = "Project key", required = true)
      @PathVariable String projectKey,
      @Parameter(description = "Launch ID", required = true)
      @PathVariable Long launchId,
      @Parameter(description = "Execution ID", required = true)
      @PathVariable Long executionId,
      @AuthenticationPrincipal ReportPortalUser user) {

    // TODO: Implement
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @DeleteMapping("/launch/{launchId}/test-case/execution/{executionId}")
  @Operation(summary = "Delete specific test case execution from launch")
  public OperationCompletionRS deleteTestCaseExecution(
      @Parameter(description = "Project key", required = true)
      @PathVariable String projectKey,
      @Parameter(description = "Launch ID", required = true)
      @PathVariable Long launchId,
      @Parameter(description = "Execution ID", required = true)
      @PathVariable Long executionId,
      @AuthenticationPrincipal ReportPortalUser user) {

    // TODO: Implement
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @GetMapping("/launch/{launchId}/test-case/{testCaseId}/execution")
  @Operation(summary = "Get all executions of specific test case in launch")
  public List<TmsTestCaseExecutionRS> getTestCaseExecutionsInLaunch(
      @Parameter(description = "Project key", required = true)
      @PathVariable String projectKey,
      @Parameter(description = "Launch ID", required = true)
      @PathVariable Long launchId,
      @Parameter(description = "Test Case ID", required = true)
      @PathVariable Long testCaseId,
      @AuthenticationPrincipal ReportPortalUser user) {

    // TODO: Implement
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @PatchMapping("/launch/{launchId}/test-case/execution/{executionId}")
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

    // TODO: Implement
    throw new UnsupportedOperationException("Not implemented yet");
  }
}
