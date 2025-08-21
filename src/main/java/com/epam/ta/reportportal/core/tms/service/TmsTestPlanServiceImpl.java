package com.epam.ta.reportportal.core.tms.service;

import static com.epam.reportportal.rules.exception.ErrorType.NOT_FOUND;

import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.core.tms.db.repository.TmsTestPlanRepository;
import com.epam.ta.reportportal.core.tms.dto.TmsTestPlanRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsTestPlanRS;
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

  private static final String TMS_TEST_PLAN_NOT_FOUND_BY_ID = "TMS Test Plan with id: %d for project: %d";

  private final TmsTestPlanRepository testPlanRepository;
  private final TmsTestPlanMapper tmsTestPlanMapper;
  private final TmsTestPlanAttributeService tmsTestPlanAttributeService;

  @Override
  @Transactional(readOnly = true)
  public TmsTestPlanRS getById(long projectId, Long testPlanId) {
    return testPlanRepository.findByIdAndProjectId(testPlanId, projectId)
        .map(tmsTestPlanMapper::convertToRS)
        .orElseThrow(() -> new ReportPortalException(
            NOT_FOUND, TMS_TEST_PLAN_NOT_FOUND_BY_ID.formatted(testPlanId, projectId))
        );
  }

  @Override
  @Transactional
  public TmsTestPlanRS create(long projectId, TmsTestPlanRQ testPlanRQ) {
    var tmsTestPlan = tmsTestPlanMapper.convertFromRQ(projectId, testPlanRQ);

    testPlanRepository.save(tmsTestPlan);

    tmsTestPlanAttributeService.createTestPlanAttributes(tmsTestPlan,
        testPlanRQ.getTags());

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
              testPlanRQ.getTags());

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
              testPlanRQ.getTags());

          return tmsTestPlanMapper.convertToRS(existingTestPlan);
        }).orElseThrow(() -> new ReportPortalException(
            NOT_FOUND, TMS_TEST_PLAN_NOT_FOUND_BY_ID.formatted(testPlanId, projectId))
        );
  }

  @Override
  @Transactional
  public void delete(long projectId, Long testPlanId) {
    tmsTestPlanAttributeService.deleteAllByTestPlanId(testPlanId);
    testPlanRepository.deleteByIdAndProject_Id(testPlanId, projectId);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<TmsTestPlanRS> getByCriteria(Long projectId,
      Pageable pageable) {
    return tmsTestPlanMapper.convertToRS(
        testPlanRepository.findByCriteria(projectId,
        pageable
    ));
  }
}
