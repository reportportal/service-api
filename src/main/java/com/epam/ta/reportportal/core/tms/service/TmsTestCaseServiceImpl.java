package com.epam.ta.reportportal.core.tms.service;

import static com.epam.reportportal.rules.exception.ErrorType.NOT_FOUND;

import com.epam.reportportal.rules.exception.ErrorType;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.core.tms.db.repository.TmsTestCaseRepository;
import com.epam.ta.reportportal.core.tms.db.repository.TmsTestPlanTestCaseRepository;
import com.epam.ta.reportportal.core.tms.dto.TmsTestCaseRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsTestCaseRS;
import com.epam.ta.reportportal.core.tms.dto.TmsTestCaseTestFolderRQ;
import com.epam.ta.reportportal.core.tms.dto.batch.BatchDeleteTestCasesRQ;
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
        getTestFolderId(projectId, tmsTestCaseRQ.getTestFolder()));

    tmsTestCaseRepository.save(tmsTestCase);

    if (CollectionUtils.isNotEmpty(tmsTestCaseRQ.getTags())) {
      tmsTestCaseAttributeService.createTestCaseAttributes(tmsTestCase, tmsTestCaseRQ.getTags());
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
                  getTestFolderId(projectId, tmsTestCaseRQ.getTestFolder())));

          tmsTestCaseAttributeService.updateTestCaseAttributes(existingTestCase,
              tmsTestCaseRQ.getTags());

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
                  getTestFolderId(projectId, tmsTestCaseRQ.getTestFolder())));

          tmsTestCaseAttributeService.patchTestCaseAttributes(existingTestCase,
              tmsTestCaseRQ.getTags());

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
    if (CollectionUtils.isNotEmpty(patchRequest.getTags())) {
      tmsTestCaseAttributeService.patchTestCaseAttributes(
          tmsTestCaseRepository.findAllById(testCaseIds),
          patchRequest.getTags()
      );
    }
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
    var testCaseRequests = importer.importFromFile(
        file,
        tmsTestFolderService.resolveTestFolderRQ(testFolderId, testFolderName)
    );

    return testCaseRequests.stream()
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
  public void deleteTagsFromTestCase(Long projectId, Long testCaseId, List<Long> attributeIds) {
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
  public void deleteTagsFromTestCases(Long projectId, List<Long> testCaseIds,
      List<Long> attributeIds) {
    var existingTestCaseIds = new HashSet<>(tmsTestCaseRepository
        .findExistingIdsByProjectIdAndIds(projectId, testCaseIds)
    );

    if (existingTestCaseIds.size() != testCaseIds.size()) {
      var missingIds = testCaseIds.stream()
          .filter(id -> !existingTestCaseIds.contains(id))
          .toList();

      throw new ReportPortalException(
          ErrorType.NOT_FOUND, TEST_CASES_NOT_FOUND_BY_IDS.formatted(missingIds, projectId)
      );
    }

    tmsTestCaseAttributeService.deleteByTestCaseIdsAndAttributeIds(testCaseIds,
        attributeIds);
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

  private Long getTestFolderId(long projectId, TmsTestCaseTestFolderRQ testFolderRQ) {
    if (Objects.isNull(testFolderRQ)) {
      return null;
    }
    var testFolderId = testFolderRQ.getId();
    if (Objects.nonNull(testFolderId)) {
      if (tmsTestFolderService.existsById(projectId, testFolderId)) {
        return testFolderId;
      } else {
        throw new ReportPortalException(
            NOT_FOUND, TEST_FOLDER_NOT_FOUND_BY_ID.formatted(testFolderId, projectId));
      }
    } else {
      return tmsTestFolderService
          .create(projectId, testFolderRQ.getName())
          .getId();
    }
  }
}
