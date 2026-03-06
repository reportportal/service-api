package com.epam.reportportal.base.core.tms.service;

import static com.epam.reportportal.base.infrastructure.rules.exception.ErrorType.BAD_REQUEST_ERROR;
import static com.epam.reportportal.base.infrastructure.rules.exception.ErrorType.NOT_FOUND;

import com.epam.reportportal.base.core.item.TestItemService;
import com.epam.reportportal.base.core.launch.DeleteLaunchHandler;
import com.epam.reportportal.base.core.tms.dto.AddTestCaseToLaunchRQ;
import com.epam.reportportal.base.core.tms.dto.CreateTmsManualLaunchRQ;
import com.epam.reportportal.base.core.tms.dto.CreateTmsManualLaunchRS;
import com.epam.reportportal.base.core.tms.dto.TmsManualLaunchRQ;
import com.epam.reportportal.base.core.tms.dto.TmsManualLaunchRS;
import com.epam.reportportal.base.core.tms.dto.TmsTestCaseExecutionCommentRQ;
import com.epam.reportportal.base.core.tms.dto.TmsTestCaseExecutionCommentRS;
import com.epam.reportportal.base.core.tms.dto.TmsTestCaseExecutionRQ;
import com.epam.reportportal.base.core.tms.dto.TmsTestCaseExecutionRS;
import com.epam.reportportal.base.core.tms.dto.TmsTestFolderRS;
import com.epam.reportportal.base.core.tms.dto.batch.BatchAddTestCasesToLaunchRQ;
import com.epam.reportportal.base.core.tms.dto.batch.BatchDeleteManualLaunchesRQ;
import com.epam.reportportal.base.core.tms.dto.batch.BatchDeleteTestCaseExecutionError;
import com.epam.reportportal.base.core.tms.dto.batch.BatchDeleteTestCaseExecutionsRQ;
import com.epam.reportportal.base.core.tms.dto.batch.BatchDeleteTestCaseExecutionsResultRS;
import com.epam.reportportal.base.core.tms.dto.batch.BatchManualLaunchOperationError;
import com.epam.reportportal.base.core.tms.dto.batch.BatchManualLaunchOperationResultRS;
import com.epam.reportportal.base.core.tms.dto.batch.BatchTestCaseOperationError;
import com.epam.reportportal.base.core.tms.dto.batch.BatchTestCaseOperationResultRS;
import com.epam.reportportal.base.core.tms.mapper.TmsManualLaunchMapper;
import com.epam.reportportal.base.core.user.GetUserHandler;
import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.Filter;
import com.epam.reportportal.base.infrastructure.persistence.dao.LaunchRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.filterable.TmsManualLaunchFilterableRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.LaunchTypeEnum;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.StatusEnum;
import com.epam.reportportal.base.infrastructure.persistence.entity.launch.Launch;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.MembershipDetails;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.base.model.Page;
import com.epam.reportportal.base.util.OffsetRequest;
import com.epam.reportportal.base.ws.converter.PagedResourcesAssembler;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service implementation for managing TMS Manual Launches.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TmsManualLaunchServiceImpl implements TmsManualLaunchService {

  private static final String LAUNCH_NOT_FOUND_BY_ID = "Launch with id: %d for project: %d";
  private static final String TEST_CASE_ALREADY_IN_LAUNCH = "Test case with id: %d already exists in launch: %d";
  private static final String TEST_CASE_EXECUTION_IN_LAUNCH =
      "Test Case execution: %d for Launch: %d";
  private static final String TEST_CASE_NOT_FOUND_BY_ID = "Test case with id: %d for project: %d";

  private final LaunchRepository launchRepository;
  private final TmsManualLaunchFilterableRepository tmsManualLaunchFilterableRepository;
  private final TmsManualLaunchMapper tmsManualLaunchMapper;
  private final TmsManualLaunchAttributeService tmsManualLaunchAttributeService;
  private final TestItemService testItemService;
  private final DeleteLaunchHandler deleteLaunchHandler;
  private final TestFolderItemServiceImpl testFolderItemService;
  private final TmsStepExecutionService tmsStepExecutionService;
  private final GetUserHandler getUserHandler;
  private final TmsTestPlanService tmsTestPlanService;

  private TmsTestCaseExecutionService tmsTestCaseExecutionService;
  private TmsTestCaseService tmsTestCaseService;

  @Autowired
  public void setTmsTestCaseExecutionService(
      TmsTestCaseExecutionService tmsTestCaseExecutionService) {
    this.tmsTestCaseExecutionService = tmsTestCaseExecutionService;
  }

  @Autowired
  public void setTmsTestCaseService(
      TmsTestCaseService tmsTestCaseService) {
    this.tmsTestCaseService = tmsTestCaseService;
  }

  @Override
  @Transactional
  public CreateTmsManualLaunchRS create(Long projectId,
      ReportPortalUser user,
      CreateTmsManualLaunchRQ request) {
    log.debug("Creating manual launch for project: {}", projectId);

    // Create Launch entity
    var launch = tmsManualLaunchMapper.convertFromCreateTmsManualLaunchRQ(projectId, user, request);
    launch.setLaunchType(LaunchTypeEnum.MANUAL);
    launch.setStatus(StatusEnum.IN_PROGRESS);
    launch = launchRepository.save(launch);

    // Create attributes if present
    if (CollectionUtils.isNotEmpty(request.getAttributes())) {
      tmsManualLaunchAttributeService.createAttributes(launch, request.getAttributes());
    }

    // Add test cases to launch if present
    var addTestCasesResult = tmsTestCaseExecutionService
        .addTestCasesToLaunch(
            projectId,
            launch,
            CollectionUtils.isEmpty(request.getTestCaseIds()) ?
                tmsTestPlanService.getTestCaseIdsAddedToPlan(projectId, request.getTestPlan() != null ? request.getTestPlan().getId() : null) :
                request.getTestCaseIds()
        );

    log.debug("Created manual launch with ID: {} for project: {}", launch.getId(), projectId);

    return tmsManualLaunchMapper.convertToCreateTmsManualLaunchRS(launch, addTestCasesResult);
  }

  @Override
  @Transactional(readOnly = true)
  public TmsManualLaunchRS getById(long projectId, Long launchId) {
    log.debug("Getting manual launch by ID: {} for project: {}", launchId, projectId);

    var launch = launchRepository.findManualLaunchByIdAndProjectId(launchId, projectId)
        .orElseThrow(() -> new ReportPortalException(
            NOT_FOUND, LAUNCH_NOT_FOUND_BY_ID.formatted(launchId, projectId))
        );

    // Get execution statistic
    var testCaseExecutionStatistic = tmsTestCaseExecutionService.getTestCaseExecutionStatistic(
        launchId);

    // Get user and test plan maps
    var user = getUserHandler.getUserById(launch.getUserId());
    var testPlan = tmsTestPlanService.getEntityById(projectId, launch.getTestPlanId());

    return tmsManualLaunchMapper.convert(launch, testCaseExecutionStatistic, user, testPlan);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<TmsManualLaunchRS> getManualLaunches(long projectId, Filter filter,
      Pageable pageable) {
    log.debug("Getting manual launches for project: {}", projectId);

    var launchesPage = tmsManualLaunchFilterableRepository.findByProjectIdAndFilter(
        projectId, filter, pageable
    );

    if (launchesPage.getContent().isEmpty()) {
      return PagedResourcesAssembler
          .<TmsManualLaunchRS>pageConverter()
          .apply(new PageImpl<>(Collections.emptyList(), pageable, 0));
    }

    var launches = launchesPage.getContent();
    var launchIds = launches
        .stream()
        .map(Launch::getId)
        .collect(Collectors.toList());

    // Collect unique user IDs and test plan IDs
    var userIds = launches
        .stream()
        .map(Launch::getUserId)
        .filter(Objects::nonNull)
        .toList();

    var testPlanIds = launches
        .stream()
        .map(Launch::getTestPlanId)
        .filter(Objects::nonNull)
        .toList();

    // Get maps from services
    var userMap = getUserHandler.getUserMap(userIds);
    var testPlanMap = tmsTestPlanService.getTestPlanMap(testPlanIds);
    var testCaseExecutionStatistics =
        tmsTestCaseExecutionService.getTestCaseExecutionStatistic(launchIds);

    // Convert to response DTOs
    var launchResponses = launches.stream()
        .map(launch -> tmsManualLaunchMapper.convert(
            launch,
            testCaseExecutionStatistics.get(launch.getId()),
            userMap.get(launch.getUserId()),
            testPlanMap.get(launch.getTestPlanId())
        ))
        .collect(Collectors.toList());

    return PagedResourcesAssembler
        .<TmsManualLaunchRS>pageConverter()
        .apply(new PageImpl<>(launchResponses, pageable, launchesPage.getTotalElements()));
  }

  @Override
  @Transactional
  public void delete(MembershipDetails membershipDetails, Long launchId, ReportPortalUser user) {
    var projectId = membershipDetails.getProjectId();
    log.debug("Deleting manual launch: {} for project: {}", launchId, projectId);

    if (!launchRepository.existsByIdAndProjectId(launchId, projectId)) {
      throw new ReportPortalException(
          NOT_FOUND, LAUNCH_NOT_FOUND_BY_ID.formatted(launchId, projectId));
    }

    // Delete tms step executions
    tmsStepExecutionService.deleteByLaunchId(launchId);

    // Delete test case executions (includes launch-test case associations)
    tmsTestCaseExecutionService.deleteByLaunchId(launchId);

    // Delete test folder item associations
    testFolderItemService.deleteByLaunchId(launchId);

    // Delete test items
    testItemService.deleteByLaunchId(projectId,
        launchId); //TODO check if that is required ( potential CASCADE REMOVAL)

    // Delete attributes
    tmsManualLaunchAttributeService.deleteAllByLaunchId(
        launchId); //TODO check if that is required ( potential CASCADE REMOVAL)

    // Delete launch
    deleteLaunchHandler.deleteLaunch(launchId, membershipDetails, user);

    log.debug("Deleted manual launch: {} for project: {}", launchId, projectId);
  }

  @Override
  @Transactional
  public TmsManualLaunchRS patch(long projectId, Long launchId, TmsManualLaunchRQ request) {
    log.debug("Patching manual launch: {} for project: {}", launchId, projectId);

    var existingLaunch = launchRepository
        .findManualLaunchByIdAndProjectId(launchId, projectId)
        .orElseThrow(() -> new ReportPortalException(
            NOT_FOUND, LAUNCH_NOT_FOUND_BY_ID.formatted(launchId, projectId))
        );

    // Patch launch fields
    tmsManualLaunchMapper.patch(existingLaunch, request);
    if (request.getTestPlan() != null) {
      existingLaunch.setTestPlanId(request.getTestPlan().getId());
    }
    existingLaunch = launchRepository.save(existingLaunch);

    // Patch attributes if present
    if (request.getAttributes() != null) {
      tmsManualLaunchAttributeService.updateAttributes(existingLaunch, request.getAttributes());
    }

    // Add test cases if present (don't remove existing ones)
    if (CollectionUtils.isNotEmpty(request.getTestCaseIds())) {
      tmsTestCaseExecutionService.addTestCasesToLaunch(projectId, existingLaunch,
          request.getTestCaseIds());
    }

    log.debug("Patched manual launch: {} for project: {}", launchId, projectId);

    return tmsManualLaunchMapper.convert(existingLaunch,
        tmsTestCaseExecutionService.getTestCaseExecutionStatistic(launchId));
  }

  @Override
  @Transactional
  public void addTestCaseToLaunch(Long projectId, Long launchId, AddTestCaseToLaunchRQ request) {

    // Verify launch exists and belongs to project
    var launch = launchRepository.findManualLaunchByIdAndProjectId(launchId, projectId)
        .orElseThrow(() -> new ReportPortalException(
            NOT_FOUND, LAUNCH_NOT_FOUND_BY_ID.formatted(launchId, projectId))
        );

    var testCaseId = request.getTestCaseId();

    if (!tmsTestCaseService.existsById(projectId, testCaseId)) { // Check if a test case exists
      throw new ReportPortalException(
          BAD_REQUEST_ERROR,
          TEST_CASE_NOT_FOUND_BY_ID.formatted(testCaseId, projectId)
      );
    } else if (tmsTestCaseExecutionService.isTestCaseInLaunch(testCaseId,
        launchId)) {  // Check if test case already in launch
      throw new ReportPortalException(
          BAD_REQUEST_ERROR,
          TEST_CASE_ALREADY_IN_LAUNCH.formatted(testCaseId, launchId)
      );
    }

    // Add a test case through execution service (creates execution and test item)
    tmsTestCaseExecutionService.addTestCaseToLaunch(projectId, launch, testCaseId);

    log.debug("Added test case: {} to launch: {}", testCaseId, launchId);
  }

  @Override
  @Transactional
  public BatchTestCaseOperationResultRS addTestCasesToLaunch(Long projectId, Long launchId,
      BatchAddTestCasesToLaunchRQ request) {

    // Verify launch exists and belongs to project
    var launch = launchRepository
        .findManualLaunchByIdAndProjectId(launchId, projectId)
        .orElseThrow(() -> new ReportPortalException(
            NOT_FOUND, LAUNCH_NOT_FOUND_BY_ID.formatted(launchId, projectId))
        );

    List<Long> successTestCaseIds = new ArrayList<>();
    List<BatchTestCaseOperationError> errors = new ArrayList<>();

    var testCaseIds = request.getTestCaseIds();

    // Try to add each test case
    for (var testCaseId : testCaseIds) {
      try {
        // Check if already exists
        if (!tmsTestCaseService.existsById(projectId, testCaseId)) { // Check if a test case exists
          log.warn("Test case {} for project {} does not exist", testCaseId, projectId);
          errors.add(new BatchTestCaseOperationError(
              testCaseId,
              "Test case does not exist in the project"
          ));
          continue;
        } else if (tmsTestCaseExecutionService.isTestCaseInLaunch(testCaseId, launchId)) {
          log.warn("Test case {} already exists in launch {}", testCaseId, launchId);
          errors.add(new BatchTestCaseOperationError(
              testCaseId,
              "Test case already exists in launch"
          ));
          continue;
        }

        // Add a test case through execution service (creates execution and test item)
        tmsTestCaseExecutionService.addTestCaseToLaunch(projectId, launch, testCaseId);
        successTestCaseIds.add(testCaseId);

      } catch (Exception e) {
        log.error("Failed to add test case {} to launch {}", testCaseId, launchId, e);
        errors.add(new BatchTestCaseOperationError(
            testCaseId,
            e.getMessage()
        ));
      }
    }

    var result = tmsManualLaunchMapper.convertBatchAddTestCaseOperationResultRS(testCaseIds,
        successTestCaseIds, errors);

    log.debug("Added test cases to launch: {} - success: {}, failed: {}",
        launchId, successTestCaseIds.size(), errors.size());

    return result;
  }

  @Override
  @Transactional(readOnly = true)
  public Page<TmsTestCaseExecutionRS> getLaunchTestCaseExecutions(
      Long projectId,
      Long launchId,
      Filter filter,
      OffsetRequest pageable) {

    log.debug("Getting test case executions for launch: {} in project: {}", launchId, projectId);

    // Validate launch belongs to project
    validateLaunchBelongsToProject(launchId, projectId);

    // Delegate to execution service
    return tmsTestCaseExecutionService.findByLaunchIdWithFilter(launchId, filter, pageable);
  }

  @Override
  @Transactional(readOnly = true)
  public TmsTestCaseExecutionRS getTestCaseExecution(
      Long projectId,
      Long launchId,
      Long executionId) {

    log.debug("Getting test case execution: {} for launch: {} in project: {}",
        executionId, launchId, projectId);

    // Validate launch belongs to project
    validateLaunchBelongsToProject(launchId, projectId);

    // Delegate to execution service
    return tmsTestCaseExecutionService.findByIdAndLaunchIdWithDetails(
        executionId,
        launchId
    );
  }

  @Override
  @Transactional
  public void deleteTestCaseExecution(
      Long projectId,
      Long launchId,
      Long executionId) {

    log.debug("Deleting test case execution: {} from launch: {} in project: {}",
        executionId, launchId, projectId);

    // Validate launch belongs to project
    validateLaunchBelongsToProject(launchId, projectId);

    // Verify execution exists before deletion
    if (!tmsTestCaseExecutionService.existsByTestCaseExecutionIdAndLaunchId(executionId,
        launchId)) {
      throw new ReportPortalException(
          NOT_FOUND,
          TEST_CASE_EXECUTION_IN_LAUNCH.formatted(executionId, launchId)
      );
    }

    // Delegate to execution service
    tmsTestCaseExecutionService.deleteTestCaseExecutionFromLaunch(projectId, launchId, executionId);

    log.debug("Test case execution: {} deleted from launch: {} in project: {}",
        executionId, launchId, projectId);
  }

  @Override
  @Transactional
  public BatchDeleteTestCaseExecutionsResultRS batchDeleteTestCaseExecutions(Long projectId,
      Long launchId, BatchDeleteTestCaseExecutionsRQ request) {

    // Validate launch belongs to project
    validateLaunchBelongsToProject(launchId, projectId);

    List<Long> successExecutionIds = new ArrayList<>();
    List<BatchDeleteTestCaseExecutionError> errors = new ArrayList<>();
    var executionIds = request.getExecutionIds();

    for (var executionId : executionIds) {
      try {
        // Verify execution exists and belongs to launch
        if (!tmsTestCaseExecutionService.existsByTestCaseExecutionIdAndLaunchId(executionId,
            launchId)) {
          errors.add(new BatchDeleteTestCaseExecutionError(
              executionId,
              "Test case execution not found in launch " + launchId
          ));
          continue;
        }

        tmsTestCaseExecutionService.deleteTestCaseExecutionFromLaunch(projectId, launchId,
            executionId);
        successExecutionIds.add(executionId);

      } catch (Exception e) {
        log.error("Failed to delete execution {} from launch {}", executionId, launchId, e);
        errors.add(new BatchDeleteTestCaseExecutionError(
            executionId,
            e.getMessage()
        ));
      }
    }

    return tmsManualLaunchMapper.convertToBatchDeleteExecutionsResponse(
        executionIds, successExecutionIds, errors
    );
  }

  @Override
  @Transactional(readOnly = true)
  public Page<TmsTestCaseExecutionRS> getTestCaseExecutionsInLaunchForTestCase(
      Long projectId,
      Long launchId,
      Long testCaseId,
      OffsetRequest pageable) {

    log.debug("Getting executions for test case: {} in launch: {} in project: {}",
        testCaseId, launchId, projectId);

    // Validate launch belongs to project
    validateLaunchBelongsToProject(launchId, projectId);

    // Delegate to execution service
    return tmsTestCaseExecutionService.findByTestCaseIdAndLaunchId(testCaseId, launchId, pageable);
  }

  @Override
  @Transactional
  public TmsTestCaseExecutionRS patchTestCaseExecution(
      Long projectId,
      Long launchId,
      Long executionId,
      TmsTestCaseExecutionRQ request) {

    log.debug("Patching test case execution: {} in launch: {} in project: {}",
        executionId, launchId, projectId);

    // Validate launch belongs to project
    validateLaunchBelongsToProject(launchId, projectId);

    // Delegate to execution service
    return tmsTestCaseExecutionService.patch(executionId, launchId, request);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<TmsTestFolderRS> getLaunchFolders(Long projectId, Long launchId, Filter filter, OffsetRequest pageable) {
    log.debug("Getting folders for launch: {} in project: {}", launchId, projectId);

    // Validate launch belongs to project
    validateLaunchBelongsToProject(launchId, projectId);

    // Get unique folders from test case executions
    return testFolderItemService.getSuiteFoldersByLaunch(projectId,
        launchId, filter, pageable
    );
  }

  @Override
  @Transactional
  public BatchManualLaunchOperationResultRS batchDeleteManualLaunches(
      MembershipDetails membershipDetails,
      BatchDeleteManualLaunchesRQ request, ReportPortalUser user) {
    var projectId = membershipDetails.getProjectId();
    log.debug("Batch deleting manual launches for project: {}", projectId);

    List<Long> successLaunchIds = new ArrayList<>();
    List<BatchManualLaunchOperationError> errors = new ArrayList<>();

    var launchIds = request.getLaunchIds();

    // Try to delete each launch
    for (var launchId : launchIds) {
      try {
        // Check if launch exists and belongs to project
        if (!launchRepository.existsByIdAndProjectIdAndLaunchType(launchId, projectId,
            LaunchTypeEnum.MANUAL)) {
          log.warn("Launch {} not found in project {}", launchId, projectId);
          errors.add(new BatchManualLaunchOperationError(
              launchId,
              "Manual Launch not found in project"
          ));
          continue;
        }

        // Delete tms step executions
        tmsStepExecutionService.deleteByLaunchId(launchId);

        // Delete test case executions (includes launch-test case associations)
        tmsTestCaseExecutionService.deleteByLaunchId(launchId);

        // Delete test folder item associations
        testFolderItemService.deleteByLaunchId(launchId);

        // Delete test items
        testItemService.deleteByLaunchId(projectId,
            launchId); //TODO check if that is required ( potential CASCADE REMOVAL)

        // Delete attributes
        tmsManualLaunchAttributeService.deleteAllByLaunchId(
            launchId); //TODO check if that is required ( potential CASCADE REMOVAL)

        // Delete launch
        deleteLaunchHandler.deleteLaunch(launchId, membershipDetails, user);

        successLaunchIds.add(launchId);

      } catch (Exception e) {
        log.error("Failed to delete launch {} in project {}", launchId, projectId, e);
        errors.add(new BatchManualLaunchOperationError(
            launchId,
            e.getMessage()
        ));
      }
    }

    log.debug("Batch deleted launches in project: {} - success: {}, failed: {}",
        projectId, successLaunchIds.size(), errors.size());

    return tmsManualLaunchMapper.convertToBatchDeleteResponse(
        launchIds, successLaunchIds, errors
    );
  }

  @Override
  public Optional<Long> getTestPlanIdByLaunchId(Long launchId) {
    return launchRepository.findTestPlanIdById(launchId);
  }

  @Override
  @Transactional
  public TmsTestCaseExecutionCommentRS putTestCaseExecutionComment(Long projectId, Long launchId,
      Long executionId, TmsTestCaseExecutionCommentRQ request) {
    // Validate launch belongs to project
    validateLaunchBelongsToProject(launchId, projectId);

    return tmsTestCaseExecutionService.putTestCaseExecutionComment(
        projectId, launchId, executionId, request
    );
  }

  @Override
  @Transactional
  public void deleteTestCaseExecutionComment(Long projectId, Long launchId, Long executionId) {
    // Validate launch belongs to project
    validateLaunchBelongsToProject(launchId, projectId);

    tmsTestCaseExecutionService.deleteTestCaseExecutionComment(projectId, launchId,
        executionId);
  }

  @Override
  @Transactional(readOnly = true)
  public Map<Long, Launch> getEntitiesByIds(Long projectId, List<Long> launchIds) {
    return launchRepository
        .findAllById(launchIds)
        .stream()
        .collect(
            Collectors.toMap(
                Launch::getId, Function.identity()
            )
        );
  }

  /**
   * Validates that launch belongs to the specified project.
   *
   * @param launchId  launch ID
   * @param projectId project ID
   * @throws ReportPortalException if launch isn't found or doesn't belong to project
   */
  private void validateLaunchBelongsToProject(Long launchId, Long projectId) {
    if (!launchRepository.existsByIdAndProjectId(launchId, projectId)) {
      throw new ReportPortalException(
          ErrorType.LAUNCH_NOT_FOUND,
          launchId
      );
    }
  }
}
