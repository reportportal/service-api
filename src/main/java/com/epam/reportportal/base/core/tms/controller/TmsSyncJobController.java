package com.epam.reportportal.base.core.tms.controller;

import static com.epam.reportportal.base.auth.permissions.Permissions.ALLOWED_TO_EDIT_PROJECT;
import static com.epam.reportportal.base.auth.permissions.Permissions.ALLOWED_TO_VIEW_PROJECT;

import com.epam.reportportal.base.core.tms.sync.TmsSyncJobService;
import com.epam.reportportal.base.core.tms.sync.dto.RemoteFolder;
import com.epam.reportportal.base.core.tms.sync.dto.TmsSyncJobRS;
import com.epam.reportportal.base.infrastructure.persistence.commons.EntityUtils;
import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.util.OffsetRequest;
import com.epam.reportportal.base.util.ProjectExtractor;
import com.epam.reportportal.base.ws.converter.PagedResourcesAssembler;
import com.epam.reportportal.base.ws.resolver.PagingOffset;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Provides endpoints for managing TMS synchronization jobs and remote folders.
 */
@RestController
@RequestMapping("/v1/project/{projectKey}/tms/sync")
@Tag(name = "TMS Sync", description = "TMS Synchronization API collection")
@RequiredArgsConstructor
public class TmsSyncJobController {

  private final TmsSyncJobService tmsSyncJobService;
  private final ProjectExtractor projectExtractor;

  /**
   * Starts a new TMS synchronization job.
   *
   * @param projectKey project identifier
   * @param integrationId integration identifier
   * @param remoteFolderId remote folder identifier
   * @param localFolderId local folder identifier
   * @param user authenticated user
   * @return created synchronization job
   */
  @PostMapping("/jobs")
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize(ALLOWED_TO_EDIT_PROJECT)
  @Operation(summary = "Start a new TMS synchronization job")
  public TmsSyncJobRS startSyncJob(
      @PathVariable String projectKey,
      @RequestParam Long integrationId,
      @RequestParam String remoteFolderId,
      @RequestParam(required = false) Long localFolderId,
      @AuthenticationPrincipal ReportPortalUser user) {

    var projectId = projectExtractor.extractMembershipDetails(user, EntityUtils.normalizeId(projectKey)).getProjectId();
    return tmsSyncJobService.startSyncJob(projectId, integrationId, remoteFolderId, localFolderId);
  }

  /**
   * Retrieves TMS synchronization jobs for a project.
   *
   * @param projectKey project identifier
   * @param offsetRequest pagination request
   * @param user authenticated user
   * @return paginated synchronization jobs
   */
  @GetMapping("/jobs")
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize(ALLOWED_TO_VIEW_PROJECT)
  @Operation(summary = "Get list of TMS synchronization jobs")
  public Iterable<TmsSyncJobRS> getSyncJobs(
      @PathVariable String projectKey,
      @PagingOffset OffsetRequest offsetRequest,
      @AuthenticationPrincipal ReportPortalUser user) {

    var projectId = projectExtractor.extractMembershipDetails(user, EntityUtils.normalizeId(projectKey)).getProjectId();
    var page = tmsSyncJobService.getSyncJobs(projectId, offsetRequest);
    return PagedResourcesAssembler.<TmsSyncJobRS>pageConverter().apply(page);
  }

  /**
   * Retrieves a TMS synchronization job.
   *
   * @param projectKey project identifier
   * @param jobId synchronization job identifier
   * @param user authenticated user
   * @return synchronization job details
   */
  @GetMapping("/jobs/{jobId}")
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize(ALLOWED_TO_VIEW_PROJECT)
  @Operation(summary = "Get TMS synchronization job status")
  public TmsSyncJobRS getSyncJob(
      @PathVariable String projectKey,
      @PathVariable Long jobId,
      @AuthenticationPrincipal ReportPortalUser user) {

    var projectId = projectExtractor.extractMembershipDetails(user, EntityUtils.normalizeId(projectKey)).getProjectId();
    return tmsSyncJobService.getSyncJob(projectId, jobId);
  }

  /**
   * Cancels a pending or in-progress TMS synchronization job.
   *
   * @param projectKey project identifier
   * @param jobId synchronization job identifier
   * @param user authenticated user
   */
  @DeleteMapping("/jobs/{jobId}")
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize(ALLOWED_TO_EDIT_PROJECT)
  @Operation(summary = "Cancel a pending or in-progress TMS synchronization job")
  public void cancelSyncJob(
      @PathVariable String projectKey,
      @PathVariable Long jobId,
      @AuthenticationPrincipal ReportPortalUser user) {

    var projectId = projectExtractor.extractMembershipDetails(user, EntityUtils.normalizeId(projectKey)).getProjectId();
    tmsSyncJobService.cancelSyncJob(projectId, jobId);
  }

  /**
   * Retrieves remote folders from a TMS provider.
   *
   * @param projectKey project identifier
   * @param provider TMS provider identifier
   * @param integrationId integration identifier
   * @param rootFolderId root remote folder identifier
   * @param user authenticated user
   * @return remote folder tree
   */
  @GetMapping("/{provider}/folders")
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize(ALLOWED_TO_VIEW_PROJECT)
  @Operation(summary = "Fetch remote folder tree from TMS provider")
  public List<RemoteFolder> getRemoteFolders(
      @PathVariable String projectKey,
      @PathVariable String provider,
      @RequestParam Long integrationId,
      @RequestParam(required = false, defaultValue = "0") String rootFolderId,
      @AuthenticationPrincipal ReportPortalUser user) {

    var projectId = projectExtractor.extractMembershipDetails(user, EntityUtils.normalizeId(projectKey)).getProjectId();
    return tmsSyncJobService.getRemoteFolders(projectId, integrationId, provider, rootFolderId);
  }
}
