package com.epam.reportportal.base.core.aifactory.controller;

import static com.epam.reportportal.base.auth.permissions.Permissions.ALLOWED_TO_EDIT_PROJECT;
import static com.epam.reportportal.base.auth.permissions.Permissions.ALLOWED_TO_VIEW_PROJECT;

import com.epam.reportportal.base.core.aifactory.dto.PipelineCompareRS;
import com.epam.reportportal.base.core.aifactory.dto.PipelineIterationDetailRS;
import com.epam.reportportal.base.core.aifactory.dto.PipelineIterationPatchRQ;
import com.epam.reportportal.base.core.aifactory.dto.PipelineIterationRQ;
import com.epam.reportportal.base.core.aifactory.dto.PipelineIterationSummaryRS;
import com.epam.reportportal.base.core.aifactory.dto.PipelineRS;
import com.epam.reportportal.base.core.aifactory.dto.PipelineSettingsRQ;
import com.epam.reportportal.base.core.aifactory.dto.PipelineStageRQ;
import com.epam.reportportal.base.core.aifactory.dto.PipelineStageRetryRQ;
import com.epam.reportportal.base.core.aifactory.dto.PipelineStageRetryRS;
import com.epam.reportportal.base.core.aifactory.enums.StatusCategory;
import com.epam.reportportal.base.core.aifactory.service.PipelineIterationFilter;
import com.epam.reportportal.base.core.aifactory.service.PipelineIterationService;
import com.epam.reportportal.base.core.aifactory.service.PipelineService;
import com.epam.reportportal.base.core.aifactory.service.PipelineStageRetryService;
import com.epam.reportportal.base.infrastructure.persistence.commons.EntityUtils;
import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.base.model.Page;
import com.epam.reportportal.base.util.OffsetRequest;
import com.epam.reportportal.base.util.ProjectExtractor;
import com.epam.reportportal.base.ws.converter.PagedResourcesAssembler;
import com.epam.reportportal.base.ws.resolver.PagingOffset;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Pipeline domain API: read access to pipeline definitions and their iterations,
 * the reporting endpoints called by CI/the agent (an iteration, then its stages as they finish)
 * and the per-stage CI retry trigger.
 */
@RestController
@RequestMapping("/v1/project/{projectKey}/pipeline")
@Tag(name = "Pipeline", description = "Generic pipelines: iterations made of stages that report anything as their result")
@RequiredArgsConstructor
public class PipelineController {

  private final PipelineService pipelineService;
  private final PipelineIterationService pipelineIterationService;
  private final PipelineStageRetryService pipelineStageRetryService;
  private final ProjectExtractor projectExtractor;

  @GetMapping
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize(ALLOWED_TO_VIEW_PROJECT)
  @Operation(summary = "List pipeline definitions for a project",
      description = "Ordered by name, then id. Response is a page: {content, page}. With search, only pipelines whose "
          + "name matches, or that have an iteration matching, are returned.")
  @Parameter(name = "search", in = ParameterIn.QUERY, description = "Case-insensitive substring over the pipeline "
      + "name and the values of its iterations' attributes; or an exact iteration number")
  @Parameter(name = "name", in = ParameterIn.QUERY, description = "Case-insensitive substring over the pipeline name "
      + "only (the iterations are not looked at). Applied before paging, so totals and pages are exact; combined "
      + "with search, both must match. Example: name=gener finds \"Test case generation\"")
  @Parameter(name = "offset", in = ParameterIn.QUERY, description = "Number of items to skip (default 0)")
  @Parameter(name = "limit", in = ParameterIn.QUERY, description = "Page size (default 100, max 200)")
  public Page<PipelineRS> getPipelines(
      @PathVariable String projectKey,
      @RequestParam(required = false) String search,
      @RequestParam(required = false) String name,
      @PagingOffset OffsetRequest offsetRequest,
      @AuthenticationPrincipal ReportPortalUser user) {

    var projectId = projectExtractor.extractMembershipDetails(user, EntityUtils.normalizeId(projectKey)).getProjectId();
    var page = pipelineService.getPipelines(projectId, search, name, offsetRequest);
    return PagedResourcesAssembler.<PipelineRS>pageConverter().apply(page);
  }

  @GetMapping("/{pipelineId}")
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize(ALLOWED_TO_VIEW_PROJECT)
  @Operation(summary = "Get one pipeline definition with its settings",
      description = "The same object as an item of the list, e.g. to read the current Auto-Ready settings of the "
          + "pipeline of an iteration opened by a direct link. 404 when the pipeline is not in the project.")
  public PipelineRS getPipeline(
      @PathVariable String projectKey,
      @PathVariable Long pipelineId,
      @AuthenticationPrincipal ReportPortalUser user) {

    var projectId = projectExtractor.extractMembershipDetails(user, EntityUtils.normalizeId(projectKey)).getProjectId();
    return pipelineService.getPipeline(projectId, pipelineId);
  }

  @PatchMapping("/{pipelineId}")
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize(ALLOWED_TO_EDIT_PROJECT)
  @Operation(summary = "Change the settings of a pipeline definition")
  public PipelineRS patchPipelineSettings(
      @PathVariable String projectKey,
      @PathVariable Long pipelineId,
      @Valid @RequestBody PipelineSettingsRQ rq,
      @AuthenticationPrincipal ReportPortalUser user) {

    var projectId = projectExtractor.extractMembershipDetails(user, EntityUtils.normalizeId(projectKey)).getProjectId();
    return pipelineService.patchPipelineSettings(projectId, pipelineId, rq);
  }

  @GetMapping("/{pipelineId}/iteration")
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize(ALLOWED_TO_VIEW_PROJECT)
  @Operation(summary = "List iterations of a pipeline definition",
      description = "Newest first (by iterationNumber). Response is a page: {content, page}.")
  @Parameter(name = "search", in = ParameterIn.QUERY, description = "Case-insensitive substring over the values of "
      + "the iteration's attributes, or exact iteration number (a leading # is ignored)")
  @Parameter(name = "status", in = ParameterIn.QUERY, description = "Only iterations whose status category is one "
      + "of these (repeat the parameter or comma-separate)")
  @Parameter(name = "attribute", in = ParameterIn.QUERY, description = "Exact attribute filter as key:value, e.g. "
      + "requirement:US-TMS-EXP-002 (repeat the parameter to require several)")
  @Parameter(name = "offset", in = ParameterIn.QUERY, description = "Number of items to skip (default 0)")
  @Parameter(name = "limit", in = ParameterIn.QUERY, description = "Page size (default 100, max 200)")
  public Page<PipelineIterationSummaryRS> getPipelineIterations(
      @PathVariable String projectKey,
      @PathVariable Long pipelineId,
      @RequestParam(required = false) String search,
      @RequestParam(required = false) Set<StatusCategory> status,
      @RequestParam(required = false) List<String> attribute,
      @PagingOffset OffsetRequest offsetRequest,
      @AuthenticationPrincipal ReportPortalUser user) {

    var projectId = projectExtractor.extractMembershipDetails(user, EntityUtils.normalizeId(projectKey)).getProjectId();
    var page = pipelineIterationService.getPipelineIterations(projectId, pipelineId,
        new PipelineIterationFilter(search, status, parseAttributeFilters(attribute)), offsetRequest);
    return PagedResourcesAssembler.<PipelineIterationSummaryRS>pageConverter().apply(page);
  }

  @PostMapping("/iteration")
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize(ALLOWED_TO_EDIT_PROJECT)
  @Operation(summary = "Create a pipeline iteration (called by CI/the agent)",
      description = "Reporting endpoint; requires project edit permission. The pipeline definition is created on the "
          + "first report that names a pipeline (unique per project) and records its type. The iteration may "
          + "carry the stages known so far; further stages are reported one by one with PUT "
          + ".../iteration/{iterationId}/stage/{stageKey}. With rerunOfIterationId the new iteration is marked as "
          + "a re-run of that iteration. Test case display ids that do not resolve are skipped and returned in "
          + "warnings. Send an Idempotency-Key header to make retries safe: the same key with the same body "
          + "returns the iteration created the first time (200, Idempotent-Replayed: true); the same key with a "
          + "different body is rejected with 409. Keys are scoped to the pipeline and limited to 128 characters.")
  @ApiResponse(responseCode = "201", description = "Iteration created")
  @ApiResponse(responseCode = "200", description = "Replay of an earlier request with the same Idempotency-Key")
  @ApiResponse(responseCode = "409", description = "Idempotency-Key already used with a different request body")
  public ResponseEntity<PipelineIterationDetailRS> createPipelineIteration(
      @PathVariable String projectKey,
      @Valid @RequestBody PipelineIterationRQ rq,
      @Parameter(description = "Makes the report idempotent per pipeline; 1-128 characters")
      @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
      @AuthenticationPrincipal ReportPortalUser user) {

    var projectId = projectExtractor.extractMembershipDetails(user, EntityUtils.normalizeId(projectKey)).getProjectId();
    var result = pipelineIterationService.createPipelineIteration(projectId, user.getUserId(), rq, idempotencyKey);
    if (result.replayed()) {
      return ResponseEntity.ok().header("Idempotent-Replayed", "true").body(result.iteration());
    }
    return ResponseEntity.status(HttpStatus.CREATED).body(result.iteration());
  }

  @PutMapping("/iteration/{iterationId}/stage/{stageKey}")
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize(ALLOWED_TO_EDIT_PROJECT)
  @Operation(summary = "Report one stage of an iteration (called by CI/the agent)",
      description = "Creates the stage or updates it in place, so a job can report it as pending, running and "
          + "finished. Reporting is idempotent per (iteration, stageKey): the same job reporting again updates "
          + "the current attempt, a finished stage reported by a different CI job starts a new attempt and the "
          + "finished one is kept. The iteration status is recalculated and the updated iteration returned.")
  @ApiResponse(responseCode = "404", description = "Iteration not found")
  @ApiResponse(responseCode = "400", description = "Invalid report, or the stage is maintained by ReportPortal")
  public PipelineIterationDetailRS reportPipelineStage(
      @PathVariable String projectKey,
      @PathVariable Long iterationId,
      @PathVariable String stageKey,
      @Valid @RequestBody PipelineStageRQ rq,
      @AuthenticationPrincipal ReportPortalUser user) {

    var projectId = projectExtractor.extractMembershipDetails(user, EntityUtils.normalizeId(projectKey)).getProjectId();
    return pipelineIterationService.reportStage(projectId, iterationId, stageKey, rq);
  }

  @PatchMapping("/iteration/{iterationId}")
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize(ALLOWED_TO_EDIT_PROJECT)
  @Operation(summary = "Update an iteration's attributes and timing (called by CI/the agent)",
      description = "Adds or replaces attributes by key, removes attributes by key, sets start/finish time. "
          + "The iteration status is never set directly - it follows from the stages.")
  public PipelineIterationDetailRS patchPipelineIteration(
      @PathVariable String projectKey,
      @PathVariable Long iterationId,
      @Valid @RequestBody PipelineIterationPatchRQ rq,
      @AuthenticationPrincipal ReportPortalUser user) {

    var projectId = projectExtractor.extractMembershipDetails(user, EntityUtils.normalizeId(projectKey)).getProjectId();
    return pipelineIterationService.patchPipelineIteration(projectId, iterationId, rq);
  }

  @GetMapping("/iteration/{iterationId}")
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize(ALLOWED_TO_VIEW_PROJECT)
  @Operation(summary = "Get pipeline iteration detail with its stages")
  public PipelineIterationDetailRS getPipelineIteration(
      @PathVariable String projectKey,
      @PathVariable Long iterationId,
      @AuthenticationPrincipal ReportPortalUser user) {

    var projectId = projectExtractor.extractMembershipDetails(user, EntityUtils.normalizeId(projectKey)).getProjectId();
    return pipelineIterationService.getPipelineIteration(projectId, iterationId);
  }

  @GetMapping("/iteration/{iterationId}/compare")
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize(ALLOWED_TO_VIEW_PROJECT)
  @Operation(summary = "Compare two iterations of the same pipeline definition",
      description = "The iteration in the path is the candidate, the one given by 'with' is the baseline. Stages "
          + "are matched by name, metrics are the numeric attributes, and when both iterations cover the same "
          + "scope (equal comparison-key attributes) result rows are joined side by side.")
  @Parameter(name = "with", in = ParameterIn.QUERY, required = true, description = "Id of the baseline iteration")
  public PipelineCompareRS comparePipelineIterations(
      @PathVariable String projectKey,
      @PathVariable Long iterationId,
      @RequestParam("with") Long baselineIterationId,
      @AuthenticationPrincipal ReportPortalUser user) {

    var projectId = projectExtractor.extractMembershipDetails(user, EntityUtils.normalizeId(projectKey)).getProjectId();
    return pipelineIterationService.comparePipelineIterations(projectId, iterationId, baselineIterationId);
  }

  @PostMapping("/iteration/{iterationId}/stage/{stageId}/retry")
  @ResponseStatus(HttpStatus.ACCEPTED)
  @PreAuthorize(ALLOWED_TO_EDIT_PROJECT)
  @Operation(summary = "Trigger a CI rerun for a single pipeline stage",
      description = "Allowed only for a retryable stage that failed or waits for a human. The request body is optional.")
  @ApiResponse(responseCode = "202", description = "Retry triggered; the stage and iteration become pending")
  @ApiResponse(responseCode = "400", description = "Stage already passed, not retryable, or no CI integration configured")
  @ApiResponse(responseCode = "404", description = "Stage not found")
  @ApiResponse(responseCode = "409", description = "A retry or run is already in progress for this stage")
  public PipelineStageRetryRS retryPipelineIterationStage(
      @PathVariable String projectKey,
      @PathVariable Long iterationId,
      @PathVariable Long stageId,
      @RequestBody(required = false) PipelineStageRetryRQ rq,
      @AuthenticationPrincipal ReportPortalUser user) {

    var projectId = projectExtractor.extractMembershipDetails(user, EntityUtils.normalizeId(projectKey)).getProjectId();
    return pipelineStageRetryService.retryPipelineIterationStage(projectId, user.getUserId(), iterationId, stageId, rq);
  }

  /**
   * Parses repeated {@code key:value} filters; the first colon separates key from value.
   */
  private static LinkedHashMap<String, String> parseAttributeFilters(List<String> filters) {
    var result = new LinkedHashMap<String, String>();
    if (filters == null) {
      return result;
    }
    for (var filter : filters) {
      int separator = filter.indexOf(':');
      if (separator <= 0) {
        throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR,
            "'attribute' must be given as key:value, got '" + filter + "'");
      }
      result.put(filter.substring(0, separator), filter.substring(separator + 1));
    }
    return result;
  }
}
