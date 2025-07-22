package com.epam.ta.reportportal.core.tms.controller;

import static com.epam.ta.reportportal.auth.permissions.Permissions.IS_ADMIN;

import com.epam.ta.reportportal.commons.EntityUtils;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.tms.dto.TmsTestFolderExportFileType;
import com.epam.ta.reportportal.core.tms.dto.TmsTestFolderRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsTestFolderRS;
import com.epam.ta.reportportal.core.tms.service.TmsTestFolderService;
import com.epam.ta.reportportal.model.Page;
import com.epam.ta.reportportal.util.ProjectExtractor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
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
 * Controller for managing test folders within a project. Provides endpoints to create, update,
 * retrieve, and list test folders. All endpoints are secured and require administrator privileges.
 */
@RestController
@RequestMapping("/v1/project/{projectKey}/tms/folder")
@Tag(name = "Test Folder", description = "Tms Test Folder API collection")
@RequiredArgsConstructor
@PreAuthorize(IS_ADMIN)
public class TmsTestFolderController {

  private final TmsTestFolderService tmsTestFolderService;
  private final ProjectExtractor projectExtractor;

  /**
   * Creates a new test folder for the specified project.
   *
   * @param projectKey The key of the project to which the folder will be added.
   * @param inputDto   A request payload ({@link TmsTestFolderRQ}) containing details of the test
   *                   folder to create.
   * @return A data transfer object ({@link TmsTestFolderRS}) containing the created folder's
   * details.
   */

  @PostMapping
  @Operation(
      summary = "Create Test Folder",
      description = "Creates a new test folder within the specified project"
  )
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "200",
          description = "Test folder successfully created",
          content = @Content(schema = @Schema(implementation = TmsTestFolderRS.class))
      ),
      @ApiResponse(responseCode = "400", description = "Invalid request body"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public TmsTestFolderRS createTestFolder(
      @Parameter(description = "Project key", required = true)
      @PathVariable("projectKey") String projectKey,
      @Parameter(description = "Test folder details", required = true)
      @RequestBody final TmsTestFolderRQ inputDto,
      @AuthenticationPrincipal ReportPortalUser user) {
    return tmsTestFolderService.create(
        projectExtractor
            .extractProjectDetailsAdmin(EntityUtils.normalizeId(projectKey))
            .getProjectId(),
        inputDto);
  }

  /**
   * Updates the details of an existing test folder in a project.
   *
   * @param projectKey The key of the project to which the folder belongs.
   * @param folderId   The ID of the folder to update.
   * @param inputDto   A request payload ({@link TmsTestFolderRQ}) containing updated folder
   *                   details.
   * @return A data transfer object ({@link TmsTestFolderRS}) with updated folder information.
   */

  @PutMapping("/{folderId}")
  @Operation(
      summary = "Update Test Folder",
      description = "Completely updates an existing test folder with new details"
  )
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "200",
          description = "Test folder successfully updated",
          content = @Content(schema = @Schema(implementation = TmsTestFolderRS.class))
      ),
      @ApiResponse(responseCode = "400", description = "Invalid request body"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public TmsTestFolderRS updateTestFolder(
      @Parameter(description = "Project key", required = true)
      @PathVariable("projectKey") String projectKey,
      @Parameter(description = "Test folder ID", required = true)
      @PathVariable("folderId") Long folderId,
      @Parameter(description = "Updated test folder details", required = true)
      @RequestBody TmsTestFolderRQ inputDto,
      @AuthenticationPrincipal ReportPortalUser user) {
    return tmsTestFolderService.update(
        projectExtractor
            .extractProjectDetailsAdmin(EntityUtils.normalizeId(projectKey))
            .getProjectId(),
        folderId,
        inputDto);
  }

  /**
   * Patches the details of an existing test folder in a project.
   *
   * @param projectKey The key of the project to which the folder belongs.
   * @param folderId   The ID of the folder to update.
   * @param inputDto   A request payload ({@link TmsTestFolderRQ}) containing updated folder
   *                   details.
   * @return A data transfer object ({@link TmsTestFolderRS}) with updated folder information.
   */

  @PatchMapping("/{folderId}")
  @Operation(
      summary = "Patch Test Folder",
      description = "Partially updates an existing test folder with provided details"
  )
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "200",
          description = "Test folder successfully patched",
          content = @Content(schema = @Schema(implementation = TmsTestFolderRS.class))
      ),
      @ApiResponse(responseCode = "400", description = "Invalid request body"),
      @ApiResponse(responseCode = "404", description = "Test folder not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public TmsTestFolderRS patchTestFolder(
      @Parameter(description = "Project key", required = true)
      @PathVariable("projectKey") String projectKey,
      @Parameter(description = "Test folder ID", required = true)
      @PathVariable("folderId") Long folderId,
      @Parameter(description = "Partial test folder details for update", required = true)
      @RequestBody final TmsTestFolderRQ inputDto,
      @AuthenticationPrincipal ReportPortalUser user) {
    return tmsTestFolderService.patch(
        projectExtractor
            .extractProjectDetailsAdmin(EntityUtils.normalizeId(projectKey))
            .getProjectId(),
        folderId,
        inputDto);
  }

  /**
   * Fetches details of a specific test folder by its ID.
   *
   * @param projectKey The key of the project to which the folder belongs.
   * @param folderId   The ID of the folder to retrieve.
   * @return A data transfer object ({@link TmsTestFolderRS}) containing folder information.
   */

  @GetMapping("/{folderId}")
  @Operation(
      summary = "Get Test Folder by ID",
      description = "Retrieves details of a specific test folder"
  )
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "200",
          description = "Test folder details retrieved successfully",
          content = @Content(schema = @Schema(implementation = TmsTestFolderRS.class))
      ),
      @ApiResponse(responseCode = "404", description = "Test folder not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public TmsTestFolderRS getTestFolderById(
      @Parameter(description = "Project key", required = true)
      @PathVariable("projectKey") String projectKey,
      @Parameter(description = "Test folder ID", required = true)
      @PathVariable("folderId") Long folderId,
      @AuthenticationPrincipal ReportPortalUser user) {
    return tmsTestFolderService.getById(projectExtractor
        .extractProjectDetailsAdmin(EntityUtils.normalizeId(projectKey))
        .getProjectId(), folderId);
  }

  /**
   * Retrieves all test folders associated with a project.
   *
   * @param projectKey The key of the project.
   * @return A list of data transfer objects ({@link TmsTestFolderRS}) representing the test
   * folders.
   */

  @GetMapping
  @Operation(
      summary = "Get Test Folders by project key",
      description = "Retrieves all test folders associated with a project"
  )
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "200",
          description = "Test folders retrieved successfully"
      ),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public Page<TmsTestFolderRS> getTestFolderByProject(
      @Parameter(description = "Project key", required = true)
      @PathVariable("projectKey") String projectKey,
      @AuthenticationPrincipal ReportPortalUser user,
      @Parameter(description = "Pagination parameters")
      Pageable pageable) {
    return tmsTestFolderService.getFoldersByProjectID(
        projectExtractor
            .extractProjectDetailsAdmin(EntityUtils.normalizeId(projectKey))
            .getProjectId(), pageable);
  }

  /**
   * Retrieves all subfolders for a specific test folder.
   *
   * @param projectKey The key of the project.
   * @param folderId   The ID of the parent folder.
   * @return A list of data transfer objects ({@link TmsTestFolderRS}) representing the subfolders.
   */

  @GetMapping("/{folderId}/sub-folder")
  @Operation(
      summary = "Get subfolders by test folder id",
      description = "Retrieves all subfolders for a specific test folder"
  )
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "200",
          description = "Subfolders retrieved successfully"
      ),
      @ApiResponse(responseCode = "404", description = "Parent folder not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public Page<TmsTestFolderRS> getSubfolders(
      @Parameter(description = "Project key", required = true)
      @PathVariable("projectKey") String projectKey,
      @AuthenticationPrincipal ReportPortalUser user,
      @Parameter(description = "Parent folder ID", required = true)
      @PathVariable("folderId") Long folderId,
      @Parameter(description = "Pagination parameters") Pageable pageable) {
    return tmsTestFolderService.getSubFolders(
        projectExtractor
            .extractProjectDetailsAdmin(EntityUtils.normalizeId(projectKey))
            .getProjectId(), folderId, pageable);
  }

  /**
   * Deletes a test folder based on its id.
   *
   * @param projectKey The key of the project.
   * @param folderId   The id of the test folder
   */

  @DeleteMapping("/{folderId}")
  @Operation(
      summary = "Delete test folder",
      description = "Removes a test folder and all its subfolders"
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Test folder successfully deleted"),
      @ApiResponse(responseCode = "404", description = "Test folder not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public void deleteTestFolder(
      @Parameter(description = "Project key", required = true)
      @PathVariable("projectKey") String projectKey,
      @AuthenticationPrincipal ReportPortalUser user,
      @Parameter(description = "Test folder ID to delete", required = true)
      @PathVariable("folderId") Long folderId) {
    tmsTestFolderService.delete(
        projectExtractor
            .extractProjectDetailsAdmin(EntityUtils.normalizeId(projectKey))
            .getProjectId(), folderId);
  }

  /**
   * Exports a test folder and all its subfolders to defined format.
   *
   * @param projectKey The key of the project to which the folder belongs.
   * @param folderId   The ID of the folder to export.
   * @param fileType   The format to export the folder to.
   */

  @GetMapping("/{folderId}/export/{fileType}")
  @Operation(
      summary = "Export test folder",
      description = "Exports a test folder and all its subfolders to the specified format"
  )
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "200",
          description = "Test folder exported successfully"
      ),
      @ApiResponse(responseCode = "404", description = "Test folder not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public void exportFolder(
      @Parameter(description = "Project key", required = true)
      @PathVariable("projectKey") String projectKey,
      @Parameter(description = "Test folder ID to export", required = true)
      @PathVariable("folderId") Long folderId,
      @Parameter(description = "Export file format", required = true)
      @PathVariable("fileType") TmsTestFolderExportFileType fileType,
      @AuthenticationPrincipal ReportPortalUser user,
      HttpServletResponse response) {
    tmsTestFolderService.exportFolderById(
        projectExtractor
            .extractProjectDetailsAdmin(EntityUtils.normalizeId(projectKey))
            .getProjectId(),
        folderId,
        fileType,
        response
    );
  }
}
