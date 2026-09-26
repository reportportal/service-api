package com.epam.reportportal.base.core.tms.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.reportportal.base.core.tms.dto.TmsQualityStandardCriterionRQ;
import com.epam.reportportal.base.core.tms.dto.TmsQualityStandardRQ;
import com.epam.reportportal.base.core.tms.dto.TmsQualityStandardRS;
import com.epam.reportportal.base.core.tms.mapper.TmsQualityStandardMapper;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsQualityStandardRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsTestCaseQualityScoreRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsQualityStandard;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsQualityStandardCriterion;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TmsQualityStandardServiceImplTest {

  @Mock
  private TmsQualityStandardRepository tmsQualityStandardRepository;
  @Mock
  private TmsTestCaseQualityScoreRepository tmsTestCaseQualityScoreRepository;
  @Mock
  private TmsQualityStandardMapper tmsQualityStandardMapper;

  @InjectMocks
  private TmsQualityStandardServiceImpl sut;

  private Long projectId;
  private TmsQualityStandardRQ rq;

  @BeforeEach
  void setUp() {
    projectId = 1L;
    rq = TmsQualityStandardRQ.builder()
        .name("Default TC quality rubric")
        .description("6-criteria, sum/100")
        .criteria(List.of(TmsQualityStandardCriterionRQ.builder()
            .name("Scenario correctness").maxPoints(25).sequence(1).build()))
        .build();
  }

  @Test
  void getStandard_WhenExists_ShouldReturnMappedRS() {
    var standard = new TmsQualityStandard();
    standard.setId(5L);
    standard.setProjectId(projectId);
    var expected = TmsQualityStandardRS.builder().id(5L).build();

    when(tmsQualityStandardRepository.findByProjectId(projectId)).thenReturn(Optional.of(standard));
    when(tmsQualityStandardMapper.toRS(standard)).thenReturn(expected);

    var result = sut.getStandard(projectId);

    assertEquals(expected, result);
  }

  @Test
  void getStandard_WhenNotFound_ShouldThrowNotFound() {
    when(tmsQualityStandardRepository.findByProjectId(projectId)).thenReturn(Optional.empty());

    assertThrows(ReportPortalException.class, () -> sut.getStandard(projectId));
  }

  @Test
  void createStandard_WhenNoneExists_ShouldCreateStandardWithProjectIdAndMergedCriteria() {
    var criteria = List.of(new TmsQualityStandardCriterion());
    var expected = TmsQualityStandardRS.builder().id(5L).build();

    when(tmsQualityStandardRepository.existsByProjectId(projectId)).thenReturn(false);
    when(tmsQualityStandardMapper.mergeCriteria(any(), eq(rq.getCriteria()))).thenReturn(criteria);
    when(tmsQualityStandardRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    when(tmsQualityStandardMapper.toRS(any())).thenReturn(expected);

    var result = sut.createStandard(projectId, rq);

    assertEquals(expected, result);

    var standardCaptor = ArgumentCaptor.forClass(TmsQualityStandard.class);
    verify(tmsQualityStandardRepository).save(standardCaptor.capture());
    assertEquals(projectId, standardCaptor.getValue().getProjectId());
    assertEquals("Default TC quality rubric", standardCaptor.getValue().getName());
    assertEquals(criteria, standardCaptor.getValue().getCriteria());
  }

  @Test
  void createStandard_WhenAlreadyExists_ShouldThrowResourceAlreadyExistsWithoutSaving() {
    when(tmsQualityStandardRepository.existsByProjectId(projectId)).thenReturn(true);

    assertThrows(ReportPortalException.class, () -> sut.createStandard(projectId, rq));
    verify(tmsQualityStandardRepository, never()).save(any());
  }

  @Test
  void updateStandard_WhenExists_ShouldMergeCriteriaOntoExistingStandardAndSave() {
    var standard = new TmsQualityStandard();
    standard.setId(5L);
    standard.setProjectId(projectId);
    standard.setCriteria(new java.util.ArrayList<>(List.of(new TmsQualityStandardCriterion())));
    var mergedCriteria = List.of(new TmsQualityStandardCriterion(), new TmsQualityStandardCriterion());
    var expected = TmsQualityStandardRS.builder().id(5L).build();

    when(tmsQualityStandardRepository.findByProjectId(projectId)).thenReturn(Optional.of(standard));
    when(tmsQualityStandardMapper.mergeCriteria(standard, rq.getCriteria())).thenReturn(mergedCriteria);
    when(tmsQualityStandardRepository.save(standard)).thenReturn(standard);
    when(tmsQualityStandardMapper.toRS(standard)).thenReturn(expected);

    var result = sut.updateStandard(projectId, rq);

    assertEquals(expected, result);
    assertEquals("Default TC quality rubric", standard.getName());
    assertEquals(mergedCriteria, standard.getCriteria());
  }

  @Test
  void updateStandard_WhenRemovingCriterionWithoutScores_ShouldMergeAndSave() {
    var keptCriterion = new TmsQualityStandardCriterion();
    keptCriterion.setId(900L);
    var removedCriterion = new TmsQualityStandardCriterion();
    removedCriterion.setId(901L);
    var standard = new TmsQualityStandard();
    standard.setId(5L);
    standard.setProjectId(projectId);
    standard.setCriteria(new java.util.ArrayList<>(List.of(keptCriterion, removedCriterion)));
    var keepOnlyRq = TmsQualityStandardRQ.builder()
        .name("Default TC quality rubric")
        .criteria(List.of(TmsQualityStandardCriterionRQ.builder()
            .id(900L).name("Scenario correctness").maxPoints(100).sequence(1).build()))
        .build();
    var mergedCriteria = List.of(keptCriterion);
    var expected = TmsQualityStandardRS.builder().id(5L).build();

    when(tmsQualityStandardRepository.findByProjectId(projectId)).thenReturn(Optional.of(standard));
    when(tmsTestCaseQualityScoreRepository.findDistinctCriterionIdsIn(java.util.Set.of(901L)))
        .thenReturn(List.of());
    when(tmsQualityStandardMapper.mergeCriteria(standard, keepOnlyRq.getCriteria())).thenReturn(mergedCriteria);
    when(tmsQualityStandardRepository.save(standard)).thenReturn(standard);
    when(tmsQualityStandardMapper.toRS(standard)).thenReturn(expected);

    var result = sut.updateStandard(projectId, keepOnlyRq);

    assertEquals(expected, result);
    assertEquals(mergedCriteria, standard.getCriteria());
  }

  @Test
  void updateStandard_WhenRemovingCriterionWithExistingScores_ShouldRejectWithoutSaving() {
    var keptCriterion = new TmsQualityStandardCriterion();
    keptCriterion.setId(900L);
    var removedCriterion = new TmsQualityStandardCriterion();
    removedCriterion.setId(901L);
    var standard = new TmsQualityStandard();
    standard.setId(5L);
    standard.setProjectId(projectId);
    standard.setCriteria(new java.util.ArrayList<>(List.of(keptCriterion, removedCriterion)));
    var keepOnlyRq = TmsQualityStandardRQ.builder()
        .name("Default TC quality rubric")
        .criteria(List.of(TmsQualityStandardCriterionRQ.builder()
            .id(900L).name("Scenario correctness").maxPoints(100).sequence(1).build()))
        .build();

    when(tmsQualityStandardRepository.findByProjectId(projectId)).thenReturn(Optional.of(standard));
    when(tmsTestCaseQualityScoreRepository.findDistinctCriterionIdsIn(java.util.Set.of(901L)))
        .thenReturn(List.of(901L));

    assertThrows(ReportPortalException.class, () -> sut.updateStandard(projectId, keepOnlyRq));
    verify(tmsQualityStandardRepository, never()).save(any());
  }

  @Test
  void updateStandard_WhenNotFound_ShouldThrowNotFoundWithoutSaving() {
    when(tmsQualityStandardRepository.findByProjectId(projectId)).thenReturn(Optional.empty());

    assertThrows(ReportPortalException.class, () -> sut.updateStandard(projectId, rq));
    verify(tmsQualityStandardRepository, never()).save(any());
  }

  @Test
  void deleteStandard_ShouldDelegateToRepository() {
    sut.deleteStandard(projectId);

    verify(tmsQualityStandardRepository).deleteByProjectId(projectId);
  }
}
