package com.epam.reportportal.base.core.tms.service;

import static com.epam.reportportal.base.infrastructure.rules.exception.ErrorType.BAD_REQUEST_ERROR;
import static com.epam.reportportal.base.infrastructure.rules.exception.ErrorType.LAUNCH_NOT_FOUND;
import static com.epam.reportportal.base.infrastructure.rules.exception.ErrorType.NOT_FOUND;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.reportportal.base.core.item.TestItemService;
import com.epam.reportportal.base.core.launch.DeleteLaunchHandler;
import com.epam.reportportal.base.core.tms.dto.AddTestCaseToLaunchRQ;
import com.epam.reportportal.base.core.tms.dto.CreateTmsManualLaunchRQ;
import com.epam.reportportal.base.core.tms.dto.CreateTmsManualLaunchRS;
import com.epam.reportportal.base.core.tms.dto.TmsManualLaunchExecutionStatisticRS;
import com.epam.reportportal.base.core.tms.dto.TmsManualLaunchRS;
import com.epam.reportportal.base.core.tms.dto.TmsManualLaunchTestPlanRQ;
import com.epam.reportportal.base.core.tms.dto.TmsTestCaseExecutionCommentRQ;
import com.epam.reportportal.base.core.tms.dto.TmsTestCaseExecutionCommentRS;
import com.epam.reportportal.base.core.tms.dto.batch.BatchTestCaseOperationResultRS;
import com.epam.reportportal.base.core.tms.mapper.TmsManualLaunchMapper;
import com.epam.reportportal.base.core.user.GetUserHandler;
import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.infrastructure.persistence.dao.LaunchRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.filterable.TmsManualLaunchFilterableRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.LaunchTypeEnum;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.StatusEnum;
import com.epam.reportportal.base.infrastructure.persistence.entity.launch.Launch;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.MembershipDetails;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestPlan;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.User;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TmsManualLaunchServiceImplTest {

  private final long projectId = 1L;
  private final long launchId = 100L;
  private final long testCaseId = 200L;
  private final long testPlanId = 300L;
  @Mock
  private LaunchRepository launchRepository;
  @Mock
  private TmsManualLaunchFilterableRepository tmsManualLaunchFilterableRepository;
  @Mock
  private TmsManualLaunchMapper tmsManualLaunchMapper;
  @Mock
  private TmsManualLaunchAttributeService tmsManualLaunchAttributeService;
  @Mock
  private TestItemService testItemService;
  @Mock
  private DeleteLaunchHandler deleteLaunchHandler;
  @Mock
  private TestFolderItemServiceImpl testFolderItemService;
  @Mock
  private TmsStepExecutionService tmsStepExecutionService;
  @Mock
  private GetUserHandler getUserHandler;
  @Mock
  private TmsTestPlanService tmsTestPlanService;
  @Mock
  private TmsTestCaseExecutionService tmsTestCaseExecutionService;
  @Mock
  private TmsTestCaseService tmsTestCaseService;
  @InjectMocks
  private TmsManualLaunchServiceImpl sut;
  private MembershipDetails membershipDetails;
  private ReportPortalUser user;
  private Launch launch;
  private CreateTmsManualLaunchRQ createLaunchRQ;
  private CreateTmsManualLaunchRS createLaunchRS;
  private TmsManualLaunchRS launchRS;

  @BeforeEach
  void setUp() {
    membershipDetails = mock(MembershipDetails.class);
    user = mock(ReportPortalUser.class);
  
    launch = new Launch();
    launch.setId(launchId);
    launch.setProjectId(projectId);
    launch.setUserId(2L);
    launch.setTestPlanId(testPlanId);
    launch.setLaunchType(LaunchTypeEnum.MANUAL);

    createLaunchRQ = new CreateTmsManualLaunchRQ();
    createLaunchRQ.setName("Test Launch");

    createLaunchRS = new CreateTmsManualLaunchRS();
    createLaunchRS.setId(launchId);

    launchRS = new TmsManualLaunchRS();
    launchRS.setId(launchId);
    launchRS.setName("Test Launch");

    // Manual setting of dependencies that are injected via setters in the service
    sut.setTmsTestCaseExecutionService(tmsTestCaseExecutionService);
    sut.setTmsTestCaseService(tmsTestCaseService);
  }

  // -------------------------------------------------------------------------
  // CREATE
  // -------------------------------------------------------------------------

  @Test
  void create_WithValidData_ShouldCreateAndReturnLaunch() {
    createLaunchRQ.setTestCaseIds(List.of(testCaseId));

    when(tmsManualLaunchMapper.convertFromCreateTmsManualLaunchRQ(projectId, user, createLaunchRQ))
        .thenReturn(launch);
    when(launchRepository.save(launch)).thenReturn(launch);

    BatchTestCaseOperationResultRS batchResult = new BatchTestCaseOperationResultRS();
    when(tmsTestCaseExecutionService.addTestCasesToLaunch(projectId, launch, List.of(testCaseId)))
        .thenReturn(batchResult);
    when(tmsManualLaunchMapper.convertToCreateTmsManualLaunchRS(launch, batchResult))
        .thenReturn(createLaunchRS);

    var result = sut.create(projectId, user, createLaunchRQ);

    assertNotNull(result);
    assertEquals(launchId, result.getId());
    assertEquals(LaunchTypeEnum.MANUAL, launch.getLaunchType());
    assertEquals(StatusEnum.IN_PROGRESS, launch.getStatus());

    verify(tmsManualLaunchMapper).convertFromCreateTmsManualLaunchRQ(projectId, user,
        createLaunchRQ);
    verify(launchRepository).save(launch);
    verify(tmsTestCaseExecutionService).addTestCasesToLaunch(projectId, launch,
        List.of(testCaseId));
  }

  @Test
  void create_WithTestPlanAndEmptyTestCaseIds_ShouldFetchIdsFromTestPlan() {
    TmsManualLaunchTestPlanRQ planRQ = new TmsManualLaunchTestPlanRQ();
    planRQ.setId(testPlanId);
    createLaunchRQ.setTestPlan(planRQ);
    createLaunchRQ.setTestCaseIds(Collections.emptyList());

    when(tmsManualLaunchMapper.convertFromCreateTmsManualLaunchRQ(projectId, user, createLaunchRQ))
        .thenReturn(launch);
    when(launchRepository.save(launch)).thenReturn(launch);
    when(tmsTestPlanService.getTestCaseIdsAddedToPlan(projectId, testPlanId))
        .thenReturn(List.of(10L, 20L));

    BatchTestCaseOperationResultRS batchResult = new BatchTestCaseOperationResultRS();
    when(tmsTestCaseExecutionService.addTestCasesToLaunch(projectId, launch, List.of(10L, 20L)))
        .thenReturn(batchResult);
    when(tmsManualLaunchMapper.convertToCreateTmsManualLaunchRS(launch, batchResult))
        .thenReturn(createLaunchRS);

    var result = sut.create(projectId, user, createLaunchRQ);

    assertNotNull(result);
    verify(tmsTestPlanService).getTestCaseIdsAddedToPlan(projectId, testPlanId);
    verify(tmsTestCaseExecutionService).addTestCasesToLaunch(projectId, launch, List.of(10L, 20L));
  }

  // -------------------------------------------------------------------------
  // GET BY ID
  // -------------------------------------------------------------------------

  @Test
  void getById_WhenLaunchExists_ShouldReturnLaunch() {
    when(launchRepository.findManualLaunchByIdAndProjectId(launchId, projectId))
        .thenReturn(Optional.of(launch));

    var stats = new TmsManualLaunchExecutionStatisticRS();
    when(tmsTestCaseExecutionService.getTestCaseExecutionStatistic(launchId)).thenReturn(stats);

    var launchUser = new User();
    when(getUserHandler.getUserById(launch.getUserId())).thenReturn(launchUser);

    var testPlan = new TmsTestPlan();
    when(tmsTestPlanService.getEntityById(projectId, testPlanId)).thenReturn(testPlan);

    when(tmsManualLaunchMapper.convert(launch, stats, launchUser, testPlan)).thenReturn(launchRS);

    var result = sut.getById(projectId, launchId);

    assertNotNull(result);
    assertEquals(launchId, result.getId());
    verify(launchRepository).findManualLaunchByIdAndProjectId(launchId, projectId);
    verify(tmsTestCaseExecutionService).getTestCaseExecutionStatistic(launchId);
  }

  @Test
  void getById_WhenLaunchDoesNotExist_ShouldThrowNotFoundException() {
    when(launchRepository.findManualLaunchByIdAndProjectId(launchId, projectId))
        .thenReturn(Optional.empty());

    var exception = assertThrows(ReportPortalException.class,
        () -> sut.getById(projectId, launchId));
    assertEquals(NOT_FOUND, exception.getErrorType());

    verify(launchRepository).findManualLaunchByIdAndProjectId(launchId, projectId);
    verify(tmsTestCaseExecutionService, never()).getTestCaseExecutionStatistic(anyLong());
  }

  // -------------------------------------------------------------------------
  // DELETE
  // -------------------------------------------------------------------------

  @Test
  void delete_WhenLaunchExists_ShouldDeleteAllRelatedDataAndLaunch() {
    when(launchRepository.existsByIdAndProjectId(launchId, projectId)).thenReturn(true);
    when(membershipDetails.getProjectId()).thenReturn(projectId);
  
    sut.delete(membershipDetails, launchId, user);

    verify(launchRepository).existsByIdAndProjectId(launchId, projectId);
    verify(tmsStepExecutionService).deleteByLaunchId(launchId);
    verify(tmsTestCaseExecutionService).deleteByLaunchId(launchId);
    verify(testFolderItemService).deleteByLaunchId(launchId);
    verify(testItemService).deleteByLaunchId(projectId, launchId);
    verify(tmsManualLaunchAttributeService).deleteAllByLaunchId(launchId);
    verify(deleteLaunchHandler).deleteLaunch(launchId, membershipDetails, user);
  }

  @Test
  void delete_WhenLaunchDoesNotExist_ShouldThrowNotFoundException() {
    when(membershipDetails.getProjectId()).thenReturn(projectId);
    when(launchRepository.existsByIdAndProjectId(launchId, projectId)).thenReturn(false);
  
    var exception = assertThrows(ReportPortalException.class,
        () -> sut.delete(membershipDetails, launchId, user));
    assertEquals(NOT_FOUND, exception.getErrorType());
  
    verify(deleteLaunchHandler, never()).deleteLaunch(anyLong(), any(), any());
  }

  // -------------------------------------------------------------------------
  // ADD TEST CASE TO LAUNCH
  // -------------------------------------------------------------------------

  @Test
  void addTestCaseToLaunch_WhenValid_ShouldAddExecution() {
    AddTestCaseToLaunchRQ rq = new AddTestCaseToLaunchRQ();
    rq.setTestCaseId(testCaseId);

    when(launchRepository.findManualLaunchByIdAndProjectId(launchId, projectId))
        .thenReturn(Optional.of(launch));
    when(tmsTestCaseService.existsById(projectId, testCaseId)).thenReturn(true);
    when(tmsTestCaseExecutionService.isTestCaseInLaunch(testCaseId, launchId)).thenReturn(false);

    sut.addTestCaseToLaunch(projectId, launchId, rq);

    verify(launchRepository).findManualLaunchByIdAndProjectId(launchId, projectId);
    verify(tmsTestCaseService).existsById(projectId, testCaseId);
    verify(tmsTestCaseExecutionService).addTestCaseToLaunch(projectId, launch, testCaseId);
  }

  @Test
  void addTestCaseToLaunch_WhenTestCaseNotFound_ShouldThrowBadRequest() {
    AddTestCaseToLaunchRQ rq = new AddTestCaseToLaunchRQ();
    rq.setTestCaseId(testCaseId);
  
    when(launchRepository.findManualLaunchByIdAndProjectId(launchId, projectId))
        .thenReturn(Optional.of(launch));
    when(tmsTestCaseService.existsById(projectId, testCaseId)).thenReturn(false);
  
    var exception = assertThrows(ReportPortalException.class,
        () -> sut.addTestCaseToLaunch(projectId, launchId, rq));
    assertEquals(BAD_REQUEST_ERROR, exception.getErrorType());
  
    verify(tmsTestCaseExecutionService, never()).addTestCaseToLaunch(anyLong(), any(), anyLong());
  }
  
  @Test
  void addTestCaseToLaunch_WhenAlreadyInLaunch_ShouldThrowBadRequest() {
    AddTestCaseToLaunchRQ rq = new AddTestCaseToLaunchRQ();
    rq.setTestCaseId(testCaseId);
  
    when(launchRepository.findManualLaunchByIdAndProjectId(launchId, projectId))
        .thenReturn(Optional.of(launch));
    when(tmsTestCaseService.existsById(projectId, testCaseId)).thenReturn(true);
    when(tmsTestCaseExecutionService.isTestCaseInLaunch(testCaseId, launchId)).thenReturn(true);
  
    var exception = assertThrows(ReportPortalException.class,
        () -> sut.addTestCaseToLaunch(projectId, launchId, rq));
    assertEquals(BAD_REQUEST_ERROR, exception.getErrorType());
  
    verify(tmsTestCaseExecutionService, never()).addTestCaseToLaunch(anyLong(), any(), anyLong());
  }

  // -------------------------------------------------------------------------
  // COMMENTS
  // -------------------------------------------------------------------------

  @Test
  void putTestCaseExecutionComment_WhenValid_ShouldUpdateComment() {
    long executionId = 400L;
    TmsTestCaseExecutionCommentRQ commentRQ = new TmsTestCaseExecutionCommentRQ();
    commentRQ.setComment("New comment");
    TmsTestCaseExecutionCommentRS commentRS = new TmsTestCaseExecutionCommentRS();

    when(launchRepository.existsByIdAndProjectId(launchId, projectId)).thenReturn(true);
    when(tmsTestCaseExecutionService.putTestCaseExecutionComment(projectId, launchId, executionId,
        commentRQ))
        .thenReturn(commentRS);

    var result = sut.putTestCaseExecutionComment(projectId, launchId, executionId, commentRQ);

    assertNotNull(result);
    verify(launchRepository).existsByIdAndProjectId(launchId, projectId);
    verify(tmsTestCaseExecutionService).putTestCaseExecutionComment(projectId, launchId,
        executionId, commentRQ);
  }

  @Test
  void deleteTestCaseExecutionComment_WhenLaunchNotBelongsToProject_ShouldThrowException() {
    long executionId = 400L;
    when(launchRepository.existsByIdAndProjectId(launchId, projectId)).thenReturn(false);
  
    var exception = assertThrows(ReportPortalException.class,
        () -> sut.deleteTestCaseExecutionComment(projectId, launchId, executionId));
  
    assertEquals(LAUNCH_NOT_FOUND, exception.getErrorType());
    verify(tmsTestCaseExecutionService, never()).deleteTestCaseExecutionComment(anyLong(), anyLong(), anyLong());
  }
}
