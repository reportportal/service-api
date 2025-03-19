package com.epam.ta.reportportal.core.tms.service;

import com.epam.ta.reportportal.core.tms.db.repository.TmsTestPlanAttributeRepository;
import com.epam.ta.reportportal.core.tms.db.repository.TmsTestPlanRepository;
import com.epam.ta.reportportal.core.tms.dto.TmsTestPlanRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsTestPlanRS;
import com.epam.ta.reportportal.core.tms.exception.NotFoundException;
import com.epam.ta.reportportal.core.tms.mapper.TmsTestPlanMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TmsTestPlanServiceImpl implements TmsTestPlanService {

    private static final String TMS_TEST_PLAN_NOT_FOUND_BY_ID = "TMS test pan cannot be found by id: {0} for project: {1}";

    private final TmsTestPlanRepository testPlanRepository;
    private final TmsTestPlanMapper tmsTestPlanMapper;
    private final TmsTestPlanAttributeRepository tmsTestPlanAttributeRepository;
    private final TmsMilestoneService tmsMilestoneService;
    private final TmsTestPlanAttributeService tmsTestPlanAttributeService;

    @Override
    @Transactional(readOnly = true)
    public TmsTestPlanRS getById(long projectId, Long testPlanId) {
        return testPlanRepository.findByIdAndProjectId(testPlanId, projectId)
            .map(tmsTestPlanMapper::convertToRS)
            .orElseThrow(
                NotFoundException.supplier(TMS_TEST_PLAN_NOT_FOUND_BY_ID, testPlanId, projectId));
    }

    @Override
    @Transactional
    public TmsTestPlanRS create(long projectId, TmsTestPlanRQ testPlanRQ) {
        var tmsTestPlan = tmsTestPlanMapper.convertFromRQ(projectId, testPlanRQ);

        testPlanRepository.save(tmsTestPlan);

        tmsTestPlanAttributeService.createTestPlanAttributes(tmsTestPlan,
            testPlanRQ.getAttributes());
        tmsMilestoneService.upsertTestPlanToMilestones(tmsTestPlan, testPlanRQ.getMilestoneIds());

        return tmsTestPlanMapper.convertToRS(tmsTestPlan);
    }

    @Override
    @Transactional
    public TmsTestPlanRS update(long projectId, Long testPlanId, TmsTestPlanRQ testPlanRQ) {
        return testPlanRepository.findByIdAndProjectId(testPlanId, projectId)
            .map((var existingTestPlan) -> {
                tmsTestPlanMapper.update(existingTestPlan,
                    tmsTestPlanMapper.convertFromRQ(projectId, testPlanRQ));

                tmsTestPlanAttributeService.updateTestPlanAttributes(existingTestPlan,
                    testPlanRQ.getAttributes());
                tmsMilestoneService.upsertTestPlanToMilestones(existingTestPlan,
                    testPlanRQ.getMilestoneIds());

                return tmsTestPlanMapper.convertToRS(existingTestPlan);
            }).orElseGet(() -> create(projectId, testPlanRQ));
    }

    @Override
    @Transactional
    public TmsTestPlanRS patch(long projectId, Long testPlanId, TmsTestPlanRQ testPlanRQ) {
        return testPlanRepository.findByIdAndProjectId(testPlanId, projectId)
            .map((var existingTestPlan) -> {
                tmsTestPlanMapper.patch(existingTestPlan,
                    tmsTestPlanMapper.convertFromRQ(projectId, testPlanRQ));

                tmsTestPlanAttributeService.patchTestPlanAttributes(existingTestPlan,
                    testPlanRQ.getAttributes());
                tmsMilestoneService.upsertTestPlanToMilestones(existingTestPlan,
                    testPlanRQ.getMilestoneIds());

                return tmsTestPlanMapper.convertToRS(existingTestPlan);
            }).orElseThrow(
                NotFoundException.supplier(TMS_TEST_PLAN_NOT_FOUND_BY_ID, testPlanId, projectId));
    }

    @Override
    @Transactional
    public void delete(long projectId, Long testPlanId) {
        tmsTestPlanAttributeRepository.deleteAllById_TestPlanId(testPlanId);
        tmsMilestoneService.detachTestPlanFromMilestones(testPlanId);
        testPlanRepository.deleteByIdAndProject_Id(testPlanId, projectId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TmsTestPlanRS> getByCriteria(Long projectId, List<Long> environmentIds,
        List<Long> productVersionIds,
        Pageable pageable) {
        return tmsTestPlanMapper.convertToRS(testPlanRepository.findByCriteria(projectId,
            environmentIds,
            productVersionIds,
            pageable
        ));
    }
}
