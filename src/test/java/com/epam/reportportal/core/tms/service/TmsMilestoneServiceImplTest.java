package com.epam.reportportal.core.tms.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.reportportal.core.tms.dto.DuplicateTmsMilestoneRS;
import com.epam.reportportal.core.tms.dto.TmsMilestoneRQ;
import com.epam.reportportal.core.tms.dto.TmsMilestoneRS;
import com.epam.reportportal.core.tms.dto.TmsTestPlanRS;
import com.epam.reportportal.core.tms.mapper.TmsMilestoneMapper;
import com.epam.reportportal.infrastructure.persistence.dao.tms.TmsMilestoneRepository;
import com.epam.reportportal.infrastructure.persistence.entity.tms.TmsMilestone;
import com.epam.reportportal.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.infrastructure.rules.exception.ReportPortalException;
import java.util.Collections;
import java.util.HashMap;
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
class TmsMilestoneServiceImplTest {

  @Mock
  private TmsMilestoneMapper tmsMilestoneMapper;

  @Mock
  private TmsMilestoneRepository tmsMilestoneRepository;

  @Mock
  private TmsTestPlanService tmsTestPlanService;

  @InjectMocks
  private TmsMilestoneServiceImpl sut;

  private Long projectId;
  private Long milestoneId;
  private Long testPlanId;
  private Pageable pageable;

  @BeforeEach
  void setUp() {
    projectId = 1L;
    milestoneId = 100L;
    testPlanId = 200L;
    pageable = PageRequest.of(0, 10);
  }

  // Tests for create method

  @Test
  void create_WhenValidRequest_ShouldCreateMilestoneSuccessfully() {
    // Given
    var milestoneRQ = new TmsMilestoneRQ();
    milestoneRQ.setName("Release 1.0");

    var milestoneEntity = new TmsMilestone();
    milestoneEntity.setName("Release 1.0");

    var savedMilestoneEntity = new TmsMilestone();
    savedMilestoneEntity.setId(milestoneId);
    savedMilestoneEntity.setName("Release 1.0");

    var expectedRS = new TmsMilestoneRS();
    expectedRS.setId(milestoneId);
    expectedRS.setName("Release 1.0");
    expectedRS.setTestPlans(Collections.emptyList());

    when(tmsMilestoneMapper.toEntity(projectId, milestoneRQ)).thenReturn(milestoneEntity);
    when(tmsMilestoneRepository.save(milestoneEntity)).thenReturn(savedMilestoneEntity);
    when(tmsMilestoneMapper.convert(savedMilestoneEntity, Collections.emptyList()))
        .thenReturn(expectedRS);

    // When
    var result = sut.create(projectId, milestoneRQ);

    // Then
    assertNotNull(result);
    assertEquals(milestoneId, result.getId());
    assertEquals("Release 1.0", result.getName());
    assertTrue(result.getTestPlans().isEmpty());

    verify(tmsMilestoneMapper).toEntity(projectId, milestoneRQ);
    verify(tmsMilestoneRepository).save(milestoneEntity);
    verify(tmsMilestoneMapper).convert(savedMilestoneEntity, Collections.emptyList());
  }

  @Test
  void create_WhenMilestoneWithoutProductVersion_ShouldCreateSuccessfully() {
    // Given
    var milestoneRQ = new TmsMilestoneRQ();
    milestoneRQ.setName("Sprint 1");

    var milestoneEntity = new TmsMilestone();
    milestoneEntity.setName("Sprint 1");

    var savedMilestoneEntity = new TmsMilestone();
    savedMilestoneEntity.setId(milestoneId);
    savedMilestoneEntity.setName("Sprint 1");

    var expectedRS = new TmsMilestoneRS();
    expectedRS.setId(milestoneId);
    expectedRS.setName("Sprint 1");
    expectedRS.setTestPlans(Collections.emptyList());

    when(tmsMilestoneMapper.toEntity(projectId, milestoneRQ)).thenReturn(milestoneEntity);
    when(tmsMilestoneRepository.save(milestoneEntity)).thenReturn(savedMilestoneEntity);
    when(tmsMilestoneMapper.convert(savedMilestoneEntity, Collections.emptyList()))
        .thenReturn(expectedRS);

    // When
    var result = sut.create(projectId, milestoneRQ);

    // Then
    assertNotNull(result);
    assertEquals(milestoneId, result.getId());
    assertEquals("Sprint 1", result.getName());
    assertTrue(result.getTestPlans().isEmpty());

    verify(tmsMilestoneMapper).toEntity(projectId, milestoneRQ);
    verify(tmsMilestoneRepository).save(milestoneEntity);
    verify(tmsMilestoneMapper).convert(savedMilestoneEntity, Collections.emptyList());
  }

  // Tests for getById method

  @Test
  void getById_WhenMilestoneExists_ShouldReturnMilestone() {
    // Given
    var milestoneEntity = new TmsMilestone();
    milestoneEntity.setId(milestoneId);
    milestoneEntity.setName("Release 1.0");

    var testPlans = Collections.<TmsTestPlanRS>emptyList();

    var expectedRS = new TmsMilestoneRS();
    expectedRS.setId(milestoneId);
    expectedRS.setName("Release 1.0");
    expectedRS.setTestPlans(testPlans);

    when(tmsMilestoneRepository.findByIdAndProjectId(milestoneId, projectId))
        .thenReturn(Optional.of(milestoneEntity));
    when(tmsTestPlanService.getByMilestoneId(projectId, milestoneId))
        .thenReturn(testPlans);
    when(tmsMilestoneMapper.convert(milestoneEntity, testPlans))
        .thenReturn(expectedRS);

    // When
    var result = sut.getById(projectId, milestoneId);

    // Then
    assertNotNull(result);
    assertEquals(milestoneId, result.getId());
    assertEquals("Release 1.0", result.getName());
    assertTrue(result.getTestPlans().isEmpty());

    verify(tmsMilestoneRepository).findByIdAndProjectId(milestoneId, projectId);
    verify(tmsTestPlanService).getByMilestoneId(projectId, milestoneId);
    verify(tmsMilestoneMapper).convert(milestoneEntity, testPlans);
  }

  @Test
  void getById_WhenMilestoneNotFound_ShouldThrowException() {
    // Given
    when(tmsMilestoneRepository.findByIdAndProjectId(milestoneId, projectId))
        .thenReturn(Optional.empty());

    // When/Then
    var exception = assertThrows(ReportPortalException.class,
        () -> sut.getById(projectId, milestoneId));

    assertEquals(ErrorType.NOT_FOUND, exception.getErrorType());
    verify(tmsMilestoneRepository).findByIdAndProjectId(milestoneId, projectId);
    verify(tmsTestPlanService, never()).getByMilestoneId(anyLong(), anyLong());
    verify(tmsMilestoneMapper, never()).convert(any(TmsMilestone.class), anyList());
  }

  @Test
  void getById_WhenMilestoneExistsWithTestPlans_ShouldReturnMilestoneWithTestPlans() {
    // Given
    var milestoneEntity = new TmsMilestone();
    milestoneEntity.setId(milestoneId);
    milestoneEntity.setName("Release 1.0");

    var testPlan1 = new TmsTestPlanRS();
    testPlan1.setId(100L);
    testPlan1.setName("Test Plan 1");

    var testPlan2 = new TmsTestPlanRS();
    testPlan2.setId(200L);
    testPlan2.setName("Test Plan 2");

    var testPlans = List.of(testPlan1, testPlan2);

    var expectedRS = new TmsMilestoneRS();
    expectedRS.setId(milestoneId);
    expectedRS.setName("Release 1.0");
    expectedRS.setTestPlans(testPlans);

    when(tmsMilestoneRepository.findByIdAndProjectId(milestoneId, projectId))
        .thenReturn(Optional.of(milestoneEntity));
    when(tmsTestPlanService.getByMilestoneId(projectId, milestoneId))
        .thenReturn(testPlans);
    when(tmsMilestoneMapper.convert(milestoneEntity, testPlans))
        .thenReturn(expectedRS);

    // When
    var result = sut.getById(projectId, milestoneId);

    // Then
    assertNotNull(result);
    assertEquals(milestoneId, result.getId());
    assertEquals("Release 1.0", result.getName());
    assertEquals(2, result.getTestPlans().size());

    verify(tmsMilestoneRepository).findByIdAndProjectId(milestoneId, projectId);
    verify(tmsTestPlanService).getByMilestoneId(projectId, milestoneId);
    verify(tmsMilestoneMapper).convert(milestoneEntity, testPlans);
  }

  // Tests for getAll method

  @Test
  void getAll_WhenMilestonesExist_ShouldReturnPage() {
    // Given
    var milestone1 = new TmsMilestone();
    milestone1.setId(100L);
    milestone1.setName("Release 1.0");

    var milestone2 = new TmsMilestone();
    milestone2.setId(200L);
    milestone2.setName("Release 2.0");

    var testPlan1 = new TmsTestPlanRS();
    testPlan1.setId(1L);
    var testPlan2 = new TmsTestPlanRS();
    testPlan2.setId(2L);

    var testPlansByMilestones = new HashMap<Long, List<TmsTestPlanRS>>();
    testPlansByMilestones.put(100L, List.of(testPlan1));
    testPlansByMilestones.put(200L, List.of(testPlan2));

    var milestoneRS1 = new TmsMilestoneRS();
    milestoneRS1.setId(100L);
    milestoneRS1.setName("Release 1.0");
    milestoneRS1.setTestPlans(List.of(testPlan1));

    var milestoneRS2 = new TmsMilestoneRS();
    milestoneRS2.setId(200L);
    milestoneRS2.setName("Release 2.0");
    milestoneRS2.setTestPlans(List.of(testPlan2));

    var milestonesPage = new PageImpl<>(
        List.of(milestone1, milestone2),
        pageable,
        2
    );

    when(tmsMilestoneRepository.findAllByProjectId(projectId, pageable))
        .thenReturn(milestonesPage);
    when(tmsTestPlanService.getByMilestoneIds(projectId, List.of(100L, 200L)))
        .thenReturn(testPlansByMilestones);
    when(tmsMilestoneMapper.convert(milestone1, List.of(testPlan1)))
        .thenReturn(milestoneRS1);
    when(tmsMilestoneMapper.convert(milestone2, List.of(testPlan2)))
        .thenReturn(milestoneRS2);

    // When
    var result = sut.getAll(projectId, pageable);

    // Then
    assertNotNull(result);
    assertEquals(2, result.getContent().size());
    assertEquals(10L, result.getPage().getSize());
    assertEquals(1L, result.getPage().getNumber());
    assertEquals(2L, result.getPage().getTotalElements());
    assertEquals(1L, result.getPage().getTotalPages());

    var milestones = result.getContent().stream().toList();
    assertEquals(100L, milestones.get(0).getId());
    assertEquals("Release 1.0", milestones.get(0).getName());
    assertEquals(1, milestones.get(0).getTestPlans().size());
    assertEquals(200L, milestones.get(1).getId());
    assertEquals("Release 2.0", milestones.get(1).getName());
    assertEquals(1, milestones.get(1).getTestPlans().size());

    verify(tmsMilestoneRepository).findAllByProjectId(projectId, pageable);
    verify(tmsTestPlanService).getByMilestoneIds(projectId, List.of(100L, 200L));
    verify(tmsMilestoneMapper).convert(milestone1, List.of(testPlan1));
    verify(tmsMilestoneMapper).convert(milestone2, List.of(testPlan2));
  }

  @Test
  void getAll_WhenNoMilestones_ShouldReturnEmptyPage() {
    // Given
    var emptyPage = new PageImpl<TmsMilestone>(
        Collections.emptyList(),
        pageable,
        0
    );

    when(tmsMilestoneRepository.findAllByProjectId(projectId, pageable))
        .thenReturn(emptyPage);
    when(tmsTestPlanService.getByMilestoneIds(projectId, Collections.emptyList()))
        .thenReturn(Collections.emptyMap());

    // When
    var result = sut.getAll(projectId, pageable);

    // Then
    assertNotNull(result);
    assertEquals(0, result.getContent().size());
    assertEquals(10L, result.getPage().getSize());
    assertEquals(1L, result.getPage().getNumber());
    assertEquals(0L, result.getPage().getTotalElements());
    assertEquals(0L, result.getPage().getTotalPages());

    verify(tmsMilestoneRepository).findAllByProjectId(projectId, pageable);
    verify(tmsTestPlanService).getByMilestoneIds(projectId, Collections.emptyList());
    verify(tmsMilestoneMapper, never()).convert(any(TmsMilestone.class), anyList());
  }

  @Test
  void getAll_WithCustomPageable_ShouldReturnCorrectPage() {
    // Given
    var customPageable = PageRequest.of(2, 5);

    var milestone = new TmsMilestone();
    milestone.setId(milestoneId);
    milestone.setName("Release 3.0");

    var testPlans = Collections.<TmsTestPlanRS>emptyList();
    var testPlansByMilestones = Map.of(milestoneId, testPlans);

    var milestoneRS = new TmsMilestoneRS();
    milestoneRS.setId(milestoneId);
    milestoneRS.setName("Release 3.0");
    milestoneRS.setTestPlans(testPlans);

    var milestonesPage = new PageImpl<>(
        List.of(milestone),
        customPageable,
        15
    );

    when(tmsMilestoneRepository.findAllByProjectId(projectId, customPageable))
        .thenReturn(milestonesPage);
    when(tmsTestPlanService.getByMilestoneIds(projectId, List.of(milestoneId)))
        .thenReturn(testPlansByMilestones);
    when(tmsMilestoneMapper.convert(milestone, testPlans))
        .thenReturn(milestoneRS);

    // When
    var result = sut.getAll(projectId, customPageable);

    // Then
    assertNotNull(result);
    assertEquals(1, result.getContent().size());
    assertEquals(5L, result.getPage().getSize());
    assertEquals(3L, result.getPage().getNumber());
    assertEquals(15L, result.getPage().getTotalElements());
    assertEquals(3L, result.getPage().getTotalPages());

    verify(tmsMilestoneRepository).findAllByProjectId(projectId, customPageable);
    verify(tmsTestPlanService).getByMilestoneIds(projectId, List.of(milestoneId));
    verify(tmsMilestoneMapper).convert(milestone, testPlans);
  }

  @Test
  void getAll_WhenMilestonesExistWithoutTestPlans_ShouldReturnMilestonesWithEmptyTestPlans() {
    // Given
    var milestone1 = new TmsMilestone();
    milestone1.setId(100L);
    milestone1.setName("Release 1.0");

    var milestone2 = new TmsMilestone();
    milestone2.setId(200L);
    milestone2.setName("Release 2.0");

    var emptyTestPlans = Collections.<TmsTestPlanRS>emptyList();
    var testPlansByMilestones = Map.of(
        100L, emptyTestPlans,
        200L, emptyTestPlans
    );

    var milestoneRS1 = new TmsMilestoneRS();
    milestoneRS1.setId(100L);
    milestoneRS1.setName("Release 1.0");
    milestoneRS1.setTestPlans(emptyTestPlans);

    var milestoneRS2 = new TmsMilestoneRS();
    milestoneRS2.setId(200L);
    milestoneRS2.setName("Release 2.0");
    milestoneRS2.setTestPlans(emptyTestPlans);

    var milestonesPage = new PageImpl<>(
        List.of(milestone1, milestone2),
        pageable,
        2
    );

    when(tmsMilestoneRepository.findAllByProjectId(projectId, pageable))
        .thenReturn(milestonesPage);
    when(tmsTestPlanService.getByMilestoneIds(projectId, List.of(100L, 200L)))
        .thenReturn(testPlansByMilestones);
    when(tmsMilestoneMapper.convert(milestone1, emptyTestPlans))
        .thenReturn(milestoneRS1);
    when(tmsMilestoneMapper.convert(milestone2, emptyTestPlans))
        .thenReturn(milestoneRS2);

    // When
    var result = sut.getAll(projectId, pageable);

    // Then
    assertNotNull(result);
    assertEquals(2, result.getContent().size());

    var milestones = result.getContent().stream().toList();
    assertTrue(milestones.get(0).getTestPlans().isEmpty());
    assertTrue(milestones.get(1).getTestPlans().isEmpty());

    verify(tmsMilestoneRepository).findAllByProjectId(projectId, pageable);
    verify(tmsTestPlanService).getByMilestoneIds(projectId, List.of(100L, 200L));
    verify(tmsMilestoneMapper).convert(milestone1, emptyTestPlans);
    verify(tmsMilestoneMapper).convert(milestone2, emptyTestPlans);
  }

  // Tests for patch method

  @Test
  void patch_WhenMilestoneExists_ShouldUpdateSuccessfully() {
    // Given
    var milestoneRQ = new TmsMilestoneRQ();
    milestoneRQ.setName("Updated Release 1.0");

    var existingMilestone = new TmsMilestone();
    existingMilestone.setId(milestoneId);
    existingMilestone.setName("Release 1.0");

    var updatedMilestone = new TmsMilestone();
    updatedMilestone.setId(milestoneId);
    updatedMilestone.setName("Updated Release 1.0");

    var testPlan1 = new TmsTestPlanRS();
    testPlan1.setId(100L);

    var testPlan2 = new TmsTestPlanRS();
    testPlan2.setId(200L);

    var testPlans = List.of(testPlan1, testPlan2);

    var expectedRS = new TmsMilestoneRS();
    expectedRS.setId(milestoneId);
    expectedRS.setName("Updated Release 1.0");
    expectedRS.setTestPlans(testPlans);

    when(tmsMilestoneRepository.findByIdAndProjectId(milestoneId, projectId))
        .thenReturn(Optional.of(existingMilestone));
    when(tmsMilestoneRepository.save(existingMilestone)).thenReturn(updatedMilestone);
    when(tmsTestPlanService.getByMilestoneId(projectId, milestoneId)).thenReturn(testPlans);
    when(tmsMilestoneMapper.convert(updatedMilestone, testPlans)).thenReturn(expectedRS);

    // When
    var result = sut.patch(projectId, milestoneId, milestoneRQ);

    // Then
    assertNotNull(result);
    assertEquals(milestoneId, result.getId());
    assertEquals("Updated Release 1.0", result.getName());
    assertEquals(2, result.getTestPlans().size());

    verify(tmsMilestoneRepository).findByIdAndProjectId(milestoneId, projectId);
    verify(tmsMilestoneMapper).patchEntity(projectId, milestoneRQ, existingMilestone);
    verify(tmsMilestoneRepository).save(existingMilestone);
    verify(tmsTestPlanService).getByMilestoneId(projectId, milestoneId);
    verify(tmsMilestoneMapper).convert(updatedMilestone, testPlans);
  }

  @Test
  void patch_WhenMilestoneNotFound_ShouldThrowException() {
    // Given
    var milestoneRQ = new TmsMilestoneRQ();
    milestoneRQ.setName("Updated Release");

    when(tmsMilestoneRepository.findByIdAndProjectId(milestoneId, projectId))
        .thenReturn(Optional.empty());

    // When/Then
    var exception = assertThrows(ReportPortalException.class,
        () -> sut.patch(projectId, milestoneId, milestoneRQ));

    assertEquals(ErrorType.NOT_FOUND, exception.getErrorType());
    verify(tmsMilestoneRepository).findByIdAndProjectId(milestoneId, projectId);
    verify(tmsMilestoneMapper, never()).patchEntity(anyLong(), any(), any());
    verify(tmsMilestoneRepository, never()).save(any());
  }

  @Test
  void patch_WhenMilestoneExistsWithNoTestPlans_ShouldUpdateSuccessfully() {
    // Given
    var milestoneRQ = new TmsMilestoneRQ();
    milestoneRQ.setName("Updated Sprint");

    var existingMilestone = new TmsMilestone();
    existingMilestone.setId(milestoneId);
    existingMilestone.setName("Sprint 1");

    var updatedMilestone = new TmsMilestone();
    updatedMilestone.setId(milestoneId);
    updatedMilestone.setName("Updated Sprint");

    var emptyTestPlans = Collections.<TmsTestPlanRS>emptyList();

    var expectedRS = new TmsMilestoneRS();
    expectedRS.setId(milestoneId);
    expectedRS.setName("Updated Sprint");
    expectedRS.setTestPlans(emptyTestPlans);

    when(tmsMilestoneRepository.findByIdAndProjectId(milestoneId, projectId))
        .thenReturn(Optional.of(existingMilestone));
    when(tmsMilestoneRepository.save(existingMilestone)).thenReturn(updatedMilestone);
    when(tmsTestPlanService.getByMilestoneId(projectId, milestoneId))
        .thenReturn(emptyTestPlans);
    when(tmsMilestoneMapper.convert(updatedMilestone, emptyTestPlans)).thenReturn(expectedRS);

    // When
    var result = sut.patch(projectId, milestoneId, milestoneRQ);

    // Then
    assertNotNull(result);
    assertEquals(milestoneId, result.getId());
    assertEquals("Updated Sprint", result.getName());
    assertTrue(result.getTestPlans().isEmpty());

    verify(tmsMilestoneRepository).findByIdAndProjectId(milestoneId, projectId);
    verify(tmsMilestoneMapper).patchEntity(projectId, milestoneRQ, existingMilestone);
    verify(tmsMilestoneRepository).save(existingMilestone);
    verify(tmsTestPlanService).getByMilestoneId(projectId, milestoneId);
    verify(tmsMilestoneMapper).convert(updatedMilestone, emptyTestPlans);
  }

  // Tests for delete method

  @Test
  void delete_WhenMilestoneExists_ShouldDeleteSuccessfully() {
    // Given
    when(tmsMilestoneRepository.existsByIdAndProjectId(milestoneId, projectId))
        .thenReturn(true);
    when(tmsMilestoneRepository.deleteByIdAndProjectId(milestoneId, projectId))
        .thenReturn(1);

    // When/Then
    assertDoesNotThrow(() -> sut.delete(projectId, milestoneId));

    verify(tmsMilestoneRepository).existsByIdAndProjectId(milestoneId, projectId);
    verify(tmsTestPlanService).removeTestPlansFromMilestone(projectId, milestoneId);
    verify(tmsMilestoneRepository).deleteByIdAndProjectId(milestoneId, projectId);
  }

  @Test
  void delete_WhenMilestoneNotFound_ShouldThrowException() {
    // Given
    when(tmsMilestoneRepository.existsByIdAndProjectId(milestoneId, projectId))
        .thenReturn(false);

    // When/Then
    var exception = assertThrows(ReportPortalException.class,
        () -> sut.delete(projectId, milestoneId));

    assertEquals(ErrorType.NOT_FOUND, exception.getErrorType());
    verify(tmsMilestoneRepository).existsByIdAndProjectId(milestoneId, projectId);
    verify(tmsTestPlanService, never()).removeTestPlansFromMilestone(anyLong(), anyLong());
    verify(tmsMilestoneRepository, never()).deleteByIdAndProjectId(anyLong(), anyLong());
  }

  @Test
  void delete_WhenDeletionFails_ShouldThrowException() {
    // Given
    when(tmsMilestoneRepository.existsByIdAndProjectId(milestoneId, projectId))
        .thenReturn(true);
    when(tmsMilestoneRepository.deleteByIdAndProjectId(milestoneId, projectId))
        .thenReturn(0);

    // When/Then
    var exception = assertThrows(ReportPortalException.class,
        () -> sut.delete(projectId, milestoneId));

    assertEquals(ErrorType.NOT_FOUND, exception.getErrorType());
    verify(tmsMilestoneRepository).existsByIdAndProjectId(milestoneId, projectId);
    verify(tmsTestPlanService).removeTestPlansFromMilestone(projectId, milestoneId);
    verify(tmsMilestoneRepository).deleteByIdAndProjectId(milestoneId, projectId);
  }

  @Test
  void delete_WhenMilestoneExistsWithTestPlans_ShouldRemoveTestPlansAndDeleteMilestone() {
    // Given
    when(tmsMilestoneRepository.existsByIdAndProjectId(milestoneId, projectId))
        .thenReturn(true);
    when(tmsMilestoneRepository.deleteByIdAndProjectId(milestoneId, projectId))
        .thenReturn(1);

    // When/Then
    assertDoesNotThrow(() -> sut.delete(projectId, milestoneId));

    verify(tmsMilestoneRepository).existsByIdAndProjectId(milestoneId, projectId);
    verify(tmsTestPlanService).removeTestPlansFromMilestone(projectId, milestoneId);
    verify(tmsMilestoneRepository).deleteByIdAndProjectId(milestoneId, projectId);
  }

  // Tests for removeTestPlanFromMilestone method

  @Test
  void removeTestPlanFromMilestone_WhenMilestoneExists_ShouldRemoveSuccessfully() {
    // Given
    when(tmsMilestoneRepository.existsByIdAndProjectId(milestoneId, projectId))
        .thenReturn(true);

    // When/Then
    assertDoesNotThrow(() ->
        sut.removeTestPlanFromMilestone(projectId, milestoneId, testPlanId));

    verify(tmsMilestoneRepository).existsByIdAndProjectId(milestoneId, projectId);
    verify(tmsTestPlanService).removeTestPlanFromMilestone(projectId, milestoneId, testPlanId);
  }

  @Test
  void removeTestPlanFromMilestone_WhenMilestoneNotFound_ShouldThrowException() {
    // Given
    when(tmsMilestoneRepository.existsByIdAndProjectId(milestoneId, projectId))
        .thenReturn(false);

    // When/Then
    var exception = assertThrows(ReportPortalException.class,
        () -> sut.removeTestPlanFromMilestone(projectId, milestoneId, testPlanId));

    assertEquals(ErrorType.NOT_FOUND, exception.getErrorType());
    verify(tmsMilestoneRepository).existsByIdAndProjectId(milestoneId, projectId);
    verify(tmsTestPlanService, never()).removeTestPlanFromMilestone(anyLong(), anyLong(),
        anyLong());
  }

  @Test
  void removeTestPlanFromMilestone_WhenTestPlanServiceThrowsException_ShouldPropagateException() {
    // Given
    when(tmsMilestoneRepository.existsByIdAndProjectId(milestoneId, projectId))
        .thenReturn(true);
    doThrow(new ReportPortalException(ErrorType.NOT_FOUND, "Test plan not found"))
        .when(tmsTestPlanService)
        .removeTestPlanFromMilestone(projectId, milestoneId, testPlanId);

    // When/Then
    var exception = assertThrows(ReportPortalException.class,
        () -> sut.removeTestPlanFromMilestone(projectId, milestoneId, testPlanId));

    assertEquals(ErrorType.NOT_FOUND, exception.getErrorType());
    verify(tmsMilestoneRepository).existsByIdAndProjectId(milestoneId, projectId);
    verify(tmsTestPlanService).removeTestPlanFromMilestone(projectId, milestoneId, testPlanId);
  }

  // Tests for addTestPlanToMilestone method

  @Test
  void addTestPlanToMilestone_WhenMilestoneExists_ShouldAddSuccessfully() {
    // Given
    when(tmsMilestoneRepository.existsByIdAndProjectId(milestoneId, projectId))
        .thenReturn(true);

    // When/Then
    assertDoesNotThrow(() ->
        sut.addTestPlanToMilestone(projectId, milestoneId, testPlanId));

    verify(tmsMilestoneRepository).existsByIdAndProjectId(milestoneId, projectId);
    verify(tmsTestPlanService).addTestPlanMilestone(projectId, milestoneId, testPlanId);
  }

  @Test
  void addTestPlanToMilestone_WhenMilestoneNotFound_ShouldThrowException() {
    // Given
    when(tmsMilestoneRepository.existsByIdAndProjectId(milestoneId, projectId))
        .thenReturn(false);

    // When/Then
    var exception = assertThrows(ReportPortalException.class,
        () -> sut.addTestPlanToMilestone(projectId, milestoneId, testPlanId));

    assertEquals(ErrorType.NOT_FOUND, exception.getErrorType());
    verify(tmsMilestoneRepository).existsByIdAndProjectId(milestoneId, projectId);
    verify(tmsTestPlanService, never()).addTestPlanMilestone(anyLong(), anyLong(), anyLong());
  }

  @Test
  void addTestPlanToMilestone_WhenTestPlanServiceThrowsException_ShouldPropagateException() {
    // Given
    when(tmsMilestoneRepository.existsByIdAndProjectId(milestoneId, projectId))
        .thenReturn(true);
    doThrow(new ReportPortalException(ErrorType.NOT_FOUND, "Test plan not found"))
        .when(tmsTestPlanService)
        .addTestPlanMilestone(projectId, milestoneId, testPlanId);

    // When/Then
    var exception = assertThrows(ReportPortalException.class,
        () -> sut.addTestPlanToMilestone(projectId, milestoneId, testPlanId));

    assertEquals(ErrorType.NOT_FOUND, exception.getErrorType());
    verify(tmsMilestoneRepository).existsByIdAndProjectId(milestoneId, projectId);
    verify(tmsTestPlanService).addTestPlanMilestone(projectId, milestoneId, testPlanId);
  }

  @Test
  void addTestPlanToMilestone_WhenTestPlanAlreadyInMilestone_ShouldPropagateException() {
    // Given
    when(tmsMilestoneRepository.existsByIdAndProjectId(milestoneId, projectId))
        .thenReturn(true);
    doThrow(new ReportPortalException(ErrorType.BAD_REQUEST_ERROR,
        "Test plan already in milestone"))
        .when(tmsTestPlanService)
        .addTestPlanMilestone(projectId, milestoneId, testPlanId);

    // When/Then
    var exception = assertThrows(ReportPortalException.class,
        () -> sut.addTestPlanToMilestone(projectId, milestoneId, testPlanId));

    assertEquals(ErrorType.BAD_REQUEST_ERROR, exception.getErrorType());
    verify(tmsMilestoneRepository).existsByIdAndProjectId(milestoneId, projectId);
    verify(tmsTestPlanService).addTestPlanMilestone(projectId, milestoneId, testPlanId);
  }

  // Tests for duplicate method

  @Test
  void duplicate_WhenMilestoneExists_ShouldDuplicateSuccessfully() {
    // Given
    var duplicateMilestoneRQ = new TmsMilestoneRQ();
    duplicateMilestoneRQ.setName("Duplicated Release 1.0");

    var originalMilestone = new TmsMilestone();
    originalMilestone.setId(milestoneId);
    originalMilestone.setName("Release 1.0");

    var newMilestoneEntity = new TmsMilestone();
    newMilestoneEntity.setName("Duplicated Release 1.0");

    var savedMilestoneEntity = new TmsMilestone();
    savedMilestoneEntity.setId(300L);
    savedMilestoneEntity.setName("Duplicated Release 1.0");

    var duplicateTestPlansRS = List.of(
        mock(com.epam.reportportal.core.tms.dto.DuplicateTmsTestPlanRS.class),
        mock(com.epam.reportportal.core.tms.dto.DuplicateTmsTestPlanRS.class)
    );

    var expectedRS = DuplicateTmsMilestoneRS.builder()
        .id(300L)
        .name("Duplicated Release 1.0")
        .testPlans(duplicateTestPlansRS)
        .build();

    when(tmsMilestoneRepository.findByIdAndProjectId(milestoneId, projectId))
        .thenReturn(Optional.of(originalMilestone));
    when(tmsMilestoneMapper.toEntity(projectId, duplicateMilestoneRQ))
        .thenReturn(newMilestoneEntity);
    when(tmsTestPlanService.duplicateTestPlansInMilestone(projectId, milestoneId))
        .thenReturn(duplicateTestPlansRS);
    when(tmsMilestoneRepository.save(newMilestoneEntity)).thenReturn(savedMilestoneEntity);
    when(tmsMilestoneMapper.convertToDuplicateTmsMilestoneRS(savedMilestoneEntity,
        duplicateTestPlansRS))
        .thenReturn(expectedRS);

    // When
    var result = sut.duplicate(projectId, milestoneId, duplicateMilestoneRQ);

    // Then
    assertNotNull(result);
    assertEquals(300L, result.getId());
    assertEquals("Duplicated Release 1.0", result.getName());
    assertEquals(2, result.getTestPlans().size());

    verify(tmsMilestoneRepository).findByIdAndProjectId(milestoneId, projectId);
    verify(tmsMilestoneMapper).toEntity(projectId, duplicateMilestoneRQ);
    verify(tmsTestPlanService).duplicateTestPlansInMilestone(projectId, milestoneId);
    verify(tmsMilestoneRepository).save(newMilestoneEntity);
    verify(tmsMilestoneMapper).convertToDuplicateTmsMilestoneRS(savedMilestoneEntity,
        duplicateTestPlansRS);
  }

  @Test
  void duplicate_WhenOriginalMilestoneNotFound_ShouldThrowException() {
    // Given
    var duplicateMilestoneRQ = new TmsMilestoneRQ();
    duplicateMilestoneRQ.setName("Duplicated Release");

    when(tmsMilestoneRepository.findByIdAndProjectId(milestoneId, projectId))
        .thenReturn(Optional.empty());

    // When/Then
    var exception = assertThrows(ReportPortalException.class,
        () -> sut.duplicate(projectId, milestoneId, duplicateMilestoneRQ));

    assertEquals(ErrorType.NOT_FOUND, exception.getErrorType());
    verify(tmsMilestoneRepository).findByIdAndProjectId(milestoneId, projectId);
    verify(tmsMilestoneMapper, never()).toEntity(anyLong(), any());
    verify(tmsTestPlanService, never()).duplicateTestPlansInMilestone(anyLong(), anyLong());
    verify(tmsMilestoneRepository, never()).save(any());
  }

  @Test
  void duplicate_WhenNoTestPlansInOriginalMilestone_ShouldDuplicateMilestoneOnly() {
    // Given
    var duplicateMilestoneRQ = new TmsMilestoneRQ();
    duplicateMilestoneRQ.setName("Duplicated Sprint");

    var originalMilestone = new TmsMilestone();
    originalMilestone.setId(milestoneId);
    originalMilestone.setName("Sprint 1");

    var newMilestoneEntity = new TmsMilestone();
    newMilestoneEntity.setName("Duplicated Sprint");

    var savedMilestoneEntity = new TmsMilestone();
    savedMilestoneEntity.setId(300L);
    savedMilestoneEntity.setName("Duplicated Sprint");

    var emptyDuplicateTestPlans = Collections.<com.epam.reportportal.core.tms.dto.DuplicateTmsTestPlanRS>emptyList();

    var expectedRS = DuplicateTmsMilestoneRS.builder()
        .id(300L)
        .name("Duplicated Sprint")
        .build();

    when(tmsMilestoneRepository.findByIdAndProjectId(milestoneId, projectId))
        .thenReturn(Optional.of(originalMilestone));
    when(tmsMilestoneMapper.toEntity(projectId, duplicateMilestoneRQ))
        .thenReturn(newMilestoneEntity);
    when(tmsTestPlanService.duplicateTestPlansInMilestone(projectId, milestoneId))
        .thenReturn(emptyDuplicateTestPlans);
    when(tmsMilestoneRepository.save(newMilestoneEntity)).thenReturn(savedMilestoneEntity);
    when(tmsMilestoneMapper.convertToDuplicateTmsMilestoneRS(savedMilestoneEntity,
        emptyDuplicateTestPlans))
        .thenReturn(expectedRS);

    // When
    var result = sut.duplicate(projectId, milestoneId, duplicateMilestoneRQ);

    // Then
    assertNotNull(result);
    assertEquals(300L, result.getId());
    assertEquals("Duplicated Sprint", result.getName());
    assertNull(result.getTestPlans());

    verify(tmsMilestoneRepository).findByIdAndProjectId(milestoneId, projectId);
    verify(tmsMilestoneMapper).toEntity(projectId, duplicateMilestoneRQ);
    verify(tmsTestPlanService).duplicateTestPlansInMilestone(projectId, milestoneId);
    verify(tmsMilestoneRepository).save(newMilestoneEntity);
    verify(tmsMilestoneMapper).convertToDuplicateTmsMilestoneRS(savedMilestoneEntity,
        emptyDuplicateTestPlans);
  }

  @Test
  void duplicate_WhenTestPlanDuplicationPartiallyFails_ShouldStillCreateMilestone() {
    // Given
    var duplicateMilestoneRQ = new TmsMilestoneRQ();
    duplicateMilestoneRQ.setName("Duplicated Release");

    var originalMilestone = new TmsMilestone();
    originalMilestone.setId(milestoneId);
    originalMilestone.setName("Release 1.0");

    var newMilestoneEntity = new TmsMilestone();
    newMilestoneEntity.setName("Duplicated Release");

    var savedMilestoneEntity = new TmsMilestone();
    savedMilestoneEntity.setId(300L);
    savedMilestoneEntity.setName("Duplicated Release");

    // Simulate partial failure - some test plans duplicated, some failed
    var duplicateTestPlansRS = List.of(
        mock(com.epam.reportportal.core.tms.dto.DuplicateTmsTestPlanRS.class)
    );

    var expectedRS = DuplicateTmsMilestoneRS.builder()
        .id(300L)
        .name("Duplicated Release")
        .testPlans(duplicateTestPlansRS)
        .build();

    when(tmsMilestoneRepository.findByIdAndProjectId(milestoneId, projectId))
        .thenReturn(Optional.of(originalMilestone));
    when(tmsMilestoneMapper.toEntity(projectId, duplicateMilestoneRQ))
        .thenReturn(newMilestoneEntity);
    when(tmsTestPlanService.duplicateTestPlansInMilestone(projectId, milestoneId))
        .thenReturn(duplicateTestPlansRS);
    when(tmsMilestoneRepository.save(newMilestoneEntity)).thenReturn(savedMilestoneEntity);
    when(tmsMilestoneMapper.convertToDuplicateTmsMilestoneRS(savedMilestoneEntity,
        duplicateTestPlansRS))
        .thenReturn(expectedRS);

    // When
    var result = sut.duplicate(projectId, milestoneId, duplicateMilestoneRQ);

    // Then
    assertNotNull(result);
    assertEquals(300L, result.getId());
    assertEquals("Duplicated Release", result.getName());
    assertEquals(1, result.getTestPlans().size());

    verify(tmsMilestoneRepository).findByIdAndProjectId(milestoneId, projectId);
    verify(tmsMilestoneMapper).toEntity(projectId, duplicateMilestoneRQ);
    verify(tmsTestPlanService).duplicateTestPlansInMilestone(projectId, milestoneId);
    verify(tmsMilestoneRepository).save(newMilestoneEntity);
    verify(tmsMilestoneMapper).convertToDuplicateTmsMilestoneRS(savedMilestoneEntity,
        duplicateTestPlansRS);
  }
}
