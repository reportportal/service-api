package com.epam.reportportal.base.core.tms.service;

import static com.epam.reportportal.base.infrastructure.rules.exception.ErrorType.BAD_REQUEST_ERROR;
import static com.epam.reportportal.base.infrastructure.rules.exception.ErrorType.NOT_FOUND;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import com.epam.reportportal.base.core.tms.dto.DuplicateTmsTestFolderRS;
import com.epam.reportportal.base.core.tms.dto.NewTestFolderRQ;
import com.epam.reportportal.base.core.tms.dto.TmsTestCaseRQ;
import com.epam.reportportal.base.core.tms.dto.TmsTestFolderExportFileType;
import com.epam.reportportal.base.core.tms.dto.TmsTestFolderRQ;
import com.epam.reportportal.base.core.tms.dto.TmsTestFolderRS;
import com.epam.reportportal.base.core.tms.mapper.TmsTestFolderMapper;
import com.epam.reportportal.base.core.tms.mapper.factory.TmsTestFolderExporterFactory;
import com.epam.reportportal.base.core.tms.statistics.FolderDuplicationStatistics;
import com.epam.reportportal.base.core.tms.statistics.TestCaseDuplicationStatistics;
import com.epam.reportportal.base.core.tms.validation.TestFolderIdValidator;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.Filter;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsTestFolderRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.enhanced.TmsTestFolderWithTestCaseCountRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestFolder;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.base.model.Page;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ValidationException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of the {@link TmsTestFolderService} interface that provides operations for managing test folders
 * within the TMS (Test Management System).
 *
 * <p>This service handles CRUD operations for test folders, including creating, updating,
 * retrieving, and deleting folders. It also supports hierarchical folder structures with parent-child relationships and
 * exporting folder data in various formats.
 */
@Service
@RequiredArgsConstructor
public class TmsTestFolderServiceImpl implements TmsTestFolderService {

  private static final String TEST_FOLDER_NOT_FOUND_BY_ID =
      "Test Folder with id: %d for project: %d";

  private final TmsTestFolderMapper tmsTestFolderMapper;
  private final TmsTestFolderRepository tmsTestFolderRepository;
  private final TmsTestFolderExporterFactory tmsTestFolderExporterFactory;
  private final TestFolderIdValidator testFolderIdValidator;
  private final TmsTestFolderWithTestCaseCountRepository tmsTestFolderWithTestCaseCountRepository;

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
   * parent-child relationship between the folders.
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

    createParentTestFolder(projectId,
        inputDto.getParentTestFolderId(),
        inputDto.getParentTestFolder(),
        testFolder);

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

          updateParentTestFolder(
              projectId,
              inputDto.getParentTestFolderId(),
              inputDto.getParentTestFolder(),
              existingTestFolder
          );

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
   * in the input DTO, leaving other properties unchanged.
   *
   * @param projectId    The ID of the project containing the folder
   * @param testFolderId The ID of the folder to patch
   * @param inputDto     The data transfer object containing the fields to update
   * @return A response DTO containing the patched folder's information
   * @throws ReportPortalException If the folder doesn't exist
   */
  @Override
  public TmsTestFolderRS patch(long projectId, Long testFolderId,
      TmsTestFolderRQ inputDto) {
    return tmsTestFolderRepository
        .findByIdAndProjectId(testFolderId, projectId)
        .map(existingTestFolder -> {
          tmsTestFolderMapper.patch(existingTestFolder,
              tmsTestFolderMapper.convertFromRQ(projectId, inputDto));

          patchParentTestFolder(
              projectId,
              inputDto.getParentTestFolderId(),
              inputDto.getParentTestFolder(),
              existingTestFolder
          );

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
   * <p>This method fetches the folder along with information about the number of test cases it
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
        .map(tmsTestFolderMapper::convertFromTmsTestFolderWithCountOfTestCasesToRS)
        .orElseThrow(() -> new ReportPortalException(
            NOT_FOUND, TEST_FOLDER_NOT_FOUND_BY_ID.formatted(id, projectId))
        );
  }

  /**
   * Retrieves test folders in a project.
   *
   * <p>This method returns a paginated list of folders without subfolder information.
   *
   * @param projectId The ID of the project
   * @param filter    The filter
   * @param pageable  Pagination parameters
   * @return A paginated list of response DTOs containing folder information
   */
  @Override
  @Transactional(readOnly = true)
  public Page<TmsTestFolderRS> getFoldersByCriteria(
      final long projectId,
      Filter filter,
      Pageable pageable) {
    var page = tmsTestFolderWithTestCaseCountRepository
        .findAllByProjectIdAndFilterWithCountOfTestCases(projectId, filter, pageable);
    return tmsTestFolderMapper.convert(page);
  }

  /**
   * Exports a test folder and its entire hierarchy to the specified format.
   *
   * <p>This method retrieves the folder with its complete hierarchy and uses the appropriate
   * exporter to generate the output in the requested format.
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
  public TmsTestFolderRS create(long projectId, NewTestFolderRQ testFolderRQ) {
    return create(projectId, tmsTestFolderMapper.convertToRQ(testFolderRQ));
  }

  /**
   * This method determines whether a test folder with an id exists in a project.
   *
   * @param projectId    project's id
   * @param testFolderId test folder's id
   * @return true if exists, false if not
   */
  @Override
  public Boolean existsById(long projectId, Long testFolderId) {
    return tmsTestFolderRepository.existsByIdAndProjectId(testFolderId, projectId);
  }

  @Override
  public void resolveTestFolderRQ(
      TmsTestCaseRQ testCaseRequest,
      Long testFolderId,
      String testFolderName) {
    testFolderIdValidator.validate(testFolderId, testFolderName);
    if (Objects.nonNull(testCaseRequest)) {
      if (Objects.nonNull(testFolderId)) {
        testCaseRequest.setTestFolderId(testFolderId);
      } else if (Objects.nonNull(testFolderName)) {
        testCaseRequest.setTestFolder(
            tmsTestFolderMapper.convertToTmsTestCaseTestFolderRQ(testFolderName)
        );
      }
    }
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
   * then deletes the folder and subfolder entities themselves.
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

  @Override
  public Long resolveTargetFolderId(long projectId, Long testFolderId,
      NewTestFolderRQ testFolderRQ) {
    if ((nonNull(testFolderId) && nonNull(testFolderRQ) && nonNull(testFolderRQ.getName()))
        || (isNull(testFolderId) && nonNull(testFolderRQ) && isNull(testFolderRQ.getName()))) {
      throw new ReportPortalException(BAD_REQUEST_ERROR,
          "Either target folder id or target folder name should be set");
    } else if (nonNull(testFolderId)) {
      if (!existsById(projectId, testFolderId)) {
        throw new ReportPortalException(
            NOT_FOUND, TEST_FOLDER_NOT_FOUND_BY_ID.formatted(testFolderId, projectId));
      }
      return testFolderId;
    } else if (nonNull(testFolderRQ)) {
      var targetFolder = tmsTestFolderMapper.convertFromName(projectId, testFolderRQ.getName());

      if (nonNull(testFolderRQ.getParentTestFolderId())) {
        if (!existsById(projectId, testFolderRQ.getParentTestFolderId())) {
          throw new ReportPortalException(
              NOT_FOUND,
              TEST_FOLDER_NOT_FOUND_BY_ID.formatted(testFolderRQ.getParentTestFolderId(), projectId)
          );
        }
        targetFolder.setParentTestFolder(
            tmsTestFolderMapper.convertFromId(testFolderRQ.getParentTestFolderId())
        );
      }

      return tmsTestFolderRepository.save(targetFolder).getId();
    } else {
      throw new ReportPortalException(BAD_REQUEST_ERROR,
          "Either target folder id or target folder name must be provided for duplication");
    }
  }

  @Override
  @Transactional(readOnly = true)
  public TmsTestFolder getEntityById(final long projectId, Long testFolderId) {
    return tmsTestFolderRepository
        .findByIdAndProjectId(testFolderId, projectId)
        .orElseThrow(() -> new ReportPortalException(
            NOT_FOUND, TEST_FOLDER_NOT_FOUND_BY_ID.formatted(testFolderId, projectId))
        );
  }

  /**
   * Creates a parent-child relationship between folders during folder creation.
   *
   * <p>This method handles three cases: 1. If parent folder ID is provided, it establishes a link
   * to the existing parent 2. If parent folder name is provided, it creates a new parent folder and links to it 3. If
   * both or neither are provided, it throws a validation exception
   *
   * @param projectId          The ID of the project
   * @param parentTestFolderId The parent folder ID
   * @param parentTestFolderRQ The parent folder information from the request
   * @param testFolder         The child folder entity to update
   * @throws ValidationException If the parent folder information is invalid
   */
  private void createParentTestFolder(long projectId,
      Long parentTestFolderId,
      NewTestFolderRQ parentTestFolderRQ,
      TmsTestFolder testFolder) {
    if ((nonNull(parentTestFolderId)
        && nonNull(parentTestFolderRQ)
        && nonNull(parentTestFolderRQ.getName())) || (isNull(parentTestFolderId) && nonNull(
        parentTestFolderRQ) && isNull(parentTestFolderRQ.getName()))) {
      throw new ReportPortalException(BAD_REQUEST_ERROR,
          "Either parent folder id or parent folder name should be set");
    } else if (nonNull(parentTestFolderId)) {
      if (!existsById(projectId, parentTestFolderId)) {
        throw new ReportPortalException(
            NOT_FOUND,
            TEST_FOLDER_NOT_FOUND_BY_ID.formatted(parentTestFolderId,
                projectId));
      } else {
        testFolder.setParentTestFolder(
            tmsTestFolderMapper.convertFromId(parentTestFolderId)
        );
      }
    } else if (nonNull(parentTestFolderRQ)) {
      if (nonNull(parentTestFolderRQ.getParentTestFolderId())
          && !existsById(projectId, parentTestFolderRQ.getParentTestFolderId())) {
        throw new ReportPortalException(
            NOT_FOUND,
            TEST_FOLDER_NOT_FOUND_BY_ID.formatted(parentTestFolderRQ.getParentTestFolderId(),
                projectId));
      } else {
        var parentTestFolder = tmsTestFolderMapper.convertToTestFolder(projectId,
            parentTestFolderRQ);
        if (isNull(parentTestFolder.getSubFolders())) {
          parentTestFolder.setSubFolders(new ArrayList<>());
        }
        parentTestFolder.getSubFolders().add(testFolder);
        testFolder.setParentTestFolder(tmsTestFolderRepository.save(parentTestFolder));
      }
    }
  }

  /**
   * Updates the parent-child relationship between folders during folder update.
   *
   * <p>This method handles four cases: 1. If parent folder ID is provided, it establishes a link
   * to the existing parent 2. If parent folder name is provided, it creates a new parent folder and links to it 3. If
   * both ID and name are null but parentTestFolderRQ is not null, it removes the parent link 4. If parentTestFolderRQ
   * is null, it removes the parent link
   *
   * @param projectId          The ID of the project
   * @param parentTestFolderId The parent folder ID
   * @param parentTestFolderRQ The parent folder information from the request
   * @param existingTestFolder The folder entity to update
   */
  private void updateParentTestFolder(long projectId,
      Long parentTestFolderId,
      NewTestFolderRQ parentTestFolderRQ,
      TmsTestFolder existingTestFolder) {
    if ((nonNull(parentTestFolderId)
        && nonNull(parentTestFolderRQ)
        && nonNull(parentTestFolderRQ.getName())) || (isNull(parentTestFolderId) && nonNull(
        parentTestFolderRQ) && isNull(parentTestFolderRQ.getName()))) {
      throw new ReportPortalException(BAD_REQUEST_ERROR,
          "Either parent folder id or parent folder name should be set");
    } else if (nonNull(parentTestFolderId)) {
      if (!existsById(projectId, parentTestFolderId)) {
        throw new ReportPortalException(
            NOT_FOUND,
            TEST_FOLDER_NOT_FOUND_BY_ID.formatted(parentTestFolderId,
                projectId));
      } else {
        existingTestFolder.setParentTestFolder(
            tmsTestFolderMapper.convertFromId(parentTestFolderId)
        );
      }
    } else if (nonNull(parentTestFolderRQ)) {
      if (nonNull(parentTestFolderRQ.getParentTestFolderId())
          && !existsById(projectId, parentTestFolderRQ.getParentTestFolderId())) {
        throw new ReportPortalException(
            NOT_FOUND,
            TEST_FOLDER_NOT_FOUND_BY_ID.formatted(parentTestFolderRQ.getParentTestFolderId(),
                projectId));
      } else {
        var parentTestFolder = tmsTestFolderMapper.convertToTestFolder(
            projectId, parentTestFolderRQ
        );
        if (isNull(parentTestFolder.getSubFolders())) {
          parentTestFolder.setSubFolders(new ArrayList<>());
        }
        parentTestFolder.getSubFolders().add(existingTestFolder);
        existingTestFolder.setParentTestFolder(tmsTestFolderRepository.save(parentTestFolder));
      }
    } else {
      existingTestFolder.setParentTestFolder(null);
    }
  }

  /**
   * Updates the parent-child relationship between folders during folder patch.
   *
   * <p>This method handles three cases: 1. If parent folder ID is provided, it establishes a link
   * to the existing parent 2. If parent folder name is provided, it creates a new parent folder and links to it 3. If
   * both ID and name are null but parentTestFolderRQ is not null, it removes the parent link 4. If parentTestFolderRQ
   * is null, it leaves the parent relationship unchanged
   *
   * @param projectId          The ID of the project
   * @param parentTestFolderId The parent folder ID
   * @param parentTestFolderRQ The parent folder information from the request
   * @param existingTestFolder The folder entity to update
   */
  private void patchParentTestFolder(long projectId,
      Long parentTestFolderId,
      NewTestFolderRQ parentTestFolderRQ,
      TmsTestFolder existingTestFolder) {
    if ((nonNull(parentTestFolderId)
        && nonNull(parentTestFolderRQ)
        && nonNull(parentTestFolderRQ.getName())) || (isNull(parentTestFolderId) && nonNull(
        parentTestFolderRQ) && isNull(parentTestFolderRQ.getName()))) {
      throw new ReportPortalException(BAD_REQUEST_ERROR,
          "Either parent folder id or parent folder name should be set");
    } else if (nonNull(parentTestFolderId)) {
      if (!existsById(projectId, parentTestFolderId)) {
        throw new ReportPortalException(
            NOT_FOUND,
            TEST_FOLDER_NOT_FOUND_BY_ID.formatted(parentTestFolderId,
                projectId));
      } else {
        existingTestFolder.setParentTestFolder(
            tmsTestFolderMapper.convertFromId(parentTestFolderId)
        );
      }
    } else if (nonNull(parentTestFolderRQ)) {
      if (nonNull(parentTestFolderRQ.getParentTestFolderId())
          && !existsById(projectId, parentTestFolderRQ.getParentTestFolderId())) {
        throw new ReportPortalException(
            NOT_FOUND,
            TEST_FOLDER_NOT_FOUND_BY_ID.formatted(parentTestFolderRQ.getParentTestFolderId(),
                projectId));
      } else {
        var parentTestFolder = tmsTestFolderMapper.convertToTestFolder(projectId,
            parentTestFolderRQ);
        if (isNull(parentTestFolder.getSubFolders())) {
          parentTestFolder.setSubFolders(new ArrayList<>());
        }
        parentTestFolder.getSubFolders().add(existingTestFolder);
        existingTestFolder.setParentTestFolder(tmsTestFolderRepository.save(parentTestFolder));
      }
    }
  }

  /**
   * Duplicates a test folder with all its subfolders and test cases.
   *
   * <p>This method performs a deep copy of the folder hierarchy:
   * <ul>
   *   <li>Creates a copy of the root folder with the specified name</li>
   *   <li>Recursively duplicates all subfolders maintaining the hierarchy</li>
   *   <li>Duplicates all test cases in each folder</li>
   *   <li>Generates unique names for folders if conflicts exist</li>
   *   <li>Collects statistics about successful and failed operations</li>
   * </ul>
   *
   * @param projectId The ID of the project
   * @param folderId  The ID of the folder to duplicate
   * @param inputDto  The request containing name and parent folder information
   * @return The duplicated folder details with duplication statistics
   * @throws ReportPortalException If the source folder doesn't exist or validation fails
   */
  @Override
  @Transactional
  public DuplicateTmsTestFolderRS duplicateFolder(long projectId, Long folderId,
      TmsTestFolderRQ inputDto) {

    // Validate source folder exists
    var sourceFolder = findFolderWithFullHierarchy(projectId, folderId);

    // Validate and resolve target parent folder
    var targetParentFolderId = resolveTargetParentForDuplication(projectId, inputDto);

    // Initialize statistics collectors
    var folderStatistics = new FolderDuplicationStatistics();
    var testCaseStatistics = new TestCaseDuplicationStatistics();

    // Duplicate the root folder and all its hierarchy
    var duplicatedRootFolder = duplicateFolderHierarchy(
        projectId,
        sourceFolder,
        inputDto.getName(),
        targetParentFolderId,
        folderStatistics,
        testCaseStatistics
    );

    // Count test cases in duplicated folder
    var testCaseCount = tmsTestFolderRepository.countTestCasesByFolderId(
        duplicatedRootFolder.getId()
    );

    // Build and return response using mapper
    return tmsTestFolderMapper.convertToDuplicateTmsTestFolderRS(
        duplicatedRootFolder,
        testCaseCount,
        folderStatistics,
        testCaseStatistics
    );
  }

  /**
   * Resolves target parent folder for duplication based on input parameters.
   *
   * @param projectId The ID of the project
   * @param inputDto  The request containing parent folder information
   * @return The ID of the target parent folder, or null for root level
   * @throws ReportPortalException If validation fails
   */
  private Long resolveTargetParentForDuplication(long projectId, TmsTestFolderRQ inputDto) {
    var parentTestFolderId = inputDto.getParentTestFolderId();
    var parentTestFolder = inputDto.getParentTestFolder();

    if ((nonNull(parentTestFolderId) && nonNull(parentTestFolder) && nonNull(parentTestFolder.getName()))
        || (isNull(parentTestFolderId) && nonNull(parentTestFolder) && isNull(parentTestFolder.getName()))) {
      throw new ReportPortalException(BAD_REQUEST_ERROR,
          "Either parent folder id or parent folder name should be set");
    } else if (nonNull(parentTestFolderId)) {
      if (!existsById(projectId, parentTestFolderId)) {
        throw new ReportPortalException(
            NOT_FOUND, TEST_FOLDER_NOT_FOUND_BY_ID.formatted(parentTestFolderId, projectId));
      }
      return parentTestFolderId;
    } else if (nonNull(parentTestFolder)) {
      var targetFolder = tmsTestFolderMapper.convertFromName(projectId, parentTestFolder.getName());

      if (nonNull(parentTestFolder.getParentTestFolderId())) {
        if (!existsById(projectId, parentTestFolder.getParentTestFolderId())) {
          throw new ReportPortalException(
              NOT_FOUND,
              TEST_FOLDER_NOT_FOUND_BY_ID.formatted(parentTestFolder.getParentTestFolderId(), projectId)
          );
        }
        targetFolder.setParentTestFolder(
            tmsTestFolderMapper.convertFromId(parentTestFolder.getParentTestFolderId())
        );
      }

      return tmsTestFolderRepository.save(targetFolder).getId();
    } else {
      return null; //rool-layer
    }
  }

  /**
   * Recursively duplicates a folder and all its subfolders with test cases.
   *
   * @param projectId            The ID of the project
   * @param sourceFolder         The source folder to duplicate
   * @param targetName           The name for the duplicated folder
   * @param targetParentFolderId The ID of the parent folder for the duplicate
   * @param folderStatistics     Statistics collector for folder duplication
   * @param testCaseStatistics   Statistics collector for test case duplication
   * @return The duplicated folder entity
   */
  private TmsTestFolder duplicateFolderHierarchy(
      long projectId,
      TmsTestFolder sourceFolder,
      String targetName,
      Long targetParentFolderId,
      FolderDuplicationStatistics folderStatistics,
      TestCaseDuplicationStatistics testCaseStatistics) {

    try {
      // Get target parent folder entity if specified
      TmsTestFolder targetParentFolder = null;
      if (nonNull(targetParentFolderId)) {
        targetParentFolder = getEntityById(projectId, targetParentFolderId);
      }

      // Generate unique name for the duplicated folder
      var uniqueName = generateUniqueFolderName(
          projectId, targetName, targetParentFolderId
      );

      // Create duplicate of the folder (without subfolders and test cases)
      var duplicatedFolder = tmsTestFolderMapper.duplicateTestFolder(
          sourceFolder,
          targetParentFolder
      );
      duplicatedFolder.setName(uniqueName);
      duplicatedFolder.setSubFolders(new ArrayList<>());

      // Save the duplicated folder
      duplicatedFolder = tmsTestFolderRepository.save(duplicatedFolder);
      folderStatistics.addSuccess(duplicatedFolder.getId());

      // Duplicate test cases in this folder
      duplicateTestCasesInFolder(
          projectId,
          sourceFolder,
          duplicatedFolder,
          testCaseStatistics
      );

      // Recursively duplicate all subfolders
      if (CollectionUtils.isNotEmpty(sourceFolder.getSubFolders())) {
        for (var subFolder : sourceFolder.getSubFolders()) {
          try {
            var duplicatedSubFolder = duplicateFolderHierarchy(
                projectId,
                subFolder,
                subFolder.getName() + "-copy",
                duplicatedFolder.getId(),
                folderStatistics,
                testCaseStatistics
            );
            duplicatedFolder.getSubFolders().add(duplicatedSubFolder);
          } catch (Exception e) {
            folderStatistics.addError(subFolder.getId(), e.getMessage());
          }
        }
      }
      return duplicatedFolder;
    } catch (Exception e) {
      folderStatistics.addError(sourceFolder.getId(), e.getMessage());
      throw new ReportPortalException(
          ErrorType.BAD_REQUEST_ERROR,
          "Failed to duplicate folder: " + e.getMessage()
      );
    }
  }

  /**
   * Generates a unique folder name by adding incremental suffixes if conflicts exist.
   *
   * @param projectId      The ID of the project
   * @param baseName       The base name for the folder
   * @param parentFolderId The ID of the parent folder
   * @return A unique folder name
   */
  private String generateUniqueFolderName(long projectId, String baseName, Long parentFolderId) {
    var candidateName = baseName;
    var counter = 1;

    while (tmsTestFolderRepository.existsByNameAndTestFolder(projectId, candidateName,
        parentFolderId)) {
      candidateName = baseName + "-" + counter;
      counter++;
    }

    return candidateName;
  }

  /**
   * Duplicates all test cases from source folder to target folder.
   *
   * @param projectId          The ID of the project
   * @param sourceFolder       The source folder
   * @param targetFolder       The target folder
   * @param testCaseStatistics Statistics collector for test case duplication
   */
  private void duplicateTestCasesInFolder(
      long projectId,
      TmsTestFolder sourceFolder,
      TmsTestFolder targetFolder,
      TestCaseDuplicationStatistics testCaseStatistics) {

    // Get all test case IDs in the source folder
    var testCaseIds = tmsTestFolderRepository.findTestCaseIdsByFolderId(sourceFolder.getId());

    if (CollectionUtils.isEmpty(testCaseIds)) {
      return; // No test cases to duplicate
    }

    try {
      // Use existing duplicateTestCases method from TmsTestCaseService
      var duplicateTestCasesResult = tmsTestCaseService.duplicateTestCases(
          projectId, targetFolder, testCaseIds
      );

      // Collect statistics
      testCaseStatistics.merge(duplicateTestCasesResult);

    } catch (Exception e) {
      // Record errors for all test cases in this folder
      testCaseIds.forEach(id -> testCaseStatistics.addError(id, e.getMessage()));
    }
  }

  @Override
  @Transactional(readOnly = true)
  public Page<TmsTestFolderRS> getFoldersByTestPlanId(Long projectId, Long testPlanId,
      Pageable pageable) {
    var foldersPage = tmsTestFolderRepository
        .findAllByProjectIdAndTestPlanIdWithCountOfTestCases(projectId, testPlanId, pageable);

    return tmsTestFolderMapper.convert(foldersPage);
  }
}
