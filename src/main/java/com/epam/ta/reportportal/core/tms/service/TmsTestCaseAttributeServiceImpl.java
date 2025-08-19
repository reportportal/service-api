package com.epam.ta.reportportal.core.tms.service;

import static org.apache.commons.collections4.CollectionUtils.isEmpty;

import com.epam.ta.reportportal.core.tms.db.entity.TmsTestCase;
import com.epam.ta.reportportal.core.tms.db.repository.TmsTestCaseAttributeRepository;
import com.epam.ta.reportportal.core.tms.dto.TmsAttributeRQ;
import com.epam.ta.reportportal.core.tms.mapper.TmsTestCaseAttributeMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Valid
public class TmsTestCaseAttributeServiceImpl implements TmsTestCaseAttributeService {

  private final TmsTestCaseAttributeMapper tmsTestCaseAttributeMapper;
  private final TmsTestCaseAttributeRepository tmsTestCaseAttributeRepository;
  private final TmsAttributeService tmsAttributeService;

  @Override
  @Transactional
  public void createTestCaseAttributes(@NotNull TmsTestCase tmsTestCase,
      @NotEmpty List<TmsAttributeRQ> attributes) {
    var tmsTestCaseAttributes = tmsTestCaseAttributeMapper.convertToTmsTestCaseAttributes(
        tmsAttributeService.getTmsAttributes(attributes),
        attributes
    );
    tmsTestCase.setTags(tmsTestCaseAttributes);
    tmsTestCaseAttributes.forEach(
        tmsTestCaseAttribute -> tmsTestCaseAttribute.setTestCase(tmsTestCase));
    tmsTestCaseAttributeRepository.saveAll(tmsTestCaseAttributes);
  }

  @Override
  @Transactional
  public void updateTestCaseAttributes(@NotNull TmsTestCase tmsTestCase,
      List<TmsAttributeRQ> attributes) {
    tmsTestCaseAttributeRepository.deleteAllByTestCaseId(tmsTestCase.getId());
    if (CollectionUtils.isNotEmpty(attributes)) {
      createTestCaseAttributes(tmsTestCase, attributes);
    }
  }

  @Override
  @Transactional
  public void patchTestCaseAttributes(@NotNull TmsTestCase tmsTestCase,
      List<TmsAttributeRQ> attributes) {
    if (isEmpty(attributes)) {
      return;
    }
    var tmsTestCaseAttributes = tmsTestCaseAttributeMapper.convertToTmsTestCaseAttributes(
        tmsAttributeService.getTmsAttributes(attributes), attributes);
    tmsTestCaseAttributes.forEach(
        tmsTestCaseAttribute -> tmsTestCaseAttribute.setTestCase(tmsTestCase));
    tmsTestCase.getTags().addAll(tmsTestCaseAttributes);
    tmsTestCaseAttributeRepository.saveAll(tmsTestCaseAttributes);
  }

  @Override
  @Transactional
  public void patchTestCaseAttributes(@NotNull @NotEmpty List<TmsTestCase> tmsTestCases,
      List<TmsAttributeRQ> attributes) {
    if (isEmpty(attributes)) {
      return;
    }
    var tmsTestCaseAttributes = tmsTestCaseAttributeMapper.convertToTmsTestCaseAttributes(
        tmsAttributeService.getTmsAttributes(attributes), attributes);
    tmsTestCases.forEach(tmsTestCase -> {
      tmsTestCaseAttributes.forEach(
          tmsTestCaseAttribute -> tmsTestCaseAttribute.setTestCase(tmsTestCase));
      tmsTestCase.getTags().addAll(tmsTestCaseAttributes);
      tmsTestCaseAttributeRepository.saveAll(tmsTestCaseAttributes);
    });
  }

  @Override
  @Transactional
  public void deleteAllByTestCaseId(@NotNull Long testCaseId) {
    tmsTestCaseAttributeRepository.deleteAllByTestCaseId(testCaseId);
  }

  @Override
  @Transactional
  public void deleteAllByTestFolderId(@NotNull Long projectId, @NotNull Long testFolderId) {
    tmsTestCaseAttributeRepository.deleteTestCaseAttributesByTestFolderId(projectId, testFolderId);
  }

  @Override
  @Transactional
  public void deleteAllByTestCaseIds(@NotNull @NotEmpty List<Long> testCaseIds) {
    tmsTestCaseAttributeRepository.deleteAllByTestCaseIds(testCaseIds);
  }

  @Override
  @Transactional
  public void deleteByTestCaseIdAndAttributeIds(Long testCaseId,
      List<Long> attributeIds) {
    tmsTestCaseAttributeRepository.deleteByTestCaseIdAndAttributeIds(testCaseId, attributeIds);
  }

  @Override
  @Transactional
  public void deleteByTestCaseIdsAndAttributeIds(List<Long> testCaseIds,
      List<Long> attributeIds) {
    tmsTestCaseAttributeRepository.deleteByTestCaseIdsAndAttributeIds(testCaseIds, attributeIds);
  }
}
