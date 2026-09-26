package com.epam.reportportal.base.core.aifactory.controller;

import static com.epam.reportportal.base.auth.permissions.Permissions.ALLOWED_TO_EDIT_PROJECT;
import static com.epam.reportportal.base.auth.permissions.Permissions.ALLOWED_TO_VIEW_PROJECT;

import com.epam.reportportal.base.core.aifactory.dto.PipelineCompareRS;
import com.epam.reportportal.base.core.aifactory.dto.PipelineIterationDetailRS;
import com.epam.reportportal.base.core.aifactory.dto.PipelineIterationRQ;
import com.epam.reportportal.base.core.aifactory.dto.PipelineIterationSummaryRS;
import com.epam.reportportal.base.core.aifactory.dto.PipelineRS;
import com.epam.reportportal.base.core.aifactory.dto.PipelineStageRetryRQ;
import com.epam.reportportal.base.core.aifactory.dto.PipelineStageRetryRS;
import com.epam.reportportal.base.core.aifactory.service.PipelineIterationService;
import com.epam.reportportal.base.core.aifactory.service.PipelineService;
import com.epam.reportportal.base.core.aifactory.service.PipelineStageRetryService;
import com.epam.reportportal.base.infrastructure.persistence.commons.EntityUtils;
import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.util.OffsetRequest;
import com.epam.reportportal.base.util.ProjectExtractor;
import com.epam.reportportal.base.ws.converter.PagedResourcesAssembler;
import com.epam.reportportal.base.ws.resolver.PagingOffset;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Pipeline domain API: read access to pipeline definitions and their iterations,
 * plus the ingest endpoint called by CI/the agent and the per-stage CI retry
 * trigger. See A.1-A.6 of the reviewed API contract.
 */
@RestController
@RequestMapping("/v1/project/{projectKey}/pipeline")
@Tag(name = "Pipeline", description = "AI-agent pipeline iterations and stages")
@RequiredArgsConstructor
public class PipelineController {

  private final PipelineService pipelineService;
  private final PipelineIterationService pipelineIterationService;
  private final PipelineStageRetryService pipelineStageRetryService;
  private final ProjectExtractor projectExtractor;

  @GetMapping
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize(ALLOWED_TO_VIEW_PROJECT)
  @Operation(summary = "List pipeline definitions for a project")
  public Iterable<PipelineRS> getPipelines(
      @PathVariable String projectKey,
      @PagingOffset OffsetRequest offsetRequest,
      @AuthenticationPrincipal ReportPortalUser user) {

    var projectId = projectExtractor.extractMembershipDetails(user, EntityUtils.normalizeId(projectKey)).getProjectId();
    var page = pipelineService.listPipelines(projectId, offsetRequest);
    return PagedResourcesAssembler.<PipelineRS>pageConverter().apply(page);
  }

  @GetMapping("/{pipelineId}/iteration")
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize(ALLOWED_TO_VIEW_PROJECT)
  @Operation(summary = "List iterations of a pipeline definition")
  public Iterable<PipelineIterationSummaryRS> getIterations(
      @PathVariable String projectKey,
      @PathVariable Long pipelineId,
      @PagingOffset OffsetRequest offsetRequest,
      @AuthenticationPrincipal ReportPortalUser user) {

    var projectId = projectExtractor.extractMembershipDetails(user, EntityUtils.normalizeId(projectKey)).getProjectId();
    var page = pipelineIterationService.listIterations(projectId, pipelineId, offsetRequest);
    return PagedResourcesAssembler.<PipelineIterationSummaryRS>pageConverter().apply(page);
  }

  @PostMapping("/iteration")
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize(ALLOWED_TO_EDIT_PROJECT)
  @Operation(summary = "Ingest a pipeline iteration and its stages (called by CI/the agent)")
  public PipelineIterationDetailRS ingestIteration(
      @PathVariable String projectKey,
      @Valid @RequestBody PipelineIterationRQ rq,
      @AuthenticationPrincipal ReportPortalUser user) {

    var projectId = projectExtractor.extractMembershipDetails(user, EntityUtils.normalizeId(projectKey)).getProjectId();
    return pipelineIterationService.ingestIteration(projectId, user.getUserId(), rq);
  }

  @GetMapping("/iteration/{iterationId}")
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize(ALLOWED_TO_VIEW_PROJECT)
  @Operation(summary = "Get pipeline iteration detail with its stages")
  public PipelineIterationDetailRS getIteration(
      @PathVariable String projectKey,
      @PathVariable Long iterationId,
      @AuthenticationPrincipal ReportPortalUser user) {

    var projectId = projectExtractor.extractMembershipDetails(user, EntityUtils.normalizeId(projectKey)).getProjectId();
    return pipelineIterationService.getIterationDetail(projectId, iterationId);
  }

  @GetMapping("/iteration/{iterationId}/compare")
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize(ALLOWED_TO_VIEW_PROJECT)
  @Operation(summary = "Compare two iterations of the same pipeline definition")
  public PipelineCompareRS compareIterations(
      @PathVariable String projectKey,
      @PathVariable Long iterationId,
      @RequestParam("with") Long otherIterationId,
      @AuthenticationPrincipal ReportPortalUser user) {

    var projectId = projectExtractor.extractMembershipDetails(user, EntityUtils.normalizeId(projectKey)).getProjectId();
    return pipelineIterationService.compareIterations(projectId, iterationId, otherIterationId);
  }

  @PostMapping("/iteration/{iterationId}/stage/{stageId}/retry")
  @ResponseStatus(HttpStatus.ACCEPTED)
  @PreAuthorize(ALLOWED_TO_EDIT_PROJECT)
  @Operation(summary = "Trigger a CI rerun for a single pipeline stage")
  public PipelineStageRetryRS retryStage(
      @PathVariable String projectKey,
      @PathVariable Long iterationId,
      @PathVariable Long stageId,
      @RequestBody(required = false) PipelineStageRetryRQ rq,
      @AuthenticationPrincipal ReportPortalUser user) {

    var projectId = projectExtractor.extractMembershipDetails(user, EntityUtils.normalizeId(projectKey)).getProjectId();
    return pipelineStageRetryService.retryStage(projectId, user.getUserId(), iterationId, stageId, rq);
  }
}
