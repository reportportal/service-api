package com.epam.ta.reportportal.core.tms.service;

import static org.apache.commons.collections4.CollectionUtils.isEmpty;

import com.epam.ta.reportportal.core.tms.db.entity.TmsTestPlan;
import com.epam.ta.reportportal.core.tms.db.repository.TmsMilestoneRepository;
import com.epam.ta.reportportal.core.tms.mapper.TmsMilestoneMapper;
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
    public void upsertTestPlanToMilestones(TmsTestPlan tmsTestPlan, List<Long> milestoneIds) {
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
