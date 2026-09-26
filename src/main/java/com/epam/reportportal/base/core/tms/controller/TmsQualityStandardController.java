package com.epam.reportportal.base.core.tms.controller;

import static com.epam.reportportal.base.auth.permissions.Permissions.ALLOWED_TO_EDIT_PROJECT;
import static com.epam.reportportal.base.auth.permissions.Permissions.ALLOWED_TO_VIEW_PROJECT;

import com.epam.reportportal.base.core.tms.dto.TmsQualityStandardRQ;
import com.epam.reportportal.base.core.tms.dto.TmsQualityStandardRS;
import com.epam.reportportal.base.core.tms.service.TmsQualityStandardService;
import com.epam.reportportal.base.infrastructure.persistence.commons.EntityUtils;
import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.util.ProjectExtractor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Per-project AI test-case quality rubric (one per project, MVP). See B.1-B.4
 * of the reviewed API contract.
 */
@RestController
@RequestMapping("/v1/project/{projectKey}/tms/quality-standard")
@Tag(name = "TMS Quality Standard", description = "AI test-case grading rubric")
@RequiredArgsConstructor
public class TmsQualityStandardController {

  private final TmsQualityStandardService tmsQualityStandardService;
  private final ProjectExtractor projectExtractor;

  @GetMapping
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize(ALLOWED_TO_VIEW_PROJECT)
  @Operation(summary = "Get the project's quality standard")
  public TmsQualityStandardRS getStandard(
      @PathVariable String projectKey,
      @AuthenticationPrincipal ReportPortalUser user) {

    var projectId = projectExtractor.extractMembershipDetails(user, EntityUtils.normalizeId(projectKey)).getProjectId();
    return tmsQualityStandardService.getStandard(projectId);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize(ALLOWED_TO_EDIT_PROJECT)
  @Operation(summary = "Create the project's quality standard")
  public TmsQualityStandardRS createStandard(
      @PathVariable String projectKey,
      @Valid @RequestBody TmsQualityStandardRQ rq,
      @AuthenticationPrincipal ReportPortalUser user) {

    var projectId = projectExtractor.extractMembershipDetails(user, EntityUtils.normalizeId(projectKey)).getProjectId();
    return tmsQualityStandardService.createStandard(projectId, rq);
  }

  @PutMapping
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize(ALLOWED_TO_EDIT_PROJECT)
  @Operation(summary = "Update the project's quality standard")
  public TmsQualityStandardRS updateStandard(
      @PathVariable String projectKey,
      @Valid @RequestBody TmsQualityStandardRQ rq,
      @AuthenticationPrincipal ReportPortalUser user) {

    var projectId = projectExtractor.extractMembershipDetails(user, EntityUtils.normalizeId(projectKey)).getProjectId();
    return tmsQualityStandardService.updateStandard(projectId, rq);
  }

  @DeleteMapping
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize(ALLOWED_TO_EDIT_PROJECT)
  @Operation(summary = "Remove the project's quality standard")
  public void deleteStandard(
      @PathVariable String projectKey,
      @AuthenticationPrincipal ReportPortalUser user) {

    var projectId = projectExtractor.extractMembershipDetails(user, EntityUtils.normalizeId(projectKey)).getProjectId();
    tmsQualityStandardService.deleteStandard(projectId);
  }
}
