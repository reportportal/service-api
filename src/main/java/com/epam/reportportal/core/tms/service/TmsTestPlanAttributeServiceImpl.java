package com.epam.reportportal.core.tms.service;

import static org.apache.commons.collections4.CollectionUtils.isEmpty;

import com.epam.reportportal.core.tms.dto.TmsTestPlanAttributeRQ;
import com.epam.reportportal.core.tms.mapper.TmsTestPlanAttributeMapper;
import com.epam.reportportal.infrastructure.persistence.dao.ItemAttributeRepository;
import com.epam.reportportal.infrastructure.persistence.dao.tms.TmsTestPlanAttributeRepository;
import com.epam.reportportal.infrastructure.persistence.entity.ItemAttribute;
import com.epam.reportportal.infrastructure.persistence.entity.tms.TmsTestPlan;
import com.epam.reportportal.infrastructure.persistence.entity.tms.TmsTestPlanAttribute;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service implementation for managing TMS Test Plan attributes.
 * Uses ItemAttribute entities with reuse optimization for same key+value pairs.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TmsTestPlanAttributeServiceImpl implements TmsTestPlanAttributeService {

  private final TmsTestPlanAttributeMapper tmsTestPlanAttributeMapper;
  private final TmsTestPlanAttributeRepository tmsTestPlanAttributeRepository;
  private final ItemAttributeRepository itemAttributeRepository;

  @Override
  @Transactional
  public void createTestPlanAttributes(TmsTestPlan tmsTestPlan,
      List<TmsTestPlanAttributeRQ> attributes) {
    if (isEmpty(attributes)) {
      return;
    }
    var itemAttributes = new ArrayList<ItemAttribute>();
    var tmsTestPlanAttributes = new HashSet<TmsTestPlanAttribute>();
    for  (var attribute : attributes) {
      var tmsTestPlanAttribute = tmsTestPlanAttributeMapper.convertToTmsTestPlanAttribute(
          tmsTestPlan, attribute
      );
      if (tmsTestPlanAttribute == null) {
        continue;
      }
      tmsTestPlanAttributes.add(tmsTestPlanAttribute);
      itemAttributes.add(tmsTestPlanAttribute.getItemAttribute());
    }
    if (CollectionUtils.isNotEmpty(itemAttributes)
        && CollectionUtils.isNotEmpty(tmsTestPlanAttributes)) {
      tmsTestPlan.setAttributes(tmsTestPlanAttributes);
      itemAttributeRepository.saveAll(itemAttributes);
      tmsTestPlanAttributeRepository.saveAll(tmsTestPlanAttributes);
    }
  }

  @Override
  @Transactional
  public void updateTestPlanAttributes(TmsTestPlan existingTestPlan,
      List<TmsTestPlanAttributeRQ> attributes) {
    if (CollectionUtils.isNotEmpty(existingTestPlan.getAttributes())) {
      tmsTestPlanAttributeRepository.deleteAll(existingTestPlan.getAttributes());
      existingTestPlan.getAttributes().clear();
    }
    createTestPlanAttributes(existingTestPlan, attributes);
  }

  @Override
  @Transactional
  public void patchTestPlanAttributes(TmsTestPlan existingTestPlan,
      List<TmsTestPlanAttributeRQ> attributes) {
    if (isEmpty(attributes)) {
      return;
    }
    var itemAttributes = new ArrayList<ItemAttribute>();
    var tmsTestPlanAttributes = new HashSet<TmsTestPlanAttribute>();
    for  (var attribute : attributes) {
      var tmsTestPlanAttribute = tmsTestPlanAttributeMapper.convertToTmsTestPlanAttribute(
          existingTestPlan, attribute
      );
      if (tmsTestPlanAttribute == null) {
        continue;
      }
      tmsTestPlanAttributes.add(tmsTestPlanAttribute);
      itemAttributes.add(tmsTestPlanAttribute.getItemAttribute());
    }
    if (CollectionUtils.isNotEmpty(itemAttributes)
        && CollectionUtils.isNotEmpty(tmsTestPlanAttributes)) {
      existingTestPlan.getAttributes().addAll(tmsTestPlanAttributes);
      itemAttributeRepository.saveAll(itemAttributes);
      tmsTestPlanAttributeRepository.saveAll(tmsTestPlanAttributes);
    }
  }

  @Override
  @Transactional
  public void deleteAllByTestPlanId(Long testPlanId) {
    tmsTestPlanAttributeRepository.deleteAllByTestPlanId(testPlanId);
  }

  @Override
  @Transactional
  public void duplicateTestPlanAttributes(TmsTestPlan originalTestPlan, TmsTestPlan newTestPlan) {
    if (isEmpty(originalTestPlan.getAttributes())) {
      return;
    }

    var duplicatedItemAttributes = new ArrayList<ItemAttribute>();
    var duplicatedTestPlanAttributes = new HashSet<TmsTestPlanAttribute>();
    for  (var originalAttr : originalTestPlan.getAttributes()) {
      var duplicateTestPlanAttribute = tmsTestPlanAttributeMapper.duplicateTestPlanAttribute(
          originalAttr, newTestPlan
      );
      if (duplicateTestPlanAttribute == null) {
        continue;
      }
      duplicatedTestPlanAttributes.add(duplicateTestPlanAttribute);
      duplicatedItemAttributes.add(duplicateTestPlanAttribute.getItemAttribute());
    }
    if (CollectionUtils.isNotEmpty(duplicatedItemAttributes)
        && CollectionUtils.isNotEmpty(duplicatedTestPlanAttributes)) {
      newTestPlan.setAttributes(duplicatedTestPlanAttributes);
      itemAttributeRepository.saveAll(duplicatedItemAttributes);
      tmsTestPlanAttributeRepository.saveAll(duplicatedTestPlanAttributes);
    }
  }
}
