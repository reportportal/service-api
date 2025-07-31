package com.epam.ta.reportportal.core.tms.controller;

import com.epam.ta.reportportal.commons.EntityUtils;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.tms.dto.TmsDatasetRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsDatasetRS;
import com.epam.ta.reportportal.core.tms.service.TmsDatasetService;
import com.epam.ta.reportportal.util.ProjectExtractor;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
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
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Controller for managing datasets within a project. Provides endpoints to
 * create, retrieve, update, and delete datasets, as well as upload datasets
 * from files. All endpoints are secured and require administrator privileges.
 */
@RestController
@RequestMapping("/v1/project/{projectKey}/tms/dataset")
@Tag(name = "Dataset", description = "Dataset API collection")
@RequiredArgsConstructor
public class TmsDatasetController {

  private final TmsDatasetService tmsDatasetService;
  private final ProjectExtractor projectExtractor;

  /**
   * Creates a new dataset in the specified project.
   *
   * @param projectKey The key of the project.
   * @param datasetRQ A request payload ({@link TmsDatasetRQ}) containing dataset details.
   * @return A data transfer object ({@link TmsDatasetRS}) with the created dataset's details.
   */
  @PostMapping
  public TmsDatasetRS create(@PathVariable("projectKey") String projectKey,
      @RequestBody TmsDatasetRQ datasetRQ,
      @AuthenticationPrincipal ReportPortalUser user) {
    return tmsDatasetService.create(
        projectExtractor
            .extractMembershipDetails(user, EntityUtils.normalizeId(projectKey))
            .getProjectId(),
        datasetRQ);
  }

  /**
   * Uploads a dataset from a file.
   *
   * @param projectKey The key of the project.
   * @param file      The file containing dataset data.
   * @return A list of data transfer objects ({@link TmsDatasetRS}) representing created datasets.
   */
  @PostMapping(path = "/upload", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
  public List<TmsDatasetRS> uploadFromFile(@PathVariable("projectKey") String projectKey,
      @RequestPart MultipartFile file,
      @AuthenticationPrincipal ReportPortalUser user) {
    return tmsDatasetService.uploadFromFile(
        projectExtractor
            .extractMembershipDetails(user, EntityUtils.normalizeId(projectKey))
            .getProjectId(),
        file);
  }

  /**
   * Retrieves all datasets for the specified project.
   *
   * @param projectKey The key of the project.
   * @return A list of datasets ({@link TmsDatasetRS}).
   */
  @GetMapping
  public List<TmsDatasetRS> getByProjectId(@PathVariable("projectKey") String projectKey,
      @AuthenticationPrincipal ReportPortalUser user) {
    return tmsDatasetService.getByProjectId(
        projectExtractor
            .extractMembershipDetails(user, EntityUtils.normalizeId(projectKey))
            .getProjectId());
  }

  /**
   * Fetches details of a specific dataset by its ID.
   *
   * @param projectKey The key of the project.
   * @param datasetId The ID of the dataset to retrieve.
   * @return A dataset's details ({@link TmsDatasetRS}).
   */
  @GetMapping("/{datasetId}")
  public TmsDatasetRS getById(@PathVariable("projectKey") String projectKey,
      @PathVariable("datasetId") Long datasetId,
      @AuthenticationPrincipal ReportPortalUser user) {
    return tmsDatasetService.getById(
        projectExtractor
            .extractMembershipDetails(user, EntityUtils.normalizeId(projectKey))
            .getProjectId(),
        datasetId);
  }

  /**
   * Updates an existing dataset with new data.
   *
   * @param projectKey The key of the project.
   * @param datasetId The ID of the dataset to update.
   * @param datasetRQ A request payload ({@link TmsDatasetRQ}) containing updated fields.
   * @return The updated dataset details ({@link TmsDatasetRS}).
   */
  @PutMapping("/{datasetId}")
  public TmsDatasetRS update(@PathVariable("projectKey") String projectKey,
      @PathVariable("datasetId") Long datasetId,
      @RequestBody TmsDatasetRQ datasetRQ,
      @AuthenticationPrincipal ReportPortalUser user) {
    return tmsDatasetService.update(
        projectExtractor
            .extractMembershipDetails(user, EntityUtils.normalizeId(projectKey))
            .getProjectId(),
        datasetId,
        datasetRQ);
  }

  /**
   * Applies partial modifications to an existing dataset.
   *
   * @param projectKey The key of the project.
   * @param datasetId The ID of the dataset to patch.
   * @param tmsDatasetUpdateRQ A request payload ({@link TmsDatasetRQ}) containing updated fields.
   * @return The updated dataset details ({@link TmsDatasetRS}).
   */
  @PatchMapping("/{datasetId}")
  public TmsDatasetRS patch(@PathVariable("projectKey") String projectKey,
      @PathVariable("datasetId") Long datasetId,
      @RequestBody TmsDatasetRQ tmsDatasetUpdateRQ,
      @AuthenticationPrincipal ReportPortalUser user) {
    return tmsDatasetService.patch(
        projectExtractor
            .extractMembershipDetails(user, EntityUtils.normalizeId(projectKey))
            .getProjectId(),
        datasetId,
        tmsDatasetUpdateRQ);
  }

  /**
   * Deletes a dataset from the project.
   *
   * @param projectKey The key of the project.
   * @param datasetId The ID of the dataset to delete.
   */
  @DeleteMapping("/{datasetId}")
  public void delete(@PathVariable("projectKey") String projectKey,
      @PathVariable("datasetId") Long datasetId,
      @AuthenticationPrincipal ReportPortalUser user) {
    tmsDatasetService.delete(
        projectExtractor
            .extractMembershipDetails(user, EntityUtils.normalizeId(projectKey))
            .getProjectId(),
        datasetId);
  }
}
