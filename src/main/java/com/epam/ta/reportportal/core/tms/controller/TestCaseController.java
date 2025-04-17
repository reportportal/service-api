package com.epam.ta.reportportal.core.tms.controller;

import static com.epam.ta.reportportal.auth.permissions.Permissions.IS_ADMIN;

import com.epam.ta.reportportal.commons.EntityUtils;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.tms.dto.TmsTestCaseRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsTestCaseRS;
import com.epam.ta.reportportal.core.tms.service.TmsTestCaseService;
import com.epam.ta.reportportal.util.ProjectExtractor;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for managing test cases associated with a project. All endpoints
 * are secured and require administrator privileges. Operations supported
 * include retrieval, creation, updating, and patching test cases.
 */
@RestController
@RequestMapping("/project/{projectKey}/tms/test-case")
@Tag(name = "Test Case", description = "Test Case API collection")
@RequiredArgsConstructor
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
  public TmsTestCaseRS getTestCaseById(@PathVariable("projectKey") String projectKey,
      @PathVariable("testCaseId") final long testCaseId,
      @AuthenticationPrincipal ReportPortalUser user) {
    return tmsTestCaseService.getById(
        projectExtractor
            .extractProjectDetailsAdmin(user, EntityUtils.normalizeId(projectKey))
            .getProjectId(),
        testCaseId
    );
  }

  /**
   * Retrieves all test cases associated with a specific project.
   *
   * @param projectKey The key of the project.
   * @return A list of data transfer objects ({@link TmsTestCaseRS}) representing test cases.
   */
  @PreAuthorize(IS_ADMIN)
  @GetMapping
  public List<TmsTestCaseRS> getTestCaseByProjectId(
      @PathVariable("projectKey") String projectKey,
      @AuthenticationPrincipal ReportPortalUser user) {
    return tmsTestCaseService.getTestCaseByProjectId(
        projectExtractor
            .extractProjectDetailsAdmin(user, EntityUtils.normalizeId(projectKey))
            .getProjectId()
    );
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
  public TmsTestCaseRS createTestCase(@PathVariable("projectKey") String projectKey,
      @RequestBody @Valid final TmsTestCaseRQ inputDto,
      @AuthenticationPrincipal ReportPortalUser user) {
    return tmsTestCaseService.create(
        projectExtractor
            .extractProjectDetailsAdmin(user, EntityUtils.normalizeId(projectKey))
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
  public TmsTestCaseRS updateTestCase(@PathVariable("projectKey") String projectKey,
      @PathVariable("testCaseId") final long testCaseId,
      @RequestBody final TmsTestCaseRQ inputDto,
      @AuthenticationPrincipal ReportPortalUser user) {
    return tmsTestCaseService.update(
        projectExtractor
            .extractProjectDetailsAdmin(user, EntityUtils.normalizeId(projectKey))
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
  public TmsTestCaseRS patchTestCase(@PathVariable("projectKey") String projectKey,
      @PathVariable("testCaseId") final long testCaseId,
      @RequestBody final TmsTestCaseRQ inputDto,
      @AuthenticationPrincipal ReportPortalUser user) {
    return tmsTestCaseService.patch(
        projectExtractor
            .extractProjectDetailsAdmin(user, EntityUtils.normalizeId(projectKey))
            .getProjectId(),
        testCaseId,
        inputDto
    );
  }
}
