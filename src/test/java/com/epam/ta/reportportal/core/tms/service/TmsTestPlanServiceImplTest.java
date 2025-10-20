package com.epam.ta.reportportal.core.tms.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.reportportal.rules.exception.ErrorType;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.core.tms.dto.DuplicateTmsTestPlanRS;
import com.epam.ta.reportportal.core.tms.dto.TmsTestPlanRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsTestPlanRS;
import com.epam.ta.reportportal.core.tms.dto.batch.BatchTestCaseOperationError;
import com.epam.ta.reportportal.core.tms.dto.batch.BatchTestCaseOperationResultRS;
import com.epam.ta.reportportal.core.tms.mapper.TmsTestPlanMapper;
import com.epam.ta.reportportal.dao.tms.TmsTestCaseRepository;
import com.epam.ta.reportportal.dao.tms.TmsTestPlanRepository;
import com.epam.ta.reportportal.dao.tms.TmsTestPlanTestCaseRepository;
import com.epam.ta.reportportal.dao.tms.filterable.TmsTestPlanFilterableRepository;
import com.epam.ta.reportportal.entity.tms.TmsTestPlan;
import com.epam.ta.reportportal.entity.tms.TmsTestPlanWithStatistic;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class TmsTestPlanServiceImplTest {

  @Mock
  private TmsTestPlanRepository testPlanRepository;

  @Mock
  private TmsTestPlanFilterableRepository tmsTestPlanFilterableRepository;

  @Mock
  private TmsTestPlanMapper tmsTestPlanMapper;

  @Mock
  private TmsTestPlanAttributeService tmsTestPlanAttributeService;

  @Mock
  private TmsTestPlanTestCaseRepository tmsTestPlanTestCaseRepository;

  @Mock
  private TmsTestCaseService tmsTestCaseService;

  @Mock
  private TmsTestCaseRepository tmsTestCaseRepository;

  @Mock
  private TmsTestPlanExecutionService tmsTestPlanExecutionService;

  @InjectMocks
  private TmsTestPlanServiceImpl sut;

  @Test
  void shouldGetByIdSuccess() {
    var projectId = 1L;
    var testPlanId = 2L;

    var testPlan = new TmsTestPlan();
    var testPlanWithStatistic = mock(TmsTestPlanWithStatistic.class);
    var testPlanRS = new TmsTestPlanRS();

    when(testPlanRepository.findByIdAndProjectId(testPlanId, projectId)).thenReturn(
        Optional.of(testPlan));
    when(tmsTestPlanExecutionService.enrichWithStatistics(testPlan)).thenReturn(
        testPlanWithStatistic);
    when(tmsTestPlanMapper.convertTmsTestPlanWithStatisticToRS(testPlanWithStatistic)).thenReturn(testPlanRS);

    var result = assertDoesNotThrow(() -> sut.getById(projectId, testPlanId));

    assertEquals(testPlanRS, result);

    verify(testPlanRepository).findByIdAndProjectId(testPlanId, projectId);
    verify(tmsTestPlanExecutionService).enrichWithStatistics(testPlan);
    verify(tmsTestPlanMapper).convertTmsTestPlanWithStatisticToRS(testPlanWithStatistic);
  }

  @Test
  void testGetByIdNotFound() {
    var projectId = 1L;
    var testPlanId = 2L;

    when(testPlanRepository.findByIdAndProjectId(testPlanId, projectId)).thenReturn(
        Optional.empty());

    var exception = assertThrows(ReportPortalException.class, () ->
        sut.getById(projectId, testPlanId)
    );

    assertEquals(exception.getErrorType(), ErrorType.NOT_FOUND);
    verify(testPlanRepository).findByIdAndProjectId(testPlanId, projectId);
    verify(tmsTestPlanExecutionService, never()).enrichWithStatistics(any());
  }

  @Test
  void shouldCreate() {
    var projectId = 1L;
    var testPlanRQ = new TmsTestPlanRQ();
    var testPlan = new TmsTestPlan();
    var testPlanRS = new TmsTestPlanRS();

    when(tmsTestPlanMapper.convertFromRQ(projectId, testPlanRQ)).thenReturn(testPlan);
    when(tmsTestPlanMapper.convertTmsTestPlanWithStatisticToRS(any(TmsTestPlanWithStatistic.class))).thenReturn(
        testPlanRS);

    var result = sut.create(projectId, testPlanRQ);

    assertEquals(testPlanRS, result);
    verify(tmsTestPlanMapper).convertFromRQ(projectId, testPlanRQ);
    verify(testPlanRepository).save(testPlan);
    verify(tmsTestPlanAttributeService).createTestPlanAttributes(testPlan,
        testPlanRQ.getAttributes());
    verify(tmsTestPlanMapper).convertTmsTestPlanWithStatisticToRS(any(TmsTestPlanWithStatistic.class));
  }

  @Test
  void shouldDelete() {
    var projectId = 1L;
    var testPlanId = 2L;

    assertDoesNotThrow(() -> sut.delete(projectId, testPlanId));

    verify(tmsTestPlanAttributeService).deleteAllByTestPlanId(testPlanId);
    verify(testPlanRepository).deleteByIdAndProjectId(testPlanId, projectId);
  }

  @Test
  void shouldGetByCriteria() {
    var projectId = 1L;
    var filter = mock(Filter.class);
    var pageable = PageRequest.of(0, 10);

    var testPlan = new TmsTestPlan();
    testPlan.setId(1L);

    var testPlanWithStatistic = mock(TmsTestPlanWithStatistic.class);
    var testPlanRS = new TmsTestPlanRS();
    testPlanRS.setId(1L);

    org.springframework.data.domain.Page<Long> testPlanIdsPage = new PageImpl<>(List.of(1L),
        pageable, 1);
    org.springframework.data.domain.Page<TmsTestPlanRS> testPlanPage = new PageImpl<>(
        List.of(testPlanRS), pageable, 1);

    when(tmsTestPlanFilterableRepository.findIdsByProjectIdAndFilter(projectId, filter, pageable))
        .thenReturn(testPlanIdsPage);
    when(testPlanRepository.findByIdsWithAttributes(List.of(1L))).thenReturn(List.of(testPlan));
    when(tmsTestPlanExecutionService.enrichWithStatistics(testPlan)).thenReturn(
        testPlanWithStatistic);
    when(tmsTestPlanMapper.convertTmsTestPlanWithStatisticToRS(List.of(testPlanWithStatistic), pageable, 1L))
        .thenReturn(testPlanPage);

    var result = assertDoesNotThrow(
        () -> sut.getByCriteria(projectId, filter, pageable));

    assertNotNull(result);
    assertNotNull(result.getContent());
    assertEquals(1, result.getContent().size());
    assertEquals(1L, result.getContent().iterator().next().getId());
    assertEquals(10, result.getPage().getSize());
    assertEquals(1, result.getPage().getNumber());
    assertEquals(1, result.getPage().getTotalElements());
    assertEquals(1, result.getPage().getTotalPages());

    verify(tmsTestPlanFilterableRepository).findIdsByProjectIdAndFilter(projectId, filter,
        pageable);
    verify(testPlanRepository).findByIdsWithAttributes(List.of(1L));
    verify(tmsTestPlanExecutionService).enrichWithStatistics(testPlan);
    verify(tmsTestPlanMapper).convertTmsTestPlanWithStatisticToRS(List.of(testPlanWithStatistic), pageable, 1L);
  }

  @Test
  void shouldGetByCriteriaWithNullFilter() {
    var projectId = 1L;
    Filter filter = null;
    var pageable = PageRequest.of(0, 10);

    var testPlan = new TmsTestPlan();
    testPlan.setId(1L);

    var testPlanWithStatistic = mock(TmsTestPlanWithStatistic.class);
    var testPlanRS = new TmsTestPlanRS();
    testPlanRS.setId(1L);

    org.springframework.data.domain.Page<Long> testPlanIdsPage = new PageImpl<>(List.of(1L),
        pageable, 1);
    org.springframework.data.domain.Page<TmsTestPlanRS> testPlanPage = new PageImpl<>(
        List.of(testPlanRS), pageable, 1);

    when(tmsTestPlanFilterableRepository.findIdsByProjectIdAndFilter(projectId, filter, pageable))
        .thenReturn(testPlanIdsPage);
    when(testPlanRepository.findByIdsWithAttributes(List.of(1L))).thenReturn(List.of(testPlan));
    when(tmsTestPlanExecutionService.enrichWithStatistics(testPlan)).thenReturn(
        testPlanWithStatistic);
    when(tmsTestPlanMapper.convertTmsTestPlanWithStatisticToRS(List.of(testPlanWithStatistic), pageable, 1L))
        .thenReturn(testPlanPage);

    var result = assertDoesNotThrow(
        () -> sut.getByCriteria(projectId, filter, pageable));

    assertNotNull(result);
    assertNotNull(result.getContent());
    assertEquals(1, result.getContent().size());

    verify(tmsTestPlanFilterableRepository).findIdsByProjectIdAndFilter(projectId, filter,
        pageable);
    verify(testPlanRepository).findByIdsWithAttributes(List.of(1L));
    verify(tmsTestPlanExecutionService).enrichWithStatistics(testPlan);
    verify(tmsTestPlanMapper).convertTmsTestPlanWithStatisticToRS(List.of(testPlanWithStatistic), pageable, 1L);
  }

  @Test
  void shouldGetByCriteriaWhenEmpty() {
    var projectId = 1L;
    var filter = mock(Filter.class);
    var pageable = PageRequest.of(0, 10);

    org.springframework.data.domain.Page<Long> emptyPage = new PageImpl<>(Collections.emptyList(),
        pageable, 0);

    when(tmsTestPlanFilterableRepository.findIdsByProjectIdAndFilter(projectId, filter, pageable))
        .thenReturn(emptyPage);

    var result = assertDoesNotThrow(
        () -> sut.getByCriteria(projectId, filter, pageable));

    assertNotNull(result);
    assertNotNull(result.getContent());
    assertEquals(0, result.getContent().size());
    assertEquals(10, result.getPage().getSize());
    assertEquals(1, result.getPage().getNumber()); // PagedResourcesAssembler adds +1
    assertEquals(0, result.getPage().getTotalElements());
    assertEquals(0, result.getPage().getTotalPages());

    verify(tmsTestPlanFilterableRepository).findIdsByProjectIdAndFilter(projectId, filter,
        pageable);
    verify(testPlanRepository, never()).findByIdsWithAttributes(any());
    verify(tmsTestPlanExecutionService, never()).enrichWithStatistics(any(TmsTestPlan.class));
    verify(tmsTestPlanMapper, never()).convertTmsTestPlanWithStatisticToRS(anyList(), any(), anyLong());
  }

  @Test
  void shouldUpdateExisting() {
    var projectId = 1L;
    var testPlanId = 2L;
    var testPlanRQ = new TmsTestPlanRQ();
    var existingTestPlan = new TmsTestPlan();
    var updatedTestPlan = new TmsTestPlan();
    var testPlanWithStatistic = mock(TmsTestPlanWithStatistic.class);
    var testPlanRS = new TmsTestPlanRS();

    when(testPlanRepository.findByIdAndProjectId(testPlanId, projectId)).thenReturn(
        Optional.of(existingTestPlan));
    when(tmsTestPlanMapper.convertFromRQ(projectId, testPlanRQ)).thenReturn(updatedTestPlan);
    when(tmsTestPlanExecutionService.enrichWithStatistics(existingTestPlan)).thenReturn(
        testPlanWithStatistic);
    when(tmsTestPlanMapper.convertTmsTestPlanWithStatisticToRS(testPlanWithStatistic)).thenReturn(testPlanRS);

    var result = assertDoesNotThrow(() -> sut.update(projectId, testPlanId, testPlanRQ));

    assertEquals(testPlanRS, result);
    verify(testPlanRepository).findByIdAndProjectId(testPlanId, projectId);
    verify(tmsTestPlanMapper).update(existingTestPlan, updatedTestPlan);
    verify(tmsTestPlanAttributeService).updateTestPlanAttributes(existingTestPlan,
        testPlanRQ.getAttributes());
    verify(tmsTestPlanExecutionService).enrichWithStatistics(existingTestPlan);
    verify(tmsTestPlanMapper).convertTmsTestPlanWithStatisticToRS(testPlanWithStatistic);
  }

  @Test
  void shouldCreateWhenUpdateButNotFound() {
    var projectId = 1L;
    var testPlanId = 2L;
    var testPlanRQ = new TmsTestPlanRQ();
    var testPlan = new TmsTestPlan();
    var testPlanRS = new TmsTestPlanRS();

    when(testPlanRepository.findByIdAndProjectId(testPlanId, projectId)).thenReturn(
        Optional.empty());
    when(tmsTestPlanMapper.convertFromRQ(projectId, testPlanRQ)).thenReturn(testPlan);
    when(tmsTestPlanMapper.convertTmsTestPlanWithStatisticToRS(any(TmsTestPlanWithStatistic.class))).thenReturn(
        testPlanRS);

    var result = assertDoesNotThrow(() -> sut.update(projectId, testPlanId, testPlanRQ));

    assertEquals(testPlanRS, result);
    verify(testPlanRepository).findByIdAndProjectId(testPlanId, projectId);
    verify(testPlanRepository).save(testPlan);
    verify(tmsTestPlanAttributeService).createTestPlanAttributes(testPlan,
        testPlanRQ.getAttributes());
    verify(tmsTestPlanMapper).convertTmsTestPlanWithStatisticToRS(any(TmsTestPlanWithStatistic.class));
  }

  @Test
  void shouldPatchExisting() {
    var projectId = 1L;
    var testPlanId = 2L;
    var testPlanRQ = new TmsTestPlanRQ();
    var existingTestPlan = new TmsTestPlan();
    var patchedTestPlan = new TmsTestPlan();
    var testPlanWithStatistic = mock(TmsTestPlanWithStatistic.class);
    var testPlanRS = new TmsTestPlanRS();

    when(testPlanRepository.findByIdAndProjectId(testPlanId, projectId)).thenReturn(
        Optional.of(existingTestPlan));
    when(tmsTestPlanMapper.convertFromRQ(projectId, testPlanRQ)).thenReturn(patchedTestPlan);
    when(tmsTestPlanExecutionService.enrichWithStatistics(existingTestPlan)).thenReturn(
        testPlanWithStatistic);
    when(tmsTestPlanMapper.convertTmsTestPlanWithStatisticToRS(testPlanWithStatistic)).thenReturn(testPlanRS);

    var result = assertDoesNotThrow(() -> sut.patch(projectId, testPlanId, testPlanRQ));

    assertEquals(testPlanRS, result);
    verify(testPlanRepository).findByIdAndProjectId(testPlanId, projectId);
    verify(tmsTestPlanMapper).patch(existingTestPlan, patchedTestPlan);
    verify(tmsTestPlanAttributeService).patchTestPlanAttributes(existingTestPlan,
        testPlanRQ.getAttributes());
    verify(tmsTestPlanExecutionService).enrichWithStatistics(existingTestPlan);
    verify(tmsTestPlanMapper).convertTmsTestPlanWithStatisticToRS(testPlanWithStatistic);
  }

  @Test
  void shouldThrowNotFoundExceptionWhenPatchAndNotFound() {
    var projectId = 1L;
    var testPlanId = 2L;
    var testPlanRQ = new TmsTestPlanRQ();

    when(testPlanRepository.findByIdAndProjectId(testPlanId, projectId)).thenReturn(
        Optional.empty());

    var exception = assertThrows(ReportPortalException.class, () ->
        sut.patch(projectId, testPlanId, testPlanRQ)
    );

    assertEquals(exception.getErrorType(), ErrorType.NOT_FOUND);
    verify(testPlanRepository).findByIdAndProjectId(testPlanId, projectId);
    verify(tmsTestPlanExecutionService, never()).enrichWithStatistics(any());
  }

  @Test
  void shouldAddTestCasesToPlanSuccessfully() {
    var projectId = 1L;
    var testPlanId = 2L;
    var testCaseIds = List.of(10L, 20L, 30L);
    var expectedResult = BatchTestCaseOperationResultRS.builder()
        .totalCount(3)
        .successCount(2)
        .failureCount(1)
        .errors(List.of())
        .build();

    when(testPlanRepository.existsByIdAndProject_Id(testPlanId, projectId)).thenReturn(true);
    when(tmsTestCaseService.getExistingTestCaseIds(projectId, testCaseIds))
        .thenReturn(List.of(10L, 20L, 30L));
    when(tmsTestPlanTestCaseRepository.findTestCaseIdsByTestPlanId(testPlanId))
        .thenReturn(List.of(10L)); // 10L already exists
    when(tmsTestPlanTestCaseRepository.insertTestPlanTestCaseIgnoreConflict(testPlanId, 20L))
        .thenReturn(1);
    when(tmsTestPlanTestCaseRepository.insertTestPlanTestCaseIgnoreConflict(testPlanId, 30L))
        .thenReturn(1);
    when(tmsTestPlanMapper.convertToRS(anyInt(), anyInt(), anyList()))
        .thenReturn(expectedResult);

    var result = sut.addTestCasesToPlan(projectId, testPlanId, testCaseIds);

    assertNotNull(result);
    verify(testPlanRepository).existsByIdAndProject_Id(testPlanId, projectId);
    verify(tmsTestCaseService).getExistingTestCaseIds(projectId, testCaseIds);
    verify(tmsTestPlanTestCaseRepository).findTestCaseIdsByTestPlanId(testPlanId);
    verify(tmsTestPlanMapper).convertToRS(eq(3), eq(2), anyList());
  }

  @Test
  void shouldAddTestCasesToPlanWithErrors() {
    var projectId = 1L;
    var testPlanId = 2L;
    var testCaseIds = List.of(10L, 999L); // 999L doesn't exist
    var expectedResult = BatchTestCaseOperationResultRS.builder()
        .totalCount(2)
        .successCount(1)
        .failureCount(1)
        .errors(List.of(new BatchTestCaseOperationError(999L, "Test case with id 999 not found")))
        .build();

    when(testPlanRepository.existsByIdAndProject_Id(testPlanId, projectId)).thenReturn(true);
    when(tmsTestCaseService.getExistingTestCaseIds(projectId, testCaseIds))
        .thenReturn(List.of(10L)); // Only 10L exists
    when(tmsTestPlanTestCaseRepository.findTestCaseIdsByTestPlanId(testPlanId))
        .thenReturn(List.of());
    when(tmsTestPlanTestCaseRepository.insertTestPlanTestCaseIgnoreConflict(testPlanId, 10L))
        .thenReturn(1);
    when(tmsTestPlanMapper.convertToRS(anyInt(), anyInt(), anyList()))
        .thenReturn(expectedResult);

    var result = sut.addTestCasesToPlan(projectId, testPlanId, testCaseIds);

    assertNotNull(result);
    verify(tmsTestPlanMapper).convertToRS(eq(2), eq(1), anyList());
  }

  @Test
  void shouldThrowNotFoundWhenAddTestCasesToNonExistentPlan() {
    var projectId = 1L;
    var testPlanId = 2L;
    var testCaseIds = List.of(10L, 20L);

    when(testPlanRepository.existsByIdAndProject_Id(testPlanId, projectId)).thenReturn(false);

    var exception = assertThrows(ReportPortalException.class, () ->
        sut.addTestCasesToPlan(projectId, testPlanId, testCaseIds)
    );

    assertEquals(ErrorType.NOT_FOUND, exception.getErrorType());
    verify(testPlanRepository).existsByIdAndProject_Id(testPlanId, projectId);
    verify(tmsTestCaseService, never()).getExistingTestCaseIds(any(), any());
  }

  @Test
  void shouldRemoveTestCasesFromPlanSuccessfully() {
    var projectId = 1L;
    var testPlanId = 2L;
    var testCaseIds = List.of(10L, 20L);
    var expectedResult = BatchTestCaseOperationResultRS.builder()
        .totalCount(2)
        .successCount(2)
        .failureCount(0)
        .errors(List.of())
        .build();

    when(tmsTestPlanTestCaseRepository.findTestCaseIdsByTestPlanId(testPlanId))
        .thenReturn(List.of(10L, 20L));
    when(tmsTestPlanTestCaseRepository.deleteByTestPlanIdAndTestCaseId(testPlanId, 10L))
        .thenReturn(1);
    when(tmsTestPlanTestCaseRepository.deleteByTestPlanIdAndTestCaseId(testPlanId, 20L))
        .thenReturn(1);
    when(tmsTestPlanMapper.convertToRS(anyInt(), anyInt(), anyList()))
        .thenReturn(expectedResult);

    var result = sut.removeTestCasesFromPlan(projectId, testPlanId, testCaseIds);

    assertNotNull(result);
    verify(tmsTestPlanTestCaseRepository).findTestCaseIdsByTestPlanId(testPlanId);
    verify(tmsTestPlanMapper).convertToRS(eq(2), eq(2), anyList());
  }

  @Test
  void shouldRemoveTestCasesFromPlanWithErrors() {
    var projectId = 1L;
    var testPlanId = 2L;
    var testCaseIds = List.of(10L, 999L); // 999L not in plan
    var expectedResult = BatchTestCaseOperationResultRS.builder()
        .totalCount(2)
        .successCount(1)
        .failureCount(1)
        .errors(
            List.of(new BatchTestCaseOperationError(999L, "Test case with id 999 not found in test plan")))
        .build();

    when(tmsTestPlanTestCaseRepository.findTestCaseIdsByTestPlanId(testPlanId))
        .thenReturn(List.of(10L)); // Only 10L is in plan
    when(tmsTestPlanTestCaseRepository.deleteByTestPlanIdAndTestCaseId(testPlanId, 10L))
        .thenReturn(1);
    when(tmsTestPlanMapper.convertToRS(anyInt(), anyInt(), anyList()))
        .thenReturn(expectedResult);

    var result = sut.removeTestCasesFromPlan(projectId, testPlanId, testCaseIds);

    assertNotNull(result);
    verify(tmsTestPlanMapper).convertToRS(eq(2), eq(1), anyList());
  }

  @Test
  void shouldHandleEmptyTestCaseListForAdd() {
    var projectId = 1L;
    var testPlanId = 2L;
    var testCaseIds = List.<Long>of();
    var expectedResult = BatchTestCaseOperationResultRS.builder()
        .totalCount(0)
        .successCount(0)
        .failureCount(0)
        .errors(List.of())
        .build();

    when(testPlanRepository.existsByIdAndProject_Id(testPlanId, projectId)).thenReturn(true);
    when(tmsTestCaseService.getExistingTestCaseIds(projectId, testCaseIds))
        .thenReturn(List.of());
    when(tmsTestPlanTestCaseRepository.findTestCaseIdsByTestPlanId(testPlanId))
        .thenReturn(List.of());
    when(tmsTestPlanMapper.convertToRS(anyInt(), anyInt(), anyList()))
        .thenReturn(expectedResult);

    var result = sut.addTestCasesToPlan(projectId, testPlanId, testCaseIds);

    assertNotNull(result);
    verify(tmsTestPlanMapper).convertToRS(eq(0), eq(0), anyList());
  }

  @Test
  void shouldHandleEmptyTestCaseListForRemove() {
    var projectId = 1L;
    var testPlanId = 2L;
    var testCaseIds = List.<Long>of();
    var expectedResult = BatchTestCaseOperationResultRS.builder()
        .totalCount(0)
        .successCount(0)
        .failureCount(0)
        .errors(List.of())
        .build();

    when(tmsTestPlanTestCaseRepository.findTestCaseIdsByTestPlanId(testPlanId))
        .thenReturn(List.of());
    when(tmsTestPlanMapper.convertToRS(anyInt(), anyInt(), anyList()))
        .thenReturn(expectedResult);

    var result = sut.removeTestCasesFromPlan(projectId, testPlanId, testCaseIds);

    assertNotNull(result);
    verify(tmsTestPlanMapper).convertToRS(eq(0), eq(0), anyList());
  }

  // Tests for helper methods

  @Test
  void shouldAddTestCaseToTestPlanSuccessfully() {
    var testPlanId = 1L;
    var testCaseId = 2L;

    when(tmsTestPlanTestCaseRepository.insertTestPlanTestCaseIgnoreConflict(testPlanId,
        testCaseId))
        .thenReturn(1);

    var result = sut.addTestCaseToTestPlan(testPlanId, testCaseId);

    assertTrue(result);
    verify(tmsTestPlanTestCaseRepository).insertTestPlanTestCaseIgnoreConflict(testPlanId,
        testCaseId);
  }

  @Test
  void shouldFailToAddTestCaseToTestPlan() {
    var testPlanId = 1L;
    var testCaseId = 2L;

    when(tmsTestPlanTestCaseRepository.insertTestPlanTestCaseIgnoreConflict(testPlanId,
        testCaseId))
        .thenReturn(0);

    var result = sut.addTestCaseToTestPlan(testPlanId, testCaseId);

    assertFalse(result);
    verify(tmsTestPlanTestCaseRepository).insertTestPlanTestCaseIgnoreConflict(testPlanId,
        testCaseId);
  }

  @Test
  void shouldHandleExceptionWhenAddingTestCaseToTestPlan() {
    var testPlanId = 1L;
    var testCaseId = 2L;

    when(tmsTestPlanTestCaseRepository.insertTestPlanTestCaseIgnoreConflict(testPlanId,
        testCaseId))
        .thenThrow(new RuntimeException("Database error"));

    var result = sut.addTestCaseToTestPlan(testPlanId, testCaseId);

    assertFalse(result);
    verify(tmsTestPlanTestCaseRepository).insertTestPlanTestCaseIgnoreConflict(testPlanId,
        testCaseId);
  }

  @Test
  void shouldRemoveSingleTestCaseFromPlanSuccessfully() {
    var testPlanId = 1L;
    var testCaseId = 2L;

    when(tmsTestPlanTestCaseRepository.deleteByTestPlanIdAndTestCaseId(testPlanId, testCaseId))
        .thenReturn(1);

    var result = sut.removeSingleTestCaseFromPlan(testPlanId, testCaseId);

    assertTrue(result);
    verify(tmsTestPlanTestCaseRepository).deleteByTestPlanIdAndTestCaseId(testPlanId, testCaseId);
  }

  @Test
  void shouldFailToRemoveSingleTestCaseFromPlan() {
    var testPlanId = 1L;
    var testCaseId = 2L;

    when(tmsTestPlanTestCaseRepository.deleteByTestPlanIdAndTestCaseId(testPlanId, testCaseId))
        .thenReturn(0);

    var result = sut.removeSingleTestCaseFromPlan(testPlanId, testCaseId);

    assertFalse(result);
    verify(tmsTestPlanTestCaseRepository).deleteByTestPlanIdAndTestCaseId(testPlanId, testCaseId);
  }

  @Test
  void shouldHandleExceptionWhenRemovingSingleTestCaseFromPlan() {
    var testPlanId = 1L;
    var testCaseId = 2L;

    when(tmsTestPlanTestCaseRepository.deleteByTestPlanIdAndTestCaseId(testPlanId, testCaseId))
        .thenThrow(new RuntimeException("Database error"));

    var result = sut.removeSingleTestCaseFromPlan(testPlanId, testCaseId);

    assertFalse(result);
    verify(tmsTestPlanTestCaseRepository).deleteByTestPlanIdAndTestCaseId(testPlanId, testCaseId);
  }

  // Tests for duplicate method

  @Test
  void shouldDuplicateTestPlanSuccessfully() {
    var projectId = 1L;
    var testPlanId = 2L;
    var originalTestPlan = new TmsTestPlan();
    originalTestPlan.setId(testPlanId);

    var duplicatedTestPlan = new TmsTestPlan();
    duplicatedTestPlan.setId(3L);

    var originalTestCaseIds = List.of(10L, 20L);
    var duplicatedTestCaseIds = List.of(30L, 40L);

    var duplicationResult = BatchTestCaseOperationResultRS.builder()
        .totalCount(2)
        .successCount(2)
        .failureCount(0)
        .successTestCaseIds(duplicatedTestCaseIds)
        .errors(List.of())
        .build();

    var addToPlanResult = BatchTestCaseOperationResultRS.builder()
        .totalCount(2)
        .successCount(2)
        .failureCount(0)
        .errors(List.of())
        .build();

    var combinedResult = BatchTestCaseOperationResultRS.builder()
        .totalCount(2)
        .successCount(2)
        .failureCount(0)
        .errors(List.of())
        .build();

    var expectedResponse = DuplicateTmsTestPlanRS.builder()
        .id(3L)
        .duplicationStatistic(combinedResult)
        .build();

    when(testPlanRepository.findByIdAndProjectId(testPlanId, projectId))
        .thenReturn(Optional.of(originalTestPlan));
    when(tmsTestPlanMapper.duplicateTestPlan(originalTestPlan)).thenReturn(duplicatedTestPlan);
    when(testPlanRepository.save(duplicatedTestPlan)).thenReturn(duplicatedTestPlan);
    when(tmsTestPlanTestCaseRepository.findTestCaseIdsByTestPlanId(testPlanId))
        .thenReturn(originalTestCaseIds);
    when(tmsTestCaseService.duplicateTestCases(projectId, originalTestCaseIds))
        .thenReturn(duplicationResult);
    when(tmsTestCaseService.getExistingTestCaseIds(projectId, duplicatedTestCaseIds))
        .thenReturn(duplicatedTestCaseIds);
    when(tmsTestPlanTestCaseRepository.findTestCaseIdsByTestPlanId(3L))
        .thenReturn(List.of());
    when(tmsTestPlanTestCaseRepository.insertTestPlanTestCaseIgnoreConflict(eq(3L), anyLong()))
        .thenReturn(1);
    when(testPlanRepository.existsByIdAndProject_Id(3L, projectId)).thenReturn(true);
    when(tmsTestPlanMapper.convertToRS(anyInt(), anyInt(), anyList()))
        .thenReturn(addToPlanResult);
    when(tmsTestPlanMapper.combineDuplicateTestPlanBatchResults(duplicationResult, addToPlanResult))
        .thenReturn(combinedResult);
    when(tmsTestPlanMapper.buildDuplicateTestPlanResponse(duplicatedTestPlan, combinedResult))
        .thenReturn(expectedResponse);

    var result = sut.duplicate(projectId, testPlanId);

    assertNotNull(result);
    assertEquals(3L, result.getId());
    verify(testPlanRepository).findByIdAndProjectId(testPlanId, projectId);
    verify(tmsTestPlanMapper).duplicateTestPlan(originalTestPlan);
    verify(testPlanRepository).save(duplicatedTestPlan);
    verify(tmsTestPlanAttributeService).duplicateTestPlanAttributes(originalTestPlan, duplicatedTestPlan);
    verify(tmsTestPlanTestCaseRepository).findTestCaseIdsByTestPlanId(testPlanId);
    verify(tmsTestCaseService).duplicateTestCases(projectId, originalTestCaseIds);
    verify(tmsTestPlanMapper).buildDuplicateTestPlanResponse(duplicatedTestPlan, combinedResult);
  }

  @Test
  void shouldDuplicateTestPlanWithoutTestCases() {
    var projectId = 1L;
    var testPlanId = 2L;
    var originalTestPlan = new TmsTestPlan();
    originalTestPlan.setId(testPlanId);

    var duplicatedTestPlan = new TmsTestPlan();
    duplicatedTestPlan.setId(3L);

    var emptyResult = BatchTestCaseOperationResultRS.builder()
        .totalCount(0)
        .successCount(0)
        .failureCount(0)
        .errors(List.of())
        .build();

    var expectedResponse = DuplicateTmsTestPlanRS.builder()
        .id(3L)
        .duplicationStatistic(emptyResult)
        .build();

    when(testPlanRepository.findByIdAndProjectId(testPlanId, projectId))
        .thenReturn(Optional.of(originalTestPlan));
    when(tmsTestPlanMapper.duplicateTestPlan(originalTestPlan)).thenReturn(duplicatedTestPlan);
    when(testPlanRepository.save(duplicatedTestPlan)).thenReturn(duplicatedTestPlan);
    when(tmsTestPlanTestCaseRepository.findTestCaseIdsByTestPlanId(testPlanId))
        .thenReturn(List.of());
    when(tmsTestPlanMapper.createFailedBatchResult(eq(Collections.emptyList()), anyString()))
        .thenReturn(emptyResult);
    when(tmsTestPlanMapper.buildDuplicateTestPlanResponse(duplicatedTestPlan, emptyResult))
        .thenReturn(expectedResponse);

    var result = sut.duplicate(projectId, testPlanId);

    assertNotNull(result);
    assertEquals(3L, result.getId());
    verify(testPlanRepository).findByIdAndProjectId(testPlanId, projectId);
    verify(tmsTestPlanMapper).duplicateTestPlan(originalTestPlan);
    verify(testPlanRepository).save(duplicatedTestPlan);
    verify(tmsTestPlanAttributeService).duplicateTestPlanAttributes(originalTestPlan, duplicatedTestPlan);
    verify(tmsTestCaseService, never()).duplicateTestCases(anyLong(), anyList());
    verify(tmsTestPlanMapper).buildDuplicateTestPlanResponse(duplicatedTestPlan, emptyResult);
  }

  @Test
  void shouldThrowNotFoundWhenDuplicatingNonExistentTestPlan() {
    var projectId = 1L;
    var testPlanId = 2L;

    when(testPlanRepository.findByIdAndProjectId(testPlanId, projectId))
        .thenReturn(Optional.empty());

    var exception = assertThrows(ReportPortalException.class, () ->
        sut.duplicate(projectId, testPlanId)
    );

    assertEquals(ErrorType.NOT_FOUND, exception.getErrorType());
    verify(testPlanRepository).findByIdAndProjectId(testPlanId, projectId);
    verify(tmsTestPlanMapper, never()).duplicateTestPlan(any());
    verify(testPlanRepository, never()).save(any());
  }

  @Test
  void shouldHandlePartialTestCaseDuplicationFailure() {
    var projectId = 1L;
    var testPlanId = 2L;
    var originalTestPlan = new TmsTestPlan();
    originalTestPlan.setId(testPlanId);

    var duplicatedTestPlan = new TmsTestPlan();
    duplicatedTestPlan.setId(3L);

    var originalTestCaseIds = List.of(10L, 20L, 30L);
    var duplicatedTestCaseIds = List.of(40L, 50L); // Only 2 out of 3 duplicated

    var duplicationResult = BatchTestCaseOperationResultRS.builder()
        .totalCount(3)
        .successCount(2)
        .failureCount(1)
        .successTestCaseIds(duplicatedTestCaseIds)
        .errors(List.of(new BatchTestCaseOperationError(30L, "Failed to duplicate")))
        .build();

    var addToPlanResult = BatchTestCaseOperationResultRS.builder()
        .totalCount(2)
        .successCount(2)
        .failureCount(0)
        .errors(List.of())
        .build();

    var combinedResult = BatchTestCaseOperationResultRS.builder()
        .totalCount(3)
        .successCount(2)
        .failureCount(1)
        .errors(List.of(new BatchTestCaseOperationError(30L, "Failed to duplicate")))
        .build();

    var expectedResponse = DuplicateTmsTestPlanRS.builder()
        .id(3L)
        .duplicationStatistic(combinedResult)
        .build();

    when(testPlanRepository.findByIdAndProjectId(testPlanId, projectId))
        .thenReturn(Optional.of(originalTestPlan));
    when(tmsTestPlanMapper.duplicateTestPlan(originalTestPlan)).thenReturn(duplicatedTestPlan);
    when(testPlanRepository.save(duplicatedTestPlan)).thenReturn(duplicatedTestPlan);
    when(tmsTestPlanTestCaseRepository.findTestCaseIdsByTestPlanId(testPlanId))
        .thenReturn(originalTestCaseIds);
    when(tmsTestCaseService.duplicateTestCases(projectId, originalTestCaseIds))
        .thenReturn(duplicationResult);
    when(tmsTestCaseService.getExistingTestCaseIds(projectId, duplicatedTestCaseIds))
        .thenReturn(duplicatedTestCaseIds);
    when(tmsTestPlanTestCaseRepository.findTestCaseIdsByTestPlanId(3L))
        .thenReturn(List.of());
    when(tmsTestPlanTestCaseRepository.insertTestPlanTestCaseIgnoreConflict(eq(3L), anyLong()))
        .thenReturn(1);
    when(testPlanRepository.existsByIdAndProject_Id(3L, projectId)).thenReturn(true);
    when(tmsTestPlanMapper.convertToRS(anyInt(), anyInt(), anyList()))
        .thenReturn(addToPlanResult);
    when(tmsTestPlanMapper.combineDuplicateTestPlanBatchResults(duplicationResult, addToPlanResult))
        .thenReturn(combinedResult);
    when(tmsTestPlanMapper.buildDuplicateTestPlanResponse(duplicatedTestPlan, combinedResult))
        .thenReturn(expectedResponse);

    var result = sut.duplicate(projectId, testPlanId);

    assertNotNull(result);
    assertEquals(3L, result.getId());
    assertNotNull(result.getDuplicationStatistic());
    assertEquals(1, result.getDuplicationStatistic().getFailureCount());
    verify(tmsTestCaseService).duplicateTestCases(projectId, originalTestCaseIds);
    verify(tmsTestPlanMapper).combineDuplicateTestPlanBatchResults(duplicationResult, addToPlanResult);
  }

  @Test
  void shouldHandleCompleteTestCaseDuplicationFailure() {
    var projectId = 1L;
    var testPlanId = 2L;
    var originalTestPlan = new TmsTestPlan();
    originalTestPlan.setId(testPlanId);

    var duplicatedTestPlan = new TmsTestPlan();
    duplicatedTestPlan.setId(3L);

    var originalTestCaseIds = List.of(10L, 20L);

    var duplicationResult = BatchTestCaseOperationResultRS.builder()
        .totalCount(2)
        .successCount(0)
        .failureCount(2)
        .successTestCaseIds(List.of())
        .errors(List.of(
            new BatchTestCaseOperationError(10L, "Failed to duplicate"),
            new BatchTestCaseOperationError(20L, "Failed to duplicate")
        ))
        .build();

    var expectedResponse = DuplicateTmsTestPlanRS.builder()
        .id(3L)
        .duplicationStatistic(duplicationResult)
        .build();

    when(testPlanRepository.findByIdAndProjectId(testPlanId, projectId))
        .thenReturn(Optional.of(originalTestPlan));
    when(tmsTestPlanMapper.duplicateTestPlan(originalTestPlan)).thenReturn(duplicatedTestPlan);
    when(testPlanRepository.save(duplicatedTestPlan)).thenReturn(duplicatedTestPlan);
    when(tmsTestPlanTestCaseRepository.findTestCaseIdsByTestPlanId(testPlanId))
        .thenReturn(originalTestCaseIds);
    when(tmsTestCaseService.duplicateTestCases(projectId, originalTestCaseIds))
        .thenReturn(duplicationResult);
    when(tmsTestPlanMapper.buildDuplicateTestPlanResponse(duplicatedTestPlan, duplicationResult))
        .thenReturn(expectedResponse);

    var result = sut.duplicate(projectId, testPlanId);

    assertNotNull(result);
    assertEquals(3L, result.getId());
    assertNotNull(result.getDuplicationStatistic());
    assertEquals(2, result.getDuplicationStatistic().getFailureCount());
    assertEquals(0, result.getDuplicationStatistic().getSuccessCount());
    verify(tmsTestCaseService).duplicateTestCases(projectId, originalTestCaseIds);
    verify(tmsTestCaseService, never()).getExistingTestCaseIds(anyLong(), anyList());
    verify(tmsTestPlanMapper, never()).combineDuplicateTestPlanBatchResults(any(), any());
  }

  @Test
  void shouldHandleExceptionWhenAddingDuplicatedTestCasesToPlan() {
    var projectId = 1L;
    var testPlanId = 2L;
    var originalTestPlan = new TmsTestPlan();
    originalTestPlan.setId(testPlanId);

    var duplicatedTestPlan = new TmsTestPlan();
    duplicatedTestPlan.setId(3L);

    var originalTestCaseIds = List.of(10L, 20L);
    var duplicatedTestCaseIds = List.of(30L, 40L);

    var duplicationResult = BatchTestCaseOperationResultRS.builder()
        .totalCount(2)
        .successCount(2)
        .failureCount(0)
        .successTestCaseIds(duplicatedTestCaseIds)
        .errors(List.of())
        .build();

    var failedAddResult = BatchTestCaseOperationResultRS.builder()
        .totalCount(2)
        .successCount(0)
        .failureCount(2)
        .errors(List.of(
            new BatchTestCaseOperationError(30L, "Failed to add duplicated test case to plan: Database error"),
            new BatchTestCaseOperationError(40L, "Failed to add duplicated test case to plan: Database error")
        ))
        .build();

    var combinedResult = BatchTestCaseOperationResultRS.builder()
        .totalCount(2)
        .successCount(0)
        .failureCount(2)
        .errors(failedAddResult.getErrors())
        .build();

    var expectedResponse = DuplicateTmsTestPlanRS.builder()
        .id(3L)
        .duplicationStatistic(combinedResult)
        .build();

    when(testPlanRepository.findByIdAndProjectId(testPlanId, projectId))
        .thenReturn(Optional.of(originalTestPlan));
    when(tmsTestPlanMapper.duplicateTestPlan(originalTestPlan)).thenReturn(duplicatedTestPlan);
    when(testPlanRepository.save(duplicatedTestPlan)).thenReturn(duplicatedTestPlan);
    when(tmsTestPlanTestCaseRepository.findTestCaseIdsByTestPlanId(testPlanId))
        .thenReturn(originalTestCaseIds);
    when(tmsTestCaseService.duplicateTestCases(projectId, originalTestCaseIds))
        .thenReturn(duplicationResult);
    when(testPlanRepository.existsByIdAndProject_Id(3L, projectId))
        .thenThrow(new RuntimeException("Database error"));
    when(tmsTestPlanMapper.createFailedBatchResult(duplicatedTestCaseIds,
        "Failed to add duplicated test case to plan: Database error"))
        .thenReturn(failedAddResult);
    when(tmsTestPlanMapper.combineDuplicateTestPlanBatchResults(duplicationResult, failedAddResult))
        .thenReturn(combinedResult);
    when(tmsTestPlanMapper.buildDuplicateTestPlanResponse(duplicatedTestPlan, combinedResult))
        .thenReturn(expectedResponse);

    var result = sut.duplicate(projectId, testPlanId);

    assertNotNull(result);
    assertEquals(3L, result.getId());
    assertNotNull(result.getDuplicationStatistic());
    assertEquals(2, result.getDuplicationStatistic().getFailureCount());
    verify(tmsTestPlanMapper).createFailedBatchResult(eq(duplicatedTestCaseIds), anyString());
    verify(tmsTestPlanMapper).combineDuplicateTestPlanBatchResults(duplicationResult, failedAddResult);
  }
}
