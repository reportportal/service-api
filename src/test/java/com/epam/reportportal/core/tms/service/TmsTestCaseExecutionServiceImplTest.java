package com.epam.reportportal.core.tms.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.epam.reportportal.infrastructure.persistence.dao.tms.TmsTestCaseExecutionRepository;
import com.epam.reportportal.infrastructure.persistence.entity.item.TestItem;
import com.epam.reportportal.infrastructure.persistence.entity.tms.TmsTestCaseExecution;
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
  }

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
  void getLastTestCaseExecution_WithNullTestCaseId_ShouldCallRepository() {
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
  void getLastTestCasesExecutionsByTestCaseIds_WithMixedTestCaseIds_ShouldReturnCorrectMapping() {
    // Given
    var testCaseIds = Arrays.asList(1L, 5L, 10L, 15L, 20L);
    var executions = Arrays.asList(
        TmsTestCaseExecution.builder().id(1L).testCaseId(1L).testItem(testItem1).testCaseSnapshot("{}").build(),
        TmsTestCaseExecution.builder().id(2L).testCaseId(5L).testItem(testItem2).testCaseSnapshot("{}").build(),
        TmsTestCaseExecution.builder().id(3L).testCaseId(10L).testItem(testItem3).testCaseSnapshot("{}").build()
    );

    when(tmsTestCaseExecutionRepository.findLastExecutionsByTestCaseIds(testCaseIds))
        .thenReturn(executions);

    // When
    var result = sut.getLastTestCasesExecutionsByTestCaseIds(testCaseIds);

    // Then
    assertNotNull(result);
    assertEquals(3, result.size());
    assertTrue(result.containsKey(1L));
    assertTrue(result.containsKey(5L));
    assertTrue(result.containsKey(10L));
    assertNull(result.get(15L));
    assertNull(result.get(20L));
    verify(tmsTestCaseExecutionRepository).findLastExecutionsByTestCaseIds(testCaseIds);
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

  @Test
  void getLastTestCasesExecutionsByTestCaseIds_WithLargeNumberOfIds_ShouldHandleCorrectly() {
    // Given - test with large number of IDs
    var largeTestCaseIdsList = Arrays.asList(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L);
    var executions = Arrays.asList(
        TmsTestCaseExecution.builder().id(1L).testCaseId(1L).testItem(testItem1).testCaseSnapshot("{}").build(),
        TmsTestCaseExecution.builder().id(2L).testCaseId(2L).testItem(testItem2).testCaseSnapshot("{}").build(),
        TmsTestCaseExecution.builder().id(3L).testCaseId(3L).testItem(testItem3).testCaseSnapshot("{}").build()
    );

    when(tmsTestCaseExecutionRepository.findLastExecutionsByTestCaseIds(largeTestCaseIdsList))
        .thenReturn(executions);

    // When
    var result = sut.getLastTestCasesExecutionsByTestCaseIds(largeTestCaseIdsList);

    // Then
    assertNotNull(result);
    assertEquals(3, result.size());
    assertEquals(1L, result.get(1L).getId());
    assertEquals(2L, result.get(2L).getId());
    assertEquals(3L, result.get(3L).getId());
    verify(tmsTestCaseExecutionRepository).findLastExecutionsByTestCaseIds(largeTestCaseIdsList);
  }

  @Test
  void getLastTestCasesExecutionsByTestCaseIds_WhenMapContainsAllRequestedIds_ShouldReturnCompleteMap() {
    // Given - case when all requested IDs have executions
    var testCaseIds = Arrays.asList(testCaseId1, testCaseId2);
    var executions = Arrays.asList(execution1, execution2);

    when(tmsTestCaseExecutionRepository.findLastExecutionsByTestCaseIds(testCaseIds))
        .thenReturn(executions);

    // When
    var result = sut.getLastTestCasesExecutionsByTestCaseIds(testCaseIds);

    // Then
    assertNotNull(result);
    assertEquals(testCaseIds.size(), result.size());
    for (Long testCaseId : testCaseIds) {
      assertTrue(result.containsKey(testCaseId));
      assertNotNull(result.get(testCaseId));
    }
    verify(tmsTestCaseExecutionRepository).findLastExecutionsByTestCaseIds(testCaseIds);
  }

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
}
