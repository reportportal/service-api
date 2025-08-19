package com.epam.ta.reportportal.core.tms.controller;

import com.epam.ta.reportportal.commons.EntityUtils;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.tms.dto.DeleteTagsRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsTestCaseRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsTestCaseRS;
import com.epam.ta.reportportal.core.tms.dto.batch.BatchDeleteTagsRQ;
import com.epam.ta.reportportal.core.tms.dto.batch.BatchDeleteTestCasesRQ;
import com.epam.ta.reportportal.core.tms.dto.batch.BatchPatchTestCasesRQ;
import com.epam.ta.reportportal.core.tms.service.TmsTestCaseService;
import com.epam.ta.reportportal.model.Page;
import com.epam.ta.reportportal.util.ProjectExtractor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Controller for managing test cases associated with a project. All endpoints
 * are secured and require administrator privileges. Operations supported
 * include retrieval, creation, updating, and patching test cases.
 */
@RestController
@RequestMapping("/v1/project/{projectKey}/tms/test-case")
@Tag(name = "Test Case", description = "Test Case API collection")
@RequiredArgsConstructor
@Valid
public class TestCaseController {

  private final TmsTestCaseService tmsTestCaseService;

  private final ProjectExtractor projectExtractor;

  /**
   * Retrieves a specific test case by its ID within a project.
   *
   * @param projectKey  The key of the project to which the test case belongs.
   * @param testCaseId The ID of the test case to retrieve.
   * @return A data transfer object ({@link TmsTestCaseRS}) containing details of the test case.
   */

  @GetMapping("/{testCaseId}")
  @Operation(
      summary = "Get a test case by ID",
      description = "Retrieves a specific test case by its ID within a project."
  )
  @ApiResponse(responseCode = "200", description = "Successful operation")
  public TmsTestCaseRS getTestCaseById(@PathVariable("projectKey") String projectKey,
      @PathVariable("testCaseId") final long testCaseId,
      @AuthenticationPrincipal ReportPortalUser user) {
    return tmsTestCaseService.getById(
        projectExtractor
            .extractMembershipDetails(user, EntityUtils.normalizeId(projectKey))
            .getProjectId(),
        testCaseId
    );
  }

  /**
   * Retrieves test cases associated with a specific project by criteria.
   * Supports filtering, sorting, full-text search and pagination.
   *
   * @param projectKey The key of the project.
   * @param search Optional full-text search query string for searching in test case names and descriptions
   * @param testFolderId Optional test folder ID to filter test cases by specific folder
//   * @param pageable Pagination information including sorting parameters
   * @param user Authenticated user
   * @return A paginated list of data transfer objects ({@link TmsTestCaseRS}) representing test cases.
   */
  @GetMapping
  @Operation(
      summary = "Get test cases for a project by criteria",
      description = "Retrieves test cases associated with a specific project by criteria. "
          + "Supports filtering, sorting and full-text search."
  )
  @ApiResponse(responseCode = "200", description = "Successful operation")
  public Page<TmsTestCaseRS> getTestCasesByCriteria(
      @PathVariable("projectKey") String projectKey,
      @RequestParam(value = "search", required = false) String search,
      @RequestParam(value = "testFolderId", required = false) Long testFolderId,
      //TODO add filter, sort
//      @SortFor(TmsTestCase.class) Pageable pageable,
      @AuthenticationPrincipal ReportPortalUser user) {
    return tmsTestCaseService.getTestCasesByCriteria(
        projectExtractor
            .extractMembershipDetails(user, EntityUtils.normalizeId(projectKey))
            .getProjectId(),
        search,
        testFolderId,
        Pageable.unpaged()); //TODO fix once added filter and sort
  }

  /**
   * Creates a new test case within a specific project.
   *
   * @param projectKey The key of the project to which the new test case will be added.
   * @param inputDto  A request payload ({@link TmsTestCaseRQ}) containing information about the test case to create.
   * @return A data transfer object ({@link TmsTestCaseRS}) with details of the created test case.
   */
  @PostMapping
  @Operation(
      summary = "Create a new test case",
      description = "Creates a new test case within a specific project."
  )
  @ApiResponse(responseCode = "200", description = "Test case created successfully")
  public TmsTestCaseRS createTestCase(@PathVariable("projectKey") String projectKey,
      @RequestBody @Valid final TmsTestCaseRQ inputDto,
      @AuthenticationPrincipal ReportPortalUser user) {
    return tmsTestCaseService.create(
        projectExtractor
            .extractMembershipDetails(user, EntityUtils.normalizeId(projectKey))
            .getProjectId(),
        inputDto
    );
  }

  /**
   * Updates the details of an existing test case in a project.
   *
   * @param projectKey  The key of the project to which the test case belongs.
   * @param testCaseId The ID of the test case to update.
   * @param inputDto   A request payload ({@link TmsTestCaseRQ}) containing updated information for the test case.
   * @return A data transfer object ({@link TmsTestCaseRS}) with updated details of the test case.
   */
  @PutMapping("/{testCaseId}")
  @Operation(
      summary = "Update a test case",
      description = "Updates the details of an existing test case in a project."
  )
  @ApiResponse(responseCode = "200", description = "Test case updated successfully")
  public TmsTestCaseRS updateTestCase(@PathVariable("projectKey") String projectKey,
      @PathVariable("testCaseId") final long testCaseId,
      @RequestBody @Valid final TmsTestCaseRQ inputDto,
      @AuthenticationPrincipal ReportPortalUser user) {
    return tmsTestCaseService.update(
        projectExtractor
            .extractMembershipDetails(user, EntityUtils.normalizeId(projectKey))
            .getProjectId(),
        testCaseId,
        inputDto
    );
  }

  /**
   * Applies a set of modifications to an existing test case in a project.
   * Partial updates to the test case's fields can be handled via this endpoint.
   *
   * @param projectKey  The key of the project to which the test case belongs.
   * @param testCaseId The ID of the test case to patch.
   * @param inputDto   A request payload ({@link TmsTestCaseRQ}) containing the modifications.
   * @return A data transfer object ({@link TmsTestCaseRS}) with patched details of the test case.
   */
  @PatchMapping("/{testCaseId}")
  @Operation(
      summary = "Partially update a test case",
      description = "Applies a set of modifications to an existing test case in a project."
  )
  @ApiResponse(responseCode = "200", description = "Test case patched successfully")
  public TmsTestCaseRS patchTestCase(@PathVariable("projectKey") String projectKey,
      @PathVariable("testCaseId") final long testCaseId,
      @RequestBody @Valid final TmsTestCaseRQ inputDto,
      @AuthenticationPrincipal ReportPortalUser user) {
    return tmsTestCaseService.patch(
        projectExtractor
            .extractMembershipDetails(user, EntityUtils.normalizeId(projectKey))
            .getProjectId(),
        testCaseId,
        inputDto
    );
  }

  /**
   * Deletes a specific test case by its ID within a project.
   *
   * @param projectKey  The key of the project to which the test case belongs.
   * @param testCaseId The ID of the test case to delete.
   */
  @DeleteMapping("/{testCaseId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(
      summary = "Delete a test case",
      description = "Deletes a specific test case by its ID within a project."
  )
  @ApiResponse(responseCode = "204", description = "Test case deleted successfully")
  public void deleteTestCase(@PathVariable("projectKey") String projectKey,
      @PathVariable("testCaseId") final long testCaseId,
      @AuthenticationPrincipal ReportPortalUser user) {
    tmsTestCaseService.delete(projectExtractor
        .extractMembershipDetails(user, EntityUtils.normalizeId(projectKey))
        .getProjectId(), testCaseId);
  }

  /**
   * Deletes multiple test cases by their IDs within a project.
   *
   * @param projectKey The key of the project.
   * @param deleteRequest The object contains comma-separated list of test case IDs to delete.
   */
  @DeleteMapping("/batch")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(
      summary = "Delete multiple test cases",
      description = "Deletes multiple test cases by their IDs within a project.",
      tags = {"Batch Operations"}
  )
  @ApiResponse(responseCode = "204", description = "Test cases deleted successfully")
  public void deleteTestCases(@PathVariable("projectKey") String projectKey,
      @Valid @RequestBody BatchDeleteTestCasesRQ deleteRequest,
      @AuthenticationPrincipal ReportPortalUser user) {
    tmsTestCaseService.delete(
        projectExtractor
            .extractMembershipDetails(user, EntityUtils.normalizeId(projectKey))
            .getProjectId(),
        deleteRequest
    );
  }

  /**
   * Updates multiple test cases at once, particularly useful for changing test case folder for multiple test cases.
   *
   * @param projectKey The key of the project.
   * @param patchRequest Request body containing update information
   *                     and comma-separated list of test case IDs to update.
   */
  @PatchMapping("/batch")
  @Operation(
      summary = "Partially update multiple test cases",
      description = "Updates multiple test cases at once, particularly useful for changing test case folder for multiple test cases.",
      tags = {"Batch Operations"}
  )
  @ApiResponse(responseCode = "200", description = "Test cases updated successfully")
  public void batchPatchTestCases(@PathVariable("projectKey") String projectKey,
      @Valid @RequestBody BatchPatchTestCasesRQ patchRequest,
      @AuthenticationPrincipal ReportPortalUser user) {
    tmsTestCaseService.patch(
        projectExtractor
            .extractMembershipDetails(user, EntityUtils.normalizeId(projectKey))
            .getProjectId(),
        patchRequest
    );
  }

  /**
   * Imports test cases from a file into a project.
   * Supports CSV and JSON file formats.
   *
   * @param projectKey The key of the project.
   * @param file The file containing test cases to import.
   * @return A list of created test cases.
   */
  @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Operation(
      summary = "Import test cases",
      description = "Imports test cases from a file into a project. Supports CSV and JSON formats.",
      tags = {"Import/Export"}
  )
  @ApiResponse(responseCode = "200", description = "Test cases imported successfully")
  public List<TmsTestCaseRS> importTestCases(@PathVariable("projectKey") String projectKey,
      @RequestPart("file") MultipartFile file,
      @AuthenticationPrincipal ReportPortalUser user) {
    return tmsTestCaseService.importFromFile(
        projectExtractor
            .extractMembershipDetails(user, EntityUtils.normalizeId(projectKey))
            .getProjectId(),
        file);
  }

  /**
   * Exports test cases from a project to a file.
   * Supports JSON and CSV formats.
   *
   * @param projectKey The key of the project.
   * @param ids Comma-separated list of test case IDs to export. If not provided, all test cases will be exported.
   * @param format Format of the export file (JSON or CSV).
   * @param includeAttachments Whether to include attachments in the export (only for JSON format).
   */
  @GetMapping("/export")
  @Operation(
      summary = "Export test cases",
      description = "Exports test cases from a project to a file. Supports JSON and CSV formats.",
      tags = {"Import/Export"}
  )
  @ApiResponse(
      responseCode = "200",
      description = "Test cases exported successfully",
      content = @Content(mediaType = "application/octet-stream")
  )
  public void exportTestCases(@PathVariable("projectKey") String projectKey,
      @RequestParam(value = "ids", required = false) List<Long> ids,
      @RequestParam(value = "format", required = false, defaultValue = "JSON") String format,
      @RequestParam(value = "includeAttachments", required = false, defaultValue = "false") boolean includeAttachments,
      @AuthenticationPrincipal ReportPortalUser user,
      HttpServletResponse response) {
    tmsTestCaseService.exportToFile(
        projectExtractor
            .extractMembershipDetails(user, EntityUtils.normalizeId(projectKey))
            .getProjectId(),
        ids,
        format,
        includeAttachments,
        response
    );
  }

  /**
   * Deletes specific tags from a test case by attribute IDs.
   *
   * @param projectKey The key of the project.
   * @param testCaseId The ID of the test case.
   * @param deleteRequest Request containing attribute IDs to delete.
   */
  @DeleteMapping("/{testCaseId}/tags")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(
      summary = "Delete tags from a test case",
      description = "Deletes specific tags from a test case by attribute IDs."
  )
  @ApiResponse(responseCode = "204", description = "Tags deleted successfully")
  public void deleteTagsFromTestCase(@PathVariable("projectKey") String projectKey,
      @PathVariable("testCaseId") final long testCaseId,
      @Valid @RequestBody DeleteTagsRQ deleteRequest,
      @AuthenticationPrincipal ReportPortalUser user) {
    tmsTestCaseService.deleteTagsFromTestCase(
        projectExtractor
            .extractMembershipDetails(user, EntityUtils.normalizeId(projectKey))
            .getProjectId(),
        testCaseId,
        deleteRequest.getTagIds()
    );
  }

  /**
   * Deletes specific tags from multiple test cases.
   *
   * @param projectKey The key of the project.
   * @param deleteRequest Request containing test case IDs and attribute IDs to delete.
   */
  @DeleteMapping("/tags/batch")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(
      summary = "Delete tags from multiple test cases",
      description = "Deletes specific tags from multiple test cases by their IDs.",
      tags = {"Batch Operations"}
  )
  @ApiResponse(responseCode = "204", description = "Tags deleted successfully")
  public void deleteTagsFromTestCases(@PathVariable("projectKey") String projectKey,
      @Valid @RequestBody BatchDeleteTagsRQ deleteRequest,
      @AuthenticationPrincipal ReportPortalUser user) {
    tmsTestCaseService.deleteTagsFromTestCases(
        projectExtractor
            .extractMembershipDetails(user, EntityUtils.normalizeId(projectKey))
            .getProjectId(),
        deleteRequest.getTestCaseIds(),
        deleteRequest.getTagIds()
    );
  }
}
