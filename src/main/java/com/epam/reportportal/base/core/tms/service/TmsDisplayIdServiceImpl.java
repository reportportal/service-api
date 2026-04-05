package com.epam.reportportal.base.core.tms.service;

import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsSequenceGeneratorRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.enums.TmsEntityDisplayIdType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TmsDisplayIdServiceImpl implements TmsDisplayIdService {

  private final TmsSequenceGeneratorRepository tmsSequenceGeneratorRepository;

  @Override
  @Transactional
  public String generateTestCaseDisplayId(Long projectId) {
    return tmsSequenceGeneratorRepository.generateDisplayId(
        projectId,
        TmsEntityDisplayIdType.TEST_CASE.getEntityType(),
        TmsEntityDisplayIdType.TEST_CASE.getPrefix()
    );
  }

  @Override
  @Transactional
  public String generateTestPlanDisplayId(Long projectId) {
    return tmsSequenceGeneratorRepository.generateDisplayId(
        projectId,
        TmsEntityDisplayIdType.TEST_PLAN.getEntityType(),
        TmsEntityDisplayIdType.TEST_PLAN.getPrefix()
    );
  }

  @Override
  @Transactional
  public String generateMilestoneDisplayId(Long projectId) {
    return tmsSequenceGeneratorRepository.generateDisplayId(
        projectId,
        TmsEntityDisplayIdType.MILESTONE.getEntityType(),
        TmsEntityDisplayIdType.MILESTONE.getPrefix()
    );
  }

  @Override
  @Transactional
  public String generateManualLaunchDisplayId(Long projectId) {
    return tmsSequenceGeneratorRepository.generateDisplayId(
        projectId,
        TmsEntityDisplayIdType.MANUAL_LAUNCH.getEntityType(),
        TmsEntityDisplayIdType.MANUAL_LAUNCH.getPrefix()
    );
  }
}
