package com.epam.reportportal.base.core.tms.service;

import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

import com.epam.reportportal.base.core.tms.dto.TmsManualScenarioAttributeRQ;
import com.epam.reportportal.base.core.tms.mapper.TmsManualScenarioAttributeMapper;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsManualScenarioAttributeRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsManualScenario;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsManualScenarioAttribute;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TmsManualScenarioAttributeServiceImpl implements TmsManualScenarioAttributeService {

  private final TmsManualScenarioAttributeMapper tmsManualScenarioAttributeMapper;
  private final TmsManualScenarioAttributeRepository tmsManualScenarioAttributeRepository;
  private final TmsAttributeService tmsAttributeService;

  @Override
  @Transactional
  public void createAttributes(long projectId, TmsManualScenario tmsManualScenario,
      List<TmsManualScenarioAttributeRQ> attributes) {
    if (isEmpty(attributes)) {
      return;
    }

    var tmsManualScenarioAttributes = attributes.stream()
        .map(attributeRQ -> {
          var attribute = attributeRQ.getId() != null
              ? tmsAttributeService.getEntityById(projectId, attributeRQ.getId())
              : tmsAttributeService.findOrCreateAttribute(projectId, attributeRQ.getKey(),
                  attributeRQ.getValue());
          var manualScenarioAttribute = new TmsManualScenarioAttribute();
          manualScenarioAttribute.setAttribute(attribute);
          manualScenarioAttribute.setManualScenario(tmsManualScenario);
          return manualScenarioAttribute;
        })
        .collect(Collectors.toSet());
    tmsManualScenario.setAttributes(tmsManualScenarioAttributes);
    tmsManualScenarioAttributeRepository.saveAll(tmsManualScenarioAttributes);
  }

  @Override
  @Transactional
  public void updateAttributes(long projectId, TmsManualScenario tmsManualScenario,
      List<TmsManualScenarioAttributeRQ> attributes) {
    if (isNotEmpty(tmsManualScenario.getAttributes())) {
      tmsManualScenarioAttributeRepository.deleteAll(tmsManualScenario.getAttributes());
      tmsManualScenario.setAttributes(new HashSet<>());
    }
    createAttributes(projectId, tmsManualScenario, attributes);
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
    tmsManualScenarioAttributeRepository.deleteManualScenarioAttributesByTestFolderId(projectId,
        folderId);
  }

  @Override
  @Transactional
  public void duplicateAttributes(TmsManualScenario originalScenario,
      TmsManualScenario newScenario) {
    if (CollectionUtils.isEmpty(originalScenario.getAttributes())) {
      return;
    }

    var duplicatedAttributes = originalScenario.getAttributes().stream()
        .map(originalAttribute -> tmsManualScenarioAttributeMapper.duplicateAttribute(
            originalAttribute, newScenario))
        .collect(Collectors.toSet());

    newScenario.setAttributes(duplicatedAttributes);
    tmsManualScenarioAttributeRepository.saveAll(duplicatedAttributes);
  }
}