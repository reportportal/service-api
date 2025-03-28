package com.epam.ta.reportportal.core.tms.controller;

import static com.epam.ta.reportportal.auth.permissions.Permissions.ADMIN_ONLY;

import com.epam.ta.reportportal.core.tms.dto.ProductVersionRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsProductVersionRS;
import com.epam.ta.reportportal.core.tms.service.ProductVersionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/project/{projectId}/tms/productversion")
@Tag(name = "Product Version", description = "Product Version API collection")
@RequiredArgsConstructor
public class ProductVersionController {

  private final ProductVersionService productVersionService;

  @PreAuthorize(ADMIN_ONLY)
  @GetMapping("/{productVersionId}")
  TmsProductVersionRS getById(@PathVariable("projectId") final long projectId,
                             @PathVariable("productVersionId") final long productVersionId) {
    return productVersionService.getById(projectId, productVersionId);
  }

  @PreAuthorize(ADMIN_ONLY)
  @PostMapping
  TmsProductVersionRS createVersion(@PathVariable("projectId") final long projectId,
                                   @RequestBody final ProductVersionRQ inputDto) {
    return productVersionService.create(projectId, inputDto);
  }

  @PreAuthorize(ADMIN_ONLY)
  @PutMapping("/{productVersionId}")
  TmsProductVersionRS updateVersion(@PathVariable("projectId") final long projectId,
                                 @PathVariable("productVersionId") final long productVersionId,
                                 @RequestBody final ProductVersionRQ inputDto) {
    return productVersionService.update(projectId, productVersionId, inputDto);
  }

  @PreAuthorize(ADMIN_ONLY)
  @DeleteMapping("/{productVersionId}")
  void deleteVersion(@PathVariable("projectId") final long projectId,
                     @PathVariable("productVersionId") final long productVersionId) {
    productVersionService.delete(projectId, productVersionId);
  }
}
