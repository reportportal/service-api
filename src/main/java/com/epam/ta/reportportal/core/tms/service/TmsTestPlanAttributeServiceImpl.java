package com.epam.ta.reportportal.core.tms.service;

import static org.apache.commons.collections4.CollectionUtils.isEmpty;

import com.epam.ta.reportportal.core.tms.db.entity.TmsTestPlan;
import com.epam.ta.reportportal.core.tms.db.repository.TmsTestPlanAttributeRepository;
import com.epam.ta.reportportal.core.tms.dto.TmsTestPlanAttributeRQ;
import com.epam.ta.reportportal.core.tms.mapper.TmsTestPlanAttributeMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
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
    tmsTestPlanAttributeRepository.deleteAllById_TestPlanId(existingTestPlan.getId());
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
    tmsTestPlanAttributeRepository.deleteAllById_TestPlanId(testPlanId);
  }
}
