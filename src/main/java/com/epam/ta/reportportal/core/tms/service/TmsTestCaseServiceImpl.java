package com.epam.ta.reportportal.core.tms.service;

import com.epam.ta.reportportal.core.tms.db.repository.TmsTestCaseRepository;
import com.epam.ta.reportportal.core.tms.dto.TmsTestCaseRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsTestCaseRS;
import com.epam.ta.reportportal.core.tms.dto.TmsTestCaseTestFolderRQ;
import com.epam.ta.reportportal.core.tms.dto.batch.BatchDeleteTestCasesRQ;
import com.epam.ta.reportportal.core.tms.dto.batch.BatchPatchTestCasesRQ;
import com.epam.ta.reportportal.core.tms.exception.NotFoundException;
import com.epam.ta.reportportal.core.tms.mapper.TmsTestCaseMapper;
import com.epam.ta.reportportal.core.tms.mapper.factory.TmsTestCaseExporterFactory;
import com.epam.ta.reportportal.core.tms.mapper.factory.TmsTestCaseImporterFactory;
import com.epam.ta.reportportal.model.Page;
import com.epam.ta.reportportal.ws.converter.PagedResourcesAssembler;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Valid
public class TmsTestCaseServiceImpl implements TmsTestCaseService {

  private static final String TEST_CASE_NOT_FOUND_BY_ID = "Test Case cannot be found by id: {0}. Project: {1}";

  private final TmsTestCaseMapper tmsTestCaseMapper;
  private final TmsTestCaseRepository tmsTestCaseRepository;
  private final TmsTestCaseAttributeService tmsTestCaseAttributeService;
  private final TmsTestCaseVersionService tmsTestCaseVersionService;
  private final TmsTestCaseImporterFactory importerFactory;
  private final TmsTestCaseExporterFactory exporterFactory;

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
    return tmsTestCaseMapper
        .convert(tmsTestCaseRepository.findById(testCaseId)
            .orElseThrow(
                NotFoundException.supplier(TEST_CASE_NOT_FOUND_BY_ID, testCaseId, projectId)));
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

    if (Objects.nonNull(tmsTestCaseRQ.getTestCaseDefaultVersion())) {
      tmsTestCaseVersionService.createDefaultTestCaseVersion(tmsTestCase,
          tmsTestCaseRQ.getTestCaseDefaultVersion());
    }

    return tmsTestCaseMapper.convert(tmsTestCase);
  }

  @Override
  @Transactional
  public TmsTestCaseRS update(long projectId, Long testCaseId, TmsTestCaseRQ tmsTestCaseRQ) {
    return tmsTestCaseRepository
        .findById(testCaseId)
        .map((var existingTestCase) -> {
          tmsTestCaseMapper.update(existingTestCase,
              tmsTestCaseMapper.convertFromRQ(projectId, tmsTestCaseRQ,
                  getTestFolderId(projectId, tmsTestCaseRQ.getTestFolder())));

          tmsTestCaseAttributeService.updateTestCaseAttributes(existingTestCase,
              tmsTestCaseRQ.getTags());

          tmsTestCaseVersionService.updateDefaultTestCaseVersion(existingTestCase,
              tmsTestCaseRQ.getTestCaseDefaultVersion());

          return tmsTestCaseMapper.convert(existingTestCase);
        })
        .orElseGet(() -> create(projectId, tmsTestCaseRQ));
  }

  @Override
  @Transactional
  public TmsTestCaseRS patch(long projectId, Long testCaseId, TmsTestCaseRQ tmsTestCaseRQ) {
    return tmsTestCaseRepository
        .findByIdAndProjectId(testCaseId, projectId)
        .map((var existingTestCase) -> {
          tmsTestCaseMapper.patch(existingTestCase,
              tmsTestCaseMapper.convertFromRQ(projectId, tmsTestCaseRQ,
                  getTestFolderId(projectId, tmsTestCaseRQ.getTestFolder())));

          tmsTestCaseAttributeService.patchTestCaseAttributes(existingTestCase,
              tmsTestCaseRQ.getTags());

          tmsTestCaseVersionService.patchDefaultTestCaseVersion(existingTestCase,
              tmsTestCaseRQ.getTestCaseDefaultVersion());

          return tmsTestCaseMapper.convert(existingTestCase);
        })
        .orElseThrow(
            NotFoundException.supplier(TEST_CASE_NOT_FOUND_BY_ID, testCaseId, projectId));
  }

  @Override
  @Transactional
  public void delete(long projectId, Long testCaseId) {
    tmsTestCaseAttributeService.deleteAllByTestCaseId(testCaseId);
    tmsTestCaseVersionService.deleteAllByTestCaseId(testCaseId);
    tmsTestCaseRepository.deleteById(testCaseId);
  }

  @Override
  @Transactional
  public void deleteByTestFolderId(long projectId, long folderId) {
    tmsTestCaseAttributeService.deleteAllByTestFolderId(projectId, folderId);
    tmsTestCaseVersionService.deleteAllByTestFolderId(projectId, folderId);
    tmsTestCaseRepository.deleteTestCasesByFolderId(projectId, folderId);
  }

  @Override
  @Transactional
  public void delete(long projectId,
      @Valid BatchDeleteTestCasesRQ deleteRequest) {
    tmsTestCaseAttributeService.deleteAllByTestCaseIds(deleteRequest.getTestCaseIds());
    tmsTestCaseVersionService.deleteAllByTestCaseIds(deleteRequest.getTestCaseIds());
    tmsTestCaseRepository.deleteAllByTestCaseIds(deleteRequest.getTestCaseIds());
  }

  @Override
  @Transactional
  public void patch(long projectId,
      @Valid BatchPatchTestCasesRQ patchRequest) {
    tmsTestCaseRepository.patch(projectId,
        patchRequest.getTestCaseIds(),
        patchRequest.getTestFolderId());
  }

  @Override
  @Transactional
  public List<TmsTestCaseRS> importFromFile(long projectId, MultipartFile file) {
    var importer = importerFactory.getImporter(file);
    var testCaseRequests = importer.importFromFile(file);

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
  public Page<TmsTestCaseRS> getTestCasesByCriteria(long projectId, String search, Long testFolderId, Pageable pageable) {
    var testCasePage = tmsTestCaseRepository.findByCriteria(
        projectId, search, testFolderId, pageable
    );
    return PagedResourcesAssembler
        .pageConverter(tmsTestCaseMapper::convert)
        .apply(testCasePage);
  }


  private Long getTestFolderId(long projectId, TmsTestCaseTestFolderRQ testFolderRQ) {
    if (Objects.isNull(testFolderRQ)) {
      return null;
    }
    var testFolderId = testFolderRQ.getTestFolderId();
    return Objects.nonNull(testFolderId) ? testFolderId : tmsTestFolderService
        .create(projectId, testFolderRQ.getTestFolderName())
        .getId();
  }
}
