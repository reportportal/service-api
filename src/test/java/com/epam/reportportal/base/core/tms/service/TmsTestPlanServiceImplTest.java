package com.epam.reportportal.base.core.tms.service;

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
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.epam.reportportal.base.core.tms.dto.DuplicateTmsTestPlanRS;
import com.epam.reportportal.base.core.tms.dto.TmsTestCaseInTestPlanRS;
import com.epam.reportportal.base.core.tms.dto.TmsTestFolderRS;
import com.epam.reportportal.base.core.tms.dto.TmsTestPlanRQ;
import com.epam.reportportal.base.core.tms.dto.TmsTestPlanRS;
import com.epam.reportportal.base.core.tms.dto.batch.BatchTestCaseOperationError;
import com.epam.reportportal.base.core.tms.dto.batch.BatchTestCaseOperationResultRS;
import com.epam.reportportal.base.core.tms.mapper.TmsTestPlanMapper;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.Filter;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsTestCaseRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsTestPlanRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsTestPlanTestCaseRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.filterable.TmsTestPlanFilterableRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestPlan;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestPlanWithStatistic;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.base.model.Page;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

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

  @Mock
  private TmsTestFolderService tmsTestFolderService;

  @InjectMocks
  private TmsTestPlanServiceImpl sut;

  private Long projectId;
  private Long testPlanId;
  private Long testCaseId;
  private Long milestoneId;
  private Pageable pageable;

  @BeforeEach
  void setUp() {
    projectId = 1L;
    testPlanId = 100L;
    testCaseId = 10L;
    milestoneId = 50L;
    pageable = PageRequest.of(0, 10);
  }

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
    verify(tmsTestPlanAttributeService).createTestPlanAttributes(projectId, testPlan,
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
    verify(tmsTestPlanAttributeService).updateTestPlanAttributes(projectId, existingTestPlan,
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
    verify(tmsTestPlanAttributeService).createTestPlanAttributes(projectId, testPlan,
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
    verify(tmsTestPlanAttributeService).updateTestPlanAttributes(projectId, existingTestPlan,
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

  // Tests for duplicate method with RQ

  @Test
  void shouldDuplicateTestPlanSuccessfully() {
    var projectId = 1L;
    var testPlanId = 2L;
    var duplicateTestPlanRQ = new TmsTestPlanRQ();

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
    when(tmsTestPlanMapper.duplicateTestPlan(originalTestPlan, duplicateTestPlanRQ))
        .thenReturn(duplicatedTestPlan);
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

    var result = sut.duplicate(projectId, testPlanId, duplicateTestPlanRQ);

    assertNotNull(result);
    assertEquals(3L, result.getId());
    verify(testPlanRepository).findByIdAndProjectId(testPlanId, projectId);
    verify(tmsTestPlanMapper).duplicateTestPlan(originalTestPlan, duplicateTestPlanRQ);
    verify(testPlanRepository).save(duplicatedTestPlan);
    verify(tmsTestPlanAttributeService).createTestPlanAttributes(
        projectId, duplicatedTestPlan, duplicateTestPlanRQ.getAttributes()
    );
    verify(tmsTestPlanTestCaseRepository).findTestCaseIdsByTestPlanId(testPlanId);
    verify(tmsTestCaseService).duplicateTestCases(projectId, originalTestCaseIds);
    verify(tmsTestPlanMapper).buildDuplicateTestPlanResponse(duplicatedTestPlan, combinedResult);
  }

  @Test
  void shouldDuplicateTestPlanWithoutTestCases() {
    var projectId = 1L;
    var testPlanId = 2L;
    var duplicateTestPlanRQ = new TmsTestPlanRQ();

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
    when(tmsTestPlanMapper.duplicateTestPlan(originalTestPlan, duplicateTestPlanRQ))
        .thenReturn(duplicatedTestPlan);
    when(testPlanRepository.save(duplicatedTestPlan)).thenReturn(duplicatedTestPlan);
    when(tmsTestPlanTestCaseRepository.findTestCaseIdsByTestPlanId(testPlanId))
        .thenReturn(List.of());
    when(tmsTestPlanMapper.createFailedBatchResult(eq(Collections.emptyList()), anyString()))
        .thenReturn(emptyResult);
    when(tmsTestPlanMapper.buildDuplicateTestPlanResponse(duplicatedTestPlan, emptyResult))
        .thenReturn(expectedResponse);

    var result = sut.duplicate(projectId, testPlanId, duplicateTestPlanRQ);

    assertNotNull(result);
    assertEquals(3L, result.getId());
    verify(testPlanRepository).findByIdAndProjectId(testPlanId, projectId);
    verify(tmsTestPlanMapper).duplicateTestPlan(originalTestPlan, duplicateTestPlanRQ);
    verify(testPlanRepository).save(duplicatedTestPlan);
    verify(tmsTestPlanAttributeService).createTestPlanAttributes(
        projectId, duplicatedTestPlan, duplicateTestPlanRQ.getAttributes()
    );
    verify(tmsTestCaseService, never()).duplicateTestCases(anyLong(), anyList());
    verify(tmsTestPlanMapper).buildDuplicateTestPlanResponse(duplicatedTestPlan, emptyResult);
  }

  @Test
  void shouldThrowNotFoundWhenDuplicatingNonExistentTestPlan() {
    var projectId = 1L;
    var testPlanId = 2L;
    var duplicateTestPlanRQ = new TmsTestPlanRQ();

    when(testPlanRepository.findByIdAndProjectId(testPlanId, projectId))
        .thenReturn(Optional.empty());

    var exception = assertThrows(ReportPortalException.class, () ->
        sut.duplicate(projectId, testPlanId, duplicateTestPlanRQ)
    );

    assertEquals(ErrorType.NOT_FOUND, exception.getErrorType());
    verify(testPlanRepository).findByIdAndProjectId(testPlanId, projectId);
    verify(tmsTestPlanMapper, never()).duplicateTestPlan(any(), any(TmsTestPlanRQ.class));
    verify(testPlanRepository, never()).save(any());
  }

  @Test
  void shouldHandlePartialTestCaseDuplicationFailure() {
    var projectId = 1L;
    var testPlanId = 2L;
    var duplicateTestPlanRQ = new TmsTestPlanRQ();

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
    when(tmsTestPlanMapper.duplicateTestPlan(originalTestPlan, duplicateTestPlanRQ))
        .thenReturn(duplicatedTestPlan);
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

    var result = sut.duplicate(projectId, testPlanId, duplicateTestPlanRQ);

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
    var duplicateTestPlanRQ = new TmsTestPlanRQ();

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
    when(tmsTestPlanMapper.duplicateTestPlan(originalTestPlan, duplicateTestPlanRQ))
        .thenReturn(duplicatedTestPlan);
    when(testPlanRepository.save(duplicatedTestPlan)).thenReturn(duplicatedTestPlan);
    when(tmsTestPlanTestCaseRepository.findTestCaseIdsByTestPlanId(testPlanId))
        .thenReturn(originalTestCaseIds);
    when(tmsTestCaseService.duplicateTestCases(projectId, originalTestCaseIds))
        .thenReturn(duplicationResult);
    when(tmsTestPlanMapper.buildDuplicateTestPlanResponse(duplicatedTestPlan, duplicationResult))
        .thenReturn(expectedResponse);

    var result = sut.duplicate(projectId, testPlanId, duplicateTestPlanRQ);

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
    var duplicateTestPlanRQ = new TmsTestPlanRQ();

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
    when(tmsTestPlanMapper.duplicateTestPlan(originalTestPlan, duplicateTestPlanRQ))
        .thenReturn(duplicatedTestPlan);
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

    var result = sut.duplicate(projectId, testPlanId, duplicateTestPlanRQ);

    assertNotNull(result);
    assertEquals(3L, result.getId());
    assertNotNull(result.getDuplicationStatistic());
    assertEquals(2, result.getDuplicationStatistic().getFailureCount());
    verify(tmsTestPlanMapper).createFailedBatchResult(eq(duplicatedTestCaseIds), anyString());
    verify(tmsTestPlanMapper).combineDuplicateTestPlanBatchResults(duplicationResult, failedAddResult);
  }

  // Tests for duplicate method WITHOUT RQ (new overload)

  @Test
  void shouldDuplicateTestPlanWithoutRQSuccessfully() {
    // Given
    var originalTestPlan = new TmsTestPlan();
    originalTestPlan.setId(testPlanId);

    var duplicatedTestPlan = new TmsTestPlan();
    duplicatedTestPlan.setId(200L);

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
        .id(200L)
        .duplicationStatistic(combinedResult)
        .build();

    when(testPlanRepository.findByIdAndProjectId(testPlanId, projectId))
        .thenReturn(Optional.of(originalTestPlan));
    when(tmsTestPlanMapper.duplicateTestPlan(originalTestPlan))
        .thenReturn(duplicatedTestPlan);
    when(testPlanRepository.save(duplicatedTestPlan)).thenReturn(duplicatedTestPlan);
    when(tmsTestPlanTestCaseRepository.findTestCaseIdsByTestPlanId(testPlanId))
        .thenReturn(originalTestCaseIds);
    when(tmsTestCaseService.duplicateTestCases(projectId, originalTestCaseIds))
        .thenReturn(duplicationResult);
    when(tmsTestCaseService.getExistingTestCaseIds(projectId, duplicatedTestCaseIds))
        .thenReturn(duplicatedTestCaseIds);
    when(tmsTestPlanTestCaseRepository.findTestCaseIdsByTestPlanId(200L))
        .thenReturn(List.of());
    when(tmsTestPlanTestCaseRepository.insertTestPlanTestCaseIgnoreConflict(eq(200L), anyLong()))
        .thenReturn(1);
    when(testPlanRepository.existsByIdAndProject_Id(200L, projectId)).thenReturn(true);
    when(tmsTestPlanMapper.convertToRS(anyInt(), anyInt(), anyList()))
        .thenReturn(addToPlanResult);
    when(tmsTestPlanMapper.combineDuplicateTestPlanBatchResults(duplicationResult, addToPlanResult))
        .thenReturn(combinedResult);
    when(tmsTestPlanMapper.buildDuplicateTestPlanResponse(duplicatedTestPlan, combinedResult))
        .thenReturn(expectedResponse);

    // When
    var result = sut.duplicate(projectId, testPlanId);

    // Then
    assertNotNull(result);
    assertEquals(200L, result.getId());
    verify(testPlanRepository).findByIdAndProjectId(testPlanId, projectId);
    verify(tmsTestPlanMapper).duplicateTestPlan(originalTestPlan);
    verify(testPlanRepository).save(duplicatedTestPlan);
    verify(tmsTestPlanAttributeService).duplicateTestPlanAttributes(originalTestPlan, duplicatedTestPlan);
    verify(tmsTestPlanTestCaseRepository).findTestCaseIdsByTestPlanId(testPlanId);
    verify(tmsTestCaseService).duplicateTestCases(projectId, originalTestCaseIds);
    verify(tmsTestPlanMapper).buildDuplicateTestPlanResponse(duplicatedTestPlan, combinedResult);
  }

  @Test
  void shouldDuplicateTestPlanWithoutRQWhenNoTestCases() {
    // Given
    var originalTestPlan = new TmsTestPlan();
    originalTestPlan.setId(testPlanId);

    var duplicatedTestPlan = new TmsTestPlan();
    duplicatedTestPlan.setId(200L);

    var emptyResult = BatchTestCaseOperationResultRS.builder()
        .totalCount(0)
        .successCount(0)
        .failureCount(0)
        .errors(List.of())
        .build();

    var expectedResponse = DuplicateTmsTestPlanRS.builder()
        .id(200L)
        .duplicationStatistic(emptyResult)
        .build();

    when(testPlanRepository.findByIdAndProjectId(testPlanId, projectId))
        .thenReturn(Optional.of(originalTestPlan));
    when(tmsTestPlanMapper.duplicateTestPlan(originalTestPlan))
        .thenReturn(duplicatedTestPlan);
    when(testPlanRepository.save(duplicatedTestPlan)).thenReturn(duplicatedTestPlan);
    when(tmsTestPlanTestCaseRepository.findTestCaseIdsByTestPlanId(testPlanId))
        .thenReturn(List.of());
    when(tmsTestPlanMapper.createFailedBatchResult(eq(Collections.emptyList()), anyString()))
        .thenReturn(emptyResult);
    when(tmsTestPlanMapper.buildDuplicateTestPlanResponse(duplicatedTestPlan, emptyResult))
        .thenReturn(expectedResponse);

    // When
    var result = sut.duplicate(projectId, testPlanId);

    // Then
    assertNotNull(result);
    assertEquals(200L, result.getId());
    verify(testPlanRepository).findByIdAndProjectId(testPlanId, projectId);
    verify(tmsTestPlanMapper).duplicateTestPlan(originalTestPlan);
    verify(testPlanRepository).save(duplicatedTestPlan);
    verify(tmsTestPlanAttributeService).duplicateTestPlanAttributes(originalTestPlan, duplicatedTestPlan);
    verify(tmsTestCaseService, never()).duplicateTestCases(anyLong(), anyList());
    verify(tmsTestPlanMapper).buildDuplicateTestPlanResponse(duplicatedTestPlan, emptyResult);
  }

  @Test
  void shouldThrowNotFoundWhenDuplicatingNonExistentTestPlanWithoutRQ() {
    // Given
    when(testPlanRepository.findByIdAndProjectId(testPlanId, projectId))
        .thenReturn(Optional.empty());

    // When/Then
    var exception = assertThrows(ReportPortalException.class, () ->
        sut.duplicate(projectId, testPlanId)
    );

    assertEquals(ErrorType.NOT_FOUND, exception.getErrorType());
    verify(testPlanRepository).findByIdAndProjectId(testPlanId, projectId);
    verify(tmsTestPlanMapper, never()).duplicateTestPlan(any(TmsTestPlan.class));
    verify(testPlanRepository, never()).save(any());
  }

  @Test
  void getTestCasesAddedToPlan_WhenTestPlanExists_ShouldReturnTestCases() {
    var testFolderId = 1L;
    var filter = mock(Filter.class);
    // Given
    var tmsTestCaseInTestPlan = new TmsTestCaseInTestPlanRS();
    tmsTestCaseInTestPlan.setId(testCaseId);

    Page<TmsTestCaseInTestPlanRS> testCasePage = new Page<TmsTestCaseInTestPlanRS>(
        List.of(tmsTestCaseInTestPlan),
        10L,
        0L,
        1L,
        1L
    );

    when(testPlanRepository.existsByIdAndProject_Id(testPlanId, projectId))
        .thenReturn(true);
    when(tmsTestCaseService.getTestCasesInTestPlan(projectId, testPlanId, filter, pageable))
        .thenReturn(testCasePage);

    // When
    var result = sut.getTestCasesAddedToPlan(projectId, testPlanId, filter, pageable);

    // Then
    assertNotNull(result);
    assertEquals(1, result.getContent().size());
    assertEquals(testCaseId, result.getContent().stream().findFirst().orElseThrow().getId());
    verify(testPlanRepository).existsByIdAndProject_Id(testPlanId, projectId);
    verify(tmsTestCaseService).getTestCasesInTestPlan(projectId, testPlanId, filter, pageable);
  }

  @Test
  void getTestCasesAddedToPlan_WhenTestPlanNotFound_ShouldThrowException() {
    var testFolderId = 1L;
    var filter = mock(Filter.class);
    // Given
    when(testPlanRepository.existsByIdAndProject_Id(testPlanId, projectId))
        .thenReturn(false);

    // When/Then
    var exception = assertThrows(ReportPortalException.class,
        () -> sut.getTestCasesAddedToPlan(projectId, testPlanId, filter, pageable));

    assertEquals(ErrorType.NOT_FOUND, exception.getErrorType());
    verify(testPlanRepository).existsByIdAndProject_Id(testPlanId, projectId);
    verifyNoInteractions(tmsTestCaseService);
  }

  @Test
  void getTestCaseInTestPlan_WhenTestPlanExists_ShouldReturnTestCase() {
    // Given
    var testCaseRS = TmsTestCaseInTestPlanRS.builder()
        .id(testCaseId)
        .name("Test Case")
        .build();

    when(testPlanRepository.existsByIdAndProject_Id(testPlanId, projectId))
        .thenReturn(true);
    when(tmsTestCaseService.getTestCaseInTestPlan(projectId, testPlanId, testCaseId))
        .thenReturn(testCaseRS);

    // When
    var result = sut.getTestCaseInTestPlan(projectId, testPlanId, testCaseId);

    // Then
    assertNotNull(result);
    assertEquals(testCaseId, result.getId());
    assertEquals("Test Case", result.getName());
    verify(testPlanRepository).existsByIdAndProject_Id(testPlanId, projectId);
    verify(tmsTestCaseService).getTestCaseInTestPlan(projectId, testPlanId, testCaseId);
  }

  @Test
  void getTestCaseInTestPlan_WhenTestPlanNotFound_ShouldThrowException() {
    // Given
    when(testPlanRepository.existsByIdAndProject_Id(testPlanId, projectId))
        .thenReturn(false);

    // When/Then
    var exception = assertThrows(ReportPortalException.class,
        () -> sut.getTestCaseInTestPlan(projectId, testPlanId, testCaseId));

    assertEquals(ErrorType.NOT_FOUND, exception.getErrorType());
    verify(testPlanRepository).existsByIdAndProject_Id(testPlanId, projectId);
    verifyNoInteractions(tmsTestCaseService);
  }

  @Test
  void verifyTestPlanExists_WhenTestPlanExists_ShouldNotThrowException() {
    // Given
    when(testPlanRepository.existsByIdAndProject_Id(testPlanId, projectId))
        .thenReturn(true);

    // When/Then
    assertDoesNotThrow(() -> sut.verifyTestPlanExists(projectId, testPlanId));
    verify(testPlanRepository).existsByIdAndProject_Id(testPlanId, projectId);
  }

  @Test
  void verifyTestPlanExists_WhenTestPlanNotFound_ShouldThrowException() {
    // Given
    when(testPlanRepository.existsByIdAndProject_Id(testPlanId, projectId))
        .thenReturn(false);

    // When/Then
    var exception = assertThrows(ReportPortalException.class,
        () -> sut.verifyTestPlanExists(projectId, testPlanId));

    assertEquals(ErrorType.NOT_FOUND, exception.getErrorType());
    verify(testPlanRepository).existsByIdAndProject_Id(testPlanId, projectId);
  }

  @Test
  void getTestFoldersFromPlan_WhenTestPlanExists_ShouldReturnFolders() {
    // Given
    var testFolder1 = new TmsTestFolderRS();
    testFolder1.setId(1L);
    testFolder1.setName("Folder 1");
    testFolder1.setCountOfTestCases(5L);

    var testFolder2 = new TmsTestFolderRS();
    testFolder2.setId(2L);
    testFolder2.setName("Folder 2");
    testFolder2.setCountOfTestCases(3L);

    Page<TmsTestFolderRS> expectedPage = new Page<>(
        List.of(testFolder1, testFolder2),
        10L,
        0L,
        2L,
        1L
    );

    when(testPlanRepository.existsByIdAndProject_Id(testPlanId, projectId))
        .thenReturn(true);
    when(tmsTestFolderService.getFoldersByTestPlanId(projectId, testPlanId, pageable))
        .thenReturn(expectedPage);

    // When
    var result = sut.getTestFoldersFromPlan(projectId, testPlanId, pageable);

    // Then
    assertNotNull(result);
    assertEquals(2, result.getContent().size());

    var folders = new ArrayList<>(result.getContent());
    assertEquals(1L, folders.get(0).getId());
    assertEquals("Folder 1", folders.get(0).getName());
    assertEquals(5L, folders.get(0).getCountOfTestCases());
    assertEquals(2L, folders.get(1).getId());
    assertEquals("Folder 2", folders.get(1).getName());
    assertEquals(3L, folders.get(1).getCountOfTestCases());

    verify(testPlanRepository).existsByIdAndProject_Id(testPlanId, projectId);
    verify(tmsTestFolderService).getFoldersByTestPlanId(projectId, testPlanId, pageable);
  }

  @Test
  void getTestFoldersFromPlan_WhenTestPlanNotFound_ShouldThrowException() {
    // Given
    when(testPlanRepository.existsByIdAndProject_Id(testPlanId, projectId))
        .thenReturn(false);

    // When/Then
    var exception = assertThrows(ReportPortalException.class,
        () -> sut.getTestFoldersFromPlan(projectId, testPlanId, pageable));

    assertEquals(ErrorType.NOT_FOUND, exception.getErrorType());
    verify(testPlanRepository).existsByIdAndProject_Id(testPlanId, projectId);
    verifyNoInteractions(tmsTestFolderService);
  }

  @Test
  void getTestFoldersFromPlan_WhenNoFoldersFound_ShouldReturnEmptyPage() {
    // Given
    Page<TmsTestFolderRS> emptyPage = new Page<>(
        Collections.emptyList(),
        10L,
        0L,
        0L,
        0L
    );

    when(testPlanRepository.existsByIdAndProject_Id(testPlanId, projectId))
        .thenReturn(true);
    when(tmsTestFolderService.getFoldersByTestPlanId(projectId, testPlanId, pageable))
        .thenReturn(emptyPage);

    // When
    var result = sut.getTestFoldersFromPlan(projectId, testPlanId, pageable);

    // Then
    assertNotNull(result);
    assertEquals(0, result.getContent().size());
    assertEquals(0L, result.getPage().getTotalElements());

    verify(testPlanRepository).existsByIdAndProject_Id(testPlanId, projectId);
    verify(tmsTestFolderService).getFoldersByTestPlanId(projectId, testPlanId, pageable);
  }

  @Test
  void getTestFoldersFromPlan_WithPagination_ShouldReturnCorrectPage() {
    // Given
    var customPageable = PageRequest.of(1, 5);
    var testFolder = new TmsTestFolderRS();
    testFolder.setId(10L);
    testFolder.setName("Folder 10");
    testFolder.setCountOfTestCases(2L);

    Page<TmsTestFolderRS> expectedPage = new Page<>(
        List.of(testFolder),
        5L,   // size
        1L,   // number (page)
        15L,  // totalElements
        3L    // totalPages
    );

    when(testPlanRepository.existsByIdAndProject_Id(testPlanId, projectId))
        .thenReturn(true);
    when(tmsTestFolderService.getFoldersByTestPlanId(projectId, testPlanId, customPageable))
        .thenReturn(expectedPage);

    // When
    var result = sut.getTestFoldersFromPlan(projectId, testPlanId, customPageable);

    // Then
    assertNotNull(result);
    assertEquals(1, result.getContent().size());
    assertEquals(5L, result.getPage().getSize());
    assertEquals(1L, result.getPage().getNumber());
    assertEquals(15L, result.getPage().getTotalElements());
    assertEquals(3L, result.getPage().getTotalPages());

    verify(testPlanRepository).existsByIdAndProject_Id(testPlanId, projectId);
    verify(tmsTestFolderService).getFoldersByTestPlanId(projectId, testPlanId, customPageable);
  }

  // Tests for milestone-related methods

  @Test
  void removeTestPlanFromMilestone_WhenTestPlanExists_ShouldRemoveSuccessfully() {
    // Given
    when(testPlanRepository.existsByIdAndProject_Id(testPlanId, projectId))
        .thenReturn(true);
    when(testPlanRepository.removeTestPlanFromMilestone(milestoneId, testPlanId, projectId))
        .thenReturn(1);

    // When/Then
    assertDoesNotThrow(() -> sut.removeTestPlanFromMilestone(projectId, milestoneId, testPlanId));

    verify(testPlanRepository).existsByIdAndProject_Id(testPlanId, projectId);
    verify(testPlanRepository).removeTestPlanFromMilestone(milestoneId, testPlanId, projectId);
  }

  @Test
  void removeTestPlanFromMilestone_WhenTestPlanNotFound_ShouldThrowException() {
    // Given
    when(testPlanRepository.existsByIdAndProject_Id(testPlanId, projectId))
        .thenReturn(false);

    // When/Then
    var exception = assertThrows(ReportPortalException.class,
        () -> sut.removeTestPlanFromMilestone(projectId, milestoneId, testPlanId));

    assertEquals(ErrorType.NOT_FOUND, exception.getErrorType());
    verify(testPlanRepository).existsByIdAndProject_Id(testPlanId, projectId);
    verify(testPlanRepository, never()).removeTestPlanFromMilestone(anyLong(), anyLong(), anyLong());
  }

  @Test
  void removeTestPlanFromMilestone_WhenTestPlanNotInMilestone_ShouldThrowException() {
    // Given
    when(testPlanRepository.existsByIdAndProject_Id(testPlanId, projectId))
        .thenReturn(true);
    when(testPlanRepository.removeTestPlanFromMilestone(milestoneId, testPlanId, projectId))
        .thenReturn(0);

    // When/Then
    var exception = assertThrows(ReportPortalException.class,
        () -> sut.removeTestPlanFromMilestone(projectId, milestoneId, testPlanId));

    assertEquals(ErrorType.NOT_FOUND, exception.getErrorType());
    verify(testPlanRepository).existsByIdAndProject_Id(testPlanId, projectId);
    verify(testPlanRepository).removeTestPlanFromMilestone(milestoneId, testPlanId, projectId);
  }

  @Test
  void getByMilestoneId_WhenMilestoneHasTestPlans_ShouldReturnTestPlans() {
    // Given
    var testPlan1 = new TmsTestPlan();
    testPlan1.setId(100L);

    var testPlan2 = new TmsTestPlan();
    testPlan2.setId(200L);

    var testPlanWithStatistic1 = mock(TmsTestPlanWithStatistic.class);
    var testPlanWithStatistic2 = mock(TmsTestPlanWithStatistic.class);

    var testPlanRS1 = new TmsTestPlanRS();
    testPlanRS1.setId(100L);

    var testPlanRS2 = new TmsTestPlanRS();
    testPlanRS2.setId(200L);

    when(testPlanRepository.findIdsByProjectIdAndMilestoneId(projectId, milestoneId))
        .thenReturn(List.of(100L, 200L));
    when(testPlanRepository.findByIdsWithAttributes(List.of(100L, 200L)))
        .thenReturn(List.of(testPlan1, testPlan2));
    when(tmsTestPlanExecutionService.enrichWithStatistics(testPlan1))
        .thenReturn(testPlanWithStatistic1);
    when(tmsTestPlanExecutionService.enrichWithStatistics(testPlan2))
        .thenReturn(testPlanWithStatistic2);
    when(tmsTestPlanMapper.convertTmsTestPlansWithStatisticToRS(
        List.of(testPlanWithStatistic1, testPlanWithStatistic2)))
        .thenReturn(List.of(testPlanRS1, testPlanRS2));

    // When
    var result = sut.getByMilestoneId(projectId, milestoneId);

    // Then
    assertNotNull(result);
    assertEquals(2, result.size());
    assertEquals(100L, result.get(0).getId());
    assertEquals(200L, result.get(1).getId());

    verify(testPlanRepository).findIdsByProjectIdAndMilestoneId(projectId, milestoneId);
    verify(testPlanRepository).findByIdsWithAttributes(List.of(100L, 200L));
    verify(tmsTestPlanExecutionService).enrichWithStatistics(testPlan1);
    verify(tmsTestPlanExecutionService).enrichWithStatistics(testPlan2);
    verify(tmsTestPlanMapper).convertTmsTestPlansWithStatisticToRS(anyList());
  }

  @Test
  void getByMilestoneId_WhenMilestoneHasNoTestPlans_ShouldReturnEmptyList() {
    // Given
    when(testPlanRepository.findIdsByProjectIdAndMilestoneId(projectId, milestoneId))
        .thenReturn(List.of());

    // When
    var result = sut.getByMilestoneId(projectId, milestoneId);

    // Then
    assertNotNull(result);
    assertTrue(result.isEmpty());

    verify(testPlanRepository).findIdsByProjectIdAndMilestoneId(projectId, milestoneId);
    verify(testPlanRepository, never()).findByIdsWithAttributes(anyList());
    verify(tmsTestPlanExecutionService, never()).enrichWithStatistics(any());
    verify(tmsTestPlanMapper, never()).convertTmsTestPlansWithStatisticToRS(anyList());
  }

  @Test
  void getByMilestoneIds_WhenMilestonesHaveTestPlans_ShouldReturnTestPlansGroupedByMilestone() {
    // Given
    var milestone1Id = 50L;
    var milestone2Id = 60L;
    var milestoneIds = List.of(milestone1Id, milestone2Id);

    var testPlan1 = new TmsTestPlan();
    testPlan1.setId(100L);

    var testPlan2 = new TmsTestPlan();
    testPlan2.setId(200L);

    var testPlanWithStatistic1 = mock(TmsTestPlanWithStatistic.class);
    var testPlanWithStatistic2 = mock(TmsTestPlanWithStatistic.class);

    var testPlanRS1 = new TmsTestPlanRS();
    testPlanRS1.setId(100L);
    testPlanRS1.setMilestoneId(milestone1Id);

    var testPlanRS2 = new TmsTestPlanRS();
    testPlanRS2.setId(200L);
    testPlanRS2.setMilestoneId(milestone2Id);

    var expectedMap = Map.of(
        milestone1Id, List.of(testPlanRS1),
        milestone2Id, List.of(testPlanRS2)
    );

    when(testPlanRepository.findIdsByProjectIdAndMilestoneIds(projectId, milestoneIds))
        .thenReturn(List.of(100L, 200L));
    when(testPlanRepository.findByIdsWithAttributes(List.of(100L, 200L)))
        .thenReturn(List.of(testPlan1, testPlan2));
    when(tmsTestPlanExecutionService.enrichWithStatistics(testPlan1))
        .thenReturn(testPlanWithStatistic1);
    when(tmsTestPlanExecutionService.enrichWithStatistics(testPlan2))
        .thenReturn(testPlanWithStatistic2);
    when(tmsTestPlanMapper.convertTmsTestPlansWithStatisticToMap(
        List.of(testPlanWithStatistic1, testPlanWithStatistic2)))
        .thenReturn(expectedMap);

    // When
    var result = sut.getByMilestoneIds(projectId, milestoneIds);

    // Then
    assertNotNull(result);
    assertEquals(2, result.size());
    assertTrue(result.containsKey(milestone1Id));
    assertTrue(result.containsKey(milestone2Id));
    assertEquals(1, result.get(milestone1Id).size());
    assertEquals(1, result.get(milestone2Id).size());
    assertEquals(100L, result.get(milestone1Id).get(0).getId());
    assertEquals(200L, result.get(milestone2Id).get(0).getId());

    verify(testPlanRepository).findIdsByProjectIdAndMilestoneIds(projectId, milestoneIds);
    verify(testPlanRepository).findByIdsWithAttributes(List.of(100L, 200L));
    verify(tmsTestPlanExecutionService).enrichWithStatistics(testPlan1);
    verify(tmsTestPlanExecutionService).enrichWithStatistics(testPlan2);
    verify(tmsTestPlanMapper).convertTmsTestPlansWithStatisticToMap(anyList());
  }

  @Test
  void getByMilestoneIds_WhenMilestonesHaveNoTestPlans_ShouldReturnEmptyMap() {
    // Given
    var milestoneIds = List.of(50L, 60L);

    when(testPlanRepository.findIdsByProjectIdAndMilestoneIds(projectId, milestoneIds))
        .thenReturn(List.of());

    // When
    var result = sut.getByMilestoneIds(projectId, milestoneIds);

    // Then
    assertNotNull(result);
    assertTrue(result.isEmpty());

    verify(testPlanRepository).findIdsByProjectIdAndMilestoneIds(projectId, milestoneIds);
    verify(testPlanRepository, never()).findByIdsWithAttributes(anyList());
    verify(tmsTestPlanExecutionService, never()).enrichWithStatistics(any());
    verify(tmsTestPlanMapper, never()).convertTmsTestPlansWithStatisticToMap(anyList());
  }

  @Test
  void duplicateTestPlansInMilestone_WhenMilestoneHasTestPlans_ShouldDuplicateAll() {
    // Given
    var testPlan1Id = 100L;
    var testPlan2Id = 200L;

    var duplicatedPlan1 = DuplicateTmsTestPlanRS.builder()
        .id(300L)
        .duplicationStatistic(BatchTestCaseOperationResultRS.builder()
            .totalCount(2)
            .successCount(2)
            .failureCount(0)
            .build())
        .build();

    var duplicatedPlan2 = DuplicateTmsTestPlanRS.builder()
        .id(400L)
        .duplicationStatistic(BatchTestCaseOperationResultRS.builder()
            .totalCount(1)
            .successCount(1)
            .failureCount(0)
            .build())
        .build();

    when(testPlanRepository.findIdsByProjectIdAndMilestoneId(projectId, milestoneId))
        .thenReturn(List.of(testPlan1Id, testPlan2Id));

    // Mock duplicate calls for each test plan
    var originalTestPlan1 = new TmsTestPlan();
    originalTestPlan1.setId(testPlan1Id);
    var duplicatedTestPlan1Entity = new TmsTestPlan();
    duplicatedTestPlan1Entity.setId(300L);

    var originalTestPlan2 = new TmsTestPlan();
    originalTestPlan2.setId(testPlan2Id);
    var duplicatedTestPlan2Entity = new TmsTestPlan();
    duplicatedTestPlan2Entity.setId(400L);

    // Setup for first duplicate call
    when(testPlanRepository.findByIdAndProjectId(testPlan1Id, projectId))
        .thenReturn(Optional.of(originalTestPlan1));
    when(tmsTestPlanMapper.duplicateTestPlan(originalTestPlan1))
        .thenReturn(duplicatedTestPlan1Entity);
    when(testPlanRepository.save(duplicatedTestPlan1Entity))
        .thenReturn(duplicatedTestPlan1Entity);
    when(tmsTestPlanTestCaseRepository.findTestCaseIdsByTestPlanId(testPlan1Id))
        .thenReturn(List.of());
    when(tmsTestPlanMapper.createFailedBatchResult(eq(Collections.emptyList()), anyString()))
        .thenReturn(duplicatedPlan1.getDuplicationStatistic());
    when(tmsTestPlanMapper.buildDuplicateTestPlanResponse(eq(duplicatedTestPlan1Entity), any()))
        .thenReturn(duplicatedPlan1);

    // Setup for second duplicate call
    when(testPlanRepository.findByIdAndProjectId(testPlan2Id, projectId))
        .thenReturn(Optional.of(originalTestPlan2));
    when(tmsTestPlanMapper.duplicateTestPlan(originalTestPlan2))
        .thenReturn(duplicatedTestPlan2Entity);
    when(testPlanRepository.save(duplicatedTestPlan2Entity))
        .thenReturn(duplicatedTestPlan2Entity);
    when(tmsTestPlanTestCaseRepository.findTestCaseIdsByTestPlanId(testPlan2Id))
        .thenReturn(List.of());
    when(tmsTestPlanMapper.buildDuplicateTestPlanResponse(eq(duplicatedTestPlan2Entity), any()))
        .thenReturn(duplicatedPlan2);

    // When
    var result = sut.duplicateTestPlansInMilestone(projectId, milestoneId);

    // Then
    assertNotNull(result);
    assertEquals(2, result.size());
    assertEquals(300L, result.get(0).getId());
    assertEquals(400L, result.get(1).getId());

    verify(testPlanRepository).findIdsByProjectIdAndMilestoneId(projectId, milestoneId);
    verify(testPlanRepository).findByIdAndProjectId(testPlan1Id, projectId);
    verify(testPlanRepository).findByIdAndProjectId(testPlan2Id, projectId);
    verify(tmsTestPlanMapper).duplicateTestPlan(originalTestPlan1);
    verify(tmsTestPlanMapper).duplicateTestPlan(originalTestPlan2);
  }

  @Test
  void duplicateTestPlansInMilestone_WhenMilestoneHasNoTestPlans_ShouldReturnEmptyList() {
    // Given
    when(testPlanRepository.findIdsByProjectIdAndMilestoneId(projectId, milestoneId))
        .thenReturn(List.of());

    // When
    var result = sut.duplicateTestPlansInMilestone(projectId, milestoneId);

    // Then
    assertNotNull(result);
    assertTrue(result.isEmpty());

    verify(testPlanRepository).findIdsByProjectIdAndMilestoneId(projectId, milestoneId);
    verify(testPlanRepository, never()).findByIdAndProjectId(anyLong(), anyLong());
    verify(tmsTestPlanMapper, never()).duplicateTestPlan(any(TmsTestPlan.class));
  }

  @Test
  void addTestPlanMilestone_WhenTestPlanExists_ShouldAddSuccessfully() {
    // Given
    when(testPlanRepository.existsByIdAndProject_Id(testPlanId, projectId))
        .thenReturn(true);

    // When/Then
    assertDoesNotThrow(() -> sut.addTestPlanMilestone(projectId, milestoneId, testPlanId));

    verify(testPlanRepository).existsByIdAndProject_Id(testPlanId, projectId);
    verify(testPlanRepository).addTestPlanToMilestone(milestoneId, testPlanId, projectId);
  }

  @Test
  void addTestPlanMilestone_WhenTestPlanNotFound_ShouldThrowException() {
    // Given
    when(testPlanRepository.existsByIdAndProject_Id(testPlanId, projectId))
        .thenReturn(false);

    // When/Then
    var exception = assertThrows(ReportPortalException.class,
        () -> sut.addTestPlanMilestone(projectId, milestoneId, testPlanId));

    assertEquals(ErrorType.NOT_FOUND, exception.getErrorType());
    verify(testPlanRepository).existsByIdAndProject_Id(testPlanId, projectId);
    verify(testPlanRepository, never()).addTestPlanToMilestone(anyLong(), anyLong(), anyLong());
  }

  @Test
  void removeTestPlansFromMilestone_ShouldCallRepository() {
    // Given/When
    assertDoesNotThrow(() -> sut.removeTestPlansFromMilestone(projectId, milestoneId));

    // Then
    verify(testPlanRepository).removeTestPlansFromMilestone(milestoneId, projectId);
  }
}
