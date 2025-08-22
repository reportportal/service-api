package com.epam.ta.reportportal.core.tms.service;

import static com.epam.reportportal.rules.exception.ErrorType.NOT_FOUND;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.core.tms.db.entity.TmsTestFolder;
import com.epam.ta.reportportal.core.tms.db.entity.TmsTestFolderIdWithCountOfTestCases;
import com.epam.ta.reportportal.core.tms.db.entity.TmsTestFolderWithCountOfTestCases;
import com.epam.ta.reportportal.core.tms.db.repository.TmsTestFolderRepository;
import com.epam.ta.reportportal.core.tms.dto.TmsTestFolderExportFileType;
import com.epam.ta.reportportal.core.tms.dto.TmsTestFolderRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsTestFolderRQ.ParentTmsTestFolderRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsTestFolderRS;
import com.epam.ta.reportportal.core.tms.mapper.TmsTestFolderMapper;
import com.epam.ta.reportportal.core.tms.mapper.factory.TmsTestFolderExporterFactory;
import com.epam.ta.reportportal.model.Page;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ValidationException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

/**
 * Implementation of the {@link TmsTestFolderService} interface that provides operations for
 * managing test folders within the TMS (Test Management System).
 *
 * <p>This service handles CRUD operations for test folders, including creating, updating, retrieving,
 * and deleting folders. It also supports hierarchical folder structures with parent-child
 * relationships and exporting folder data in various formats.
 */
@Service
@RequiredArgsConstructor
public class TmsTestFolderServiceImpl implements TmsTestFolderService {

  private static final String TEST_FOLDER_NOT_FOUND_BY_ID =
      "Test Folder with id: %d for project: %d";

  private final TmsTestFolderMapper tmsTestFolderMapper;
  private final TmsTestFolderRepository tmsTestFolderRepository;
  private final TmsTestFolderExporterFactory tmsTestFolderExporterFactory;

  private TmsTestCaseService tmsTestCaseService;

  @Autowired
  public void setTmsTestCaseService(
      TmsTestCaseService tmsTestCaseService) {
    this.tmsTestCaseService = tmsTestCaseService;
  }

  /**
   * Creates a new test folder in the specified project.
   *
   * <p>If the input DTO includes parent folder information, the method will establish the
   * parent-child
   * relationship between the folders.
   *
   * @param projectId The ID of the project where the folder will be created
   * @param inputDto  The data transfer object containing folder details
   * @return A response DTO containing the created folder's information
   * @throws ValidationException If the parent folder information is invalid
   */
  @Override
  @Transactional
  public TmsTestFolderRS create(final long projectId, final TmsTestFolderRQ inputDto) {
    var testFolder = tmsTestFolderMapper.convertFromRQ(projectId, inputDto);

    createParentTestFolder(projectId, inputDto.getParentTestFolder(), testFolder);

    return tmsTestFolderMapper.convertFromTmsTestFolderToRS(
        tmsTestFolderRepository.save(testFolder));
  }

  /**
   * Updates an existing test folder or creates a new one if it doesn't exist.
   *
   * <p>This method performs a complete update of the folder, replacing all its properties with the
   * values provided in the input DTO. If the folder doesn't exist, a new one will be created.
   *
   * @param projectId    The ID of the project containing the folder
   * @param testFolderId The ID of the folder to update
   * @param inputDto     The data transfer object containing updated folder details
   * @return A response DTO containing the updated or created folder's information
   */
  @Override
  @Transactional
  public TmsTestFolderRS update(final long projectId, Long testFolderId,
      final TmsTestFolderRQ inputDto) {
    return tmsTestFolderRepository
        .findByIdAndProjectId(testFolderId, projectId)
        .map(existingTestFolder -> {
          tmsTestFolderMapper.update(existingTestFolder,
              tmsTestFolderMapper.convertFromRQ(projectId, inputDto));

          updateParentTestFolder(projectId, inputDto.getParentTestFolder(), existingTestFolder);

          return tmsTestFolderMapper.convertFromTmsTestFolderToRS(
              tmsTestFolderRepository.save(existingTestFolder)
          );
        })
        .orElseGet(() -> create(projectId, inputDto));
  }

  /**
   * Partially updates an existing test folder with the provided values.
   *
   * <p>Unlike the update method, this method only changes the properties that are explicitly set
   * in
   * the input DTO, leaving other properties unchanged.
   *
   * @param projectId    The ID of the project containing the folder
   * @param testFolderId The ID of the folder to patch
   * @param inputDto     The data transfer object containing the fields to update
   * @return A response DTO containing the patched folder's information
   * @throws com.epam.reportportal.rules.exception.ReportPortalException If the folder doesn't exist
   */
  @Override
  public TmsTestFolderRS patch(long projectId, Long testFolderId,
      TmsTestFolderRQ inputDto) {
    return tmsTestFolderRepository
        .findByIdAndProjectId(testFolderId, projectId)
        .map(existingTestFolder -> {
          tmsTestFolderMapper.patch(existingTestFolder,
              tmsTestFolderMapper.convertFromRQ(projectId, inputDto));

          patchParentTestFolder(projectId, inputDto.getParentTestFolder(), existingTestFolder);

          return tmsTestFolderMapper.convertFromTmsTestFolderToRS(
              tmsTestFolderRepository.save(existingTestFolder));
        })
        .orElseThrow(() -> new ReportPortalException(
            NOT_FOUND, TEST_FOLDER_NOT_FOUND_BY_ID.formatted(testFolderId, projectId))
        );
  }

  /**
   * Retrieves a test folder by its ID.
   *
   * <p>This method fetches the folder along with information about the number of subfolders it
   * contains.
   *
   * @param projectId The ID of the project containing the folder
   * @param id        The ID of the folder to retrieve
   * @return A response DTO containing the folder's information
   * @throws ReportPortalException If the folder doesn't exist
   */
  @Override
  @Transactional(readOnly = true)
  public TmsTestFolderRS getById(long projectId, Long id) {
    return tmsTestFolderRepository
        .findByIdWithCountOfTestCases(projectId, id)
        .map(tmsTestFolderWithCountOfTestCases -> {
          var folderWithSubFolders = tmsTestFolderRepository.findByIdWithSubFolders(projectId, id);

          Map<Long, Long> subFolderTestCaseCounts = new HashMap<>();

          if (folderWithSubFolders.isPresent()) {
            var testFolder = folderWithSubFolders.get();
            tmsTestFolderWithCountOfTestCases
                .getTestFolder()
                .setSubFolders(testFolder.getSubFolders());

            if (!CollectionUtils.isEmpty(testFolder.getSubFolders())) {
              var subFolderIds = testFolder.getSubFolders()
                  .stream()
                  .map(TmsTestFolder::getId)
                  .collect(Collectors.toList());

              var testCaseCountResults = tmsTestFolderRepository
                  .findTestCaseCountsByFolderIds(projectId, subFolderIds);
              subFolderTestCaseCounts = testCaseCountResults
                  .stream()
                  .collect(Collectors.toMap(
                      TmsTestFolderIdWithCountOfTestCases::getTestFolderId,
                      TmsTestFolderIdWithCountOfTestCases::getCountOfTestCases
                  ));
            }
          }

          return tmsTestFolderMapper.convertFromTmsTestFolderWithCountOfTestCasesToRS(
              tmsTestFolderWithCountOfTestCases,
              subFolderTestCaseCounts
          );
        })
        .orElseThrow(() -> new ReportPortalException(
            NOT_FOUND, TEST_FOLDER_NOT_FOUND_BY_ID.formatted(id, projectId))
        );
  }

  /**
   * Retrieves test folders in a project.
   *
   * <p>This method returns a paginated list of folders, each with information about the number of
   * subfolders it contains.
   *
   * @param projectId  The ID of the project
   * @param testPlanId The ID of the test plan
   * @param pageable   Pagination parameters
   * @return A paginated list of response DTOs containing folder information
   */
  @Override
  @Transactional(readOnly = true)
  public Page<TmsTestFolderRS> getFoldersByCriteria(
      final long projectId,
      Long testPlanId,
      Pageable pageable) {
    var page = Objects.isNull(testPlanId) ?
        tmsTestFolderRepository
            .findAllByProjectIdWithCountOfTestCases(projectId, pageable) :
        tmsTestFolderRepository //TODO refactor that when proper filtering will be implemented
            .findAllByProjectIdAndTestPlanIdWithCountOfTestCases(projectId, testPlanId, pageable);
    return getTmsTestFoldersWithSubfoldersAndTmsTestCount(projectId, page);
  }

  /**
   * Retrieves all subfolders of a specific folder.
   *
   * <p>This method returns a paginated list of subfolders for the specified parent folder, each
   * with
   * information about the number of subfolders it contains.
   *
   * @param projectId The ID of the project
   * @param folderId  The ID of the parent folder
   * @param pageable  Pagination parameters
   * @return A paginated list of response DTOs containing subfolder information
   */
  @Override
  @Transactional(readOnly = true)
  public Page<TmsTestFolderRS> getSubFolders(long projectId, Long folderId, Pageable pageable) {
    var page = tmsTestFolderRepository.findAllByParentTestFolderIdWithCountOfTestCases(
        projectId, folderId, pageable
    );
    return getTmsTestFoldersWithSubfoldersAndTmsTestCount(projectId, page);
  }

  /**
   * Exports a test folder and its entire hierarchy to the specified format.
   *
   * <p>This method retrieves the folder with its complete hierarchy and uses the appropriate
   * exporter
   * to generate the output in the requested format.
   *
   * @param projectId The ID of the project containing the folder
   * @param folderId  The ID of the folder to export
   * @param fileType  The format to export the folder to
   * @param response  The HTTP response to write the exported data to
   * @throws ReportPortalException         If the folder doesn't exist
   * @throws UnsupportedOperationException If the requested file type is not supported
   */
  @Override
  @Transactional(readOnly = true)
  public void exportFolderById(Long projectId, Long folderId,
      TmsTestFolderExportFileType fileType, HttpServletResponse response) {
    tmsTestFolderExporterFactory
        .getExporter(fileType)
        .export(
            findFolderWithFullHierarchy(projectId, folderId),
            response
        );
  }

  @Override
  @Transactional
  public TmsTestFolderRS create(long projectId, String testFolderName) {
    return create(projectId, tmsTestFolderMapper.convertFromNameToRQ(testFolderName));
  }

  /**
   * This method determines whether a test folder with an id exists in a project.
   *
   * @param projectId project's id
   * @param testFolderId test folder's id
   * @return true if exists, false if not
   */
  @Override
  public Boolean existsById(long projectId, Long testFolderId) {
    return tmsTestFolderRepository.existsByIdAndProjectId(testFolderId, projectId);
  }

  /**
   * Retrieves a test folder with its complete hierarchy of subfolders.
   *
   * <p>This method builds a complete tree structure of the folder and all its subfolders,
   * establishing the parent-child relationships between them.
   *
   * @param projectId The ID of the project containing the folder
   * @param folderId  The ID of the root folder to retrieve
   * @return The folder entity with its complete hierarchy of subfolders
   * @throws ReportPortalException If the folder doesn't exist
   */
  @Transactional(readOnly = true)
  public TmsTestFolder findFolderWithFullHierarchy(Long projectId, Long folderId) {
    var allIds = tmsTestFolderRepository.findAllFolderIdsInHierarchy(projectId, folderId);
    if (allIds.isEmpty()) {
      throw new ReportPortalException(
          NOT_FOUND, TEST_FOLDER_NOT_FOUND_BY_ID.formatted(folderId, projectId));
    }

    var allFolders = tmsTestFolderRepository.findAllById(allIds);

    var folderMap = allFolders
        .stream()
        .collect(Collectors.toMap(TmsTestFolder::getId, Function.identity()));

    allFolders.forEach(
        folder -> {
          var parent = folder.getParentTestFolder();
          if (parent != null && folderMap.containsKey(parent.getId())) {
            var actualParent = folderMap.get(parent.getId());
            if (actualParent.getSubFolders() == null) {
              actualParent.setSubFolders(new ArrayList<>());
            }
            actualParent.getSubFolders().add(folder);
          }
        }
    );

    return Optional
        .ofNullable(folderMap.get(folderId))
        .orElseThrow(() -> new ReportPortalException(
            NOT_FOUND, TEST_FOLDER_NOT_FOUND_BY_ID.formatted(folderId, projectId))
        );
  }

  /**
   * Deletes a test folder and all its subfolders.
   *
   * <p>This method first deletes all test cases associated with the folder and its subfolders,
   * then
   * deletes the folder and subfolder entities themselves.
   *
   * @param projectId The ID of the project containing the folder
   * @param folderId  The ID of the folder to delete
   */
  @Override
  @Transactional
  public void delete(long projectId, Long folderId) {
    tmsTestCaseService.deleteByTestFolderId(projectId, folderId);
    tmsTestFolderRepository.deleteTestFolderWithSubfoldersById(projectId, folderId);
  }

  /**
   * Creates a parent-child relationship between folders during folder creation.
   *
   * <p>This method handles three cases: 1. If parent folder ID is provided, it establishes a link
   * to
   * the existing parent 2. If parent folder name is provided, it creates a new parent folder and
   * links to it 3. If both or neither are provided, it throws a validation exception
   *
   * @param projectId          The ID of the project
   * @param parentTestFolderRQ The parent folder information from the request
   * @param testFolder         The child folder entity to update
   * @throws ValidationException If the parent folder information is invalid
   */
  private void createParentTestFolder(long projectId, ParentTmsTestFolderRQ parentTestFolderRQ,
      TmsTestFolder testFolder) {
    if (nonNull(parentTestFolderRQ)) {
      if (nonNull(parentTestFolderRQ.getId()) && isNull(parentTestFolderRQ.getName())) {
        testFolder.setParentTestFolder(
            tmsTestFolderMapper.convertFromId(parentTestFolderRQ.getId())
        );
      } else if (isNull(parentTestFolderRQ.getId()) && nonNull(parentTestFolderRQ.getName())) {
        var parentTestFolder = tmsTestFolderMapper.convertFromName(projectId,
            parentTestFolderRQ.getName());
        tmsTestFolderRepository.save(parentTestFolder);
        testFolder.setParentTestFolder(parentTestFolder);
      } else {
        throw new ValidationException(
            "Either parent folder id or parent folder name should be set");
      }
    }
  }

  /**
   * Updates the parent-child relationship between folders during folder update.
   *
   * <p>This method handles four cases: 1. If parent folder ID is provided, it establishes a link
   * to
   * the existing parent 2. If parent folder name is provided, it creates a new parent folder and
   * links to it 3. If both ID and name are null but parentTestFolderRQ is not null, it removes the
   * parent link 4. If parentTestFolderRQ is null, it removes the parent link
   *
   * @param projectId          The ID of the project
   * @param parentTestFolderRQ The parent folder information from the request
   * @param existingTestFolder The folder entity to update
   */
  private void updateParentTestFolder(long projectId, ParentTmsTestFolderRQ parentTestFolderRQ,
      TmsTestFolder existingTestFolder) {
    if (nonNull(parentTestFolderRQ)) {
      if (nonNull(parentTestFolderRQ.getId()) && isNull(parentTestFolderRQ.getName())) {
        existingTestFolder.setParentTestFolder(
            tmsTestFolderMapper.convertFromId(parentTestFolderRQ.getId())
        );
      } else if (isNull(parentTestFolderRQ.getId()) && nonNull(parentTestFolderRQ.getName())) {
        var parentTestFolder = tmsTestFolderMapper.convertFromName(projectId,
            parentTestFolderRQ.getName());
        tmsTestFolderRepository.save(parentTestFolder);
        existingTestFolder.setParentTestFolder(parentTestFolder);
      } else {
        existingTestFolder.setParentTestFolder(null);
      }
    } else {
      existingTestFolder.setParentTestFolder(null);
    }
  }

  /**
   * Updates the parent-child relationship between folders during folder patch.
   *
   * <p>This method handles three cases: 1. If parent folder ID is provided, it establishes a link
   * to
   * the existing parent 2. If parent folder name is provided, it creates a new parent folder and
   * links to it 3. If both ID and name are null but parentTestFolderRQ is not null, it removes the
   * parent link 4. If parentTestFolderRQ is null, it leaves the parent relationship unchanged
   *
   * @param projectId          The ID of the project
   * @param parentTestFolderRQ The parent folder information from the request
   * @param existingTestFolder The folder entity to update
   */
  private void patchParentTestFolder(long projectId, ParentTmsTestFolderRQ parentTestFolderRQ,
      TmsTestFolder existingTestFolder) {
    if (nonNull(parentTestFolderRQ)) {
      if (nonNull(parentTestFolderRQ.getId()) && isNull(parentTestFolderRQ.getName())) {
        existingTestFolder.setParentTestFolder(
            tmsTestFolderMapper.convertFromId(parentTestFolderRQ.getId())
        );
      } else if (isNull(parentTestFolderRQ.getId()) && nonNull(parentTestFolderRQ.getName())) {
        var parentTestFolder = tmsTestFolderMapper.convertFromName(projectId,
            parentTestFolderRQ.getName());
        tmsTestFolderRepository.save(parentTestFolder);
        existingTestFolder.setParentTestFolder(parentTestFolder);
      } else {
        existingTestFolder.setParentTestFolder(null);
      }
    }
  }

  private Page<TmsTestFolderRS> getTmsTestFoldersWithSubfoldersAndTmsTestCount(
      Long projectId,
      org.springframework.data.domain.Page<TmsTestFolderWithCountOfTestCases> page) {
    if (page.hasContent()) {
      var folderIds = page
          .getContent()
          .stream()
          .map(TmsTestFolderWithCountOfTestCases::getTestFolder)
          .map(TmsTestFolder::getId)
          .collect(Collectors.toList());

      var subFoldersMap = tmsTestFolderRepository
          .findByIdsWithSubFolders(folderIds)
          .stream()
          .collect(Collectors.toMap(
              TmsTestFolder::getId,
              TmsTestFolder::getSubFolders,
              (existing, replacement) -> existing
          ));

      var allSubFolderIds = subFoldersMap.values()
          .stream()
          .flatMap(Collection::stream)
          .map(TmsTestFolder::getId)
          .distinct()
          .collect(Collectors.toList());

      Map<Long, Long> subFolderTestCaseCounts = new HashMap<>();
      if (!allSubFolderIds.isEmpty()) {
        var testCaseCountResults = tmsTestFolderRepository
            .findTestCaseCountsByFolderIds(projectId, allSubFolderIds);
        subFolderTestCaseCounts = testCaseCountResults
            .stream()
            .collect(Collectors.toMap(
                TmsTestFolderIdWithCountOfTestCases::getTestFolderId,
                TmsTestFolderIdWithCountOfTestCases::getCountOfTestCases
            ));
      }

      page
          .getContent()
          .stream()
          .map(TmsTestFolderWithCountOfTestCases::getTestFolder)
          .forEach(testFolder -> {
            var subFolders = subFoldersMap.get(testFolder.getId());
            if (subFolders != null) {
              testFolder.setSubFolders(subFolders);
            }
          });

      return tmsTestFolderMapper.convert(page, subFolderTestCaseCounts);
    }

    return tmsTestFolderMapper.convert(page);
  }
}
