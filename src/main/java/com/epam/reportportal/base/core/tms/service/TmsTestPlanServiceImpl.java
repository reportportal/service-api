package com.epam.reportportal.base.core.tms.service;

import static com.epam.reportportal.base.infrastructure.rules.exception.ErrorType.NOT_FOUND;

import com.epam.reportportal.base.core.tms.dto.DuplicateTmsTestPlanRS;
import com.epam.reportportal.base.core.tms.dto.TmsManualLaunchTestPlanRS;
import com.epam.reportportal.base.core.tms.dto.TmsTestCaseInTestPlanRS;
import com.epam.reportportal.base.core.tms.dto.TmsTestFolderRS;
import com.epam.reportportal.base.core.tms.dto.TmsTestPlanRQ;
import com.epam.reportportal.base.core.tms.dto.TmsTestPlanRS;
import com.epam.reportportal.base.core.tms.dto.batch.BatchAddTestCasesToPlanRQ;
import com.epam.reportportal.base.core.tms.dto.batch.BatchTestCaseOperationError;
import com.epam.reportportal.base.core.tms.dto.batch.BatchTestCaseOperationResultRS;
import com.epam.reportportal.base.core.tms.mapper.TmsTestPlanMapper;
import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.Filter;
import com.epam.reportportal.base.infrastructure.persistence.dao.LaunchRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsMilestoneRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsTestPlanRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsTestPlanTestCaseRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.filterable.TmsTestPlanFilterableRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.MembershipDetails;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestPlan;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.base.model.Page;
import com.epam.reportportal.base.ws.converter.PagedResourcesAssembler;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class TmsTestPlanServiceImpl implements TmsTestPlanService {

  private static final String TMS_TEST_PLAN_NOT_FOUND_BY_ID =
      "TMS Test Plan with id: %d for project: %d";
  private static final String TMS_TEST_PLAN_NOT_IN_PROJECT =
      "Test plan '%s' is not under the specified project.";
  private static final String TMS_MILESTONE_NOT_FOUND_BY_ID =
      "TMS Milestone with id: %d for project: %d";
  private static final int INSERT_CHUNK_SIZE = 1000;

  private final LaunchRepository launchRepository;
  private final TmsTestPlanRepository testPlanRepository;
  private final TmsMilestoneRepository tmsMilestoneRepository;
  private final TmsTestPlanFilterableRepository tmsTestPlanFilterableRepository;
  private final TmsTestPlanMapper tmsTestPlanMapper;
  private final TmsTestPlanAttributeService tmsTestPlanAttributeService;
  private final TmsTestPlanTestCaseRepository tmsTestPlanTestCaseRepository;
  private final TmsTestCaseService tmsTestCaseService;
  private final TmsTestPlanExecutionService tmsTestPlanExecutionService;
  private final TmsTestFolderService tmsTestFolderService;
  private final TmsFolderTestCaseResolver tmsFolderTestCaseResolver;

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
    verifyMilestoneExistsIfPresent(projectId, testPlanRQ);
    var tmsTestPlan = tmsTestPlanMapper.convertFromRQ(projectId, testPlanRQ);

    testPlanRepository.save(tmsTestPlan);

    tmsTestPlanAttributeService.createTestPlanAttributes(projectId, tmsTestPlan,
        testPlanRQ.getAttributes());

    return tmsTestPlanMapper.convertTmsTestPlanWithStatisticToRS(
        tmsTestPlanExecutionService.enrichWithStatistics(tmsTestPlan)
    );
  }

  @Override
  @Transactional
  public TmsTestPlanRS update(long projectId, Long testPlanId, TmsTestPlanRQ testPlanRQ) {
    verifyMilestoneExistsIfPresent(projectId, testPlanRQ);
    return testPlanRepository.findByIdAndProjectId(testPlanId, projectId)
        .map((var existingTestPlan) -> {
          tmsTestPlanMapper.update(existingTestPlan,
              tmsTestPlanMapper.convertFromRQ(projectId, testPlanRQ));

          tmsTestPlanAttributeService.updateTestPlanAttributes(projectId, existingTestPlan,
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
    verifyMilestoneExistsIfPresent(projectId, testPlanRQ);
    return testPlanRepository.findByIdAndProjectId(testPlanId, projectId)
        .map((var existingTestPlan) -> {
          tmsTestPlanMapper.patch(existingTestPlan,
              tmsTestPlanMapper.convertFromRQ(projectId, testPlanRQ));

          tmsTestPlanAttributeService.updateTestPlanAttributes(projectId, existingTestPlan,
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
    var tmsTestPlan = testPlanRepository.findByIdAndProjectId(testPlanId,
        projectId).orElseThrow(() -> new ReportPortalException(
        NOT_FOUND, TMS_TEST_PLAN_NOT_FOUND_BY_ID.formatted(testPlanId, projectId))
    );
    launchRepository.clearTestPlanIdsByProjectIdAndTestPlanIds(projectId, List.of(testPlanId));
    tmsTestPlanAttributeService.deleteAllByTestPlanId(testPlanId);
    testPlanRepository.delete(tmsTestPlan);
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

    var orderedTestPlans = tmsTestPlanExecutionService.enrichWithStatistics(
        testPlanIds
            .getContent()
            .stream()
            .map(testPlans::get)
            .toList()
    );

    return PagedResourcesAssembler
        .<TmsTestPlanRS>pageConverter()
        .apply(tmsTestPlanMapper
            .convertTmsTestPlanWithStatisticToRS(orderedTestPlans, pageable,
                testPlanIds.getTotalElements()));
  }

  private BatchTestCaseOperationResultRS addTestCasesToPlan(Long projectId, Long testPlanId,
      List<Long> testCaseIds) {
    verifyTestPlanExists(projectId, testPlanId);
    return addTestCasesToPlan(projectId, testPlanId, testCaseIds, List.of());
  }

  @Override
  @Transactional
  public BatchTestCaseOperationResultRS addTestCasesToPlan(Long projectId, Long testPlanId,
      BatchAddTestCasesToPlanRQ request) {
    verifyTestPlanExists(projectId, testPlanId);
    var folderTestCaseIds = tmsFolderTestCaseResolver.resolve(projectId, request.getFolderIds());
    return addTestCasesToPlan(projectId, testPlanId,
        request.getTestCaseIds() != null ? request.getTestCaseIds() : List.of(), folderTestCaseIds);
  }

  private BatchTestCaseOperationResultRS addTestCasesToPlan(Long projectId, Long testPlanId,
      List<Long> testCaseIds, List<Long> folderTestCaseIds) {
    var errors = new ArrayList<BatchTestCaseOperationError>();
    var totalCount = testCaseIds.size();

    // Get existing test case IDs for the project
    var existingTestCaseIds = new HashSet<>(
        tmsTestCaseService.getExistingTestCaseIds(projectId, testCaseIds)
    );

    // Get test case IDs already added to the plan
    var testCaseIdsAreInTestPlan = new HashSet<>(
        tmsTestPlanTestCaseRepository.findTestCaseIdsByTestPlanId(testPlanId)
    );

    var testCaseIdsToAdd = new LinkedHashSet<Long>();
    for (var testCaseId : testCaseIds) {
      // Check if a test case exists
      if (!existingTestCaseIds.contains(testCaseId)) {
        errors.add(new BatchTestCaseOperationError(testCaseId,
            String.format("Test case with id %s not found", testCaseId)));
        continue;
      }

      // Check if already added to the plan (or requested twice)
      if (testCaseIdsAreInTestPlan.contains(testCaseId) || !testCaseIdsToAdd.add(testCaseId)) {
        errors.add(new BatchTestCaseOperationError(
            testCaseId,
            String.format("Test case with id %s already exists in test plan", testCaseId)
        ));
      }
    }

    // Folder-resolved test cases are added idempotently: those already in the plan count as success
    var explicitTestCaseIds = new HashSet<>(testCaseIds);
    var folderTestCaseIdsInTestPlan = new ArrayList<Long>();
    for (var testCaseId : folderTestCaseIds) {
      if (explicitTestCaseIds.contains(testCaseId)) {
        continue;
      }
      totalCount++;
      if (testCaseIdsAreInTestPlan.contains(testCaseId)) {
        folderTestCaseIdsInTestPlan.add(testCaseId);
      } else {
        testCaseIdsToAdd.add(testCaseId);
      }
    }

    // Count only the rows the statement really inserted: a concurrent request may have won
    var addedTestCaseIds = new HashSet<Long>();
    for (var chunk : ListUtils.partition(List.copyOf(testCaseIdsToAdd), INSERT_CHUNK_SIZE)) {
      addedTestCaseIds.addAll(
          tmsTestPlanTestCaseRepository.insertTestPlanTestCasesInProject(projectId, testPlanId,
              chunk));
    }

    var successTestCaseIds = new ArrayList<Long>();
    for (var testCaseId : testCaseIdsToAdd) {
      if (addedTestCaseIds.contains(testCaseId) || !explicitTestCaseIds.contains(testCaseId)) {
        // A folder-resolved test case skipped by the insert is already in the plan
        successTestCaseIds.add(testCaseId);
      } else {
        errors.add(new BatchTestCaseOperationError(testCaseId,
            String.format("Test case with id %s already exists in test plan", testCaseId)));
      }
    }
    successTestCaseIds.addAll(folderTestCaseIdsInTestPlan);
    var result = tmsTestPlanMapper.convertToRS(totalCount, successTestCaseIds.size(), errors);
    result.setSuccessTestCaseIds(successTestCaseIds);
    return result;
  }

  @Override
  @Transactional
  public BatchTestCaseOperationResultRS removeTestCasesFromPlan(Long projectId, Long testPlanId,
      List<Long> testCaseIds) {

    verifyTestPlanExists(projectId, testPlanId);

    var errors = new ArrayList<BatchTestCaseOperationError>();
    var totalCount = testCaseIds.size();

    // Get test case IDs that are actually in the plan
    var testCaseIdsInTestPlan = new HashSet<>(
        tmsTestPlanTestCaseRepository.findTestCaseIdsByTestPlanId(testPlanId)
    );

    var testCaseIdsToRemove = new LinkedHashSet<Long>();
    for (var testCaseId : testCaseIds) {
      // Check if a test case is in the plan (a repeated id is not in the plan any more)
      if (!testCaseIdsInTestPlan.contains(testCaseId) || !testCaseIdsToRemove.add(testCaseId)) {
        errors.add(new BatchTestCaseOperationError(testCaseId,
            String.format("Test case with id %s not found in test plan", testCaseId)));
      }
    }

    var removedTestCaseIds = new HashSet<Long>();
    if (!testCaseIdsToRemove.isEmpty()) {
      // Count only the rows the statement really deleted: a concurrent request may have won
      removedTestCaseIds.addAll(tmsTestPlanTestCaseRepository.deleteByTestPlanIdAndTestCaseIds(
          testPlanId, List.copyOf(testCaseIdsToRemove)));
      for (var testCaseId : testCaseIdsToRemove) {
        if (!removedTestCaseIds.contains(testCaseId)) {
          errors.add(new BatchTestCaseOperationError(testCaseId,
              String.format("Test case with id %s not found in test plan", testCaseId)));
        }
      }
    }

    return tmsTestPlanMapper.convertToRS(totalCount, removedTestCaseIds.size(), errors);
  }

  @Override
  @Transactional
  public boolean addTestCaseToTestPlan(Long projectId, Long testPlanId, Long testCaseId) {
    return tmsTestPlanTestCaseRepository.insertTestPlanTestCaseInProject(projectId, testPlanId,
        testCaseId) == 1;
  }

  @Override
  @Transactional
  public DuplicateTmsTestPlanRS duplicate(MembershipDetails membershipDetails,
      ReportPortalUser user, Long testPlanId,
      TmsTestPlanRQ duplicateTestPlanRQ) {
    var projectId = membershipDetails.getProjectId();
    // Get original test plan
    var originalTestPlan = testPlanRepository
        .findByIdAndProjectId(testPlanId, projectId)
        .orElseThrow(() -> new ReportPortalException(
            NOT_FOUND, TMS_TEST_PLAN_NOT_FOUND_BY_ID.formatted(testPlanId, projectId))
        );
    verifyMilestoneExistsIfPresent(projectId, duplicateTestPlanRQ);

    // Duplicate test plan entity
    var duplicatedTestPlan = tmsTestPlanMapper.duplicateTestPlan(originalTestPlan,
        duplicateTestPlanRQ);

    duplicatedTestPlan = testPlanRepository.save(duplicatedTestPlan);

    // Duplicate test plan attributes
    tmsTestPlanAttributeService.createTestPlanAttributes(
        projectId, duplicatedTestPlan, duplicateTestPlanRQ.getAttributes()
    );

    // Get test case IDs from the original plan
    var originalTestCaseIds = tmsTestPlanTestCaseRepository.findTestCaseIdsByTestPlanId(
        originalTestPlan.getId());

    // Process test case duplication and addition to plan
    var duplicateTestCasesStatistic = processBatchTestCaseDuplication(membershipDetails,
        user,
        duplicatedTestPlan.getId(),
        originalTestCaseIds);

    return tmsTestPlanMapper.buildDuplicateTestPlanResponse(duplicatedTestPlan,
        duplicateTestCasesStatistic);
  }

  @Override
  @Transactional
  public DuplicateTmsTestPlanRS duplicate(MembershipDetails membershipDetails,
      ReportPortalUser user, Long testPlanId, Long targetMilestoneId) {
    var projectId = membershipDetails.getProjectId();
    // Get original test plan
    var originalTestPlan = testPlanRepository
        .findByIdAndProjectId(testPlanId, projectId)
        .orElseThrow(() -> new ReportPortalException(
            NOT_FOUND, TMS_TEST_PLAN_NOT_FOUND_BY_ID.formatted(testPlanId, projectId))
        );

    // Duplicate test plan entity
    var duplicatedTestPlan = tmsTestPlanMapper.duplicateTestPlan(originalTestPlan,
        targetMilestoneId);

    duplicatedTestPlan = testPlanRepository.save(duplicatedTestPlan);

    // Duplicate test plan attributes
    tmsTestPlanAttributeService.duplicateTestPlanAttributes(originalTestPlan, duplicatedTestPlan);

    // Get test case IDs from the original plan
    var originalTestCaseIds = tmsTestPlanTestCaseRepository.findTestCaseIdsByTestPlanId(
        originalTestPlan.getId());

    // Process test case duplication and addition to plan
    var duplicateTestCasesStatistic = processBatchTestCaseDuplication(membershipDetails,
        user,
        duplicatedTestPlan.getId(),
        originalTestCaseIds);

    return tmsTestPlanMapper.buildDuplicateTestPlanResponse(duplicatedTestPlan,
        duplicateTestCasesStatistic);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<TmsTestCaseInTestPlanRS> getTestCasesAddedToPlan(Long projectId, Long testPlanId,
      Filter filter, Pageable pageable) {
    verifyTestPlanExists(projectId, testPlanId);
    return tmsTestCaseService.getTestCasesInTestPlan(projectId, testPlanId, filter, pageable);
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

  @Override
  @Transactional(readOnly = true)
  public Map<Long, TmsTestPlan> getTestPlanMap(Long projectId, List<Long> testPlanIds) {
    if (CollectionUtils.isEmpty(testPlanIds)) {
      return Collections.emptyMap();
    }

    return testPlanRepository
        .findByProjectIdAndIds(projectId, testPlanIds)
        .stream()
        .collect(Collectors.toMap(TmsTestPlan::getId, Function.identity()));
  }

  @Override
  @Transactional(readOnly = true)
  public TmsTestPlan getEntityById(Long projectId, Long testPlanId) {
    return testPlanRepository
        .findByIdAndProjectId(testPlanId, projectId)
        .orElse(null);
  }

  @Override
  @Transactional(readOnly = true)
  public List<Long> getTestCaseIdsAddedToPlan(long projectId, Long testPlanId) {
    verifyTestPlanExists(projectId, testPlanId);
    return tmsTestCaseService.getTestCaseIdsInTestPlan(projectId, testPlanId);
  }

  @Override
  @Transactional
  public void removeTestPlanFromMilestone(Long projectId, Long milestoneId, Long testPlanId) {
    verifyMilestoneExists(projectId, milestoneId);
    verifyTestPlanExists(projectId, testPlanId);

    var updatedCount = testPlanRepository.removeTestPlanFromMilestone(milestoneId, testPlanId,
        projectId);

    if (updatedCount == 0) {
      throw new ReportPortalException(NOT_FOUND,
          "Test plan " + testPlanId + " in milestone " + milestoneId);
    }
  }

  @Override
  @Transactional(readOnly = true)
  public List<TmsTestPlanRS> getByMilestoneId(Long projectId, Long milestoneId) {
    var testPlanIds = testPlanRepository.findIdsByProjectIdAndMilestoneId(
        projectId, milestoneId
    );

    if (testPlanIds.isEmpty()) {
      return List.of();
    }

    var testPlans = testPlanRepository
        .findByIdsWithAttributes(testPlanIds)
        .stream()
        .collect(Collectors.toMap(TmsTestPlan::getId, Function.identity()));

    var orderedTestPlans = tmsTestPlanExecutionService.enrichWithStatistics(
        testPlanIds
            .stream()
            .map(testPlans::get)
            .toList()
    );

    return tmsTestPlanMapper
        .convertTmsTestPlansWithStatisticToRS(orderedTestPlans);
  }

  @Override
  public Map<Long, List<TmsTestPlanRS>> getByMilestoneIds(Long projectId, List<Long> milestoneIds) {
    var testPlanIds = testPlanRepository.findIdsByProjectIdAndMilestoneIds(
        projectId, milestoneIds
    );

    if (testPlanIds.isEmpty()) {
      return Map.of();
    }

    var testPlans = testPlanRepository
        .findByIdsWithAttributes(testPlanIds)
        .stream()
        .collect(Collectors.toMap(TmsTestPlan::getId, Function.identity()));

    var orderedTestPlans = tmsTestPlanExecutionService.enrichWithStatistics(
        testPlanIds
            .stream()
            .map(testPlans::get)
            .toList()
    );

    return tmsTestPlanMapper
        .convertTmsTestPlansWithStatisticToMap(orderedTestPlans);
  }

  @Override
  @Transactional
  public List<DuplicateTmsTestPlanRS> duplicateTestPlansInMilestone(
      MembershipDetails membershipDetails,
      ReportPortalUser user, Long sourceMilestoneId, Long targetMilestoneId) {
    var projectId = membershipDetails.getProjectId();
    var testPlanIds = testPlanRepository.findIdsByProjectIdAndMilestoneId(
        projectId, sourceMilestoneId
    );

    if (testPlanIds.isEmpty()) {
      return List.of();
    }
    var result = new ArrayList<DuplicateTmsTestPlanRS>();
    for (var testPlanId : testPlanIds) {
      var duplicatedTestPlanRS = duplicate(membershipDetails, user, testPlanId, targetMilestoneId);
      result.add(duplicatedTestPlanRS);
    }
    return result;
  }

  @Override
  @Transactional
  public void addTestPlanMilestone(Long projectId, Long milestoneId, Long testPlanId) {
    verifyMilestoneExists(projectId, milestoneId);
    verifyTestPlanExists(projectId, testPlanId);

    testPlanRepository.addTestPlanToMilestone(milestoneId, testPlanId,
        projectId);
  }

  @Override
  @Transactional
  public void deleteTestPlansByMilestoneId(Long projectId, Long milestoneId) {
    verifyMilestoneExists(projectId, milestoneId);

    var testPlanIds = testPlanRepository.findIdsByProjectIdAndMilestoneId(projectId, milestoneId);

    if (testPlanIds.isEmpty()) {
      return;
    }

    launchRepository.clearTestPlanIdsByProjectIdAndTestPlanIds(projectId, testPlanIds);
    testPlanIds.forEach(tmsTestPlanAttributeService::deleteAllByTestPlanId);
    testPlanRepository.deleteByProjectIdAndMilestoneId(projectId, milestoneId);
  }

  private BatchTestCaseOperationResultRS processBatchTestCaseDuplication(
      MembershipDetails membershipDetails,
      ReportPortalUser user,
      Long newTestPlanId,
      List<Long> originalTestCaseIds) {
    if (originalTestCaseIds.isEmpty()) {
      return tmsTestPlanMapper.createFailedBatchResult(Collections.emptyList(),
          "No test cases to duplicate");
    }

    var projectId = membershipDetails.getProjectId();

    // Step 1: Duplicate test cases in batch
    var duplicationResult = tmsTestCaseService.duplicateTestCases(membershipDetails, user,
        originalTestCaseIds);

    // Step 2: Add successfully duplicated test cases to the new plan
    if (!duplicationResult.getSuccessTestCaseIds().isEmpty()) {
      var addToPlanResult = addTestCasesToPlan(projectId, newTestPlanId,
          duplicationResult.getSuccessTestCaseIds());
      return tmsTestPlanMapper.combineDuplicateTestPlanBatchResults(duplicationResult,
          addToPlanResult);
    }

    return duplicationResult;
  }

  @Override
  @Transactional(readOnly = true)
  public Page<TmsManualLaunchTestPlanRS> getTestPlanNames(Long projectId, String search,
      Pageable pageable) {
    var page = testPlanRepository
        .findIdAndNameByProjectIdAndSearch(projectId, search, pageable)
        .map(info -> TmsManualLaunchTestPlanRS.builder()
            .id(info.getId())
            .name(info.getName())
            .build());
    return PagedResourcesAssembler
        .<TmsManualLaunchTestPlanRS>pageConverter()
        .apply(page);
  }

  private void verifyMilestoneExistsIfPresent(Long projectId, TmsTestPlanRQ testPlanRQ) {
    if (testPlanRQ != null && testPlanRQ.getMilestoneId() != null) {
      verifyMilestoneExists(projectId, testPlanRQ.getMilestoneId());
    }
  }

  public void verifyMilestoneExists(Long projectId, Long milestoneId) {
    if (!tmsMilestoneRepository.existsByIdAndProjectId(milestoneId, projectId)) {
      throw new ReportPortalException(
          NOT_FOUND, TMS_MILESTONE_NOT_FOUND_BY_ID.formatted(milestoneId, projectId)
      );
    }
  }
}
