package com.epam.ta.reportportal.core.tms.service;

import static com.epam.reportportal.rules.exception.ErrorType.BAD_REQUEST_ERROR;
import static com.epam.reportportal.rules.exception.ErrorType.NOT_FOUND;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import com.epam.reportportal.rules.exception.ErrorType;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.core.tms.db.repository.TmsTestCaseRepository;
import com.epam.ta.reportportal.core.tms.db.repository.TmsTestPlanTestCaseRepository;
import com.epam.ta.reportportal.core.tms.dto.NewTestFolderRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsTestCaseRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsTestCaseRS;
import com.epam.ta.reportportal.core.tms.dto.batch.BatchDeleteTestCasesRQ;
import com.epam.ta.reportportal.core.tms.dto.batch.BatchDuplicateTestCasesRQ;
import com.epam.ta.reportportal.core.tms.dto.batch.BatchPatchTestCaseAttributesRQ;
import com.epam.ta.reportportal.core.tms.dto.batch.BatchPatchTestCasesRQ;
import com.epam.ta.reportportal.core.tms.mapper.TmsTestCaseMapper;
import com.epam.ta.reportportal.core.tms.mapper.factory.TmsTestCaseExporterFactory;
import com.epam.ta.reportportal.core.tms.mapper.factory.TmsTestCaseImporterFactory;
import com.epam.ta.reportportal.model.Page;
import com.epam.ta.reportportal.ws.converter.PagedResourcesAssembler;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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
  private final TmsTestCaseAttributeService tmsTestCaseAttributeService;
  private final TmsTestCaseVersionService tmsTestCaseVersionService;
  private final TmsTestCaseImporterFactory importerFactory;
  private final TmsTestCaseExporterFactory exporterFactory;
  private final TmsTestPlanTestCaseRepository tmsTestPlanTestCaseRepository;

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
        .findByTestFolder_ProjectId(projectId)
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
        tmsTestCaseVersionService.getDefaultVersion(testCaseId));
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
      tmsTestCaseAttributeService.createTestCaseAttributes(tmsTestCase, tmsTestCaseRQ.getAttributes());
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

          return tmsTestCaseMapper.convert(existingTestCase, defaultVersion);
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

          return tmsTestCaseMapper.convert(existingTestCase, defaultVersion);
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
  public Page<TmsTestCaseRS> getTestCasesByCriteria(long projectId, String search,
      Long testFolderId, Long testPlanId, Pageable pageable) {
    var testCaseIds = tmsTestCaseRepository.findIdsByCriteria(projectId, search, testFolderId,
        testPlanId, pageable);
    if (testCaseIds.hasContent()) {
      var testCaseDefaultVersions = tmsTestCaseVersionService.getDefaultVersions(
          testCaseIds.getContent());
      var testCases = tmsTestCaseRepository
          .findByProjectIdAndIds(projectId, testCaseIds.getContent());
      var page = tmsTestCaseMapper.convert(testCases, testCaseDefaultVersions, pageable);

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

  private Long getTestFolderId(long projectId, Long testFolderId,
      NewTestFolderRQ testFolderRQ) {
    if (isNull(testFolderId) && isNull(testFolderRQ) ||
        isNull(testFolderId) && isNull(testFolderRQ.getName()) ||
        nonNull(testFolderId) && nonNull(testFolderRQ) && nonNull(testFolderRQ.getName())) {
      throw new ReportPortalException(BAD_REQUEST_ERROR,
          "Either parent folder id or parent folder name should be set");
    } else if (Objects.nonNull(testFolderId)) {
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

    var duplicatedTestCase = tmsTestCaseRepository.save(
        tmsTestCaseMapper.duplicateTestCase(originalTestCase, targetFolder)
    );

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
}
