package com.epam.ta.reportportal.core.tms.service;

import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

import com.epam.ta.reportportal.core.tms.db.entity.TmsManualScenario;
import com.epam.ta.reportportal.core.tms.db.repository.TmsManualScenarioAttributeRepository;
import com.epam.ta.reportportal.core.tms.dto.TmsManualScenarioAttributeRQ;
import com.epam.ta.reportportal.core.tms.mapper.TmsManualScenarioAttributeMapper;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TmsManualScenarioAttributeServiceImpl implements TmsManualScenarioAttributeService {

  private final TmsManualScenarioAttributeMapper tmsManualScenarioAttributeMapper;
  private final TmsManualScenarioAttributeRepository tmsManualScenarioAttributeRepository;

  @Override
  @Transactional
  public void createAttributes(TmsManualScenario tmsManualScenario,
      List<TmsManualScenarioAttributeRQ> attributes) {
    if (isEmpty(attributes)) {
      return;
    }
    var tmsManualScenarioAttributes = tmsManualScenarioAttributeMapper.convertToTmsManualScenarioAttributes(
        attributes);
    tmsManualScenario.setAttributes(tmsManualScenarioAttributes);
    tmsManualScenarioAttributes.forEach(
        tmsManualScenarioAttribute -> tmsManualScenarioAttribute.setManualScenario(tmsManualScenario));
    tmsManualScenarioAttributeRepository.saveAll(tmsManualScenarioAttributes);
  }

  @Override
  @Transactional
  public void updateAttributes(TmsManualScenario tmsManualScenario,
      List<TmsManualScenarioAttributeRQ> attributes) {
    if (isNotEmpty(tmsManualScenario.getAttributes())) {
      tmsManualScenarioAttributeRepository.deleteAll(tmsManualScenario.getAttributes());
      tmsManualScenario.setAttributes(new HashSet<>());
    }
    createAttributes(tmsManualScenario, attributes);
  }

  @Override
  @Transactional
  public void patchAttributes(TmsManualScenario tmsManualScenario,
      List<TmsManualScenarioAttributeRQ> attributes) {
    if (isEmpty(attributes)) {
      return;
    }
    var tmsManualScenarioAttributes = tmsManualScenarioAttributeMapper.convertToTmsManualScenarioAttributes(
        attributes);
    tmsManualScenario.getAttributes().addAll(tmsManualScenarioAttributes);
    tmsManualScenarioAttributes.forEach(
        tmsManualScenarioAttribute -> tmsManualScenarioAttribute.setManualScenario(tmsManualScenario));
    tmsManualScenarioAttributeRepository.saveAll(tmsManualScenarioAttributes);
  }

  @Override
  @Transactional
  public void deleteAllByTestCaseId(Long testCaseId) {
    tmsManualScenarioAttributeRepository.deleteAllByTestCaseId(testCaseId);
  }

  @Override
  @Transactional
  public void deleteAllByTestCaseIds(List<Long> testCaseIds) {
    if (testCaseIds != null && !testCaseIds.isEmpty()) {
      tmsManualScenarioAttributeRepository.deleteAllByTestCaseIds(testCaseIds);
    }
  }

  @Override
  @Transactional
  public void deleteAllByTestFolderId(Long projectId, Long folderId) {
    tmsManualScenarioAttributeRepository.deleteManualScenarioAttributesByTestFolderId(projectId, folderId);
  }
}
