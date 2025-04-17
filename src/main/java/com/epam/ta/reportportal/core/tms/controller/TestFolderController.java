package com.epam.ta.reportportal.core.tms.controller;

import static com.epam.ta.reportportal.auth.permissions.Permissions.IS_ADMIN;

import com.epam.ta.reportportal.commons.EntityUtils;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.tms.dto.TestFolderRQ;
import com.epam.ta.reportportal.core.tms.dto.TestFolderRS;
import com.epam.ta.reportportal.core.tms.service.TestFolderService;
import com.epam.ta.reportportal.util.ProjectExtractor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for managing test folders within a project. Provides endpoints
 * to create, update, retrieve, and list test folders. All endpoints are
 * secured and require administrator privileges.
 */
@RestController
@RequestMapping("/project/{projectKey}/tms/folder")
@Tag(name = "Test Folder", description = "Test Folder API collection")
@RequiredArgsConstructor
public class TestFolderController {

  private final TestFolderService testFolderService;
  private final ProjectExtractor projectExtractor;

  /**
   * Creates a new test folder for the specified project.
   *
   * @param projectKey The key of the project to which the folder will be added.
   * @param inputDto  A request payload ({@link TestFolderRQ}) containing details of the test folder to create.
   * @return A data transfer object ({@link TestFolderRS}) containing the created folder's details.
   */
  @PreAuthorize(IS_ADMIN)
  @PostMapping
  @Operation(summary = "Create Test Folder")
  TestFolderRS createTestFolder(@PathVariable("projectKey") String projectKey,
      @RequestBody final TestFolderRQ inputDto,
      @AuthenticationPrincipal ReportPortalUser user) {
    return testFolderService.createFolder(
        projectExtractor
            .extractProjectDetailsAdmin(user, EntityUtils.normalizeId(projectKey))
            .getProjectId(),
        inputDto);
  }

  /**
   * Updates the details of an existing test folder in a project.
   *
   * @param projectKey The key of the project to which the folder belongs.
   * @param folderId  The ID of the folder to update.
   * @param inputDto  A request payload ({@link TestFolderRQ}) containing updated folder details.
   * @return A data transfer object ({@link TestFolderRS}) with updated folder information.
   */
  @PreAuthorize(IS_ADMIN)
  @PutMapping("/{folderId}")
  @Operation(summary = "Update Test Folder")
  TestFolderRS updateTestFolder(@PathVariable("projectKey") String projectKey,
      @PathVariable("folderId") final long folderId,
      @RequestBody final TestFolderRQ inputDto,
      @AuthenticationPrincipal ReportPortalUser user) {
    return testFolderService.updateFolder(
        projectExtractor
            .extractProjectDetailsAdmin(user, EntityUtils.normalizeId(projectKey))
            .getProjectId(),
        folderId,
        inputDto);
  }

  /**
   * Fetches details of a specific test folder by its ID.
   *
   * @param projectKey The key of the project to which the folder belongs.
   * @param folderId  The ID of the folder to retrieve.
   * @return A data transfer object ({@link TestFolderRS}) containing folder information.
   */
  @PreAuthorize(IS_ADMIN)
  @GetMapping("/{folderId}")
  @Operation(summary = "Get Test Folder by ID")
  TestFolderRS getTestFolderById(@PathVariable("projectKey") String projectKey,
      @PathVariable("folderId") final long folderId,
      @AuthenticationPrincipal ReportPortalUser user) {
    // Note: Even though the project ID is not used in the service method,
    // we still extract it to maintain consistency and for potential future use
    projectExtractor
        .extractProjectDetailsAdmin(user, EntityUtils.normalizeId(projectKey))
        .getProjectId();
    return testFolderService.getFolderById(folderId);
  }

  /**
   * Retrieves all test folders associated with a project.
   *
   * @param projectKey The key of the project.
   * @return A list of data transfer objects ({@link TestFolderRS}) representing the test folders.
   */
  @PreAuthorize(IS_ADMIN)
  @GetMapping
  @Operation(summary = "Get Test Folders by project ID")
  List<TestFolderRS> getTestFolderByProjectId(@PathVariable("projectKey") String projectKey,
      @AuthenticationPrincipal ReportPortalUser user) {
    return testFolderService.getFolderByProjectID(
        projectExtractor
            .extractProjectDetailsAdmin(user, EntityUtils.normalizeId(projectKey))
            .getProjectId());
  }
}
