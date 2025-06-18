package com.epam.ta.reportportal.core.tms.controller;

import static com.epam.ta.reportportal.auth.permissions.Permissions.IS_ADMIN;

import com.epam.ta.reportportal.commons.EntityUtils;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.tms.db.entity.TmsTestCase;
import com.epam.ta.reportportal.core.tms.dto.TmsTestCaseRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsTestCaseRS;
import com.epam.ta.reportportal.core.tms.dto.batch.BatchDeleteTestCasesRQ;
import com.epam.ta.reportportal.core.tms.dto.batch.BatchPatchTestCasesRQ;
import com.epam.ta.reportportal.core.tms.service.TmsTestCaseService;
import com.epam.ta.reportportal.util.ProjectExtractor;
import com.epam.ta.reportportal.ws.resolver.SortFor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
@RequestMapping("/project/{projectKey}/tms/test-case")
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
  @PreAuthorize(IS_ADMIN)
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
            .extractProjectDetailsAdmin(EntityUtils.normalizeId(projectKey))
            .getProjectId(),
        testCaseId
    );
  }

  /**
   * Retrieves all test cases associated with a specific project.
   * Supports filtering and pagination according to OpenAPI specification.
   *
   * @param projectKey The key of the project.
   * @param search Optional search query string
   * @param pageable Pagination information
   * @return A paginated list of data transfer objects ({@link TmsTestCaseRS}) representing test cases.
   */
  @PreAuthorize(IS_ADMIN)
  @GetMapping
  @Operation(
      summary = "Get test cases for a project by criteria",
      description = "Retrieves test cases associated with a specific project by criteria. Supports filtering, sorting and full-text search."
  )
  @ApiResponse(responseCode = "200", description = "Successful operation")
  public Page<TmsTestCaseRS> getTestCasesByCriteria(
      @PathVariable("projectKey") String projectKey,
      @RequestParam(value = "search", required = false) String search,
//      @FilterFor(TmsTestCase.class) Filter filter,
      @SortFor(TmsTestCase.class) Pageable pageable,
      @AuthenticationPrincipal ReportPortalUser user) {
    throw new UnsupportedOperationException("Method not implemented yet");
  }

  /**
   * Creates a new test case within a specific project.
   *
   * @param projectKey The key of the project to which the new test case will be added.
   * @param inputDto  A request payload ({@link TmsTestCaseRQ}) containing information about the test case to create.
   * @return A data transfer object ({@link TmsTestCaseRS}) with details of the created test case.
   */
  @PreAuthorize(IS_ADMIN)
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
            .extractProjectDetailsAdmin(EntityUtils.normalizeId(projectKey))
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
  @PreAuthorize(IS_ADMIN)
  @PutMapping("/{testCaseId}")
  @Operation(
      summary = "Update a test case",
      description = "Updates the details of an existing test case in a project."
  )
  @ApiResponse(responseCode = "200", description = "Test case updated successfully")
  public TmsTestCaseRS updateTestCase(@PathVariable("projectKey") String projectKey,
      @PathVariable("testCaseId") final long testCaseId,
      @RequestBody final TmsTestCaseRQ inputDto,
      @AuthenticationPrincipal ReportPortalUser user) {
    return tmsTestCaseService.update(
        projectExtractor
            .extractProjectDetailsAdmin(EntityUtils.normalizeId(projectKey))
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
  @PreAuthorize(IS_ADMIN)
  @PatchMapping("/{testCaseId}")
  @Operation(
      summary = "Partially update a test case",
      description = "Applies a set of modifications to an existing test case in a project."
  )
  @ApiResponse(responseCode = "200", description = "Test case patched successfully")
  public TmsTestCaseRS patchTestCase(@PathVariable("projectKey") String projectKey,
      @PathVariable("testCaseId") final long testCaseId,
      @RequestBody final TmsTestCaseRQ inputDto,
      @AuthenticationPrincipal ReportPortalUser user) {
    return tmsTestCaseService.patch(
        projectExtractor
            .extractProjectDetailsAdmin(EntityUtils.normalizeId(projectKey))
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
  @PreAuthorize(IS_ADMIN)
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
    throw new UnsupportedOperationException("Method not implemented yet");
  }

  /**
   * Deletes multiple test cases by their IDs within a project.
   *
   * @param projectKey The key of the project.
   * @param deleteRequest The object contains comma-separated list of test case IDs to delete.
   */
  @PreAuthorize(IS_ADMIN)
  @DeleteMapping("/batch/delete")
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
            .extractProjectDetailsAdmin(EntityUtils.normalizeId(projectKey))
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
  @PreAuthorize(IS_ADMIN)
  @PatchMapping("/batch/patch")
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
            .extractProjectDetailsAdmin(EntityUtils.normalizeId(projectKey))
            .getProjectId(),
        patchRequest
    );
  }

  /**
   * Imports test cases from a file into a project.
   *
   * @param projectKey The key of the project.
   * @param file The file containing test cases to import.
   */
  @PreAuthorize(IS_ADMIN)
  @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Operation(
      summary = "Import test cases",
      description = "Imports test cases from a file into a project.",
      tags = {"Import/Export"}
  )
  @ApiResponse(responseCode = "200", description = "Test cases imported successfully")
  public void importTestCases(@PathVariable("projectKey") String projectKey,
      @RequestPart("file") MultipartFile file,
      @AuthenticationPrincipal ReportPortalUser user) {
    throw new UnsupportedOperationException("Method not implemented yet");
  }

  /**
   * Exports test cases from a project to a file.
   *
   * @param projectKey The key of the project.
   * @param ids Comma-separated list of test case IDs to export. If not provided, all test cases will be exported.
   * @param format Format of the export file.
   * @param includeAttachments Whether to include attachments in the export.
   */
  @PreAuthorize(IS_ADMIN)
  @GetMapping("/export")
  @Operation(
      summary = "Export test cases",
      description = "Exports test cases from a project to a file.",
      tags = {"Import/Export"}
  )
  @ApiResponse(
      responseCode = "200",
      description = "Test cases exported successfully",
      content = @Content(mediaType = "application/octet-stream")
  )
  public void exportTestCases(@PathVariable("projectKey") String projectKey,
      @RequestParam(value = "ids", required = false) String ids,
      @RequestParam(value = "format", required = false, defaultValue = "JSON") String format,
      @RequestParam(value = "includeAttachments", required = false, defaultValue = "false") boolean includeAttachments,
      @AuthenticationPrincipal ReportPortalUser user,
      HttpServletResponse response) {
    throw new UnsupportedOperationException("Method not implemented yet");
  }
}
