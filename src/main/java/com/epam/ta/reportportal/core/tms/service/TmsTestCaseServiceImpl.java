package com.epam.ta.reportportal.core.tms.service;

import static com.epam.reportportal.rules.exception.ErrorType.NOT_FOUND;

import com.epam.reportportal.rules.exception.ErrorType;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.core.tms.dto.NewTestFolderRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsTestCaseRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsTestCaseRS;
import com.epam.ta.reportportal.core.tms.dto.batch.BatchDeleteTestCasesRQ;
import com.epam.ta.reportportal.core.tms.dto.batch.BatchDuplicateTestCasesRQ;
import com.epam.ta.reportportal.core.tms.dto.batch.BatchTestCaseOperationError;
import com.epam.ta.reportportal.core.tms.dto.batch.BatchTestCaseOperationResultRS;
import com.epam.ta.reportportal.core.tms.dto.batch.BatchPatchTestCaseAttributesRQ;
import com.epam.ta.reportportal.core.tms.dto.batch.BatchPatchTestCasesRQ;
import com.epam.ta.reportportal.core.tms.mapper.TmsTestCaseMapper;
import com.epam.ta.reportportal.core.tms.mapper.factory.TmsTestCaseExporterFactory;
import com.epam.ta.reportportal.core.tms.mapper.factory.TmsTestCaseImporterFactory;
import com.epam.ta.reportportal.dao.tms.TmsTestCaseRepository;
import com.epam.ta.reportportal.dao.tms.TmsTestPlanTestCaseRepository;
import com.epam.ta.reportportal.dao.tms.filterable.TmsTestCaseFilterableRepository;
import com.epam.ta.reportportal.entity.tms.TmsTestCase;
import com.epam.ta.reportportal.model.Page;
import com.epam.ta.reportportal.ws.converter.PagedResourcesAssembler;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Valid
public class TmsTestCaseServiceImpl implements TmsTestCaseService {

  private static final String TEST_CASE_NOT_FOUND_BY_ID = "Test Case with id: %d for projectId: %d";
  private static final String TEST_CASES_NOT_FOUND_BY_IDS = "Test Cases with ids: %s for projectId: %d";
  private static final String TEST_FOLDER_NOT_FOUND_BY_ID =
      "Test Folder with id: %d for project: %d";

  private final TmsTestCaseMapper tmsTestCaseMapper;
  private final TmsTestCaseRepository tmsTestCaseRepository;
  private final TmsTestCaseFilterableRepository tmsTestCaseFilterableRepository;
  private final TmsTestCaseAttributeService tmsTestCaseAttributeService;
  private final TmsTestCaseVersionService tmsTestCaseVersionService;
  private final TmsTestCaseImporterFactory importerFactory;
  private final TmsTestCaseExporterFactory exporterFactory;
  private final TmsTestPlanTestCaseRepository tmsTestPlanTestCaseRepository;
  private final TmsTestCaseExecutionService tmsTestCaseExecutionService;

  private TmsTestFolderService tmsTestFolderService;

  @Autowired
  public void setTmsTestFolderService(
      TmsTestFolderService tmsTestFolderService) {
    this.tmsTestFolderService = tmsTestFolderService;
  }

  @Override
  @Transactional(readOnly = true)
  public List<TmsTestCaseRS> getTestCaseByProjectId(long projectId) {
    return tmsTestCaseRepository
        .findByProjectId(projectId)
        .stream()
        .map(tmsTestCaseMapper::convert)
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public TmsTestCaseRS getById(long projectId, Long testCaseId) {
    return tmsTestCaseMapper.convert(
        tmsTestCaseRepository
            .findByProjectIdAndId(projectId, testCaseId)
            .orElseThrow(() -> new ReportPortalException(
                NOT_FOUND, TEST_CASE_NOT_FOUND_BY_ID.formatted(testCaseId, projectId))
            ),
        tmsTestCaseVersionService.getDefaultVersion(testCaseId),
        tmsTestCaseExecutionService.getLastTestCaseExecution(testCaseId));
  }


  @Override
  @Transactional
  public TmsTestCaseRS create(long projectId, TmsTestCaseRQ tmsTestCaseRQ) {
    var tmsTestCase = tmsTestCaseMapper.convertFromRQ(projectId, tmsTestCaseRQ,
        getTestFolderId(
            projectId,
            tmsTestCaseRQ.getTestFolderId(),
            tmsTestCaseRQ.getTestFolder()
        )
    );

    tmsTestCaseRepository.save(tmsTestCase);

    if (CollectionUtils.isNotEmpty(tmsTestCaseRQ.getAttributes())) {
      tmsTestCaseAttributeService.createTestCaseAttributes(tmsTestCase,
          tmsTestCaseRQ.getAttributes());
    }

    var defaultVersion = tmsTestCaseVersionService.createDefaultTestCaseVersion(tmsTestCase,
        tmsTestCaseRQ.getManualScenario());

    return tmsTestCaseMapper.convert(tmsTestCase, defaultVersion);
  }

  @Override
  @Transactional
  public TmsTestCaseRS update(long projectId, Long testCaseId, TmsTestCaseRQ tmsTestCaseRQ) {
    return tmsTestCaseRepository
        .findByProjectIdAndId(projectId, testCaseId)
        .map((var existingTestCase) -> {
          tmsTestCaseMapper.update(existingTestCase,
              tmsTestCaseMapper.convertFromRQ(projectId, tmsTestCaseRQ,
                  getTestFolderId(projectId, tmsTestCaseRQ.getTestFolderId(),
                      tmsTestCaseRQ.getTestFolder())));

          tmsTestCaseAttributeService.updateTestCaseAttributes(existingTestCase,
              tmsTestCaseRQ.getAttributes());

          var defaultVersion = tmsTestCaseVersionService.updateDefaultTestCaseVersion(
              existingTestCase,
              tmsTestCaseRQ.getManualScenario());

          var lastTestCaseExecution = tmsTestCaseExecutionService.getLastTestCaseExecution(
              existingTestCase.getId()
          );

          return tmsTestCaseMapper.convert(
              existingTestCase, defaultVersion, lastTestCaseExecution
          );
        })
        .orElseGet(() -> create(projectId, tmsTestCaseRQ));
  }

  @Override
  @Transactional
  public TmsTestCaseRS patch(long projectId, Long testCaseId, TmsTestCaseRQ tmsTestCaseRQ) {
    return tmsTestCaseRepository
        .findByProjectIdAndId(projectId, testCaseId)
        .map((var existingTestCase) -> {
          tmsTestCaseMapper.patch(existingTestCase,
              tmsTestCaseMapper.convertFromRQ(projectId, tmsTestCaseRQ,
                  getTestFolderId(projectId, tmsTestCaseRQ.getTestFolderId(),
                      tmsTestCaseRQ.getTestFolder())));

          tmsTestCaseAttributeService.patchTestCaseAttributes(existingTestCase,
              tmsTestCaseRQ.getAttributes());

          var defaultVersion = tmsTestCaseVersionService.patchDefaultTestCaseVersion(
              existingTestCase,
              tmsTestCaseRQ.getManualScenario());

          var lastTestCaseExecution = tmsTestCaseExecutionService.getLastTestCaseExecution(
              existingTestCase.getId()
          );

          return tmsTestCaseMapper.convert(
              existingTestCase, defaultVersion, lastTestCaseExecution
          );
        })
        .orElseThrow(() -> new ReportPortalException(
            NOT_FOUND, TEST_CASE_NOT_FOUND_BY_ID.formatted(testCaseId, projectId))
        );
  }

  @Override
  @Transactional
  public void delete(long projectId, Long testCaseId) {
    tmsTestCaseAttributeService.deleteAllByTestCaseId(testCaseId);
    tmsTestCaseVersionService.deleteAllByTestCaseId(testCaseId);
    tmsTestPlanTestCaseRepository.deleteAllByTestCaseId(testCaseId);
    tmsTestCaseRepository.deleteById(testCaseId);
  }

  @Override
  @Transactional
  public void deleteByTestFolderId(long projectId, long folderId) {
    tmsTestCaseAttributeService.deleteAllByTestFolderId(projectId, folderId);
    tmsTestCaseVersionService.deleteAllByTestFolderId(projectId, folderId);
    tmsTestPlanTestCaseRepository.deleteAllByTestFolderId(projectId, folderId);
    tmsTestCaseRepository.deleteTestCasesByFolderId(projectId, folderId);
  }

  @Override
  @Transactional
  public void delete(long projectId,
      @Valid BatchDeleteTestCasesRQ deleteRequest) {
    tmsTestCaseAttributeService.deleteAllByTestCaseIds(deleteRequest.getTestCaseIds());
    tmsTestCaseVersionService.deleteAllByTestCaseIds(deleteRequest.getTestCaseIds());
    tmsTestPlanTestCaseRepository.deleteAllByTestCaseIds(deleteRequest.getTestCaseIds());
    tmsTestCaseRepository.deleteAllByTestCaseIds(deleteRequest.getTestCaseIds());
  }

  @Override
  @Transactional
  public void patch(long projectId,
      @Valid BatchPatchTestCasesRQ patchRequest) {
    var testCaseIds = patchRequest.getTestCaseIds();
    var testFolderId = patchRequest.getTestFolderId();
    if (Objects.nonNull(testFolderId) && !tmsTestFolderService.existsById(projectId,
        testFolderId)) {
      throw new ReportPortalException(
          NOT_FOUND, TEST_FOLDER_NOT_FOUND_BY_ID.formatted(testFolderId, projectId));
    }
    if (Objects.nonNull(testFolderId)
        || Objects.nonNull(patchRequest.getPriority())) {
      tmsTestCaseRepository.patch(projectId,
          testCaseIds,
          testFolderId,
          patchRequest.getPriority());
    }
  }

  @Override
  @Transactional
  public List<TmsTestCaseRS> importFromFile(long projectId,
      Long testFolderId,
      String testFolderName,
      MultipartFile file) {
    var importer = importerFactory.getImporter(file);
    var testCaseRequests = importer.importFromFile(file);

    return testCaseRequests
        .stream()
        .peek(testCaseRequest -> tmsTestFolderService.resolveTestFolderRQ(
            testCaseRequest, testFolderId, testFolderName))
        .map(testCaseRQ -> create(projectId, testCaseRQ))
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public void exportToFile(Long projectId, List<Long> testCaseIds, String format,
      boolean includeAttachments, HttpServletResponse response) {
    List<TmsTestCaseRS> testCases;

    if (testCaseIds != null && !testCaseIds.isEmpty()) {
      testCases = testCaseIds.stream()
          .map(id -> getById(projectId, id))
          .toList();
    } else {
      testCases = getTestCaseByProjectId(projectId);
    }
    exporterFactory
        .getExporter(format)
        .export(testCases, includeAttachments, response);

  }

  @Override
  @Transactional(readOnly = true)
  public Page<TmsTestCaseRS> getTestCasesByCriteria(long projectId, Filter filter,
      Pageable pageable) {
    var testCaseIds = tmsTestCaseFilterableRepository.findIdsByProjectIdAndFilter(
        projectId, filter, pageable
    );
    if (testCaseIds.hasContent()) {
      var testCaseDefaultVersions = tmsTestCaseVersionService.getDefaultVersions(
          testCaseIds.getContent());
      var testCases = tmsTestCaseRepository
          .findByProjectIdAndIds(projectId, testCaseIds.getContent())
          .stream()
          .collect(Collectors.toMap(TmsTestCase::getId, Function.identity()));

      var orderedTestTestCases = testCaseIds
          .getContent()
          .stream()
          .map(testCases::get)
          .filter(Objects::nonNull)
          .toList();

      var lastTestCasesExecutions = tmsTestCaseExecutionService.getLastTestCasesExecutionsByTestCaseIds(
          testCaseIds.getContent()
      );

      var page = tmsTestCaseMapper.convert(
          orderedTestTestCases,
          testCaseDefaultVersions,
          lastTestCasesExecutions,
          pageable,
          testCaseIds.getTotalElements()
      );

      return PagedResourcesAssembler
          .<TmsTestCaseRS>pageConverter()
          .apply(page);
    } else {
      return PagedResourcesAssembler
          .<TmsTestCaseRS>pageConverter()
          .apply(new PageImpl<>(Collections.emptyList(), pageable, 0));
    }
  }

  @Override
  @Transactional
  public void deleteAttributesFromTestCase(Long projectId, Long testCaseId,
      List<Long> attributeIds) {
    if (!tmsTestCaseRepository.existsByTestFolder_Project_IdAndId(projectId, testCaseId)) {
      throw new ReportPortalException(
          ErrorType.NOT_FOUND, TEST_CASE_NOT_FOUND_BY_ID.formatted(testCaseId, projectId)
      );
    }
    tmsTestCaseAttributeService.deleteByTestCaseIdAndAttributeIds(testCaseId,
        attributeIds);
  }

  @Override
  @Transactional
  public void patchTestCaseAttributes(Long projectId, BatchPatchTestCaseAttributesRQ patchRequest) {
    validateTestCasesExist(projectId, patchRequest.getTestCaseIds());

    var attributesToRemove = Optional
        .ofNullable(patchRequest.getAttributesToRemove())
        .orElse(Collections.emptyList());
    var attributesToAdd = Optional
        .ofNullable(patchRequest.getAttributeIdsToAdd())
        .orElse(Collections.emptyList());

    var attributesSetToRemove = new HashSet<>(attributesToRemove);
    var attributesSetToAdd = new HashSet<>(attributesToAdd);

    var intersection = new HashSet<>(attributesSetToRemove);
    intersection.retainAll(attributesSetToAdd);

    attributesSetToRemove.removeAll(intersection);
    attributesSetToAdd.removeAll(intersection);

    if (!attributesSetToRemove.isEmpty()) {
      tmsTestCaseAttributeService.deleteByTestCaseIdsAndAttributeIds(
          patchRequest.getTestCaseIds(), attributesSetToRemove
      );
    }

    if (!attributesSetToAdd.isEmpty()) {
      tmsTestCaseAttributeService.addAttributesToTestCases(
          patchRequest.getTestCaseIds(), attributesSetToAdd
      );
    }
  }

  @Override
  @Transactional(readOnly = true)
  public void validateTestCasesExist(Long projectId, List<Long> testCaseIds) {
    var existingTestCaseIds = new HashSet<>(
        tmsTestCaseRepository.findExistingIdsByProjectIdAndIds(projectId, testCaseIds)
    );

    var notFoundTestCaseIds = testCaseIds.stream()
        .filter(id -> !existingTestCaseIds.contains(id))
        .toList();

    if (!notFoundTestCaseIds.isEmpty()) {
      throw new ReportPortalException(
          NOT_FOUND,
          TEST_CASES_NOT_FOUND_BY_IDS.formatted(notFoundTestCaseIds, projectId)
      );
    }
  }

  @Override
  @Transactional
  public List<TmsTestCaseRS> duplicate(long projectId, BatchDuplicateTestCasesRQ duplicateRequest) {
    validateTestCasesExist(projectId, duplicateRequest.getTestCaseIds());

    var targetFolderId = tmsTestFolderService.resolveTargetFolderId(
        projectId,
        duplicateRequest.getTestFolderId(),
        duplicateRequest.getTestFolder()
    );

    return duplicateRequest
        .getTestCaseIds()
        .stream()
        .map(testCaseId -> duplicateTestCase(projectId, testCaseId, targetFolderId))
        .toList();
  }

  @Override
  @Transactional
  public BatchTestCaseOperationResultRS duplicateTestCases(long projectId, List<Long> testCaseIds) {
    var errors = new ArrayList<BatchTestCaseOperationError>();
    var successfulIds = new ArrayList<Long>();

    for (var testCaseId : testCaseIds) {
      try {
        var originalTestCase = tmsTestCaseRepository
            .findByProjectIdAndId(projectId, testCaseId)
            .orElseThrow(() -> new ReportPortalException(
                NOT_FOUND, TEST_CASE_NOT_FOUND_BY_ID.formatted(testCaseId, projectId))
            );

        var originalDefaultVersion = tmsTestCaseVersionService.getDefaultVersion(testCaseId);

        var duplicatedTestCase = tmsTestCaseMapper.duplicateTestCase(
            originalTestCase, originalTestCase.getTestFolder()
        );

        duplicatedTestCase.setName(generateUniqueTestCaseName(
            projectId,
            originalTestCase.getName(),
            originalTestCase.getTestFolder().getId())
        );

        duplicatedTestCase = tmsTestCaseRepository.save(duplicatedTestCase);

        tmsTestCaseVersionService.duplicateDefaultVersion(duplicatedTestCase,
            originalDefaultVersion);

        if (CollectionUtils.isNotEmpty(originalTestCase.getAttributes())) {
          tmsTestCaseAttributeService.duplicateTestCaseAttributes(originalTestCase,
              duplicatedTestCase);
        }

        successfulIds.add(duplicatedTestCase.getId());

      } catch (Exception e) {
        errors.add(new BatchTestCaseOperationError(testCaseId,
            "Failed to duplicate test case: " + e.getMessage()));
      }
    }

    return tmsTestCaseMapper.toBatchOperationResult(successfulIds, errors);
  }

  private Long getTestFolderId(long projectId, Long testFolderId,
      NewTestFolderRQ testFolderRQ) {
    if (Objects.isNull(testFolderId) && Objects.isNull(testFolderRQ)) {
      return null;
    }
    if (Objects.nonNull(testFolderId)) {
      if (tmsTestFolderService.existsById(projectId, testFolderId)) {
        return testFolderId;
      } else {
        throw new ReportPortalException(
            NOT_FOUND, TEST_FOLDER_NOT_FOUND_BY_ID.formatted(testFolderId, projectId));
      }
    } else {
      return tmsTestFolderService
          .create(projectId, testFolderRQ)
          .getId();
    }
  }

  @Transactional
  public TmsTestCaseRS duplicateTestCase(long projectId, Long testCaseId, Long targetFolderId) {
    var originalTestCase = tmsTestCaseRepository
        .findByProjectIdAndId(projectId, testCaseId)
        .orElseThrow(() -> new ReportPortalException(
            NOT_FOUND, TEST_CASE_NOT_FOUND_BY_ID.formatted(testCaseId, projectId))
        );

    var originalDefaultVersion = tmsTestCaseVersionService.getDefaultVersion(testCaseId);

    var targetFolder = tmsTestFolderService.getEntityById(projectId, targetFolderId);

    var duplicatedTestCase = tmsTestCaseMapper.duplicateTestCase(originalTestCase, targetFolder);

    duplicatedTestCase.setName(generateUniqueTestCaseName(
        projectId, originalTestCase.getName(),
        originalTestCase.getTestFolder().getId())
    );

    duplicatedTestCase = tmsTestCaseRepository.save(duplicatedTestCase);

    var duplicatedDefaultVersion = tmsTestCaseVersionService.duplicateDefaultVersion(
        duplicatedTestCase, originalDefaultVersion);

    if (CollectionUtils.isNotEmpty(originalTestCase.getAttributes())) {
      tmsTestCaseAttributeService.duplicateTestCaseAttributes(originalTestCase, duplicatedTestCase);
    }

    return tmsTestCaseMapper.convert(duplicatedTestCase, duplicatedDefaultVersion);
  }

  @Override
  @Transactional(readOnly = true)
  public boolean existsById(Long projectId, Long testCaseId) {
    return tmsTestCaseRepository.existsByIdAndProjectId(testCaseId, projectId);
  }


  @Override
  @Transactional(readOnly = true)
  public List<Long> getExistingTestCaseIds(Long projectId, List<Long> testCaseIds) {
    if (testCaseIds == null || testCaseIds.isEmpty()) {
      return List.of();
    }
    return tmsTestCaseRepository.findExistingTestCaseIds(projectId, testCaseIds);
  }

  private String generateUniqueTestCaseName(long projectId, String originalName, Long testFolderId) {
    var baseName = originalName + "-copy";
    var uniqueName = baseName;
    var counter = 1;

    while (tmsTestCaseRepository.existsByNameAndTestFolder(projectId, uniqueName, testFolderId)) {
      uniqueName = baseName + "-" + counter;
      counter++;
    }

    return uniqueName;
  }

}
