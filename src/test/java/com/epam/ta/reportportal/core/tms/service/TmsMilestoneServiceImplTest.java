package com.epam.ta.reportportal.core.tms.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.epam.ta.reportportal.core.tms.db.entity.TmsMilestone;
import com.epam.ta.reportportal.core.tms.db.entity.TmsTestPlan;
import com.epam.ta.reportportal.core.tms.db.repository.TmsMilestoneRepository;
import com.epam.ta.reportportal.core.tms.mapper.TmsMilestoneMapper;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TmsMilestoneServiceImplTest {

  @Mock
  private TmsMilestoneMapper tmsMilestoneMapper;

  @Mock
  private TmsMilestoneRepository tmsMilestoneRepository;

  @InjectMocks
  private TmsMilestoneServiceImpl tmsMilestoneService;

  private TmsTestPlan testPlan;
  private List<Long> milestoneIds;
  private Set<TmsMilestone> milestones;

  @BeforeEach
  void setUp() {
    testPlan = new TmsTestPlan();
    testPlan.setId(1L);
    testPlan.setMilestones(new HashSet<>());

    milestoneIds = Arrays.asList(1L, 2L, 3L);

    milestones = new HashSet<>();
    milestones.add(createMilestone(1L));
    milestones.add(createMilestone(2L));
    milestones.add(createMilestone(3L));
  }

  private TmsMilestone createMilestone(Long id) {
    TmsMilestone milestone = new TmsMilestone();
    milestone.setId(id);
    return milestone;
  }

  @Test
  void createTestPlanMilestones_WithNullMilestoneIds_ShouldDoNothing() {
    // When
    tmsMilestoneService.createTestPlanMilestones(testPlan, null);

    // Then
    verifyNoInteractions(tmsMilestoneMapper, tmsMilestoneRepository);
    assertTrue(testPlan.getMilestones().isEmpty());
  }

  @Test
  void createTestPlanMilestones_WithEmptyMilestoneIds_ShouldDoNothing() {
    // When
    tmsMilestoneService.createTestPlanMilestones(testPlan, Collections.emptyList());

    // Then
    verifyNoInteractions(tmsMilestoneMapper, tmsMilestoneRepository);
    assertTrue(testPlan.getMilestones().isEmpty());
  }

  @Test
  void createTestPlanMilestones_WithValidMilestoneIds_ShouldCreateMilestones() {
    // Given
    when(tmsMilestoneMapper.convertToTmsMilestones(milestoneIds)).thenReturn(milestones);

    // When
    tmsMilestoneService.createTestPlanMilestones(testPlan, milestoneIds);

    // Then
    verify(tmsMilestoneMapper).convertToTmsMilestones(milestoneIds);

    for (TmsMilestone milestone : milestones) {
      verify(tmsMilestoneRepository).attachTestPlanToMilestone(testPlan, milestone.getId());
      assertEquals(testPlan, milestone.getTestPlan());
    }

    assertEquals(milestones, testPlan.getMilestones());
  }

  @Test
  void patchTestPlanMilestones_WithNullMilestoneIds_ShouldDoNothing() {
    // When
    tmsMilestoneService.patchTestPlanMilestones(testPlan, null);

    // Then
    verifyNoInteractions(tmsMilestoneMapper, tmsMilestoneRepository);
    assertTrue(testPlan.getMilestones().isEmpty());
  }

  @Test
  void patchTestPlanMilestones_WithEmptyMilestoneIds_ShouldDoNothing() {
    // When
    tmsMilestoneService.patchTestPlanMilestones(testPlan, Collections.emptyList());

    // Then
    verifyNoInteractions(tmsMilestoneMapper, tmsMilestoneRepository);
    assertTrue(testPlan.getMilestones().isEmpty());
  }

  @Test
  void patchTestPlanMilestones_WithValidMilestoneIds_ShouldAddMilestones() {
    // Given
    when(tmsMilestoneMapper.convertToTmsMilestones(milestoneIds)).thenReturn(milestones);

    // Add some existing milestones
    TmsMilestone existingMilestone = createMilestone(4L);
    testPlan.getMilestones().add(existingMilestone);

    // When
    tmsMilestoneService.patchTestPlanMilestones(testPlan, milestoneIds);

    // Then
    verify(tmsMilestoneMapper).convertToTmsMilestones(milestoneIds);

    for (TmsMilestone milestone : milestones) {
      verify(tmsMilestoneRepository).attachTestPlanToMilestone(testPlan, milestone.getId());
      assertEquals(testPlan, milestone.getTestPlan());
    }

    assertEquals(4, testPlan.getMilestones().size());
    assertTrue(testPlan.getMilestones().contains(existingMilestone));
    assertTrue(testPlan.getMilestones().containsAll(milestones));
  }

  @Test
  void updateTestPlanMilestones_WithNullMilestoneIds_ShouldDoNothing() {
    // When
    tmsMilestoneService.updateTestPlanMilestones(testPlan, null);

    // Then
    verifyNoInteractions(tmsMilestoneMapper, tmsMilestoneRepository);
    assertTrue(testPlan.getMilestones().isEmpty());
  }

  @Test
  void updateTestPlanMilestones_WithEmptyMilestoneIds_ShouldDoNothing() {
    // When
    tmsMilestoneService.updateTestPlanMilestones(testPlan, Collections.emptyList());

    // Then
    verifyNoInteractions(tmsMilestoneMapper, tmsMilestoneRepository);
    assertTrue(testPlan.getMilestones().isEmpty());
  }

  @Test
  void updateTestPlanMilestones_WithValidMilestoneIds_ShouldUpdateMilestones() {
    // Given
    when(tmsMilestoneMapper.convertToTmsMilestones(milestoneIds)).thenReturn(milestones);

    // Add some existing milestones
    TmsMilestone existingMilestone = createMilestone(4L);
    testPlan.getMilestones().add(existingMilestone);

    // When
    tmsMilestoneService.updateTestPlanMilestones(testPlan, milestoneIds);

    // Then
    verify(tmsMilestoneRepository).detachTestPlanFromMilestones(testPlan.getId());
    verify(tmsMilestoneMapper).convertToTmsMilestones(milestoneIds);

    for (TmsMilestone milestone : milestones) {
      verify(tmsMilestoneRepository).attachTestPlanToMilestone(testPlan, milestone.getId());
      assertEquals(testPlan, milestone.getTestPlan());
    }

    assertEquals(milestones, testPlan.getMilestones());
    assertFalse(testPlan.getMilestones().contains(existingMilestone));
  }

  @Test
  void detachTestPlanFromMilestones_ShouldCallRepository() {
    // When
    tmsMilestoneService.detachTestPlanFromMilestones(testPlan.getId());

    // Then
    verify(tmsMilestoneRepository).detachTestPlanFromMilestones(testPlan.getId());
  }
}
