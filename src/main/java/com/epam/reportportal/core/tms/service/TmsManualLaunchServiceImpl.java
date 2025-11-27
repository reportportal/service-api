package com.epam.reportportal.core.tms.service;

import static com.epam.reportportal.infrastructure.rules.exception.ErrorType.BAD_REQUEST_ERROR;
import static com.epam.reportportal.infrastructure.rules.exception.ErrorType.NOT_FOUND;

import com.epam.reportportal.core.item.TestItemService;
import com.epam.reportportal.core.tms.dto.AddTestCaseToLaunchRQ;
import com.epam.reportportal.core.tms.dto.TmsManualLaunchRQ;
import com.epam.reportportal.core.tms.dto.TmsManualLaunchRS;
import com.epam.reportportal.core.tms.dto.TmsTestCaseExecutionRQ;
import com.epam.reportportal.core.tms.dto.TmsTestCaseExecutionRS;
import com.epam.reportportal.core.tms.dto.TmsTestFolderRS;
import com.epam.reportportal.core.tms.dto.batch.BatchAddTestCasesToLaunchRQ;
import com.epam.reportportal.core.tms.dto.batch.BatchDeleteManualLaunchesRQ;
import com.epam.reportportal.core.tms.dto.batch.BatchManualLaunchOperationError;
import com.epam.reportportal.core.tms.dto.batch.BatchManualLaunchOperationResultRS;
import com.epam.reportportal.core.tms.dto.batch.BatchTestCaseOperationError;
import com.epam.reportportal.core.tms.dto.batch.BatchTestCaseOperationResultRS;
import com.epam.reportportal.core.tms.mapper.TmsManualLaunchMapper;
import com.epam.reportportal.infrastructure.persistence.commons.querygen.Filter;
import com.epam.reportportal.infrastructure.persistence.dao.LaunchRepository;
import com.epam.reportportal.infrastructure.persistence.dao.tms.filterable.TmsManualLaunchFilterableRepository;
import com.epam.reportportal.infrastructure.persistence.entity.enums.LaunchTypeEnum;
import com.epam.reportportal.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.model.Page;
import com.epam.reportportal.util.OffsetRequest;
import com.epam.reportportal.ws.converter.PagedResourcesAssembler;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
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

  private final LaunchRepository launchRepository;
  private final TmsManualLaunchFilterableRepository tmsManualLaunchFilterableRepository;
  private final TmsManualLaunchMapper tmsManualLaunchMapper;
  private final TmsManualLaunchAttributeService tmsManualLaunchAttributeService;
  private final TestItemService testItemService;
  private final TmsTestFolderService tmsTestFolderService;

  private TmsTestCaseExecutionService tmsTestCaseExecutionService;

  @Autowired
  public void setTmsTestCaseExecutionService(
      TmsTestCaseExecutionService tmsTestCaseExecutionService) {
    this.tmsTestCaseExecutionService = tmsTestCaseExecutionService;
  }

  @Override
  @Transactional
  public TmsManualLaunchRS create(long projectId, TmsManualLaunchRQ request) {
    log.debug("Creating manual launch for project: {}", projectId);

    // Create Launch entity
    var launch = tmsManualLaunchMapper.convertFromRQ(projectId, request);
    launch.setLaunchType(LaunchTypeEnum.MANUAL);
    launch = launchRepository.save(launch);

    // Create attributes if present
    if (CollectionUtils.isNotEmpty(request.getAttributes())) {
      tmsManualLaunchAttributeService.createAttributes(launch, request.getAttributes());
    }

    // Add test cases to launch if present
    if (CollectionUtils.isNotEmpty(request.getTestCaseIds())) {
      tmsTestCaseExecutionService.addTestCasesToLaunch(projectId, launch, request.getTestCaseIds());
    }

    log.info("Created manual launch with ID: {} for project: {}", launch.getId(), projectId);

    return tmsManualLaunchMapper.convert(launch);
  }

  @Override
  @Transactional(readOnly = true)
  public TmsManualLaunchRS getById(long projectId, Long launchId) {
    log.debug("Getting manual launch by ID: {} for project: {}", launchId, projectId);

    var launch = launchRepository.findByIdAndProjectId(launchId, projectId)
        .orElseThrow(() -> new ReportPortalException(
            NOT_FOUND, LAUNCH_NOT_FOUND_BY_ID.formatted(launchId, projectId))
        );

    return tmsManualLaunchMapper.convert(launch);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<TmsManualLaunchRS> getManualLaunches(
      long projectId, Filter filter, Pageable pageable) {
    log.debug("Getting manual launches for project: {}", projectId);

    var launchesPage = tmsManualLaunchFilterableRepository.findByProjectIdAndFilter(
        projectId, filter, pageable
    );

    if (launchesPage.hasContent()) {
      var launchResponses = launchesPage.getContent()
          .stream()
          .map(tmsManualLaunchMapper::convert)
          .toList();

      return PagedResourcesAssembler
          .<TmsManualLaunchRS>pageConverter()
          .apply(new PageImpl<>(launchResponses, pageable, launchesPage.getTotalElements()));
    } else {
      return PagedResourcesAssembler
          .<TmsManualLaunchRS>pageConverter()
          .apply(new PageImpl<>(Collections.emptyList(), pageable, 0));
    }
  }

  @Override
  @Transactional
  public void delete(long projectId, Long launchId) {
    log.debug("Deleting manual launch: {} for project: {}", launchId, projectId);

    var launch = launchRepository
        .findByIdAndProjectId(launchId, projectId)
        .orElseThrow(() -> new ReportPortalException(
            NOT_FOUND, LAUNCH_NOT_FOUND_BY_ID.formatted(launchId, projectId))
        );

    // Delete test case executions (includes launch-test case associations)
    tmsTestCaseExecutionService.deleteByLaunchId(launchId);

    // Delete test items
    testItemService.deleteByLaunchId(projectId, launchId);

    // Delete attributes
    tmsManualLaunchAttributeService.deleteAllByLaunchId(launchId);

    // Delete launch
    launchRepository.delete(launch);

    log.info("Deleted manual launch: {} for project: {}", launchId, projectId);
  }

  @Override
  @Transactional
  public TmsManualLaunchRS patch(long projectId, Long launchId, TmsManualLaunchRQ request) {
    log.debug("Patching manual launch: {} for project: {}", launchId, projectId);

    var existingLaunch = launchRepository
        .findByIdAndProjectId(launchId, projectId)
        .orElseThrow(() -> new ReportPortalException(
            NOT_FOUND, LAUNCH_NOT_FOUND_BY_ID.formatted(launchId, projectId))
        );

    // Patch launch fields
    tmsManualLaunchMapper.patch(existingLaunch, request);
    existingLaunch = launchRepository.save(existingLaunch);

    // Patch attributes if present
    if (request.getAttributes() != null) {
      tmsManualLaunchAttributeService.updateAttributes(existingLaunch, request.getAttributes());
    }

    // Add test cases if present (don't remove existing ones)
    if (CollectionUtils.isNotEmpty(request.getTestCaseIds())) {
      tmsTestCaseExecutionService.addTestCasesToLaunch(projectId, existingLaunch, request.getTestCaseIds());
    }

    log.info("Patched manual launch: {} for project: {}", launchId, projectId);

    return tmsManualLaunchMapper.convert(existingLaunch);
  }

  @Override
  @Transactional
  public void addTestCaseToLaunch(Long projectId, Long launchId, AddTestCaseToLaunchRQ request) {

    // Verify launch exists and belongs to project
    var launch = launchRepository.findByIdAndProjectId(launchId, projectId)
        .orElseThrow(() -> new ReportPortalException(
            NOT_FOUND, LAUNCH_NOT_FOUND_BY_ID.formatted(launchId, projectId))
        );

    var testCaseId = request.getTestCaseId();

    // Check if test case already in launch
    if (tmsTestCaseExecutionService.isTestCaseInLaunch(testCaseId, launchId)) {
      throw new ReportPortalException(
          BAD_REQUEST_ERROR,
          TEST_CASE_ALREADY_IN_LAUNCH.formatted(testCaseId, launchId)
      );
    }

    // Add a test case through execution service (creates execution and test item)
    tmsTestCaseExecutionService.addTestCasesToLaunch(projectId, launch, List.of(testCaseId));

    log.info("Added test case: {} to launch: {}", testCaseId, launchId);
  }

  @Override
  @Transactional
  public BatchTestCaseOperationResultRS addTestCasesToLaunch(Long projectId, Long launchId, BatchAddTestCasesToLaunchRQ request) {

    // Verify launch exists and belongs to project
    var launch = launchRepository.findByIdAndProjectId(launchId, projectId)
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
        if (tmsTestCaseExecutionService.isTestCaseInLaunch(testCaseId, launchId)) {
          log.warn("Test case {} already exists in launch {}", testCaseId, launchId);
          errors.add(new BatchTestCaseOperationError(
              testCaseId,
              "Test case already exists in launch"
          ));
          continue;
        }

        // Add a test case through execution service (creates execution and test item)
        tmsTestCaseExecutionService.addTestCasesToLaunch(projectId, launch, List.of(testCaseId));
        successTestCaseIds.add(testCaseId);

      } catch (Exception e) {
        log.error("Failed to add test case {} to launch {}", testCaseId, launchId, e);
        errors.add(new BatchTestCaseOperationError(
            testCaseId,
            e.getMessage()
        ));
      }
    }

    var result = tmsManualLaunchMapper.convertBatchAddTestCaseOperationResultRS(testCaseIds, successTestCaseIds, errors);

    log.info("Added test cases to launch: {} - success: {}, failed: {}",
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
    if (!tmsTestCaseExecutionService.existsByTestCaseExecutionIdAndLaunchId(executionId, launchId)) {
      throw new ReportPortalException(
          NOT_FOUND,
          TEST_CASE_EXECUTION_IN_LAUNCH.formatted(executionId, launchId)
      );
    }

    // Delegate to execution service
    tmsTestCaseExecutionService.removeTestCaseExecutionFromLaunch(projectId, launchId, executionId);

    log.info("Test case execution: {} deleted from launch: {} in project: {}",
        executionId, launchId, projectId);
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

  /**
   * Gets unique folders from test cases in launch.
   *
   * @param projectId project ID
   * @param launchId  launch ID
   * @param pageable  pagination parameters
   * @return page of test folders
   */
  @Override
  @Transactional(readOnly = true)
  public Page<TmsTestFolderRS> getLaunchFolders(Long projectId, Long launchId, Pageable pageable) {
    log.debug("Getting folders for launch: {} in project: {}", launchId, projectId);

    // Validate launch belongs to project
    validateLaunchBelongsToProject(launchId, projectId);

    // Get unique folders from test case executions
    return tmsTestFolderService.getFoldersByLaunchIdWithTestCaseCount(projectId,
        launchId, pageable
    );
  }

  @Override
  @Transactional
  public BatchManualLaunchOperationResultRS batchDeleteManualLaunches(Long projectId, BatchDeleteManualLaunchesRQ request) {
    log.debug("Batch deleting manual launches for project: {}", projectId);

    List<Long> successLaunchIds = new ArrayList<>();
    List<BatchManualLaunchOperationError> errors = new ArrayList<>();

    var launchIds = request.getLaunchIds();

    // Try to delete each launch
    for (var launchId : launchIds) {
      try {
        // Check if launch exists and belongs to project
        var launch = launchRepository.findByIdAndProjectId(launchId, projectId);
        if (launch.isEmpty()) {
          log.warn("Launch {} not found in project {}", launchId, projectId);
          errors.add(new BatchManualLaunchOperationError(
              launchId,
              "Launch not found in project"
          ));
          continue;
        }

        var launchEntity = launch.get();

        // Check if it's manual launch
        if (launchEntity.getLaunchType() != LaunchTypeEnum.MANUAL) {
          log.warn("Launch {} is not manual launch", launchId);
          errors.add(new BatchManualLaunchOperationError(
              launchId,
              "Launch is not manual launch"
          ));
          continue;
        }

        // Delete test case executions (includes launch-test case associations)
        tmsTestCaseExecutionService.deleteByLaunchId(launchId);

        // Delete test items
        testItemService.deleteByLaunchId(projectId, launchId);

        // Delete attributes
        tmsManualLaunchAttributeService.deleteAllByLaunchId(launchId);

        // Delete launch
        launchRepository.delete(launchEntity);

        successLaunchIds.add(launchId);

      } catch (Exception e) {
        log.error("Failed to delete launch {} in project {}", launchId, projectId, e);
        errors.add(new BatchManualLaunchOperationError(
            launchId,
            e.getMessage()
        ));
      }
    }

    log.info("Batch deleted launches in project: {} - success: {}, failed: {}",
        projectId, successLaunchIds.size(), errors.size());

    return tmsManualLaunchMapper.convertToBatchDeleteResponse(
        launchIds, successLaunchIds, errors
    );
  }

  @Override
  public Optional<Long> getTestPlanIdByLaunchId(Long launchId) {
    return launchRepository.findTestPlanIdById(launchId);
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
