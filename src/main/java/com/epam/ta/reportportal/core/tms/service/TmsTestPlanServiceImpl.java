package com.epam.ta.reportportal.core.tms.service;

import static com.epam.reportportal.rules.exception.ErrorType.NOT_FOUND;

import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.dao.tms.filterable.TmsTestPlanFilterableRepository;
import com.epam.ta.reportportal.entity.tms.TmsTestPlan;
import com.epam.ta.reportportal.dao.tms.TmsTestPlanRepository;
import com.epam.ta.reportportal.dao.tms.TmsTestPlanTestCaseRepository;
import com.epam.ta.reportportal.core.tms.dto.TmsTestPlanRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsTestPlanRS;
import com.epam.ta.reportportal.core.tms.dto.batch.BatchOperationError;
import com.epam.ta.reportportal.core.tms.dto.batch.BatchOperationResultRS;
import com.epam.ta.reportportal.core.tms.mapper.TmsTestPlanMapper;
import com.epam.ta.reportportal.model.Page;
import com.epam.ta.reportportal.ws.converter.PagedResourcesAssembler;
import jakarta.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TmsTestPlanServiceImpl implements TmsTestPlanService {

  private static final String TMS_TEST_PLAN_NOT_FOUND_BY_ID = "TMS Test Plan with id: %d for project: %d";

  private final TmsTestPlanRepository testPlanRepository;
  private final TmsTestPlanFilterableRepository tmsTestPlanFilterableRepository;
  private final TmsTestPlanMapper tmsTestPlanMapper;
  private final TmsTestPlanAttributeService tmsTestPlanAttributeService;
  private final TmsTestPlanTestCaseRepository tmsTestPlanTestCaseRepository;
  private final TmsTestCaseService tmsTestCaseService;

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
        testPlanRQ.getAttributes());

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

          return tmsTestPlanMapper.convertToRS(existingTestPlan);
        }).orElseThrow(() -> new ReportPortalException(
            NOT_FOUND, TMS_TEST_PLAN_NOT_FOUND_BY_ID.formatted(testPlanId, projectId))
        );
  }

  @Override
  @Transactional
  public void delete(long projectId, Long testPlanId) {
    tmsTestPlanAttributeService.deleteAllByTestPlanId(testPlanId);
    testPlanRepository.deleteByIdAndProjectId(testPlanId, projectId);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<TmsTestPlanRS> getByCriteria(Long projectId, Filter filter, Pageable pageable) {
    var testPlanIds = tmsTestPlanFilterableRepository.findIdsByProjectIdAndFilter(
        projectId, filter, pageable
    );

    if (testPlanIds.isEmpty()) {
      return PagedResourcesAssembler
          .<TmsTestPlanRS>pageConverter()
          .apply(new PageImpl<>(Collections.emptyList(), pageable, 0));
    }

    var testPlans = testPlanRepository
        .findByIdsWithAttributes(testPlanIds.getContent()).stream()
        .collect(Collectors.toMap(TmsTestPlan::getId, Function.identity()));

    var orderedTestPlans = testPlanIds
        .getContent()
        .stream()
        .map(testPlans::get)
        .filter(Objects::nonNull)
        .toList();
    return PagedResourcesAssembler
        .<TmsTestPlanRS>pageConverter()
        .apply(tmsTestPlanMapper.convertToRS(
            orderedTestPlans, testPlanIds, pageable, testPlanIds.getTotalElements()
        ));
  }

  @Override
  @Transactional
  public BatchOperationResultRS addTestCasesToPlan(Long projectId, Long testPlanId,
      @NotEmpty List<Long> testCaseIds) {
    if (!testPlanRepository.existsByIdAndProject_Id(testPlanId, projectId)) {
      throw new ReportPortalException(
          NOT_FOUND, TMS_TEST_PLAN_NOT_FOUND_BY_ID.formatted(testPlanId, projectId)
      );
    }

    var errors = new ArrayList<BatchOperationError>();
    var successCount = 0;
    var totalCount = testCaseIds.size();

    // Get existing test case IDs for the project
    var existingTestCaseIds = new HashSet<>(
        tmsTestCaseService.getExistingTestCaseIds(projectId, testCaseIds)
    );

    // Get test case IDs already added to the plan
    var testCaseIdsAreInTestPlan = new HashSet<>(
        tmsTestPlanTestCaseRepository.findTestCaseIdsByTestPlanId(testPlanId)
    );

    for (var testCaseId : testCaseIds) {
      try {
        // Check if a test case exists
        if (!existingTestCaseIds.contains(testCaseId)) {
          errors.add(new BatchOperationError(testCaseId,
              String.format("Test case with id %s not found", testCaseId)));
          continue;
        }

        // Check if already added to the plan
        if (testCaseIdsAreInTestPlan.contains(testCaseId)) {
          errors.add(new BatchOperationError(
              testCaseId,
              String.format("Test case with id %s already exists in test plan", testCaseId)
          ));
          continue;
        }

        // Try to add a test case
        if (addTestCaseToTestPlan(testPlanId, testCaseId)) {
          successCount++;
          testCaseIdsAreInTestPlan.add(testCaseId);
        } else {
          errors.add(new BatchOperationError(testCaseId, "Failed to add test case"));
        }
      } catch (Exception e) {
        errors.add(new BatchOperationError(testCaseId, e.getMessage()));
      }
    }

    return tmsTestPlanMapper.convertToRS(totalCount, successCount, errors);
  }

  @Override
  @Transactional
  public BatchOperationResultRS removeTestCasesFromPlan(Long projectId, Long testPlanId,
      List<Long> testCaseIds) {
    var errors = new ArrayList<BatchOperationError>();
    var successCount = 0;
    var totalCount = testCaseIds.size();

    // Get test case IDs that are actually in the plan
    var testCaseIdsInTestPlan = new HashSet<>(
        tmsTestPlanTestCaseRepository.findTestCaseIdsByTestPlanId(testPlanId)
    );

    for (var testCaseId : testCaseIds) {
      try {
        // Check if a test case is in the plan
        if (!testCaseIdsInTestPlan.contains(testCaseId)) {
          errors.add(new BatchOperationError(testCaseId,
              String.format("Test case with id %s not found in test plan", testCaseId)));
          continue;
        }

        // Try to remove a test case
        if (removeSingleTestCaseFromPlan(testPlanId, testCaseId)) {
          successCount++;
          testCaseIdsInTestPlan.remove(testCaseId);
        } else {
          errors.add(new BatchOperationError(
              testCaseId, "Failed to remove test case")
          );
        }
      } catch (Exception e) {
        errors.add(new BatchOperationError(testCaseId, e.getMessage()));
      }
    }

    return tmsTestPlanMapper.convertToRS(totalCount, successCount, errors);
  }

  @Override
  @Transactional
  public boolean addTestCaseToTestPlan(Long testPlanId, Long testCaseId) {
    try {
      int inserted = tmsTestPlanTestCaseRepository.insertTestPlanTestCaseIgnoreConflict(testPlanId,
          testCaseId);
      return inserted == 1;
    } catch (Exception e) {
      return false;
    }
  }

  @Override
  @Transactional
  public boolean removeSingleTestCaseFromPlan(Long testPlanId, Long testCaseId) {
    try {
      int deleted = tmsTestPlanTestCaseRepository.deleteByTestPlanIdAndTestCaseId(testPlanId,
          testCaseId);
      return deleted == 1;
    } catch (Exception e) {
      return false;
    }
  }
}
