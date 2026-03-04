package com.epam.reportportal.base.core.tms.service;

import static com.epam.reportportal.base.infrastructure.rules.exception.ErrorType.NOT_FOUND;

import com.epam.reportportal.base.core.tms.dto.DuplicateTmsTestPlanRS;
import com.epam.reportportal.base.core.tms.dto.TmsTestCaseInTestPlanRS;
import com.epam.reportportal.base.core.tms.dto.TmsTestFolderRS;
import com.epam.reportportal.base.core.tms.dto.TmsTestPlanRQ;
import com.epam.reportportal.base.core.tms.dto.TmsTestPlanRS;
import com.epam.reportportal.base.core.tms.dto.batch.BatchTestCaseOperationError;
import com.epam.reportportal.base.core.tms.dto.batch.BatchTestCaseOperationResultRS;
import com.epam.reportportal.base.core.tms.mapper.TmsTestPlanMapper;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.Filter;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsTestPlanRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsTestPlanTestCaseRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.filterable.TmsTestPlanFilterableRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestPlan;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestPlanExecutionStatistic;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestPlanWithStatistic;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.base.model.Page;
import com.epam.reportportal.base.ws.converter.PagedResourcesAssembler;
import jakarta.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TmsTestPlanServiceImpl implements TmsTestPlanService {

  private static final String TMS_TEST_PLAN_NOT_FOUND_BY_ID =
      "TMS Test Plan with id: %d for project: %d";

  private final TmsTestPlanRepository testPlanRepository;
  private final TmsTestPlanFilterableRepository tmsTestPlanFilterableRepository;
  private final TmsTestPlanMapper tmsTestPlanMapper;
  private final TmsTestPlanAttributeService tmsTestPlanAttributeService;
  private final TmsTestPlanTestCaseRepository tmsTestPlanTestCaseRepository;
  private final TmsTestCaseService tmsTestCaseService;
  private final TmsTestPlanExecutionService tmsTestPlanExecutionService;
  private final TmsTestFolderService tmsTestFolderService;

  @Override
  @Transactional(readOnly = true)
  public TmsTestPlanRS getById(long projectId, Long testPlanId) {
    return testPlanRepository.findByIdAndProjectId(testPlanId, projectId)
        .map(tmsTestPlanExecutionService::enrichWithStatistics)
        .map(tmsTestPlanMapper::convertTmsTestPlanWithStatisticToRS)
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

    return tmsTestPlanMapper.convertTmsTestPlanWithStatisticToRS(
        TmsTestPlanWithStatistic.of(
            tmsTestPlan,
            new TmsTestPlanExecutionStatistic(0, 0) //TODO fix that
        )
    );
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

          return tmsTestPlanMapper.convertTmsTestPlanWithStatisticToRS(
              tmsTestPlanExecutionService.enrichWithStatistics(existingTestPlan)
          );
        })
        .orElseGet(() -> create(projectId, testPlanRQ));
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

          return tmsTestPlanMapper.convertTmsTestPlanWithStatisticToRS(
              tmsTestPlanExecutionService.enrichWithStatistics(existingTestPlan)
          );
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
        .map(tmsTestPlanExecutionService::enrichWithStatistics)
        .filter(Objects::nonNull)
        .toList();

    return PagedResourcesAssembler
        .<TmsTestPlanRS>pageConverter()
        .apply(tmsTestPlanMapper
            .convertTmsTestPlanWithStatisticToRS(orderedTestPlans, pageable,
                testPlanIds.getTotalElements()));
  }

  @Override
  @Transactional
  public BatchTestCaseOperationResultRS addTestCasesToPlan(Long projectId, Long testPlanId,
      @NotEmpty List<Long> testCaseIds) {
    verifyTestPlanExists(projectId, testPlanId);

    var errors = new ArrayList<BatchTestCaseOperationError>();
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
          errors.add(new BatchTestCaseOperationError(testCaseId,
              String.format("Test case with id %s not found", testCaseId)));
          continue;
        }

        // Check if already added to the plan
        if (testCaseIdsAreInTestPlan.contains(testCaseId)) {
          errors.add(new BatchTestCaseOperationError(
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
          errors.add(new BatchTestCaseOperationError(testCaseId, "Failed to add test case"));
        }
      } catch (Exception e) {
        errors.add(new BatchTestCaseOperationError(testCaseId, e.getMessage()));
      }
    }

    return tmsTestPlanMapper.convertToRS(totalCount, successCount, errors);
  }

  @Override
  @Transactional
  public BatchTestCaseOperationResultRS removeTestCasesFromPlan(Long projectId, Long testPlanId,
      List<Long> testCaseIds) {
    var errors = new ArrayList<BatchTestCaseOperationError>();
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
          errors.add(new BatchTestCaseOperationError(testCaseId,
              String.format("Test case with id %s not found in test plan", testCaseId)));
          continue;
        }

        // Try to remove a test case
        if (removeSingleTestCaseFromPlan(testPlanId, testCaseId)) {
          successCount++;
          testCaseIdsInTestPlan.remove(testCaseId);
        } else {
          errors.add(new BatchTestCaseOperationError(
              testCaseId, "Failed to remove test case")
          );
        }
      } catch (Exception e) {
        errors.add(new BatchTestCaseOperationError(testCaseId, e.getMessage()));
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
  public DuplicateTmsTestPlanRS duplicate(Long projectId, Long testPlanId,
      TmsTestPlanRQ duplicateTestPlanRQ) {
    // Get original test plan
    var originalTestPlan = testPlanRepository
        .findByIdAndProjectId(testPlanId, projectId)
        .orElseThrow(() -> new ReportPortalException(
            NOT_FOUND, TMS_TEST_PLAN_NOT_FOUND_BY_ID.formatted(testPlanId, projectId))
        );

    // Duplicate test plan entity
    var duplicatedTestPlan = tmsTestPlanMapper.duplicateTestPlan(originalTestPlan, duplicateTestPlanRQ);

    duplicatedTestPlan = testPlanRepository.save(duplicatedTestPlan);

    // Duplicate test plan attributes
    tmsTestPlanAttributeService.createTestPlanAttributes(
        duplicatedTestPlan, duplicateTestPlanRQ.getAttributes()
    );

    // Get test case IDs from the original plan
    var originalTestCaseIds = tmsTestPlanTestCaseRepository.findTestCaseIdsByTestPlanId(
        originalTestPlan.getId());

    // Process test case duplication and addition to plan
    var duplicateTestCasesStatistic = processBatchTestCaseDuplication(projectId, duplicatedTestPlan.getId(),
        originalTestCaseIds);

    return tmsTestPlanMapper.buildDuplicateTestPlanResponse(duplicatedTestPlan, duplicateTestCasesStatistic);
  }

  private BatchTestCaseOperationResultRS processBatchTestCaseDuplication(long projectId, Long newTestPlanId,
      List<Long> originalTestCaseIds) {
    if (originalTestCaseIds.isEmpty()) {
      return tmsTestPlanMapper.createFailedBatchResult(Collections.emptyList(),
          "No test cases to duplicate");
    }

    // Step 1: Duplicate test cases in batch
    var duplicationResult = tmsTestCaseService.duplicateTestCases(projectId, originalTestCaseIds);

    // Step 2: Add successfully duplicated test cases to the new plan
    if (!duplicationResult.getSuccessTestCaseIds().isEmpty()) {
      try {
        var addToPlanResult = addTestCasesToPlan(projectId, newTestPlanId,
            duplicationResult.getSuccessTestCaseIds());
        return tmsTestPlanMapper.combineDuplicateTestPlanBatchResults(duplicationResult, addToPlanResult);
      } catch (Exception e) {
        // If adding to plan fails completely, mark all duplicated test cases as failed
        var failedAddResult = tmsTestPlanMapper.createFailedBatchResult(
            duplicationResult.getSuccessTestCaseIds(),
            "Failed to add duplicated test case to plan: " + e.getMessage()
        );
        return tmsTestPlanMapper.combineDuplicateTestPlanBatchResults(duplicationResult, failedAddResult);
      }
    }

    return duplicationResult;
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

  @Override
  @Transactional(readOnly = true)
  public Page<TmsTestCaseInTestPlanRS> getTestCasesAddedToPlan(Long projectId, Long testPlanId,
      Pageable pageable) {
    verifyTestPlanExists(projectId, testPlanId);
    return tmsTestCaseService.getTestCasesInTestPlan(projectId, testPlanId, pageable);
  }

  @Override
  @Transactional(readOnly = true)
  public TmsTestCaseInTestPlanRS getTestCaseInTestPlan(Long projectId, Long testPlanId,
      Long testCaseId) {
    verifyTestPlanExists(projectId, testPlanId);
    return tmsTestCaseService.getTestCaseInTestPlan(projectId, testPlanId, testCaseId);
  }

  @Override
  public void verifyTestPlanExists(Long projectId, Long testPlanId) {
    if (!testPlanRepository.existsByIdAndProject_Id(testPlanId, projectId)) {
      throw new ReportPortalException(
          NOT_FOUND, TMS_TEST_PLAN_NOT_FOUND_BY_ID.formatted(testPlanId, projectId)
      );
    }
  }

  @Override
  @Transactional(readOnly = true)
  public Page<TmsTestFolderRS> getTestFoldersFromPlan(Long projectId, Long testPlanId,
      Pageable pageable) {
    verifyTestPlanExists(projectId, testPlanId);

    return tmsTestFolderService.getFoldersByTestPlanId(projectId, testPlanId, pageable);
  }
}
