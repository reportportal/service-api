package com.epam.ta.reportportal.core.tms.controller;

import com.epam.ta.reportportal.core.tms.dto.TmsAttributeRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsAttributeRS;
import com.epam.ta.reportportal.core.tms.service.TmsAttributeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/tms/attribute")
@RequiredArgsConstructor
@Validated
@Tag(name = "TMS Attribute Controller", description = "TMS Attribute management operations")
public class TmsAttributeController {

  private final TmsAttributeService tmsAttributeService;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Create TMS Attribute", description = "Creates a new TMS attribute")
  @ApiResponse(responseCode = "201", description = "TMS Attribute created successfully")
  @ApiResponse(responseCode = "400", description = "Bad request")
  @ApiResponse(responseCode = "409", description = "TMS Attribute with such key already exists")
  public TmsAttributeRS createAttribute(@RequestBody @Valid TmsAttributeRQ request) {
    return tmsAttributeService.create(request);
  }

  @PatchMapping("/{attributeId}")
  @Operation(summary = "Patch TMS Attribute", description = "Patch existing TMS attribute")
  @ApiResponse(responseCode = "200", description = "TMS Attribute patched successfully")
  @ApiResponse(responseCode = "400", description = "Bad request")
  @ApiResponse(responseCode = "404", description = "TMS Attribute not found")
  @ApiResponse(responseCode = "409", description = "TMS Attribute with such key already exists")
  public TmsAttributeRS patchAttribute(
      @PathVariable @NotNull @Parameter(description = "TMS Attribute ID") Long attributeId,
      @RequestBody @Valid TmsAttributeRQ request) {
    return tmsAttributeService.patch(attributeId, request);
  }

  @GetMapping
  @Operation(summary = "Get all TMS Attributes", description = "Retrieves paginated list of TMS attributes")
  @ApiResponse(responseCode = "200", description = "TMS Attributes retrieved successfully")
  public com.epam.ta.reportportal.model.Page<TmsAttributeRS> getAllAttributes(
      @PageableDefault(size = 50, sort = "id", direction = Sort.Direction.ASC)
      @Parameter(description = "Pagination parameters") Pageable pageable) {
    return tmsAttributeService.getAll(pageable);
  }

  @GetMapping("/{attributeId}")
  @Operation(summary = "Get TMS Attribute by ID", description = "Retrieves TMS attribute by its ID")
  @ApiResponse(responseCode = "200", description = "TMS Attribute retrieved successfully")
  @ApiResponse(responseCode = "404", description = "TMS Attribute not found")
  public TmsAttributeRS getAttributeById(
      @PathVariable @NotNull @Parameter(description = "TMS Attribute ID") Long attributeId) {
    return tmsAttributeService.getById(attributeId);
  }
}
