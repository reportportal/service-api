package com.epam.reportportal.base.core.tms.service;

import static org.apache.commons.collections4.CollectionUtils.isEmpty;

import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestPlan;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsTestPlanAttributeRepository;
import com.epam.reportportal.base.core.tms.dto.TmsTestPlanAttributeRQ;
import com.epam.reportportal.base.core.tms.mapper.TmsTestPlanAttributeMapper;
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
  private final TmsAttributeService tmsAttributeService;

  @Override
  @Transactional
  public void createTestPlanAttributes(long projectId, TmsTestPlan tmsTestPlan,
      List<TmsTestPlanAttributeRQ> attributes) {
    if (isEmpty(attributes)) {
      return;
    }
    
    var tmsTestPlanAttributes = attributes.stream()
        .map(attributeRQ -> {
          var tmsAttribute = attributeRQ.getId() != null
              ? tmsAttributeService.getEntityById(projectId, attributeRQ.getId())
              : tmsAttributeService.findOrCreateAttribute(projectId, attributeRQ.getKey(), attributeRQ.getValue());
          
          return tmsTestPlanAttributeMapper.createTestPlanAttribute(tmsTestPlan, tmsAttribute);
        })
        .collect(Collectors.toSet());
    
    tmsTestPlan.setAttributes(tmsTestPlanAttributes);
    tmsTestPlanAttributeRepository.saveAll(tmsTestPlanAttributes);
  }

  @Override
  @Transactional
  public void updateTestPlanAttributes(long projectId, TmsTestPlan existingTestPlan,
      List<TmsTestPlanAttributeRQ> attributes) {
    if (CollectionUtils.isNotEmpty(existingTestPlan.getAttributes())) {
      tmsTestPlanAttributeRepository.deleteAll(existingTestPlan.getAttributes());
      existingTestPlan.getAttributes().clear();
    }
    createTestPlanAttributes(projectId, existingTestPlan, attributes);
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