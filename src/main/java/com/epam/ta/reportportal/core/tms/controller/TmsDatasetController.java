package com.epam.ta.reportportal.core.tms.controller;

import static com.epam.ta.reportportal.auth.permissions.Permissions.ADMIN_ONLY;

import com.epam.ta.reportportal.core.tms.dto.TmsDatasetRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsDatasetRS;
import com.epam.ta.reportportal.core.tms.service.TmsDatasetService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
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

@RestController
@RequestMapping("/project/{projectId}/tms/dataset")
@RequiredArgsConstructor
public class TmsDatasetController {

  private final TmsDatasetService tmsDatasetService;

  /**
   * Create a new Dataset.
   *
   * @param projectId The ID of the project.
   * @param datasetRQ Dataset creation request object.
   * @return Created Dataset information.
   */
  @PostMapping
  @PreAuthorize(ADMIN_ONLY)
  public TmsDatasetRS create(@PathVariable("projectId") Long projectId,
      @RequestBody TmsDatasetRQ datasetRQ) {
    return tmsDatasetService.create(projectId, datasetRQ);
  }

  /**
   * Extract Dataset from a file
   *
   * @param projectId The ID of the project
   * @param file      file containing dataset data
   * @return Response with created dataset details
   */
  @PostMapping(path = "/upload", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
  @PreAuthorize(ADMIN_ONLY)
  public List<TmsDatasetRS> uploadFromFile(@PathVariable("projectId") Long projectId,
      @RequestPart MultipartFile file) {
    return tmsDatasetService.uploadFromFile(projectId, file);
  }

  /**
   * Retrieve a list of Datasets for the specified project.
   *
   * @param projectId The ID of the project.
   * @return List of Datasets.
   */
  @GetMapping
  @PreAuthorize(ADMIN_ONLY)
  public List<TmsDatasetRS> getByProjectId(@PathVariable("projectId") Long projectId) {
    return tmsDatasetService.getByProjectId(projectId);
  }

  /**
   * Retrieve details of a specific Dataset.
   *
   * @param projectId The ID of the project.
   * @param datasetId The ID of the Dataset.
   * @return Dataset details.
   */
  @GetMapping("/{datasetId}")
  @PreAuthorize(ADMIN_ONLY)
  public TmsDatasetRS getById(@PathVariable("projectId") Long projectId,
      @PathVariable("datasetId") Long datasetId) {
    return tmsDatasetService.getById(projectId, datasetId);
  }

  /**
   * Fully update a Dataset.
   *
   * @param projectId The ID of the project.
   * @param datasetId The ID of the Dataset.
   * @param datasetRQ Dataset update request object.
   * @return Updated Dataset details.
   */
  @PutMapping("/{datasetId}")
  @PreAuthorize(ADMIN_ONLY)
  public TmsDatasetRS update(@PathVariable("projectId") Long projectId,
      @PathVariable("datasetId") Long datasetId, @RequestBody TmsDatasetRQ datasetRQ) {
    return tmsDatasetService.update(projectId, datasetId, datasetRQ);
  }

  /**
   * Patch a Dataset.
   *
   * @param projectId          The ID of the project.
   * @param datasetId          The ID of the Dataset.
   * @param tmsDatasetUpdateRQ Patch dataset request.
   * @return Updated Dataset details.
   */
  @PatchMapping("/{datasetId}")
  @PreAuthorize(ADMIN_ONLY)
  public TmsDatasetRS patch(@PathVariable("projectId") Long projectId,
      @PathVariable("datasetId") Long datasetId, @RequestBody TmsDatasetRQ tmsDatasetUpdateRQ) {
    return tmsDatasetService.patch(projectId, datasetId, tmsDatasetUpdateRQ);
  }

  /**
   * Delete a Dataset.
   *
   * @param projectId The ID of the project.
   * @param datasetId The ID of the Dataset.
   */
  @DeleteMapping("/{datasetId}")
  @PreAuthorize(ADMIN_ONLY)
  public void delete(@PathVariable("projectId") Long projectId,
      @PathVariable("datasetId") Long datasetId) {
    tmsDatasetService.delete(projectId, datasetId);
  }
}
