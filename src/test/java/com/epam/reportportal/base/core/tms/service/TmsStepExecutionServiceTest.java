package com.epam.reportportal.base.core.tms.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.reportportal.base.infrastructure.persistence.dao.TmsStepExecutionRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.item.TestItem;
import com.epam.reportportal.base.infrastructure.persistence.entity.launch.Launch;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsStepExecution;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TmsStepExecutionServiceTest {

  private final Long launchId = 100L;
  private final Long testCaseExecutionId = 200L;
  @Mock
  private TmsStepExecutionRepository tmsStepExecutionRepository;
  @InjectMocks
  private TmsStepExecutionService sut;
  @Captor
  private ArgumentCaptor<List<TmsStepExecution>> listCaptor;
  private Launch launch;

  @BeforeEach
  void setUp() {
    launch = new Launch();
    launch.setId(launchId);
  }

  // -------------------------------------------------------------------------
  // CREATE
  // -------------------------------------------------------------------------

  @Test
  void createTmsStepExecutions_WithEmptySteps_ShouldDoNothing() {
    sut.createTmsStepExecutions(testCaseExecutionId, Collections.emptyList(), launch,
        Collections.emptyList());

    verify(tmsStepExecutionRepository, never()).saveAll(anyList());
  }

  @Test
  void createTmsStepExecutions_WithValidStepsAndTmsIds_ShouldCreateAndSave() {
    TestItem step1 = new TestItem();
    step1.setItemId(1L);
    TestItem step2 = new TestItem();
    step2.setItemId(2L);

    List<TestItem> steps = List.of(step1, step2);
    List<Long> tmsStepIds = List.of(10L, 20L);

    when(tmsStepExecutionRepository.saveAll(anyList())).thenReturn(Collections.emptyList());

    sut.createTmsStepExecutions(testCaseExecutionId, steps, launch, tmsStepIds);

    verify(tmsStepExecutionRepository).saveAll(listCaptor.capture());
    List<TmsStepExecution> savedExecutions = listCaptor.getValue();

    assertEquals(2, savedExecutions.size());

    assertEquals(testCaseExecutionId, savedExecutions.get(0).getTestCaseExecutionId());
    assertEquals(step1, savedExecutions.get(0).getTestItem());
    assertEquals(launchId, savedExecutions.get(0).getLaunchId());
    assertEquals(10L, savedExecutions.get(0).getTmsStepId());

    assertEquals(testCaseExecutionId, savedExecutions.get(1).getTestCaseExecutionId());
    assertEquals(step2, savedExecutions.get(1).getTestItem());
    assertEquals(launchId, savedExecutions.get(1).getLaunchId());
    assertEquals(20L, savedExecutions.get(1).getTmsStepId());
  }

  @Test
  void createTmsStepExecutions_WithoutTmsIds_ShouldCreateWithNullTmsIds() {
    TestItem step1 = new TestItem();
    step1.setItemId(1L);

    List<TestItem> steps = List.of(step1);

    when(tmsStepExecutionRepository.saveAll(anyList())).thenReturn(Collections.emptyList());

    sut.createTmsStepExecutions(testCaseExecutionId, steps, launch, null);

    verify(tmsStepExecutionRepository).saveAll(listCaptor.capture());
    List<TmsStepExecution> savedExecutions = listCaptor.getValue();

    assertEquals(1, savedExecutions.size());
    assertNotNull(savedExecutions.get(0).getTestItem());
    assertEquals(launchId, savedExecutions.get(0).getLaunchId());
    assertNull(savedExecutions.get(0).getTmsStepId());
  }

  // -------------------------------------------------------------------------
  // RETRIEVE
  // -------------------------------------------------------------------------

  @Test
  void getStepExecutionsByTestCaseExecution_ShouldReturnList() {
    TmsStepExecution execution = new TmsStepExecution();
    when(tmsStepExecutionRepository.findByTestCaseExecutionId(testCaseExecutionId))
        .thenReturn(List.of(execution));

    var result = sut.getStepExecutionsByTestCaseExecution(testCaseExecutionId);

    assertNotNull(result);
    assertEquals(1, result.size());
    verify(tmsStepExecutionRepository).findByTestCaseExecutionId(testCaseExecutionId);
  }

  // -------------------------------------------------------------------------
  // DELETE
  // -------------------------------------------------------------------------

  @Test
  void deleteStepExecutionsByTestCaseExecution_ShouldDelete() {
    sut.deleteStepExecutionsByTestCaseExecution(testCaseExecutionId);
    verify(tmsStepExecutionRepository).deleteByTestCaseExecutionId(testCaseExecutionId);
  }

  @Test
  void deleteByLaunchId_ShouldDelete() {
    sut.deleteByLaunchId(launchId);
    verify(tmsStepExecutionRepository).deleteByLaunchId(launchId);
  }
}
