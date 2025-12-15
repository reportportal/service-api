package com.epam.reportportal.core.tms.service;

import static org.apache.commons.collections4.CollectionUtils.isEmpty;

import com.epam.reportportal.infrastructure.persistence.entity.tms.TmsTestPlan;
import com.epam.reportportal.infrastructure.persistence.dao.tms.TmsMilestoneRepository;
import com.epam.reportportal.core.tms.mapper.TmsMilestoneMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TmsMilestoneServiceImpl implements TmsMilestoneService {

  private final TmsMilestoneMapper tmsMilestoneMapper;
  private final TmsMilestoneRepository tmsMilestoneRepository;

  @Override
  @Transactional
  public void createTestPlanMilestones(TmsTestPlan tmsTestPlan, List<Long> milestoneIds) {
    if (isEmpty(milestoneIds)) {
      return;
    }
    var milestones = tmsMilestoneMapper.convertToTmsMilestones(milestoneIds);
    milestones.forEach(milestone -> {
      milestone.setTestPlan(tmsTestPlan);
      tmsMilestoneRepository.attachTestPlanToMilestone(tmsTestPlan, milestone.getId());
    });
    tmsTestPlan.setMilestones(milestones);
  }

  @Override
  @Transactional
  public void patchTestPlanMilestones(TmsTestPlan tmsTestPlan, List<Long> milestoneIds) {
    if (isEmpty(milestoneIds)) {
      return;
    }
    var milestones = tmsMilestoneMapper.convertToTmsMilestones(milestoneIds);
    milestones.forEach(milestone -> {
      milestone.setTestPlan(tmsTestPlan);
      tmsMilestoneRepository.attachTestPlanToMilestone(tmsTestPlan, milestone.getId());
    });
    tmsTestPlan.getMilestones().addAll(milestones);
  }

  @Override
  @Transactional
  public void updateTestPlanMilestones(TmsTestPlan tmsTestPlan, List<Long> milestoneIds) {
    if (isEmpty(milestoneIds)) {
      return;
    }
    tmsMilestoneRepository.detachTestPlanFromMilestones(tmsTestPlan.getId());
    var milestones = tmsMilestoneMapper.convertToTmsMilestones(milestoneIds);
    milestones.forEach(milestone -> {
      milestone.setTestPlan(tmsTestPlan);
      tmsMilestoneRepository.attachTestPlanToMilestone(tmsTestPlan, milestone.getId());
    });
    tmsTestPlan.setMilestones(milestones);
  }

  @Override
  @Transactional
  public void detachTestPlanFromMilestones(Long testPlanId) {
    tmsMilestoneRepository.detachTestPlanFromMilestones(testPlanId);
  }

}
