package com.epam.reportportal.core.tms.controller;

import com.epam.reportportal.core.tms.dto.TmsAttributeRQ;
import com.epam.reportportal.core.tms.dto.TmsAttributeRS;
import com.epam.reportportal.core.tms.service.TmsAttributeService;
import com.epam.reportportal.infrastructure.persistence.commons.EntityUtils;
import com.epam.reportportal.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.infrastructure.persistence.commons.querygen.Filter;
import com.epam.reportportal.infrastructure.persistence.entity.tms.TmsAttribute;
import com.epam.reportportal.model.Page;
import com.epam.reportportal.util.OffsetRequest;
import com.epam.reportportal.util.ProjectExtractor;
import com.epam.reportportal.ws.resolver.FilterFor;
import com.epam.reportportal.ws.resolver.PagingOffset;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.QueryParam;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/project/{projectKey}/tms/attribute")
@RequiredArgsConstructor
@Validated
@Tag(name = "TMS Attribute Controller", description = "TMS Attribute management operations")
public class TmsAttributeController {

  private final TmsAttributeService tmsAttributeService;
  private final ProjectExtractor projectExtractor;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Create TMS Attribute", description = "Creates a new TMS attribute")
  @ApiResponse(responseCode = "201", description = "TMS Attribute created successfully")
  @ApiResponse(responseCode = "400", description = "Bad request")
  @ApiResponse(responseCode = "409", description = "TMS Attribute with such key already exists in project")
  public TmsAttributeRS createAttribute(
      @PathVariable String projectKey,
      @RequestBody @Valid TmsAttributeRQ request,
      @AuthenticationPrincipal ReportPortalUser user) {
    var projectId = projectExtractor
        .extractMembershipDetails(user, EntityUtils.normalizeId(projectKey))
        .getProjectId();
    return tmsAttributeService.create(projectId, request);
  }

  @PatchMapping("/{attributeId}")
  @Operation(summary = "Patch TMS Attribute", description = "Patch existing TMS attribute")
  @ApiResponse(responseCode = "200", description = "TMS Attribute patched successfully")
  @ApiResponse(responseCode = "400", description = "Bad request")
  @ApiResponse(responseCode = "404", description = "TMS Attribute not found")
  @ApiResponse(responseCode = "409", description = "TMS Attribute with such key already exists in project")
  public TmsAttributeRS patchAttribute(
      @PathVariable String projectKey,
      @PathVariable @NotNull @Parameter(description = "TMS Attribute ID") Long attributeId,
      @RequestBody @Valid TmsAttributeRQ request,
      @AuthenticationPrincipal ReportPortalUser user) {
    var projectId = projectExtractor
        .extractMembershipDetails(user, EntityUtils.normalizeId(projectKey))
        .getProjectId();
    return tmsAttributeService.patch(projectId, attributeId, request);
  }

  @GetMapping
  @Operation(summary = "Get all TMS Attributes", description = "Retrieves paginated list of TMS attributes with filtering and trigram search support")
  @ApiResponse(responseCode = "200", description = "TMS Attributes retrieved successfully")
  public Page<TmsAttributeRS> getAllAttributes(
      @PathVariable String projectKey,
      @PagingOffset(sortable = TmsAttribute.class) OffsetRequest offsetRequest,
      @FilterFor(TmsAttribute.class) Filter filter,
      @AuthenticationPrincipal ReportPortalUser user) {
    var projectId = projectExtractor
        .extractMembershipDetails(user, EntityUtils.normalizeId(projectKey))
        .getProjectId();
    return tmsAttributeService.getAll(projectId, filter, offsetRequest);
  }

  @GetMapping("/{attributeId}")
  @Operation(summary = "Get TMS Attribute by ID", description = "Retrieves TMS attribute by its ID")
  @ApiResponse(responseCode = "200", description = "TMS Attribute retrieved successfully")
  @ApiResponse(responseCode = "404", description = "TMS Attribute not found")
  public TmsAttributeRS getAttributeById(
      @PathVariable String projectKey,
      @PathVariable @NotNull @Parameter(description = "TMS Attribute ID") Long attributeId,
      @AuthenticationPrincipal ReportPortalUser user) {
    var projectId = projectExtractor
        .extractMembershipDetails(user, EntityUtils.normalizeId(projectKey))
        .getProjectId();
    return tmsAttributeService.getById(projectId, attributeId);
  }

  @GetMapping("/key")
  @Operation(summary = "Get distinct TMS Attribute keys", description = "Retrieves distinct attribute keys filtered by user input for autocomplete")
  @ApiResponse(responseCode = "200", description = "TMS Attribute keys retrieved successfully")
  public List<String> getAllKeys(
      @PathVariable String projectKey,
      @RequestParam(value = "search", required = false) String search,
      @AuthenticationPrincipal ReportPortalUser user) {
    var projectId = projectExtractor
        .extractMembershipDetails(user, EntityUtils.normalizeId(projectKey))
        .getProjectId();
    return tmsAttributeService.getKeysByCriteria(projectId, search);
  }

  @GetMapping("/value")
  @Operation(summary = "Get distinct TMS Attribute values", description = "Retrieves distinct attribute values filtered by user input for autocomplete")
  @ApiResponse(responseCode = "200", description = "TMS Attribute values retrieved successfully")
  public List<String> getAllValues(
      @PathVariable String projectKey,
      @RequestParam(value = "search", required = false) String search,
      @AuthenticationPrincipal ReportPortalUser user) {
    var projectId = projectExtractor
        .extractMembershipDetails(user, EntityUtils.normalizeId(projectKey))
        .getProjectId();
    return tmsAttributeService.getValuesByCriteria(projectId, search);
  }
}
