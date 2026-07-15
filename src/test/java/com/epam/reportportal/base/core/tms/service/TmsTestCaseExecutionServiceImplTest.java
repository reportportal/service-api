package com.epam.reportportal.base.core.tms.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.epam.reportportal.base.core.item.FinishTestItemHandler;
import com.epam.reportportal.base.core.item.UpdateTestItemHandler;
import com.epam.reportportal.base.core.tms.dto.TmsTestCaseExecutionCommentRQ;
import com.epam.reportportal.base.core.tms.dto.TmsTestCaseExecutionCommentRS;
import com.epam.reportportal.base.core.tms.dto.TmsTestCaseExecutionRQ;
import com.epam.reportportal.base.core.tms.dto.TmsTestCaseExecutionRS;
import com.epam.reportportal.base.core.tms.mapper.NestedStepItemBuilder;
import com.epam.reportportal.base.core.tms.mapper.TestCaseItemBuilder;
import com.epam.reportportal.base.core.tms.mapper.TmsTestCaseExecutionMapper;
import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.infrastructure.persistence.dao.TestItemRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsTestCaseExecutionRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.StatusEnum;
import com.epam.reportportal.base.infrastructure.persistence.entity.item.TestItem;
import com.epam.reportportal.base.infrastructure.persistence.entity.item.TestItemResults;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.MembershipDetails;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsStepExecution;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestCaseExecution;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.base.model.item.UpdateTestItemRQ;
import com.epam.reportportal.base.reporting.FinishTestItemRQ;
import java.util.Arrays;
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
class TmsTestCaseExecutionServiceImplTest {

  @Mock
  private TmsTestCaseExecutionRepository tmsTestCaseExecutionRepository;

  @Mock
  private TmsTestCaseExecutionCommentService tmsTestCaseExecutionCommentService;

  @Mock
  private UpdateTestItemHandler updateTestItemHandler;

  @Mock
  private FinishTestItemHandler finishTestItemHandler;

  @Mock
  private TmsTestCaseExecutionMapper tmsTestCaseExecutionMapper;

  @Mock
  private TestItemRepository testItemRepository;

  @Mock
  private TmsManualLaunchService tmsManualLaunchService;

  @Mock
  private TmsTestCaseService tmsTestCaseService;

  @Mock
  private TestCaseItemBuilder testCaseItemBuilder;

  @Mock
  private NestedStepItemBuilder nestedStepItemBuilder;

  @Mock
  private TmsStepExecutionService tmsStepExecutionService;

  @InjectMocks
  private TmsTestCaseExecutionServiceImpl sut;

  private TmsTestCaseExecution execution1;
  private TmsTestCaseExecution execution2;
  private TmsTestCaseExecution execution3;
  private TestItem testItem1;
  private TestItem testItem2;
  private TestItem testItem3;
  private Long testCaseId1;
  private Long testCaseId2;
  private Long testCaseId3;
  private Long testPlanId;

  @BeforeEach
  void setUp() {
    testCaseId1 = 1L;
    testCaseId2 = 2L;
    testCaseId3 = 3L;

    testPlanId = 100L;

    testItem1 = new TestItem();
    testItem1.setItemId(10L);
    testItem1.setName("Test Item 1");
    testItem1.setPath("1.10");
    var results1 = new TestItemResults();
    results1.setStatus(StatusEnum.TO_RUN);
    testItem1.setItemResults(results1);

    testItem2 = new TestItem();
    testItem2.setItemId(20L);
    testItem2.setName("Test Item 2");

    testItem3 = new TestItem();
    testItem3.setItemId(30L);
    testItem3.setName("Test Item 3");

    execution1 = TmsTestCaseExecution.builder()
        .id(100L)
        .testCaseId(testCaseId1)
        .testCaseVersionId(1L)
        .testItem(testItem1)
        .testCaseSnapshot("{\"name\": \"Test Case 1\"}")
        .build();

    execution2 = TmsTestCaseExecution.builder()
        .id(200L)
        .testCaseId(testCaseId2)
        .testCaseVersionId(2L)
        .testItem(testItem2)
        .testCaseSnapshot("{\"name\": \"Test Case 2\"}")
        .build();

    execution3 = TmsTestCaseExecution.builder()
        .id(300L)
        .testCaseId(testCaseId3)
        .testCaseVersionId(3L)
        .testItem(testItem3)
        .testCaseSnapshot("{\"name\": \"Test Case 3\"}")
        .build();

    sut.setTmsManualLaunchService(tmsManualLaunchService);
    sut.setTmsTestCaseService(tmsTestCaseService);
  }

  // ==================== patch: Active -> Active ====================

  @Test
  void patch_WhenActiveStatusChangesToAnotherActive_ShouldSaveDirectlyAndBypassHandlers() {
    // Given - current TO_RUN, target IN_PROGRESS → Active -> Active path
    var executionId = 100L;
    var launchId = 10L;
    var request = TmsTestCaseExecutionRQ.builder().status("IN_PROGRESS").build();
    var membershipDetails = new MembershipDetails();
    var user = mock(ReportPortalUser.class);

    when(tmsTestCaseExecutionRepository.findByTestCaseExecutionIdAndLaunchId(executionId, launchId))
        .thenReturn(Optional.of(execution1));
    when(testItemRepository.save(any(TestItem.class))).thenReturn(testItem1);
    when(tmsTestCaseExecutionRepository.save(execution1)).thenReturn(execution1);
    when(tmsTestCaseExecutionMapper.convert(execution1)).thenReturn(new TmsTestCaseExecutionRS());

    // When
    var result = sut.patch(membershipDetails, user, executionId, launchId, request);

    // Then
    assertNotNull(result);
    assertEquals(StatusEnum.IN_PROGRESS, testItem1.getItemResults().getStatus());
    verify(testItemRepository).save(testItem1);
    verifyNoInteractions(updateTestItemHandler);
    verifyNoInteractions(finishTestItemHandler);
    verifyNoInteractions(testCaseItemBuilder);
  }

  @Test
  void patch_WhenStatusSameAsCurrentStatus_ShouldDoNothingAndSkipAllHandlers() {
    // Given - current TO_RUN, target TO_RUN → same status, nothing should happen
    var executionId = 100L;
    var launchId = 10L;
    var request = TmsTestCaseExecutionRQ.builder().status("TO_RUN").build();
    var membershipDetails = new MembershipDetails();
    var user = mock(ReportPortalUser.class);

    when(tmsTestCaseExecutionRepository.findByTestCaseExecutionIdAndLaunchId(executionId, launchId))
        .thenReturn(Optional.of(execution1));
    when(tmsTestCaseExecutionRepository.save(execution1)).thenReturn(execution1);
    when(tmsTestCaseExecutionMapper.convert(execution1)).thenReturn(new TmsTestCaseExecutionRS());

    // When
    var result = sut.patch(membershipDetails, user, executionId, launchId, request);

    // Then
    assertNotNull(result);
    verifyNoInteractions(updateTestItemHandler);
    verifyNoInteractions(finishTestItemHandler);
    verifyNoInteractions(testCaseItemBuilder);
    verify(testItemRepository, never()).save(any());
  }

  // ==================== patch: Active -> Terminal ====================

  // ==================== patch: Terminal -> Active (RETRY) ====================

  // ==================== patch: Terminal -> Terminal ====================

  @Test
  void patch_WhenBothStatusesAreTerminal_ShouldSaveDirectlyAndBypassHandlers() {
    // Given - current FAILED, target PASSED → Terminal -> Terminal path
    var executionId = 100L;
    var launchId = 10L;
    var request = TmsTestCaseExecutionRQ.builder().status("PASSED").build();
    var membershipDetails = new MembershipDetails();
    var user = mock(ReportPortalUser.class);

    testItem1.getItemResults().setStatus(StatusEnum.FAILED);

    when(tmsTestCaseExecutionRepository.findByTestCaseExecutionIdAndLaunchId(executionId, launchId))
        .thenReturn(Optional.of(execution1));
    when(testItemRepository.save(any(TestItem.class))).thenReturn(testItem1);
    when(tmsTestCaseExecutionRepository.save(execution1)).thenReturn(execution1);
    when(tmsTestCaseExecutionMapper.convert(execution1)).thenReturn(new TmsTestCaseExecutionRS());

    // When
    var result = sut.patch(membershipDetails, user, executionId, launchId, request);

    // Then
    assertNotNull(result);
    assertEquals(StatusEnum.PASSED, testItem1.getItemResults().getStatus());
    verify(testItemRepository).save(testItem1);
    verifyNoInteractions(updateTestItemHandler);
    verifyNoInteractions(finishTestItemHandler);
    verifyNoInteractions(testCaseItemBuilder);
  }

  // ==================== patch: execution not found ====================

  @Test
  void patch_WhenExecutionNotFound_ShouldThrowReportPortalException() {
    // Given
    var executionId = 999L;
    var launchId = 10L;
    var request = TmsTestCaseExecutionRQ.builder().status("FAILED").build();
    var membershipDetails = new MembershipDetails();
    var user = mock(ReportPortalUser.class);

    when(tmsTestCaseExecutionRepository.findByTestCaseExecutionIdAndLaunchId(executionId, launchId))
        .thenReturn(Optional.empty());

    // When / Then
    assertThrows(
        ReportPortalException.class,
        () -> sut.patch(membershipDetails, user, executionId, launchId, request)
    );
  }

  // ==================== getLastTestCasesExecutionsByTestCaseIds ====================

  @Test
  void getLastTestCasesExecutionsByTestCaseIds_WithMultipleExecutions_ShouldReturnMapWithAllExecutions() {
    // Given
    var testCaseIds = Arrays.asList(testCaseId1, testCaseId2, testCaseId3);
    var executions = Arrays.asList(execution1, execution2, execution3);

    when(tmsTestCaseExecutionRepository.findLastExecutionsByTestCaseIds(testCaseIds))
        .thenReturn(executions);

    // When
    var result = sut.getLastTestCasesExecutionsByTestCaseIds(testCaseIds);

    // Then
    assertNotNull(result);
    assertEquals(3, result.size());
    assertEquals(execution1, result.get(testCaseId1));
    assertEquals(execution2, result.get(testCaseId2));
    assertEquals(execution3, result.get(testCaseId3));
    verify(tmsTestCaseExecutionRepository).findLastExecutionsByTestCaseIds(testCaseIds);
  }

  @Test
  void getLastTestCasesExecutionsByTestCaseIds_WithSingleExecution_ShouldReturnMapWithOneEntry() {
    // Given
    var testCaseIds = List.of(testCaseId1);
    var executions = List.of(execution1);

    when(tmsTestCaseExecutionRepository.findLastExecutionsByTestCaseIds(testCaseIds))
        .thenReturn(executions);

    // When
    var result = sut.getLastTestCasesExecutionsByTestCaseIds(testCaseIds);

    // Then
    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(execution1, result.get(testCaseId1));
    verify(tmsTestCaseExecutionRepository).findLastExecutionsByTestCaseIds(testCaseIds);
  }

  @Test
  void getLastTestCasesExecutionsByTestCaseIds_WhenRepositoryReturnsNull_ShouldReturnEmptyMap() {
    // Given
    var testCaseIds = Arrays.asList(testCaseId1, testCaseId2);

    when(tmsTestCaseExecutionRepository.findLastExecutionsByTestCaseIds(testCaseIds))
        .thenReturn(null);

    // When
    var result = sut.getLastTestCasesExecutionsByTestCaseIds(testCaseIds);

    // Then
    assertNotNull(result);
    assertTrue(result.isEmpty());
    verify(tmsTestCaseExecutionRepository).findLastExecutionsByTestCaseIds(testCaseIds);
  }

  @Test
  void getLastTestCasesExecutionsByTestCaseIds_WhenRepositoryReturnsEmptyList_ShouldReturnEmptyMap() {
    // Given
    var testCaseIds = Arrays.asList(testCaseId1, testCaseId2);
    var emptyExecutions = Collections.<TmsTestCaseExecution>emptyList();

    when(tmsTestCaseExecutionRepository.findLastExecutionsByTestCaseIds(testCaseIds))
        .thenReturn(emptyExecutions);

    // When
    var result = sut.getLastTestCasesExecutionsByTestCaseIds(testCaseIds);

    // Then
    assertNotNull(result);
    assertTrue(result.isEmpty());
    verify(tmsTestCaseExecutionRepository).findLastExecutionsByTestCaseIds(testCaseIds);
  }

  @Test
  void getLastTestCasesExecutionsByTestCaseIds_WithEmptyTestCaseIdsList_ShouldReturnEmptyMap() {
    // Given
    var emptyTestCaseIds = Collections.<Long>emptyList();
    var emptyExecutions = Collections.<TmsTestCaseExecution>emptyList();

    when(tmsTestCaseExecutionRepository.findLastExecutionsByTestCaseIds(emptyTestCaseIds))
        .thenReturn(emptyExecutions);

    // When
    var result = sut.getLastTestCasesExecutionsByTestCaseIds(emptyTestCaseIds);

    // Then
    assertNotNull(result);
    assertTrue(result.isEmpty());
    verify(tmsTestCaseExecutionRepository).findLastExecutionsByTestCaseIds(emptyTestCaseIds);
  }

  @Test
  void getLastTestCasesExecutionsByTestCaseIds_WithPartialResults_ShouldReturnOnlyAvailableExecutions() {
    // Given - requesting 3 IDs, but receiving only 2 executions
    var testCaseIds = Arrays.asList(testCaseId1, testCaseId2, testCaseId3);
    var executions = Arrays.asList(execution1, execution2);

    when(tmsTestCaseExecutionRepository.findLastExecutionsByTestCaseIds(testCaseIds))
        .thenReturn(executions);

    // When
    var result = sut.getLastTestCasesExecutionsByTestCaseIds(testCaseIds);

    // Then
    assertNotNull(result);
    assertEquals(2, result.size());
    assertEquals(execution1, result.get(testCaseId1));
    assertEquals(execution2, result.get(testCaseId2));
    assertNull(result.get(testCaseId3));
    verify(tmsTestCaseExecutionRepository).findLastExecutionsByTestCaseIds(testCaseIds);
  }

  // ==================== getLastTestCaseExecution ====================

  @Test
  void getLastTestCaseExecution_WhenExecutionExists_ShouldReturnExecution() {
    // Given
    when(tmsTestCaseExecutionRepository.findLastExecutionByTestCaseId(testCaseId1))
        .thenReturn(Optional.of(execution1));

    // When
    var result = sut.getLastTestCaseExecution(testCaseId1);

    // Then
    assertNotNull(result);
    assertEquals(execution1, result);
    assertEquals(testCaseId1, result.getTestCaseId());
    assertEquals(100L, result.getId());
    verify(tmsTestCaseExecutionRepository).findLastExecutionByTestCaseId(testCaseId1);
  }

  @Test
  void getLastTestCaseExecution_WhenExecutionDoesNotExist_ShouldReturnNull() {
    // Given
    var nonExistentTestCaseId = 999L;

    when(tmsTestCaseExecutionRepository.findLastExecutionByTestCaseId(nonExistentTestCaseId))
        .thenReturn(Optional.empty());

    // When
    var result = sut.getLastTestCaseExecution(nonExistentTestCaseId);

    // Then
    assertNull(result);
    verify(tmsTestCaseExecutionRepository).findLastExecutionByTestCaseId(nonExistentTestCaseId);
  }

  @Test
  void getLastTestCaseExecution_WithValidTestCaseId_ShouldReturnCorrectExecution() {
    // Given
    when(tmsTestCaseExecutionRepository.findLastExecutionByTestCaseId(testCaseId2))
        .thenReturn(Optional.of(execution2));

    // When
    var result = sut.getLastTestCaseExecution(testCaseId2);

    // Then
    assertNotNull(result);
    assertEquals(execution2, result);
    assertEquals(testCaseId2, result.getTestCaseId());
    assertEquals(200L, result.getId());
    assertEquals(testItem2, result.getTestItem());
    verify(tmsTestCaseExecutionRepository).findLastExecutionByTestCaseId(testCaseId2);
  }

  @Test
  void getLastTestCaseExecution_WithNullTestCaseId_ShouldCallRepositoryAndReturnNull() {
    // Given
    when(tmsTestCaseExecutionRepository.findLastExecutionByTestCaseId(null))
        .thenReturn(Optional.empty());

    // When
    var result = sut.getLastTestCaseExecution(null);

    // Then
    assertNull(result);
    verify(tmsTestCaseExecutionRepository).findLastExecutionByTestCaseId(null);
  }

  @Test
  void getLastTestCaseExecution_VerifyExecutionProperties_ShouldReturnCompleteExecution() {
    // Given
    var executionWithAllProperties = TmsTestCaseExecution.builder()
        .id(999L)
        .testCaseId(testCaseId1)
        .testCaseVersionId(5L)
        .testItem(testItem1)
        .testCaseSnapshot("{\"name\": \"Complete Test Case\", \"description\": \"Full description\"}")
        .build();

    when(tmsTestCaseExecutionRepository.findLastExecutionByTestCaseId(testCaseId1))
        .thenReturn(Optional.of(executionWithAllProperties));

    // When
    var result = sut.getLastTestCaseExecution(testCaseId1);

    // Then
    assertNotNull(result);
    assertEquals(999L, result.getId());
    assertEquals(testCaseId1, result.getTestCaseId());
    assertEquals(5L, result.getTestCaseVersionId());
    assertEquals(testItem1, result.getTestItem());
    assertNotNull(result.getTestCaseSnapshot());
    assertTrue(result.getTestCaseSnapshot().contains("Complete Test Case"));
    verify(tmsTestCaseExecutionRepository).findLastExecutionByTestCaseId(testCaseId1);
  }

  // ==================== findLastExecutionsByTestCaseIdsAndTestPlanId ====================

  @Test
  void findLastExecutionsByTestCaseIdsAndTestPlanId_WithEmptyList_ShouldReturnEmptyMap() {
    // Given
    var emptyTestCaseIds = Collections.<Long>emptyList();

    // When
    var result = sut.findLastExecutionsByTestCaseIdsAndTestPlanId(emptyTestCaseIds, testPlanId);

    // Then
    assertNotNull(result);
    assertTrue(result.isEmpty());
    verifyNoInteractions(tmsTestCaseExecutionRepository);
  }

  @Test
  void findLastExecutionsByTestCaseIdsAndTestPlanId_WithNullList_ShouldReturnEmptyMap() {
    // Given
    List<Long> nullTestCaseIds = null;

    // When
    var result = sut.findLastExecutionsByTestCaseIdsAndTestPlanId(nullTestCaseIds, testPlanId);

    // Then
    assertNotNull(result);
    assertTrue(result.isEmpty());
    verifyNoInteractions(tmsTestCaseExecutionRepository);
  }

  // ==================== findByTestCaseIdAndTestPlanId ====================

  @Test
  void findByTestCaseIdAndTestPlanId_WithExecutions_ShouldReturnList() {
    // Given
    var executions = Arrays.asList(execution1, execution2);

    when(tmsTestCaseExecutionRepository.findByTestCaseIdAndTestPlanId(testCaseId1, testPlanId))
        .thenReturn(executions);

    // When
    var result = sut.findByTestCaseIdAndTestPlanId(testCaseId1, testPlanId);

    // Then
    assertNotNull(result);
    assertEquals(2, result.size());
    assertEquals(execution1, result.get(0));
    assertEquals(execution2, result.get(1));
    verify(tmsTestCaseExecutionRepository).findByTestCaseIdAndTestPlanId(testCaseId1, testPlanId);
  }

  @Test
  void findByTestCaseIdAndTestPlanId_WithNullTestCaseId_ShouldReturnEmptyList() {
    // Given
    Long nullTestCaseId = null;

    // When
    var result = sut.findByTestCaseIdAndTestPlanId(nullTestCaseId, testPlanId);

    // Then
    assertNotNull(result);
    assertTrue(result.isEmpty());
    verifyNoInteractions(tmsTestCaseExecutionRepository);
  }

  @Test
  void findByTestCaseIdAndTestPlanId_WithNullTestPlanId_ShouldReturnEmptyList() {
    // Given
    Long nullTestPlanId = null;

    // When
    var result = sut.findByTestCaseIdAndTestPlanId(testCaseId1, nullTestPlanId);

    // Then
    assertNotNull(result);
    assertTrue(result.isEmpty());
    verifyNoInteractions(tmsTestCaseExecutionRepository);
  }

  @Test
  void findByTestCaseIdAndTestPlanId_WithNoExecutions_ShouldReturnEmptyList() {
    // Given
    when(tmsTestCaseExecutionRepository.findByTestCaseIdAndTestPlanId(testCaseId1, testPlanId))
        .thenReturn(Collections.emptyList());

    // When
    var result = sut.findByTestCaseIdAndTestPlanId(testCaseId1, testPlanId);

    // Then
    assertNotNull(result);
    assertTrue(result.isEmpty());
    verify(tmsTestCaseExecutionRepository).findByTestCaseIdAndTestPlanId(testCaseId1, testPlanId);
  }

  // ==================== addTestCasesToLaunch ====================

  @Test
  void addTestCasesToLaunch_WithEmptyList_ShouldReturnEmptyResult() {
    // Given
    var launch = new com.epam.reportportal.base.infrastructure.persistence.entity.launch.Launch();
    launch.setId(10L);

    // When
    var result = sut.addTestCasesToLaunch(1L, launch, Collections.emptyList());

    // Then
    assertNotNull(result);
    assertEquals(0, result.getTotalCount());
    assertTrue(result.getSuccessTestCaseIds().isEmpty());
    assertTrue(result.getErrors().isEmpty());
  }

  // ==================== isTestCaseInLaunch ====================

  @Test
  void isTestCaseInLaunch_WhenExists_ShouldReturnTrue() {
    // Given
    when(tmsTestCaseExecutionRepository.existsByTestCaseIdAndLaunchId(testCaseId1, 10L))
        .thenReturn(true);

    // When
    var result = sut.isTestCaseInLaunch(testCaseId1, 10L);

    // Then
    assertTrue(result);
    verify(tmsTestCaseExecutionRepository).existsByTestCaseIdAndLaunchId(testCaseId1, 10L);
  }

  // ==================== patchTestCaseExecutionComment ====================

  @Test
  void patchTestCaseExecutionComment_WhenValid_ShouldCallCommentService() {
    // Given
    var projectId = 1L;
    var launchId = 10L;
    var executionId = 100L;
    var request = new TmsTestCaseExecutionCommentRQ();
    request.setComment("Patched");

    var response = new TmsTestCaseExecutionCommentRS();

    when(tmsTestCaseExecutionRepository.findByTestCaseExecutionIdAndLaunchId(executionId, launchId))
        .thenReturn(Optional.of(execution1));
    when(tmsTestCaseExecutionCommentService.patchTestCaseExecutionComment(execution1, request))
        .thenReturn(response);

    // When
    var result = sut.patchTestCaseExecutionComment(projectId, launchId, executionId, request);

    // Then
    assertNotNull(result);
    verify(tmsTestCaseExecutionRepository).findByTestCaseExecutionIdAndLaunchId(executionId, launchId);
    verify(tmsTestCaseExecutionCommentService).patchTestCaseExecutionComment(execution1, request);
  }

  @Test
  void patchTestCaseExecutionComment_WhenExecutionNotFound_ShouldThrowReportPortalException() {
    // Given
    var projectId = 1L;
    var launchId = 10L;
    var executionId = 999L;
    var request = new TmsTestCaseExecutionCommentRQ();

    when(tmsTestCaseExecutionRepository.findByTestCaseExecutionIdAndLaunchId(executionId, launchId))
        .thenReturn(Optional.empty());

    // When / Then
    assertThrows(
        ReportPortalException.class,
        () -> sut.patchTestCaseExecutionComment(projectId, launchId, executionId, request)
    );
  }
}