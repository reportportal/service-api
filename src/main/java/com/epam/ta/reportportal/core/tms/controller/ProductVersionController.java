package com.epam.ta.reportportal.core.tms.controller;

import static com.epam.ta.reportportal.auth.permissions.Permissions.IS_ADMIN;

import com.epam.ta.reportportal.core.tms.dto.ProductVersionRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsProductVersionRS;
import com.epam.ta.reportportal.core.tms.service.ProductVersionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/project/{projectId}/tms/productversion")
@Tag(name = "Product Version", description = "Product Version API collection")
@RequiredArgsConstructor
public class ProductVersionController {

  private final ProductVersionService productVersionService;

  @PreAuthorize(IS_ADMIN)
  @GetMapping("/{productVersionId}")
  TmsProductVersionRS getById(@PathVariable("projectId") final long projectId,
                             @PathVariable("productVersionId") final long productVersionId) {
    return productVersionService.getById(projectId, productVersionId);
  }

  @PreAuthorize(IS_ADMIN)
  @PostMapping
  TmsProductVersionRS createVersion(@PathVariable("projectId") final long projectId,
                                   @RequestBody final ProductVersionRQ inputDto) {
    return productVersionService.create(projectId, inputDto);
  }

  @PreAuthorize(IS_ADMIN)
  @PutMapping("/{productVersionId}")
  TmsProductVersionRS updateVersion(@PathVariable("projectId") final long projectId,
                                 @PathVariable("productVersionId") final long productVersionId,
                                 @RequestBody final ProductVersionRQ inputDto) {
    return productVersionService.update(projectId, productVersionId, inputDto);
  }

  @PreAuthorize(IS_ADMIN)
  @DeleteMapping("/{productVersionId}")
  void deleteVersion(@PathVariable("projectId") final long projectId,
                     @PathVariable("productVersionId") final long productVersionId) {
    productVersionService.delete(projectId, productVersionId);
  }
}
