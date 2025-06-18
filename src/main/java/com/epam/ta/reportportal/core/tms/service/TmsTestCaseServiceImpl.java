package com.epam.ta.reportportal.core.tms.service;

import com.epam.ta.reportportal.core.tms.db.repository.TmsTestCaseRepository;
import com.epam.ta.reportportal.core.tms.dto.TmsTestCaseRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsTestCaseRS;
import com.epam.ta.reportportal.core.tms.dto.batch.BatchDeleteTestCasesRQ;
import com.epam.ta.reportportal.core.tms.dto.batch.BatchPatchTestCasesRQ;
import com.epam.ta.reportportal.core.tms.exception.NotFoundException;
import com.epam.ta.reportportal.core.tms.mapper.TmsTestCaseMapper;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Valid
public class TmsTestCaseServiceImpl implements TmsTestCaseService {

  private static final String TEST_CASE_NOT_FOUND_BY_ID = "Test Case cannot be found by id: {0}. Project: {1}";

  private final TmsTestCaseMapper tmsTestCaseMapper;
  private final TmsTestCaseRepository tmsTestCaseRepository;
  private final TmsTestCaseAttributeService tmsTestCaseAttributeService;

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
    var tmsTestCase = tmsTestCaseMapper.convertFromRQ(projectId, tmsTestCaseRQ);

    tmsTestCaseRepository.save(tmsTestCase);

    tmsTestCaseAttributeService.createTestCaseAttributes(tmsTestCase, tmsTestCaseRQ.getTags());

    return tmsTestCaseMapper.convert(tmsTestCase);
  }

  @Override
  @Transactional
  public TmsTestCaseRS update(long projectId, Long testCaseId, TmsTestCaseRQ tmsTestCaseRQ) {
    return tmsTestCaseRepository
        .findById(testCaseId)
        .map((var existingTestCase) -> {
          tmsTestCaseMapper.update(existingTestCase,
              tmsTestCaseMapper.convertFromRQ(projectId, tmsTestCaseRQ));

          tmsTestCaseAttributeService.updateTestCaseAttributes(existingTestCase,
              tmsTestCaseRQ.getTags());

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
              tmsTestCaseMapper.convertFromRQ(projectId, tmsTestCaseRQ));

          tmsTestCaseAttributeService.patchTestCaseAttributes(existingTestCase,
              tmsTestCaseRQ.getTags());

          return tmsTestCaseMapper.convert(existingTestCase);
        })
        .orElseThrow(
            NotFoundException.supplier(TEST_CASE_NOT_FOUND_BY_ID, testCaseId, projectId));
  }

  @Override
  @Transactional
  public void delete(long projectId, Long testCaseId) {
    tmsTestCaseAttributeService.deleteAllByTestCaseId(testCaseId);
    tmsTestCaseRepository.deleteById(testCaseId);
  }

  @Override
  @Transactional
  public void deleteByTestFolderId(long projectId, long folderId) {
    tmsTestCaseAttributeService.deleteAllByTestFolderId(projectId, folderId);
    tmsTestCaseRepository.deleteTestCasesByFolderId(projectId, folderId);
  }

  @Override
  @Transactional
  public void delete(long projectId,
      @Valid BatchDeleteTestCasesRQ deleteRequest) {
    tmsTestCaseAttributeService.deleteAllByTestCaseIds(deleteRequest.getTestCaseIds());
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
}
