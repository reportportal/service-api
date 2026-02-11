package com.epam.reportportal.base.core.tms.service;

import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsTestCaseExecutionRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestCaseExecution;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TmsTestCaseExecutionServiceImpl implements TmsTestCaseExecutionService {

  private final TmsTestCaseExecutionRepository tmsTestCaseExecutionRepository;

  @Override
  public Map<Long, TmsTestCaseExecution> getLastTestCasesExecutionsByTestCaseIds(List<Long> testCaseIds) {
    return Optional
        .ofNullable(tmsTestCaseExecutionRepository.findLastExecutionsByTestCaseIds(testCaseIds))
        .orElse(Collections.emptyList())
        .stream()
        .collect(Collectors.toMap(
            TmsTestCaseExecution::getTestCaseId, Function.identity()
        ));
  }

  @Override
  public TmsTestCaseExecution getLastTestCaseExecution(Long testCaseId) {
    return tmsTestCaseExecutionRepository
        .findLastExecutionByTestCaseId(testCaseId)
        .orElse(null);
  }

  @Override
  @Transactional(readOnly = true)
  public Map<Long, TmsTestCaseExecution> findLastExecutionsByTestCaseIdsAndTestPlanId(
      List<Long> testCaseIds, Long testPlanId) {

    if (testCaseIds == null || testCaseIds.isEmpty()) {
      return Map.of();
    }

    return tmsTestCaseExecutionRepository
        .findLastExecutionsByTestCaseIdsAndTestPlanId(testCaseIds, testPlanId)
        .stream()
        .collect(Collectors.toMap(
            TmsTestCaseExecution::getTestCaseId,
            Function.identity(),
            (existing, replacement) -> existing // keep first in case of duplicates
        ));
  }

  @Override
  @Transactional(readOnly = true)
  public List<TmsTestCaseExecution> findByTestCaseIdAndTestPlanId(Long testCaseId,
      Long testPlanId) {

    if (testCaseId == null || testPlanId == null) {
      return List.of();
    }

    return tmsTestCaseExecutionRepository.findByTestCaseIdAndTestPlanId(testCaseId, testPlanId);
  }
}
