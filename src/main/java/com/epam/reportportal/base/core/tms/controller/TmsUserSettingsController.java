package com.epam.reportportal.base.core.tms.controller;

import static com.epam.reportportal.base.auth.permissions.Permissions.ALLOWED_TO_VIEW_PROJECT;

import com.epam.reportportal.base.core.tms.dto.TmsUserSettingsRQ;
import com.epam.reportportal.base.core.tms.dto.TmsUserSettingsRS;
import com.epam.reportportal.base.core.tms.service.TmsUserSettingsService;
import com.epam.reportportal.base.infrastructure.persistence.commons.EntityUtils;
import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.util.ProjectExtractor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for the TMS list settings of the current user in a project (items per page, sort
 * mode, tree view mode, ...). The settings belong to the authenticated user; every user who can
 * view the project, Viewers included, can read and change their own settings.
 */
@RestController
@RequestMapping("/v1/project/{projectKey}/tms/user-settings")
@Tag(name = "TMS User Settings", description = "TMS list settings of the current user")
@RequiredArgsConstructor
public class TmsUserSettingsController {

  private final TmsUserSettingsService tmsUserSettingsService;
  private final ProjectExtractor projectExtractor;

  /**
   * Returns the list settings of the current user in the project.
   *
   * @param projectKey the key of the project
   * @param user       authenticated user
   * @return stored settings ({@link TmsUserSettingsRS}), empty if nothing is stored
   */
  @GetMapping
  @PreAuthorize(ALLOWED_TO_VIEW_PROJECT)
  @Operation(
      summary = "Get TMS list settings of the current user",
      description = "Returns the list settings of the current user in the project."
  )
  @ApiResponse(responseCode = "200", description = "Settings returned successfully")
  public TmsUserSettingsRS getUserSettings(@PathVariable String projectKey,
      @AuthenticationPrincipal ReportPortalUser user) {
    var projectId = projectExtractor
        .extractMembershipDetails(user, EntityUtils.normalizeId(projectKey))
        .getProjectId();
    return tmsUserSettingsService.get(projectId, user.getUserId());
  }

  /**
   * Sets or removes list settings of the current user in the project. Keys that are not in the
   * request stay unchanged; a {@code null} value removes the key.
   *
   * @param projectKey the key of the project
   * @param request    keys to set or remove ({@link TmsUserSettingsRQ})
   * @param user       authenticated user
   * @return all settings of the user in the project after the update ({@link TmsUserSettingsRS})
   */
  @PutMapping
  @PreAuthorize(ALLOWED_TO_VIEW_PROJECT)
  @Operation(
      summary = "Update TMS list settings of the current user",
      description = "Sets or removes the given keys. Other stored keys stay unchanged."
  )
  @ApiResponse(responseCode = "200", description = "Settings updated successfully")
  public TmsUserSettingsRS updateUserSettings(@PathVariable String projectKey,
      @Valid @RequestBody TmsUserSettingsRQ request,
      @AuthenticationPrincipal ReportPortalUser user) {
    var projectId = projectExtractor
        .extractMembershipDetails(user, EntityUtils.normalizeId(projectKey))
        .getProjectId();
    return tmsUserSettingsService.update(projectId, user.getUserId(), request);
  }
}
