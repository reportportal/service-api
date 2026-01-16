package com.epam.reportportal.core.tms.service;

import com.epam.reportportal.core.tms.dto.DuplicateTmsTestFolderRS;
import com.epam.reportportal.core.tms.dto.NewTestFolderRQ;
import com.epam.reportportal.core.tms.dto.TmsTestCaseRQ;
import com.epam.reportportal.core.tms.dto.TmsTestFolderExportFileType;
import com.epam.reportportal.core.tms.dto.TmsTestFolderRQ;
import com.epam.reportportal.core.tms.dto.TmsTestFolderRS;
import com.epam.reportportal.infrastructure.persistence.commons.querygen.Filter;
import com.epam.reportportal.infrastructure.persistence.entity.tms.TmsTestFolder;
import com.epam.reportportal.model.Page;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Pageable;

/**
 * Service interface for managing TMS Test Folders.
 */
public interface TmsTestFolderService extends CrudService<TmsTestFolderRQ, TmsTestFolderRS, Long> {

  /**
   * Retrieves test folders by criteria with pagination.
   *
   * @param projectId The ID of the project.
   * @param filter    The filter criteria.
   * @param pageable  Pagination parameters.
   * @return A paginated list of test folders.
   */
  Page<TmsTestFolderRS> getFoldersByCriteria(long projectId, Filter filter, Pageable pageable);

  /**
   * Exports a test folder and its hierarchy to the specified format.
   *
   * @param projectId The ID of the project.
   * @param folderId  The ID of the folder to export.
   * @param fileType  The export file format.
   * @param response  The HTTP response to write the exported data to.
   */
  void exportFolderById(Long projectId, Long folderId, TmsTestFolderExportFileType fileType,
      HttpServletResponse response);

  /**
   * Creates a new test folder with basic information.
   *
   * @param projectId     The ID of the project.
   * @param testFolderRQ  The folder creation request.
   * @return The created test folder response DTO.
   */
  TmsTestFolderRS create(long projectId, NewTestFolderRQ testFolderRQ);

  /**
   * Checks if a test folder exists by its ID.
   *
   * @param projectId    The ID of the project.
   * @param testFolderId The ID of the test folder.
   * @return true if the folder exists, false otherwise.
   */
  Boolean existsById(long projectId, Long testFolderId);

  /**
   * Resolves and sets test folder information in the test case request.
   *
   * @param testCaseRequest The test case request to update.
   * @param testFolderId    The ID of the test folder (optional).
   * @param testFolderName  The name of the test folder (optional).
   */
  void resolveTestFolderRQ(TmsTestCaseRQ testCaseRequest, Long testFolderId, String testFolderName);

  /**
   * Resolves target folder.
   * Either creates a new folder or validates existing folder.
   *
   * @param projectId    The ID of the project.
   * @param testFolderId The ID of existing folder (optional).
   * @param testFolderRQ The new folder information (optional).
   * @return The ID of the target folder.
   */
  Long resolveTargetFolderId(long projectId, Long testFolderId, NewTestFolderRQ testFolderRQ);

  /**
   * Retrieves a test folder entity by its ID.
   *
   * @param projectId    The ID of the project.
   * @param testFolderId The ID of the test folder.
   * @return The test folder entity.
   */
  TmsTestFolder getEntityById(long projectId, Long testFolderId);

  /**
   * Duplicates a test folder with all its subfolders and test cases.
   *
   * @param projectId The ID of the project.
   * @param folderId  The ID of the folder to duplicate.
   * @param inputDto  The request containing name and parent folder information.
   * @return The duplicated folder details with duplication statistics.
   */
  DuplicateTmsTestFolderRS duplicateFolder(long projectId, Long folderId, TmsTestFolderRQ inputDto);

  /**
   * Retrieves test folders by test plan ID with pagination.
   * Returns folders containing test cases that are part of the specified test plan.
   *
   * @param projectId  the project ID
   * @param testPlanId the test plan ID
   * @param pageable   pagination parameters
   * @return page of test folders with count of test cases
   */
  Page<TmsTestFolderRS> getFoldersByTestPlanId(Long projectId, Long testPlanId, Pageable pageable);

  /**
   * Resolves folder path to folder ID, creating missing folders as needed.
   * <p>
   * If parentFolderId is provided, the path hierarchy is created under that folder.
   * If parentFolderId is null, the path hierarchy is created from the project root.
   * </p>
   *
   * @param projectId      the project ID
   * @param parentFolderId optional parent folder ID (from API parameter)
   * @param pathHierarchy  list of folder names from root to leaf
   * @return the ID of the leaf folder (last in path), or parentFolderId if path is empty
   */
  Long resolveFolderPath(Long projectId, Long parentFolderId, List<String> pathHierarchy);

  /**
   * Resolves folder paths for multiple test cases efficiently with caching.
   * <p>
   * Uses a cache to avoid duplicate folder creation and lookups within the same import batch.
   * </p>
   *
   * @param projectId       the project ID
   * @param parentFolderId  optional parent folder ID (from API parameter)
   * @param pathHierarchies list of path hierarchies to resolve
   * @return map of path (joined with "/") to resolved folder ID
   */
  Map<String, Long> resolveFolderPathsBatch(Long projectId, Long parentFolderId,
      List<List<String>> pathHierarchies);
}