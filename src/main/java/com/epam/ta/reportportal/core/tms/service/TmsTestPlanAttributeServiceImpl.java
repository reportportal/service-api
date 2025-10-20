package com.epam.ta.reportportal.core.tms.service;

import static org.apache.commons.collections4.CollectionUtils.isEmpty;

import com.epam.ta.reportportal.entity.tms.TmsTestPlan;
import com.epam.ta.reportportal.dao.tms.TmsTestPlanAttributeRepository;
import com.epam.ta.reportportal.core.tms.dto.TmsTestPlanAttributeRQ;
import com.epam.ta.reportportal.core.tms.mapper.TmsTestPlanAttributeMapper;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TmsTestPlanAttributeServiceImpl implements TmsTestPlanAttributeService {

  private final TmsTestPlanAttributeMapper tmsTestPlanAttributeMapper;
  private final TmsTestPlanAttributeRepository tmsTestPlanAttributeRepository;

  @Override
  @Transactional
  public void createTestPlanAttributes(TmsTestPlan tmsTestPlan,
      List<TmsTestPlanAttributeRQ> attributes) {
    if (isEmpty(attributes)) {
      return;
    }
    var tmsTestPlanAttributes = tmsTestPlanAttributeMapper.convertToTmsTestPlanAttributes(
        attributes);
    tmsTestPlan.setAttributes(tmsTestPlanAttributes);
    tmsTestPlanAttributes.forEach(
        tmsTestPlanAttribute -> tmsTestPlanAttribute.setTestPlan(tmsTestPlan));
    tmsTestPlanAttributeRepository.saveAll(tmsTestPlanAttributes);
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
    var tmsTestPlanAttributes = tmsTestPlanAttributeMapper.convertToTmsTestPlanAttributes(
        attributes);
    existingTestPlan.getAttributes().addAll(tmsTestPlanAttributes);
    tmsTestPlanAttributes.forEach(
        tmsTestPlanAttribute -> tmsTestPlanAttribute.setTestPlan(existingTestPlan));
    tmsTestPlanAttributeRepository.saveAll(tmsTestPlanAttributes);
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
    
    var newTestPlanAttributes = originalTestPlan
        .getAttributes()
        .stream()
        .map(originalAttr -> tmsTestPlanAttributeMapper.duplicateTestPlanAttribute(
            originalAttr, newTestPlan))
        .collect(Collectors.toSet());
        
    newTestPlan.setAttributes(newTestPlanAttributes);
    newTestPlanAttributes.forEach(attr -> attr.setTestPlan(newTestPlan));
    tmsTestPlanAttributeRepository.saveAll(newTestPlanAttributes);
  }
}
