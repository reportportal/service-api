package com.epam.ta.reportportal.core.tms.service;

import com.epam.ta.reportportal.dao.tms.TmsTestCaseExecutionRepository;
import com.epam.ta.reportportal.entity.tms.TmsTestCaseExecution;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
}
