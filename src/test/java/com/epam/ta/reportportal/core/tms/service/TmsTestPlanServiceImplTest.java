package com.epam.ta.reportportal.core.tms.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.reportportal.rules.exception.ErrorType;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.core.tms.db.entity.TmsTestPlan;
import com.epam.ta.reportportal.core.tms.db.repository.TmsTestPlanRepository;
import com.epam.ta.reportportal.core.tms.dto.TmsTestPlanRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsTestPlanRS;
import com.epam.ta.reportportal.core.tms.mapper.TmsTestPlanMapper;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class TmsTestPlanServiceImplTest {

  @Mock
  private TmsTestPlanRepository testPlanRepository;

  @Mock
  private TmsTestPlanMapper tmsTestPlanMapper;


  @Mock
  private TmsMilestoneService tmsMilestoneService;

  @Mock
  private TmsTestPlanAttributeService tmsTestPlanAttributeService;

  @InjectMocks
  private TmsTestPlanServiceImpl sut;

  @Test
  void shouldGetByIdSuccess() {
    var projectId = 1L;
    var testPlanId = 2L;

    var testPlan = new TmsTestPlan();
    var testPlanRS = new TmsTestPlanRS();

    when(testPlanRepository.findByIdAndProjectId(testPlanId, projectId)).thenReturn(
        Optional.of(testPlan));
    when(tmsTestPlanMapper.convertToRS(testPlan)).thenReturn(testPlanRS);

    var result = assertDoesNotThrow(() -> sut.getById(projectId, testPlanId));

    assertEquals(testPlanRS, result);

    verify(testPlanRepository).findByIdAndProjectId(testPlanId, projectId);
    verify(tmsTestPlanMapper).convertToRS(testPlan);
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

    assertTrue(exception.getErrorType().equals(ErrorType.NOT_FOUND));
    verify(testPlanRepository).findByIdAndProjectId(testPlanId, projectId);
  }

  @Test
  void shouldCreate() {
    var projectId = 1L;
    var testPlanRQ = new TmsTestPlanRQ();
    var testPlan = new TmsTestPlan();
    var testPlanRS = new TmsTestPlanRS();

    when(tmsTestPlanMapper.convertFromRQ(projectId, testPlanRQ)).thenReturn(testPlan);
    when(tmsTestPlanMapper.convertToRS(testPlan)).thenReturn(testPlanRS);

    var result = sut.create(projectId, testPlanRQ);

    assertEquals(testPlanRS, result);
    verify(tmsTestPlanMapper).convertFromRQ(projectId, testPlanRQ);
    verify(testPlanRepository).save(testPlan);
    verify(tmsTestPlanAttributeService).createTestPlanAttributes(testPlan,
        testPlanRQ.getTags());
    verify(tmsMilestoneService).createTestPlanMilestones(testPlan,
        testPlanRQ.getMilestoneIds());
  }

  @Test
  void shouldDelete() {
    var projectId = 1L;
    var testPlanId = 2L;

    assertDoesNotThrow(() -> sut.delete(projectId, testPlanId));

    verify(tmsTestPlanAttributeService).deleteAllByTestPlanId(testPlanId);
    verify(tmsMilestoneService).detachTestPlanFromMilestones(testPlanId);
    verify(testPlanRepository).deleteByIdAndProject_Id(testPlanId, projectId);
  }

  @Test
  void shouldGetByCriteria() {
    var projectId = 1L;
    List<Long> environmentIds = Collections.singletonList(1L);
    List<Long> productVersionIds = Collections.singletonList(1L);
    var pageable = PageRequest.of(0, 10);

    var testPlan = new TmsTestPlan();
    var testPlanRS = new TmsTestPlanRS();
    Page<TmsTestPlan> testPlanPage = new PageImpl<>(List.of(testPlan));

    when(testPlanRepository.findByCriteria(projectId, environmentIds, productVersionIds,
        pageable)).thenReturn(testPlanPage);
    when(tmsTestPlanMapper.convertToRS(testPlanPage)).thenReturn(
        new PageImpl<>(List.of(testPlanRS)));

    var result = assertDoesNotThrow(
        () -> sut.getByCriteria(projectId, environmentIds, productVersionIds, pageable));

    assertNotNull(result);
    assertEquals(1, result.getContent().size());
    assertEquals(testPlanRS, result.getContent().get(0));
    verify(testPlanRepository).findByCriteria(projectId, environmentIds, productVersionIds,
        pageable);
    verify(tmsTestPlanMapper).convertToRS(testPlanPage);
  }

  @Test
  void shouldUpdateExisting() {
    var projectId = 1L;
    var testPlanId = 2L;
    var testPlanRQ = new TmsTestPlanRQ();
    var existingTestPlan = new TmsTestPlan();
    var updatedTestPlan = new TmsTestPlan();
    var testPlanRS = new TmsTestPlanRS();

    when(testPlanRepository.findByIdAndProjectId(testPlanId, projectId)).thenReturn(
        Optional.of(existingTestPlan));
    when(tmsTestPlanMapper.convertFromRQ(projectId, testPlanRQ)).thenReturn(updatedTestPlan);
    when(tmsTestPlanMapper.convertToRS(existingTestPlan)).thenReturn(testPlanRS);

    var result = assertDoesNotThrow(() -> sut.update(projectId, testPlanId, testPlanRQ));

    assertEquals(testPlanRS, result);
    verify(testPlanRepository).findByIdAndProjectId(testPlanId, projectId);
    verify(tmsTestPlanMapper).update(existingTestPlan, updatedTestPlan);
    verify(tmsTestPlanAttributeService).updateTestPlanAttributes(existingTestPlan,
        testPlanRQ.getTags());
    verify(tmsMilestoneService).updateTestPlanMilestones(existingTestPlan,
        testPlanRQ.getMilestoneIds());
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
    when(tmsTestPlanMapper.convertToRS(testPlan)).thenReturn(testPlanRS);

    var result = assertDoesNotThrow(() -> sut.update(projectId, testPlanId, testPlanRQ));

    assertEquals(testPlanRS, result);
    verify(testPlanRepository).findByIdAndProjectId(testPlanId, projectId);
    verify(testPlanRepository).save(testPlan);
    verify(tmsTestPlanAttributeService).createTestPlanAttributes(testPlan,
        testPlanRQ.getTags());
    verify(tmsMilestoneService).createTestPlanMilestones(testPlan,
        testPlanRQ.getMilestoneIds());
  }

  @Test
  void shouldPatchExisting() {
    var projectId = 1L;
    var testPlanId = 2L;
    var testPlanRQ = new TmsTestPlanRQ();
    var existingTestPlan = new TmsTestPlan();
    var patchedTestPlan = new TmsTestPlan();
    var testPlanRS = new TmsTestPlanRS();

    when(testPlanRepository.findByIdAndProjectId(testPlanId, projectId)).thenReturn(
        Optional.of(existingTestPlan));
    when(tmsTestPlanMapper.convertFromRQ(projectId, testPlanRQ)).thenReturn(patchedTestPlan);
    when(tmsTestPlanMapper.convertToRS(existingTestPlan)).thenReturn(testPlanRS);

    var result = assertDoesNotThrow(() -> sut.patch(projectId, testPlanId, testPlanRQ));

    assertEquals(testPlanRS, result);
    verify(testPlanRepository).findByIdAndProjectId(testPlanId, projectId);
    verify(tmsTestPlanMapper).patch(existingTestPlan, patchedTestPlan);
    verify(tmsTestPlanAttributeService).patchTestPlanAttributes(existingTestPlan,
        testPlanRQ.getTags());
    verify(tmsMilestoneService).patchTestPlanMilestones(existingTestPlan,
        testPlanRQ.getMilestoneIds());
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

    assertTrue(exception.getErrorType().equals(ErrorType.NOT_FOUND));
    verify(testPlanRepository).findByIdAndProjectId(testPlanId, projectId);
  }
}
