package com.epam.ta.reportportal.core.tms.controller;

import static com.epam.ta.reportportal.auth.permissions.Permissions.IS_ADMIN;

import com.epam.ta.reportportal.commons.EntityUtils;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.tms.dto.ProductVersionRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsProductVersionRS;
import com.epam.ta.reportportal.core.tms.service.ProductVersionService;
import com.epam.ta.reportportal.util.ProjectExtractor;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for managing product versions within a project. Each endpoint
 * in this controller is secured and requires the user to have administrator
 * privileges. Operations supported include creating, retrieving, updating,
 * and deleting product versions associated with a specific project.
 */
@RestController
@RequestMapping("/v1/project/{projectKey}/tms/productversion")
@Tag(name = "Product Version", description = "Product Version API collection")
@RequiredArgsConstructor
public class ProductVersionController {

  private final ProductVersionService productVersionService;
  private final ProjectExtractor projectExtractor;

  /**
   * Retrieves a specific product version by its ID within a project.
   *
   * @param projectKey        The key of the project to which the product version belongs.
   * @param productVersionId The ID of the product version to retrieve.
   * @return A data transfer object ({@link TmsProductVersionRS}) containing details of the product version.
   */
  @PreAuthorize(IS_ADMIN)
  @GetMapping("/{productVersionId}")
  TmsProductVersionRS getById(@PathVariable("projectKey") String projectKey,
      @PathVariable("productVersionId") final long productVersionId,
      @AuthenticationPrincipal ReportPortalUser user) {
    return productVersionService.getById(
        projectExtractor
            .extractProjectDetailsAdmin(EntityUtils.normalizeId(projectKey))
            .getProjectId(),
        productVersionId);
  }

  /**
   * Creates a new product version in the specified project.
   *
   * @param projectKey The key of the project to which the new product version will be added.
   * @param inputDto  A request payload ({@link ProductVersionRQ}) containing information
   *                  about the product version to create.
   * @return A data transfer object ({@link TmsProductVersionRS}) with details of the created product version.
   */
  @PreAuthorize(IS_ADMIN)
  @PostMapping
  TmsProductVersionRS createVersion(@PathVariable("projectKey") String projectKey,
      @RequestBody final ProductVersionRQ inputDto,
      @AuthenticationPrincipal ReportPortalUser user) {
    return productVersionService.create(
        projectExtractor
            .extractProjectDetailsAdmin(EntityUtils.normalizeId(projectKey))
            .getProjectId(),
        inputDto);
  }

  /**
   * Updates the details of an existing product version in a project.
   *
   * @param projectKey        The key of the project to which the product version belongs.
   * @param productVersionId The ID of the product version to update.
   * @param inputDto         A request payload ({@link ProductVersionRQ}) containing updated information
   *                         for the product version.
   * @return A data transfer object ({@link TmsProductVersionRS}) with updated details of the product version.
   */
  @PreAuthorize(IS_ADMIN)
  @PutMapping("/{productVersionId}")
  TmsProductVersionRS updateVersion(@PathVariable("projectKey") String projectKey,
      @PathVariable("productVersionId") final long productVersionId,
      @RequestBody final ProductVersionRQ inputDto,
      @AuthenticationPrincipal ReportPortalUser user) {
    return productVersionService.update(
        projectExtractor
            .extractProjectDetailsAdmin(EntityUtils.normalizeId(projectKey))
            .getProjectId(),
        productVersionId,
        inputDto);
  }

  /**
   * Deletes a specific product version from a project.
   *
   * @param projectKey        The key of the project to which the product version belongs.
   * @param productVersionId The ID of the product version to delete.
   */
  @PreAuthorize(IS_ADMIN)
  @DeleteMapping("/{productVersionId}")
  void deleteVersion(@PathVariable("projectKey") String projectKey,
      @PathVariable("productVersionId") final long productVersionId,
      @AuthenticationPrincipal ReportPortalUser user) {
    productVersionService.delete(
        projectExtractor
            .extractProjectDetailsAdmin(EntityUtils.normalizeId(projectKey))
            .getProjectId(),
        productVersionId);
  }
}
