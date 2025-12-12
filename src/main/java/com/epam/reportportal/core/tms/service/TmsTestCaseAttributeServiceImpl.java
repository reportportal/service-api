package com.epam.reportportal.core.tms.service;

import static org.apache.commons.collections4.CollectionUtils.isEmpty;

import com.epam.reportportal.core.tms.dto.TmsTestCaseAttributeRQ;
import com.epam.reportportal.core.tms.mapper.TmsTestCaseAttributeMapper;
import com.epam.reportportal.infrastructure.persistence.dao.tms.TmsTestCaseAttributeRepository;
import com.epam.reportportal.infrastructure.persistence.entity.tms.TmsTestCase;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
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

  @Override
  @Transactional
  public void createTestCaseAttributes(@NotNull TmsTestCase tmsTestCase,
      @NotEmpty List<TmsTestCaseAttributeRQ> attributes) {
    var tmsTestCaseAttributes = tmsTestCaseAttributeMapper.convertToTmsTestCaseAttributes(
        attributes);
    tmsTestCase.setAttributes(tmsTestCaseAttributes);
    tmsTestCaseAttributes.forEach(
        tmsTestCaseAttribute -> tmsTestCaseAttribute.setTestCase(tmsTestCase));
    tmsTestCaseAttributeRepository.saveAll(tmsTestCaseAttributes);
  }

  @Override
  @Transactional
  public void updateTestCaseAttributes(@NotNull TmsTestCase tmsTestCase,
      List<TmsTestCaseAttributeRQ> attributes) {
    if (CollectionUtils.isNotEmpty(tmsTestCase.getAttributes())) { //TODO refactor to the option with highest performance
      tmsTestCaseAttributeRepository.deleteAll(tmsTestCase.getAttributes());
      tmsTestCase.setAttributes(new HashSet<>());
    }
    if (CollectionUtils.isNotEmpty(attributes)) {
      createTestCaseAttributes(tmsTestCase, attributes);
    }
  }

  @Override
  @Transactional
  public void patchTestCaseAttributes(@NotNull TmsTestCase tmsTestCase,
      List<TmsTestCaseAttributeRQ> attributes) {
    if (isEmpty(attributes)) {
      return;
    }
    var tmsTestCaseAttributes = tmsTestCaseAttributeMapper.convertToTmsTestCaseAttributes(
        attributes);
    tmsTestCaseAttributes.forEach(
        tmsTestCaseAttribute -> tmsTestCaseAttribute.setTestCase(tmsTestCase));
    tmsTestCase.getAttributes().addAll(tmsTestCaseAttributes);
    tmsTestCaseAttributeRepository.saveAll(tmsTestCaseAttributes);
  }

  @Override
  @Transactional
  public void patchTestCaseAttributes(@NotNull @NotEmpty List<TmsTestCase> tmsTestCases,
      List<TmsTestCaseAttributeRQ> attributes) {
    if (isEmpty(attributes)) {
      return;
    }
    var tmsTestCaseAttributes = tmsTestCaseAttributeMapper.convertToTmsTestCaseAttributes(
        attributes);
    tmsTestCases.forEach(tmsTestCase -> {
      tmsTestCaseAttributes.forEach(
          tmsTestCaseAttribute -> tmsTestCaseAttribute.setTestCase(tmsTestCase));
      tmsTestCase.getAttributes().addAll(tmsTestCaseAttributes);
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
      Collection<Long> attributeIds) {
    tmsTestCaseAttributeRepository.deleteByTestCaseIdsAndAttributeIds(testCaseIds, attributeIds);
  }

  @Override
  @Transactional
  public void duplicateTestCaseAttributes(TmsTestCase originalTestCase, TmsTestCase newTestCase) {
    if (CollectionUtils.isNotEmpty(originalTestCase.getAttributes())) {
      var duplicatedAttributes = originalTestCase
          .getAttributes()
          .stream()
          .map(originalAttribute -> tmsTestCaseAttributeMapper.duplicateTestCaseAttribute(
              originalAttribute, newTestCase))
          .collect(Collectors.toSet());

      var savedAttributes = new HashSet<>(
          tmsTestCaseAttributeRepository.saveAll(duplicatedAttributes));

      newTestCase.setAttributes(savedAttributes);
    }
  }

  @Override
  @Transactional
  public void addAttributesToTestCases(@NotNull @NotEmpty List<Long> testCaseIds,
      @NotNull @NotEmpty Collection<Long> attributeIds) {
    var testCaseAttributes = testCaseIds
        .stream()
        .flatMap(testCaseId -> attributeIds
            .stream()
            .map(attributeId -> tmsTestCaseAttributeMapper.createTestCaseAttribute(
                testCaseId, attributeId)
            ))
        .toList();

    tmsTestCaseAttributeRepository.saveAll(testCaseAttributes);
  }
}
