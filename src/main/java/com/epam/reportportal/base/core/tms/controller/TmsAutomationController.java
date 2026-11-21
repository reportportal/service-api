package com.epam.reportportal.base.core.tms.controller;

import static com.epam.reportportal.base.auth.permissions.Permissions.ALLOWED_TO_EDIT_PROJECT;
import static com.epam.reportportal.base.auth.permissions.Permissions.ALLOWED_TO_VIEW_PROJECT;

import com.epam.reportportal.base.core.tms.dto.TmsAutomationEnvironmentRS;
import com.epam.reportportal.base.core.tms.dto.TmsAutomationStartRQ;
import com.epam.reportportal.base.core.tms.pipeline.TmsAutomationStartService;
import com.epam.reportportal.base.infrastructure.persistence.commons.EntityUtils;
import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.util.ProjectExtractor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.LinkedHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Sends Ready Test Cases to automation ("Automate") and tells which environments the autotests can run against.
 * <p>
 * Example: {@code POST .../tms/automation} with {@code {"testCaseIds": [42, 43], "environment": "beta5"}} answers 202
 * with the one iteration all accepted cases belong to; the job reports its stages into that iteration.
 */
@RestController
@RequestMapping("/v1/project/{projectKey}/tms/automation")
@Tag(name = "Test Automation", description = "Send Ready test cases to the test automation pipeline")
@RequiredArgsConstructor
public class TmsAutomationController {

  private final TmsAutomationStartService automationStartService;
  private final ProjectExtractor projectExtractor;

  @PreAuthorize(ALLOWED_TO_EDIT_PROJECT)
  @PostMapping
  @Operation(summary = "Send Ready test cases to automation",
      description = "Creates one iteration of the Test automation pipeline for all accepted cases and starts the CI "
          + "job. Cases that are not Ready, are being fixed by the agent or are already in an automation iteration "
          + "are skipped with a reason. Answers 202 {iteration, accepted, skipped}; 409 with reason "
          + "ALREADY_AUTOMATED_CONFIRM_REQUIRED and the testCaseIds that need confirmation (nothing is started; "
          + "send confirmReautomate and confirmedTestCaseIds to go on); 409 with reason NO_ELIGIBLE_TEST_CASES "
          + "and the skipped list when no case can be sent; 409 CI_NOT_CONNECTED (error code 40916) while the "
          + "pipeline cannot start jobs. When the CI refuses the start, nothing stays recorded.")
  @ApiResponse(responseCode = "202", description = "Automation started")
  @ApiResponse(responseCode = "409", description = "Confirmation required, nothing to send, or CI not connected")
  public ResponseEntity<?> start(@PathVariable("projectKey") String projectKey,
      @RequestBody @Valid TmsAutomationStartRQ rq, @AuthenticationPrincipal ReportPortalUser user) {
    var projectId = projectExtractor.extractMembershipDetails(user, EntityUtils.normalizeId(projectKey))
        .getProjectId();
    var outcome = automationStartService.start(projectId, user.getUserId(), rq);
    var body = new LinkedHashMap<String, Object>();
    switch (outcome) {
      case TmsAutomationStartService.Started(var response) -> {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
      }
      case TmsAutomationStartService.ConfirmRequired(var testCaseIds) -> {
        body.put("reason", "ALREADY_AUTOMATED_CONFIRM_REQUIRED");
        body.put("message", "Some test cases are already automated; confirm to automate them again");
        body.put("testCaseIds", testCaseIds);
      }
      case TmsAutomationStartService.NothingEligible(var skipped) -> {
        body.put("reason", "NO_ELIGIBLE_TEST_CASES");
        body.put("message", "None of the test cases can be sent to automation");
        body.put("skipped", skipped);
      }
    }
    return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
  }

  @PreAuthorize(ALLOWED_TO_VIEW_PROJECT)
  @GetMapping("/environment")
  @Operation(summary = "Environments the autotests can run against",
      description = "The environments of the CI connection of the Test automation pipeline, the first being the "
          + "default. 400 when none is configured.")
  @ApiResponse(responseCode = "200", description = "Successful operation")
  public TmsAutomationEnvironmentRS getEnvironments(@PathVariable("projectKey") String projectKey,
      @AuthenticationPrincipal ReportPortalUser user) {
    return automationStartService.getEnvironments(
        projectExtractor.extractMembershipDetails(user, EntityUtils.normalizeId(projectKey)).getProjectId());
  }
}
