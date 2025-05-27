package com.epam.ta.reportportal.core.tms.service;

import static org.apache.commons.collections4.CollectionUtils.isEmpty;

import com.epam.ta.reportportal.core.tms.db.entity.TmsTestCase;
import com.epam.ta.reportportal.core.tms.db.repository.TmsTestCaseAttributeRepository;
import com.epam.ta.reportportal.core.tms.dto.TmsTestCaseAttributeRQ;
import com.epam.ta.reportportal.core.tms.mapper.TmsTestCaseAttributeMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TmsTestCaseAttributeServiceImpl implements TmsTestCaseAttributeService {

  private final TmsTestCaseAttributeMapper tmsTestCaseAttributeMapper;
  private final TmsTestCaseAttributeRepository tmsTestCaseAttributeRepository;

  @Override
  @Transactional
  public void createTestCaseAttributes(TmsTestCase tmsTestCase,
      List<TmsTestCaseAttributeRQ> attributes) {
    if (isEmpty(attributes)) {
      return;
    }
    var tmsTestCaseAttributes = tmsTestCaseAttributeMapper.convertToTmsTestCaseAttributes(
        attributes);
    tmsTestCase.setTags(tmsTestCaseAttributes);
    tmsTestCaseAttributes.forEach(
        tmsTestCaseAttribute -> tmsTestCaseAttribute.setTestCase(tmsTestCase));
    tmsTestCaseAttributeRepository.saveAll(tmsTestCaseAttributes);
  }

  @Override
  @Transactional
  public void updateTestCaseAttributes(TmsTestCase tmsTestCase,
      List<TmsTestCaseAttributeRQ> attributes) {
    tmsTestCaseAttributeRepository.deleteAllById_TestCaseId(tmsTestCase.getId());
    createTestCaseAttributes(tmsTestCase, attributes);
  }

  @Override
  @Transactional
  public void patchTestCaseAttributes(TmsTestCase tmsTestCase,
      List<TmsTestCaseAttributeRQ> attributes) {
    if (isEmpty(attributes)) {
      return;
    }
    var tmsTestCaseAttributes = tmsTestCaseAttributeMapper.convertToTmsTestCaseAttributes(
        attributes);
    tmsTestCaseAttributes.forEach(
        tmsTestCaseAttribute -> tmsTestCaseAttribute.setTestCase(tmsTestCase));
    tmsTestCase.getTags().addAll(tmsTestCaseAttributes);
    tmsTestCaseAttributeRepository.saveAll(tmsTestCaseAttributes);
  }

  @Override
  @Transactional
  public void deleteAllByTestCaseId(Long testCaseId) {
    tmsTestCaseAttributeRepository.deleteAllById_TestCaseId(testCaseId);
  }

  @Override
  @Transactional
  public void deleteAllByTestFolderId(Long projectId, Long testFolderId) {
    tmsTestCaseAttributeRepository.deleteTestCaseAttributesByTestFolderId(projectId, testFolderId);
  }
}
