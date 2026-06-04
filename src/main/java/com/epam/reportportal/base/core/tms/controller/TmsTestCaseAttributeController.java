package com.epam.reportportal.base.core.tms.controller;

import com.epam.reportportal.base.core.tms.dto.GetAttributesByTestCaseIdsRQ;
import com.epam.reportportal.base.core.tms.dto.TmsAttributeRS;
import com.epam.reportportal.base.core.tms.service.TmsTestCaseAttributeService;
import com.epam.reportportal.base.infrastructure.persistence.commons.EntityUtils;
import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsAttribute;
import com.epam.reportportal.base.model.Page;
import com.epam.reportportal.base.util.OffsetRequest;
import com.epam.reportportal.base.util.ProjectExtractor;
import com.epam.reportportal.base.ws.resolver.PagingOffset;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for retrieving TMS attributes associated with specific test cases.
 */
@RestController
@RequestMapping("/v1/project/{projectKey}/tms/test-case/attribute")
@RequiredArgsConstructor
@Validated
@Tag(name = "TMS Test Case Attribute Controller",
    description = "Operations for retrieving TMS attributes by test cases")
public class TmsTestCaseAttributeController {

  private final TmsTestCaseAttributeService tmsTestCaseAttributeService;
  private final ProjectExtractor projectExtractor;

  /**
   * Retrieves unique TMS attributes assigned to the specified test cases with pagination.
   *
   * @param projectKey    The key of the project.
   * @param request       Request body containing list of test case IDs.
   * @param offsetRequest Pagination and sorting parameters.
   * @param user          Authenticated user.
   * @return A paginated list of unique TMS attributes.
   */
  @PostMapping
  @Operation(
      summary = "Get attributes by test case IDs",
      description = "Retrieves unique TMS attributes assigned to the specified test cases "
          + "with pagination and sorting support."
  )
  @ApiResponse(responseCode = "200", description = "Attributes retrieved successfully")
  @ApiResponse(responseCode = "400", description = "Bad request - test case IDs must not be empty")
  public Page<TmsAttributeRS> getAttributesByTestCaseIds(
      @PathVariable String projectKey,
      @RequestBody @Valid GetAttributesByTestCaseIdsRQ request,
      @PagingOffset(sortable = TmsAttribute.class) OffsetRequest offsetRequest,
      @AuthenticationPrincipal ReportPortalUser user) {
    var projectId = projectExtractor
        .extractMembershipDetails(user, EntityUtils.normalizeId(projectKey))
        .getProjectId();
    return tmsTestCaseAttributeService.getAttributesByTestCaseIds(
        projectId, request.getTestCaseIds(), offsetRequest);
  }
}
