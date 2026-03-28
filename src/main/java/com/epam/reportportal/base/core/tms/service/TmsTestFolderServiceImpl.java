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
import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.Filter;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsTestFolderRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.enhanced.TmsTestFolderWithTestCaseCountRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.MembershipDetails;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestFolder;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.base.model.Page;
import jakarta.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of the {@link TmsTestFolderService} interface that provides operations for
 * managing test folders within the TMS (Test Management System).
 */
@Slf4j
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
  public void setTmsTestCaseService(TmsTestCaseService tmsTestCaseService) {
    this.tmsTestCaseService = tmsTestCaseService;
  }

  @Override
  @Transactional
  public TmsTestFolderRS create(final long projectId, final TmsTestFolderRQ inputDto) {
    var testFolder = tmsTestFolderMapper.convertFromRQ(projectId, inputDto);

    createParentTestFolder(projectId,
        inputDto.getParentTestFolderId(),
        inputDto.getParentTestFolder(),
        testFolder);

    var parentId = testFolder.getParentTestFolder() != null
        ? testFolder.getParentTestFolder().getId() : null;

    int newIndex;
    if (inputDto.getIndex() != null) {
      newIndex = inputDto.getIndex();
      tmsTestFolderRepository.shiftIndexes(projectId, parentId, newIndex, 1);
    } else {
      var max = tmsTestFolderRepository.findMaxIndex(projectId, parentId);
      newIndex = (max == null) ? 0 : max + 1;
    }
    testFolder.setIndex(newIndex);

    return tmsTestFolderMapper.convertFromTmsTestFolderToRS(
        tmsTestFolderRepository.save(testFolder));
  }

  @Override
  @Transactional
  public TmsTestFolderRS update(final long projectId, Long testFolderId,
      final TmsTestFolderRQ inputDto) {
    return tmsTestFolderRepository
        .findByIdAndProjectId(testFolderId, projectId)
        .map(existingTestFolder -> {
          // Save old values BEFORE any modifications
          var oldParentId = existingTestFolder.getParentTestFolder() != null
              ? existingTestFolder.getParentTestFolder().getId() : null;
          var oldIndex = existingTestFolder.getIndex();
          if (oldIndex == null) {
            oldIndex = 0;
          }

          // Save target index from request
          var targetIndex = inputDto.getIndex();

          // Resolve new parent ID WITHOUT setting it on the folder yet
          var newParentId = resolveNewParentId(
              projectId,
              inputDto.getParentTestFolderId(),
              inputDto.getParentTestFolder()
          );

          validateNoCycle(projectId, existingTestFolder.getId(), newParentId);

          // Apply other changes via mapper
          tmsTestFolderMapper.update(existingTestFolder,
              tmsTestFolderMapper.convertFromRQ(projectId, inputDto));

          // Restore old index and parent to avoid auto-flush issues
          existingTestFolder.setIndex(oldIndex);
          existingTestFolder.setParentTestFolder(
              oldParentId != null ? tmsTestFolderMapper.convertFromId(oldParentId) : null
          );

          // Handle index changes
          handleIndexChange(projectId, existingTestFolder, oldParentId, oldIndex,
              newParentId, targetIndex);

          // NOW set the new parent after index operations are done
          if (!Objects.equals(oldParentId, newParentId)) {
            existingTestFolder.setParentTestFolder(
                newParentId != null ? tmsTestFolderMapper.convertFromId(newParentId) : null
            );
          }

          return tmsTestFolderMapper.convertFromTmsTestFolderToRS(
              tmsTestFolderRepository.save(existingTestFolder)
          );
        })
        .orElseGet(() -> create(projectId, inputDto));
  }

  @Override
  @Transactional
  public TmsTestFolderRS patch(long projectId, Long testFolderId, TmsTestFolderRQ inputDto) {
    return tmsTestFolderRepository
        .findByIdAndProjectId(testFolderId, projectId)
        .map(existingTestFolder -> {
          // Save old values BEFORE any modifications
          var oldParentId = existingTestFolder.getParentTestFolder() != null
              ? existingTestFolder.getParentTestFolder().getId() : null;
          var oldIndex = existingTestFolder.getIndex();
          if (oldIndex == null) {
            oldIndex = 0;
          }

          // Save target index from request
          var targetIndex = inputDto.getIndex();

          // Resolve new parent ID WITHOUT setting it on the folder yet
          var newParentId = resolveNewParentIdForPatch(
              projectId,
              oldParentId,
              inputDto.getParentTestFolderId(),
              inputDto.getParentTestFolder()
          );

          validateNoCycle(projectId, existingTestFolder.getId(), newParentId);

          // Apply other changes via mapper
          tmsTestFolderMapper.patch(existingTestFolder,
              tmsTestFolderMapper.convertFromRQ(projectId, inputDto));

          // Restore old index and parent to avoid auto-flush issues
          existingTestFolder.setIndex(oldIndex);
          existingTestFolder.setParentTestFolder(
              oldParentId != null ? tmsTestFolderMapper.convertFromId(oldParentId) : null
          );

          // Handle index changes
          handleIndexChange(projectId, existingTestFolder, oldParentId, oldIndex,
              newParentId, targetIndex);

          // NOW set the new parent after index operations are done
          if (!Objects.equals(oldParentId, newParentId)) {
            existingTestFolder.setParentTestFolder(
                newParentId != null ? tmsTestFolderMapper.convertFromId(newParentId) : null
            );
          }

          return tmsTestFolderMapper.convertFromTmsTestFolderToRS(
              tmsTestFolderRepository.save(existingTestFolder));
        })
        .orElseThrow(() -> new ReportPortalException(
            NOT_FOUND, TEST_FOLDER_NOT_FOUND_BY_ID.formatted(testFolderId, projectId))
        );
  }

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

  @Override
  @Transactional(readOnly = true)
  public Page<TmsTestFolderRS> getFoldersByCriteria(
      final long projectId,
      Filter filter,
      Pageable pageable) {
    var page = tmsTestFolderWithTestCaseCountRepository
        .findAllByProjectIdAndFilterWithCountOfTestCases(projectId, filter, pageable);

    var mappedPage = tmsTestFolderMapper.convert(page);

    if (mappedPage.getContent().isEmpty() || CollectionUtils.isEmpty(filter.getFilterConditions())) {
      return mappedPage;
    }

    var folderIds = page
        .getContent()
        .stream()
        .map(folder -> folder.getTestFolder().getId())
        .toList();

    var allParentIds = tmsTestFolderRepository.findAllParentFolderIds(projectId, folderIds);

    var missingParentIds = allParentIds.stream()
        .filter(id -> !folderIds.contains(id))
        .collect(Collectors.toSet());

    if (missingParentIds.isEmpty()) {
      return mappedPage;
    }

    var missingParents = tmsTestFolderRepository.findAllById(missingParentIds);

    var content = new ArrayList<>(mappedPage.getContent());
    missingParents.forEach(parent -> {
      var rs = tmsTestFolderMapper.convertFromTmsTestFolderToRS(parent);
      rs.setCountOfTestCases(0L);
      content.add(rs);
    });

    return new Page<>(content, mappedPage.getPage());
  }

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

  @Override
  @Transactional
  public void delete(MembershipDetails membershipDetails,
      ReportPortalUser user, Long folderId) {
    var projectId = membershipDetails.getProjectId();
    var folder = getEntityById(projectId, folderId);
    var parentId = folder.getParentTestFolder() != null
        ? folder.getParentTestFolder().getId() : null;
    var index = folder.getIndex();

    tmsTestCaseService.deleteByTestFolderId(membershipDetails, user, folderId);
    tmsTestFolderRepository.deleteTestFolderWithSubfoldersById(projectId, folderId);

    if (index != null) {
      tmsTestFolderRepository.shiftIndexes(projectId, parentId, index + 1, -1);
    }
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
          "Either target folder id or target folder name must be provided");
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

  @Override
  @Transactional
  public DuplicateTmsTestFolderRS duplicateFolder(long projectId, Long folderId,
      TmsTestFolderRQ inputDto) {

    var sourceFolder = findFolderWithFullHierarchy(projectId, folderId);
    var targetParentFolderId = resolveTargetParentForDuplication(projectId, inputDto);

    var folderStatistics = new FolderDuplicationStatistics();
    var testCaseStatistics = new TestCaseDuplicationStatistics();

    var duplicatedRootFolder = duplicateFolderHierarchy(
        projectId,
        sourceFolder,
        inputDto.getName(),
        targetParentFolderId,
        inputDto.getIndex(),
        folderStatistics,
        testCaseStatistics
    );

    var testCaseCount = tmsTestFolderRepository.countTestCasesByFolderId(
        duplicatedRootFolder.getId()
    );

    return tmsTestFolderMapper.convertToDuplicateTmsTestFolderRS(
        duplicatedRootFolder,
        testCaseCount,
        folderStatistics,
        testCaseStatistics
    );
  }

  @Override
  @Transactional(readOnly = true)
  public Page<TmsTestFolderRS> getFoldersByTestPlanId(Long projectId, Long testPlanId,
      Pageable pageable) {
    var foldersPage = tmsTestFolderRepository
        .findAllByProjectIdAndTestPlanIdWithCountOfTestCases(projectId, testPlanId, pageable);

    return tmsTestFolderMapper.convert(foldersPage);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<TmsTestFolderRS> getFoldersByLaunchIdWithTestCaseCount(Long projectId, Long launchId,
      Pageable pageable) {
    var foldersPage = tmsTestFolderRepository.findUniqueFoldersByLaunchIdWithTestCaseCount(
        launchId, pageable
    );

    return tmsTestFolderMapper.convert(foldersPage);
  }

  @Override
  @Transactional
  public Long resolveFolderPath(Long projectId, Long parentFolderId, List<String> pathHierarchy) {
    if (CollectionUtils.isEmpty(pathHierarchy)) {
      return parentFolderId;
    }

    var currentParentId = parentFolderId;

    for (var folderName : pathHierarchy) {
      currentParentId = findOrCreateFolder(projectId, currentParentId, folderName);
    }

    return currentParentId;
  }

  @Override
  @Transactional
  public Map<String, Long> resolveFolderPathsBatch(Long projectId, Long parentFolderId,
      List<List<String>> pathHierarchies) {
    Map<String, Long> pathToFolderIdCache = new HashMap<>();

    for (var pathHierarchy : pathHierarchies) {
      if (CollectionUtils.isEmpty(pathHierarchy)) {
        continue;
      }

      var pathKey = String.join("/", pathHierarchy);
      if (pathToFolderIdCache.containsKey(pathKey)) {
        continue;
      }

      var resolvedParentId = parentFolderId;
      var currentPathBuilder = new StringBuilder();

      for (int i = 0; i < pathHierarchy.size(); i++) {
        var folderName = pathHierarchy.get(i);
        if (i > 0) {
          currentPathBuilder.append("/");
        }
        currentPathBuilder.append(folderName);
        var currentPath = currentPathBuilder.toString();

        if (pathToFolderIdCache.containsKey(currentPath)) {
          resolvedParentId = pathToFolderIdCache.get(currentPath);
        } else {
          resolvedParentId = findOrCreateFolder(projectId, resolvedParentId, folderName);
          pathToFolderIdCache.put(currentPath, resolvedParentId);
        }
      }
    }

    return pathToFolderIdCache;
  }

  /**
   * Resolves the new parent folder ID for update operation.
   * Creates a new parent folder if needed, but does NOT set it on the target folder.
   *
   * @param projectId          project id
   * @param parentTestFolderId parent folder id from request
   * @param parentTestFolderRQ parent folder details from request
   * @return resolved parent folder ID, or null for root level
   */
  private Long resolveNewParentId(long projectId,
      Long parentTestFolderId,
      NewTestFolderRQ parentTestFolderRQ) {

    if ((nonNull(parentTestFolderId) && nonNull(parentTestFolderRQ)
        && nonNull(parentTestFolderRQ.getName()))
        || (isNull(parentTestFolderId) && nonNull(parentTestFolderRQ)
        && isNull(parentTestFolderRQ.getName()))) {
      throw new ReportPortalException(BAD_REQUEST_ERROR,
          "Either parent folder id or parent folder name should be set");
    }

    if (nonNull(parentTestFolderId)) {
      if (!existsById(projectId, parentTestFolderId)) {
        throw new ReportPortalException(
            NOT_FOUND,
            TEST_FOLDER_NOT_FOUND_BY_ID.formatted(parentTestFolderId, projectId));
      }
      return parentTestFolderId;
    }

    if (nonNull(parentTestFolderRQ)) {
      return createParentFolderAndGetId(projectId, parentTestFolderRQ);
    }

    // No parent specified - move to root
    return null;
  }

  /**
   * Resolves the new parent folder ID for patch operation.
   * Unlike update, patch keeps the existing parent if no parent info is provided.
   *
   * @param projectId          project id
   * @param currentParentId    current parent folder id
   * @param parentTestFolderId parent folder id from request
   * @param parentTestFolderRQ parent folder details from request
   * @return resolved parent folder ID
   */
  private Long resolveNewParentIdForPatch(long projectId,
      Long currentParentId,
      Long parentTestFolderId,
      NewTestFolderRQ parentTestFolderRQ) {

    // Check for explicit "move to root" request
    if (nonNull(parentTestFolderRQ)
        && isNull(parentTestFolderId)
        && isNull(parentTestFolderRQ.getName())
        && isNull(parentTestFolderRQ.getParentTestFolderId())) {
      return null;
    }

    if (nonNull(parentTestFolderId) && nonNull(parentTestFolderRQ)
        && nonNull(parentTestFolderRQ.getName())) {
      throw new ReportPortalException(BAD_REQUEST_ERROR,
          "Either parent folder id or parent folder name should be set");
    }

    if (nonNull(parentTestFolderId)) {
      if (!existsById(projectId, parentTestFolderId)) {
        throw new ReportPortalException(
            NOT_FOUND,
            TEST_FOLDER_NOT_FOUND_BY_ID.formatted(parentTestFolderId, projectId));
      }
      return parentTestFolderId;
    }

    if (nonNull(parentTestFolderRQ) && nonNull(parentTestFolderRQ.getName())) {
      return createParentFolderAndGetId(projectId, parentTestFolderRQ);
    }

    // No parent info provided - keep current parent
    return currentParentId;
  }

  /**
   * Creates a new parent folder and returns its ID.
   *
   * @param projectId          project id
   * @param parentTestFolderRQ parent folder details
   * @return ID of the created parent folder
   */
  private Long createParentFolderAndGetId(long projectId, NewTestFolderRQ parentTestFolderRQ) {
    if (nonNull(parentTestFolderRQ.getParentTestFolderId())
        && !existsById(projectId, parentTestFolderRQ.getParentTestFolderId())) {
      throw new ReportPortalException(
          NOT_FOUND,
          TEST_FOLDER_NOT_FOUND_BY_ID.formatted(parentTestFolderRQ.getParentTestFolderId(),
              projectId));
    }

    var newParentFolder = tmsTestFolderMapper.convertToTestFolder(projectId, parentTestFolderRQ);

    var grandParentId = newParentFolder.getParentTestFolder() != null
        ? newParentFolder.getParentTestFolder().getId() : null;

    var max = tmsTestFolderRepository.findMaxIndex(projectId, grandParentId);
    newParentFolder.setIndex(max != null ? max + 1 : 0);

    return tmsTestFolderRepository.save(newParentFolder).getId();
  }

  /**
   * Creates a parent-child relationship between folders during folder creation.
   *
   * @param projectId          The ID of the project
   * @param parentTestFolderId The parent folder ID
   * @param parentTestFolderRQ The parent folder information from the request
   * @param testFolder         The child folder entity to update
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
            TEST_FOLDER_NOT_FOUND_BY_ID.formatted(parentTestFolderId, projectId));
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

        var max = tmsTestFolderRepository.findMaxIndex(projectId,
            parentTestFolder.getParentTestFolder() != null
                ? parentTestFolder.getParentTestFolder().getId() : null);
        parentTestFolder.setIndex(max != null ? max + 1 : 0);

        testFolder.setParentTestFolder(tmsTestFolderRepository.save(parentTestFolder));
      }
    }
  }

  private Long resolveTargetParentForDuplication(long projectId, TmsTestFolderRQ inputDto) {
    var parentTestFolderId = inputDto.getParentTestFolderId();
    var parentTestFolder = inputDto.getParentTestFolder();

    if ((nonNull(parentTestFolderId) && nonNull(parentTestFolder) && nonNull(
        parentTestFolder.getName()))
        || (isNull(parentTestFolderId) && nonNull(parentTestFolder) && isNull(
        parentTestFolder.getName()))) {
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
              TEST_FOLDER_NOT_FOUND_BY_ID.formatted(parentTestFolder.getParentTestFolderId(),
                  projectId)
          );
        }
        targetFolder.setParentTestFolder(
            tmsTestFolderMapper.convertFromId(parentTestFolder.getParentTestFolderId())
        );
      }

      Integer max = tmsTestFolderRepository.findMaxIndex(projectId,
          targetFolder.getParentTestFolder() != null
              ? targetFolder.getParentTestFolder().getId() : null);
      targetFolder.setIndex(max != null ? max + 1 : 0);

      return tmsTestFolderRepository.save(targetFolder).getId();
    } else {
      return null;
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
      Integer targetIndex,
      FolderDuplicationStatistics folderStatistics,
      TestCaseDuplicationStatistics testCaseStatistics) {

    try {
      TmsTestFolder targetParentFolder = null;
      if (nonNull(targetParentFolderId)) {
        targetParentFolder = getEntityById(projectId, targetParentFolderId);
      }

      var uniqueName = generateUniqueFolderName(
          projectId, targetName, targetParentFolderId
      );

      var duplicatedFolder = tmsTestFolderMapper.duplicateTestFolder(
          sourceFolder,
          targetParentFolder
      );
      duplicatedFolder.setName(uniqueName);
      duplicatedFolder.setSubFolders(new ArrayList<>());

      if (targetIndex != null) {
        duplicatedFolder.setIndex(targetIndex);
      } else {
        var max = tmsTestFolderRepository.findMaxIndex(projectId, targetParentFolderId);
        duplicatedFolder.setIndex(max != null ? max + 1 : 0);
      }

      duplicatedFolder = tmsTestFolderRepository.save(duplicatedFolder);
      folderStatistics.addSuccess(duplicatedFolder.getId());

      duplicateTestCasesInFolder(
          projectId,
          sourceFolder,
          duplicatedFolder,
          testCaseStatistics
      );

      if (CollectionUtils.isNotEmpty(sourceFolder.getSubFolders())) {
        for (var subFolder : sourceFolder.getSubFolders()) {
          try {
            var duplicatedSubFolder = duplicateFolderHierarchy(
                projectId,
                subFolder,
                subFolder.getName() + "-copy",
                duplicatedFolder.getId(),
                subFolder.getIndex(),
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

  private void duplicateTestCasesInFolder(
      long projectId,
      TmsTestFolder sourceFolder,
      TmsTestFolder targetFolder,
      TestCaseDuplicationStatistics testCaseStatistics) {

    var testCaseIds = tmsTestFolderRepository.findTestCaseIdsByFolderId(sourceFolder.getId());

    if (CollectionUtils.isEmpty(testCaseIds)) {
      return;
    }

    try {
      var duplicateTestCasesResult = tmsTestCaseService.duplicateTestCases(
          projectId, targetFolder, testCaseIds
      );

      testCaseStatistics.merge(duplicateTestCasesResult);

    } catch (Exception e) {
      testCaseIds.forEach(id -> testCaseStatistics.addError(id, e.getMessage()));
    }
  }

  private Long findOrCreateFolder(Long projectId, Long parentId, String folderName) {
    return tmsTestFolderRepository
        .findByProjectIdAndParentIdAndName(projectId, parentId, folderName)
        .map(TmsTestFolder::getId)
        .orElseGet(() -> createFolderInternal(projectId, parentId, folderName));
  }

  private Long createFolderInternal(Long projectId, Long parentId, String folderName) {
    var folder = tmsTestFolderMapper.convertFromName(projectId, folderName);

    if (parentId != null) {
      var parentFolder = new TmsTestFolder();
      parentFolder.setId(parentId);
      folder.setParentTestFolder(parentFolder);
    }

    var max = tmsTestFolderRepository.findMaxIndex(projectId, parentId);
    folder.setIndex(max != null ? max + 1 : 0);

    var savedFolder = tmsTestFolderRepository.save(folder);
    log.debug("Created folder '{}' with ID {} under parent {}", folderName, savedFolder.getId(),
        parentId);

    return savedFolder.getId();
  }

  /**
   * Handles index changes when moving a folder within the same parent or to a different parent.
   *
   * <p>IMPORTANT: This method expects that folder.index contains the OLD index value,
   * not the target index. The target index should be passed as a parameter.
   * This is crucial to avoid Hibernate auto-flush issues where the folder might
   * accidentally be included in bulk update operations.
   *
   * @param projectId   project id
   * @param folder      the folder being moved (with OLD index value)
   * @param oldParentId the original parent folder id
   * @param oldIndex    the original index of the folder
   * @param newParentId the new parent folder id
   * @param targetIndex the desired target index (can be null for append to end)
   */
  private void handleIndexChange(long projectId, TmsTestFolder folder, Long oldParentId,
      int oldIndex, Long newParentId, Integer targetIndex) {
    var parentChanged = !Objects.equals(oldParentId, newParentId);

    if (parentChanged) {
      // Shift folders in the old parent (close the gap)
      tmsTestFolderRepository.shiftIndexes(projectId, oldParentId, oldIndex + 1, -1);

      int newIndex;
      if (targetIndex != null) {
        newIndex = targetIndex;
        // Make room in the new parent
        tmsTestFolderRepository.shiftIndexes(projectId, newParentId, newIndex, 1);
      } else {
        // Append to the end
        var max = tmsTestFolderRepository.findMaxIndex(projectId, newParentId);
        newIndex = (max == null) ? 0 : max + 1;
      }
      folder.setIndex(newIndex);
    } else {
      // Moving within the same parent
      if (targetIndex != null && targetIndex != oldIndex) {
        if (targetIndex > oldIndex) {
          // Moving down: shift items between (oldIndex+1) and targetIndex by -1
          tmsTestFolderRepository.shiftIndexesBetween(projectId, newParentId,
              oldIndex + 1, targetIndex, -1);
        } else {
          // Moving up: shift items between targetIndex and (oldIndex-1) by +1
          tmsTestFolderRepository.shiftIndexesBetween(projectId, newParentId,
              targetIndex, oldIndex - 1, 1);
        }
        folder.setIndex(targetIndex);
      }
    }
  }

  private void validateNoCycle(long projectId, Long currentFolderId, Long newParentId) {
    if (newParentId == null || currentFolderId == null) {
      return;
    }
    if (currentFolderId.equals(newParentId)) {
      throw new ReportPortalException(BAD_REQUEST_ERROR, "Test folder cannot be its own parent.");
    }
    var hierarchyIds = tmsTestFolderRepository.findAllFolderIdsInHierarchy(projectId, currentFolderId);
    if (hierarchyIds.contains(newParentId)) {
      throw new ReportPortalException(BAD_REQUEST_ERROR, "Cannot move a test folder into its own descendant.");
    }
  }
}
