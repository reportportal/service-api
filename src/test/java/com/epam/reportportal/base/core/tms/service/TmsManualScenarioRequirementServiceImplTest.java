package com.epam.reportportal.base.core.tms.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.reportportal.base.core.tms.dto.TmsRequirementRQ;
import com.epam.reportportal.base.core.tms.mapper.TmsManualScenarioRequirementMapper;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsManualScenarioRequirementRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsManualScenario;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsManualScenarioRequirement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TmsManualScenarioRequirementServiceImplTest {

  @Mock
  private TmsManualScenarioRequirementRepository tmsManualScenarioRequirementRepository;

  @Mock
  private TmsManualScenarioRequirementMapper tmsManualScenarioRequirementMapper;

  @InjectMocks
  private TmsManualScenarioRequirementServiceImpl tmsManualScenarioRequirementService;

  @Captor
  private ArgumentCaptor<List<TmsManualScenarioRequirement>> requirementsCaptor;

  private TmsManualScenario manualScenario;

  @BeforeEach
  void setUp() {
    manualScenario = createManualScenario();
  }

  // ===== CREATE =====

  @Test
  void shouldCreateRequirements() {
    // Given
    var requirementRQ1 = TmsRequirementRQ.builder().id("REQ-001").value("Requirement 1").build();
    var requirementRQ2 = TmsRequirementRQ.builder().id("REQ-002").value("Requirement 2").build();
    var requirements = List.of(requirementRQ1, requirementRQ2);

    var entity1 = createRequirementEntity("REQ-001", "Requirement 1");
    var entity2 = createRequirementEntity("REQ-002", "Requirement 2");
    var savedEntities = List.of(entity1, entity2);

    when(tmsManualScenarioRequirementRepository.findById("REQ-001")).thenReturn(Optional.empty());
    when(tmsManualScenarioRequirementRepository.findById("REQ-002")).thenReturn(Optional.empty());
    when(tmsManualScenarioRequirementMapper.toEntity(requirementRQ1)).thenReturn(entity1);
    when(tmsManualScenarioRequirementMapper.toEntity(requirementRQ2)).thenReturn(entity2);
    when(tmsManualScenarioRequirementRepository.saveAll(anyList())).thenReturn(savedEntities);

    // When
    tmsManualScenarioRequirementService.createRequirements(manualScenario, requirements);

    // Then
    verify(tmsManualScenarioRequirementMapper).toEntity(requirementRQ1);
    verify(tmsManualScenarioRequirementMapper).toEntity(requirementRQ2);
    verify(tmsManualScenarioRequirementRepository).saveAll(requirementsCaptor.capture());

    var savedRequirements = requirementsCaptor.getValue();
    assertThat(savedRequirements).hasSize(2);

    // Verify number assignment
    assertThat(savedRequirements.get(0).getNumber()).isEqualTo(0);
    assertThat(savedRequirements.get(1).getNumber()).isEqualTo(1);

    // Verify manual scenario assignment
    savedRequirements.forEach(req -> assertThat(req.getManualScenario()).isEqualTo(manualScenario));
  }

  @Test
  void shouldCreateRequirementsWithExistingEntity() {
    // Given
    var requirementRQ = TmsRequirementRQ.builder().id("REQ-001").value("Updated Value").build();
    var requirements = List.of(requirementRQ);

    var existingEntity = createRequirementEntity("REQ-001", "Old Value");
    existingEntity.setNumber(5); // Old number

    when(tmsManualScenarioRequirementRepository.findById("REQ-001"))
        .thenReturn(Optional.of(existingEntity));
    when(tmsManualScenarioRequirementRepository.saveAll(anyList()))
        .thenReturn(List.of(existingEntity));

    // When
    tmsManualScenarioRequirementService.createRequirements(manualScenario, requirements);

    // Then
    verify(tmsManualScenarioRequirementMapper, never()).toEntity(any());
    verify(tmsManualScenarioRequirementRepository).saveAll(requirementsCaptor.capture());

    var savedRequirements = requirementsCaptor.getValue();
    assertThat(savedRequirements).hasSize(1);
    assertThat(savedRequirements.get(0).getValue()).isEqualTo("Updated Value");
    assertThat(savedRequirements.get(0).getNumber()).isEqualTo(0); // Number reset to 0
    assertThat(savedRequirements.get(0).getManualScenario()).isEqualTo(manualScenario);
  }

  @Test
  void shouldNotCreateRequirementsWhenListIsNull() {
    // When
    tmsManualScenarioRequirementService.createRequirements(manualScenario, null);

    // Then
    verify(tmsManualScenarioRequirementRepository, never()).saveAll(any());
  }

  @Test
  void shouldNotCreateRequirementsWhenListIsEmpty() {
    // When
    tmsManualScenarioRequirementService.createRequirements(manualScenario, Collections.emptyList());

    // Then
    verify(tmsManualScenarioRequirementRepository, never()).saveAll(any());
  }

  // ===== UPDATE =====

  @Test
  void shouldUpdateRequirements() {
    // Given
    var existingReq1 = createRequirementEntity("REQ-OLD-1", "Old Requirement 1");
    var existingReq2 = createRequirementEntity("REQ-OLD-2", "Old Requirement 2");
    manualScenario.setRequirements(new ArrayList<>(List.of(existingReq1, existingReq2)));

    var requirementRQ = TmsRequirementRQ.builder().id("REQ-NEW").value("New Requirement").build();
    var requirements = List.of(requirementRQ);
    var entity = createRequirementEntity("REQ-NEW", "New Requirement");

    when(tmsManualScenarioRequirementRepository.findById("REQ-NEW")).thenReturn(Optional.empty());
    when(tmsManualScenarioRequirementMapper.toEntity(requirementRQ)).thenReturn(entity);
    when(tmsManualScenarioRequirementRepository.saveAll(anyList())).thenReturn(List.of(entity));

    // When
    tmsManualScenarioRequirementService.updateRequirements(manualScenario, requirements);

    // Then
    verify(tmsManualScenarioRequirementRepository).deleteAll(requirementsCaptor.capture());
    var deletedRequirements = requirementsCaptor.getValue();
    assertThat(deletedRequirements).containsExactlyInAnyOrder(existingReq1, existingReq2);

    verify(tmsManualScenarioRequirementRepository).saveAll(anyList());
  }

  @Test
  void shouldUpdateRequirementsWhenNoExistingRequirements() {
    // Given
    manualScenario.setRequirements(null);

    var requirementRQ = TmsRequirementRQ.builder().id("REQ-NEW").value("New Requirement").build();
    var requirements = List.of(requirementRQ);
    var entity = createRequirementEntity("REQ-NEW", "New Requirement");

    when(tmsManualScenarioRequirementRepository.findById("REQ-NEW")).thenReturn(Optional.empty());
    when(tmsManualScenarioRequirementMapper.toEntity(requirementRQ)).thenReturn(entity);
    when(tmsManualScenarioRequirementRepository.saveAll(anyList())).thenReturn(List.of(entity));

    // When
    tmsManualScenarioRequirementService.updateRequirements(manualScenario, requirements);

    // Then
    verify(tmsManualScenarioRequirementRepository, never()).deleteAll(any());
    verify(tmsManualScenarioRequirementRepository).saveAll(requirementsCaptor.capture());

    var savedRequirements = requirementsCaptor.getValue();
    assertThat(savedRequirements).hasSize(1);
  }

  @Test
  void shouldUpdateRequirementsWhenExistingRequirementsEmpty() {
    // Given
    manualScenario.setRequirements(new ArrayList<>());

    var requirementRQ = TmsRequirementRQ.builder().id("REQ-NEW").value("New Requirement").build();
    var requirements = List.of(requirementRQ);
    var entity = createRequirementEntity("REQ-NEW", "New Requirement");

    when(tmsManualScenarioRequirementRepository.findById("REQ-NEW")).thenReturn(Optional.empty());
    when(tmsManualScenarioRequirementMapper.toEntity(requirementRQ)).thenReturn(entity);
    when(tmsManualScenarioRequirementRepository.saveAll(anyList())).thenReturn(List.of(entity));

    // When
    tmsManualScenarioRequirementService.updateRequirements(manualScenario, requirements);

    // Then
    verify(tmsManualScenarioRequirementRepository, never()).deleteAll(any());
    verify(tmsManualScenarioRequirementRepository).saveAll(requirementsCaptor.capture());

    var savedRequirements = requirementsCaptor.getValue();
    assertThat(savedRequirements).hasSize(1);
  }

  @Test
  void shouldDeleteExistingRequirementsOnUpdateWithNull() {
    // Given
    var existingReq = createRequirementEntity("REQ-OLD", "Old Requirement");
    manualScenario.setRequirements(new ArrayList<>(List.of(existingReq)));

    // When
    tmsManualScenarioRequirementService.updateRequirements(manualScenario, null);

    // Then
    verify(tmsManualScenarioRequirementRepository).deleteAll(requirementsCaptor.capture());
    var deletedRequirements = requirementsCaptor.getValue();
    assertThat(deletedRequirements).containsExactly(existingReq);

    verify(tmsManualScenarioRequirementRepository, never()).saveAll(any());
  }

  @Test
  void shouldDeleteExistingRequirementsOnUpdateWithEmptyList() {
    // Given
    var existingReq = createRequirementEntity("REQ-OLD", "Old Requirement");
    manualScenario.setRequirements(new ArrayList<>(List.of(existingReq)));

    // When
    tmsManualScenarioRequirementService.updateRequirements(manualScenario, Collections.emptyList());

    // Then
    verify(tmsManualScenarioRequirementRepository).deleteAll(requirementsCaptor.capture());
    var deletedRequirements = requirementsCaptor.getValue();
    assertThat(deletedRequirements).containsExactly(existingReq);

    verify(tmsManualScenarioRequirementRepository, never()).saveAll(any());
  }

  // ===== PATCH =====

  @Test
  void shouldPatchRequirementsWithNewEntries() {
    // Given
    var requirementRQ = TmsRequirementRQ.builder().id("REQ-NEW").value("New Requirement").build();
    var requirements = List.of(requirementRQ);
    var entity = createRequirementEntity("REQ-NEW", "New Requirement");

    var existingReq1 = createRequirementEntity("REQ-001", "Existing 1");
    existingReq1.setNumber(0);
    var existingReq2 = createRequirementEntity("REQ-002", "Existing 2");
    existingReq2.setNumber(1);

    manualScenario.setRequirements(new ArrayList<>());

    when(tmsManualScenarioRequirementRepository.findByManualScenarioIdOrderByNumberAsc(
        manualScenario.getId())).thenReturn(List.of(existingReq1, existingReq2));
    when(tmsManualScenarioRequirementRepository.findById("REQ-NEW")).thenReturn(Optional.empty());
    when(tmsManualScenarioRequirementMapper.toEntity(requirementRQ)).thenReturn(entity);
    when(tmsManualScenarioRequirementRepository.saveAll(anyList())).thenReturn(List.of(entity));

    // When
    tmsManualScenarioRequirementService.patchRequirements(manualScenario, requirements);

    // Then
    verify(tmsManualScenarioRequirementRepository).saveAll(requirementsCaptor.capture());
    var savedRequirements = requirementsCaptor.getValue();
    assertThat(savedRequirements).hasSize(1);
    assertThat(savedRequirements.get(0).getId()).isEqualTo("REQ-NEW");
    assertThat(savedRequirements.get(0).getNumber()).isEqualTo(2); // maxNumber was 1, so next is 2
    assertThat(savedRequirements.get(0).getManualScenario()).isEqualTo(manualScenario);
  }

  @Test
  void shouldPatchRequirementsUpdatingExistingValue() {
    // Given
    var requirementRQ = TmsRequirementRQ.builder().id("REQ-001").value("Updated Value").build();
    var requirements = List.of(requirementRQ);
    var existingEntity = createRequirementEntity("REQ-001", "Old Value");
    existingEntity.setManualScenario(manualScenario);
    existingEntity.setNumber(0);

    manualScenario.setRequirements(new ArrayList<>());

    when(tmsManualScenarioRequirementRepository.findByManualScenarioIdOrderByNumberAsc(
        manualScenario.getId())).thenReturn(List.of(existingEntity));
    when(tmsManualScenarioRequirementRepository.saveAll(anyList()))
        .thenReturn(List.of(existingEntity));

    // When
    tmsManualScenarioRequirementService.patchRequirements(manualScenario, requirements);

    // Then
    verify(tmsManualScenarioRequirementMapper, never()).toEntity(any());
    verify(tmsManualScenarioRequirementRepository).saveAll(requirementsCaptor.capture());

    var savedRequirements = requirementsCaptor.getValue();
    assertThat(savedRequirements).hasSize(1);
    assertThat(savedRequirements.get(0).getValue()).isEqualTo("Updated Value");
    assertThat(savedRequirements.get(0).getNumber()).isEqualTo(0); // Number preserved
  }

  @Test
  void shouldPatchRequirementsWithGlobalExistingEntity() {
    // Given
    var requirementRQ = TmsRequirementRQ.builder().id("REQ-GLOBAL").value("Global Value").build();
    var requirements = List.of(requirementRQ);

    var globalEntity = createRequirementEntity("REQ-GLOBAL", "Old Global Value");
    var otherScenario = new TmsManualScenario();
    otherScenario.setId(999L);
    globalEntity.setManualScenario(otherScenario);
    globalEntity.setNumber(5);

    manualScenario.setRequirements(new ArrayList<>());

    when(tmsManualScenarioRequirementRepository.findByManualScenarioIdOrderByNumberAsc(
        manualScenario.getId())).thenReturn(Collections.emptyList());
    when(tmsManualScenarioRequirementRepository.findById("REQ-GLOBAL"))
        .thenReturn(Optional.of(globalEntity));
    when(tmsManualScenarioRequirementRepository.saveAll(anyList()))
        .thenReturn(List.of(globalEntity));

    // When
    tmsManualScenarioRequirementService.patchRequirements(manualScenario, requirements);

    // Then
    verify(tmsManualScenarioRequirementMapper, never()).toEntity(any());
    verify(tmsManualScenarioRequirementRepository).saveAll(requirementsCaptor.capture());

    var savedRequirements = requirementsCaptor.getValue();
    assertThat(savedRequirements).hasSize(1);
    assertThat(savedRequirements.get(0).getValue()).isEqualTo("Global Value");
    assertThat(savedRequirements.get(0).getNumber()).isEqualTo(0); // maxNumber was -1, so next is 0
    assertThat(savedRequirements.get(0).getManualScenario()).isEqualTo(manualScenario);
  }

  @Test
  void shouldNotPatchRequirementsWhenListIsNull() {
    // When
    tmsManualScenarioRequirementService.patchRequirements(manualScenario, null);

    // Then
    verify(tmsManualScenarioRequirementRepository, never()).saveAll(any());
  }

  @Test
  void shouldNotPatchRequirementsWhenListIsEmpty() {
    // When
    tmsManualScenarioRequirementService.patchRequirements(manualScenario, Collections.emptyList());

    // Then
    verify(tmsManualScenarioRequirementRepository, never()).saveAll(any());
  }

  // ===== DELETE =====

  @Test
  void shouldDeleteAllByTestCaseId() {
    // When
    tmsManualScenarioRequirementService.deleteAllByTestCaseId(123L);

    // Then
    verify(tmsManualScenarioRequirementRepository).deleteAllByTestCaseId(123L);
  }

  @Test
  void shouldNotDeleteWhenTestCaseIdIsNull() {
    // When
    tmsManualScenarioRequirementService.deleteAllByTestCaseId(null);

    // Then
    verify(tmsManualScenarioRequirementRepository, never()).deleteAllByTestCaseId(any());
  }

  @Test
  void shouldDeleteAllByTestCaseIds() {
    // Given
    var testCaseIds = List.of(1L, 2L, 3L);

    // When
    tmsManualScenarioRequirementService.deleteAllByTestCaseIds(testCaseIds);

    // Then
    verify(tmsManualScenarioRequirementRepository).deleteAllByTestCaseIds(testCaseIds);
  }

  @Test
  void shouldNotDeleteWhenTestCaseIdsIsEmpty() {
    // When
    tmsManualScenarioRequirementService.deleteAllByTestCaseIds(Collections.emptyList());

    // Then
    verify(tmsManualScenarioRequirementRepository, never()).deleteAllByTestCaseIds(any());
  }

  @Test
  void shouldNotDeleteWhenTestCaseIdsIsNull() {
    // When
    tmsManualScenarioRequirementService.deleteAllByTestCaseIds(null);

    // Then
    verify(tmsManualScenarioRequirementRepository, never()).deleteAllByTestCaseIds(any());
  }

  @Test
  void shouldDeleteAllByTestFolderId() {
    // When
    tmsManualScenarioRequirementService.deleteAllByTestFolderId(1L, 123L);

    // Then
    verify(tmsManualScenarioRequirementRepository).deleteAllByTestFolderId(1L, 123L);
  }

  @Test
  void shouldNotDeleteByTestFolderIdWhenProjectIdIsNull() {
    // When
    tmsManualScenarioRequirementService.deleteAllByTestFolderId(null, 123L);

    // Then
    verify(tmsManualScenarioRequirementRepository, never()).deleteAllByTestFolderId(any(), any());
  }

  @Test
  void shouldNotDeleteByTestFolderIdWhenFolderIdIsNull() {
    // When
    tmsManualScenarioRequirementService.deleteAllByTestFolderId(1L, null);

    // Then
    verify(tmsManualScenarioRequirementRepository, never()).deleteAllByTestFolderId(any(), any());
  }

  // ===== DUPLICATE =====

  @Test
  void shouldDuplicateRequirements() {
    // Given
    var originalScenario = createManualScenario();
    var duplicatedScenario = new TmsManualScenario();
    duplicatedScenario.setId(20L);

    var originalReq1 = createRequirementEntity("REQ-001", "Requirement 1");
    originalReq1.setManualScenario(originalScenario);
    originalReq1.setNumber(1);

    var originalReq2 = createRequirementEntity("REQ-002", "Requirement 2");
    originalReq2.setManualScenario(originalScenario);
    originalReq2.setNumber(0);

    originalScenario.setRequirements(List.of(originalReq1, originalReq2));

    var duplicatedReq1 = createRequirementEntity("REQ-001", "Requirement 1");
    var duplicatedReq2 = createRequirementEntity("REQ-002", "Requirement 2");

    when(tmsManualScenarioRequirementMapper.duplicate(originalReq2)).thenReturn(duplicatedReq2);
    when(tmsManualScenarioRequirementMapper.duplicate(originalReq1)).thenReturn(duplicatedReq1);

    // When
    tmsManualScenarioRequirementService.duplicateRequirements(originalScenario, duplicatedScenario);

    // Then
    verify(tmsManualScenarioRequirementMapper).duplicate(originalReq1);
    verify(tmsManualScenarioRequirementMapper).duplicate(originalReq2);
    verify(tmsManualScenarioRequirementRepository).saveAll(requirementsCaptor.capture());

    var savedRequirements = requirementsCaptor.getValue();
    assertThat(savedRequirements).hasSize(2);

    // Verify sorting by number (0 should come before 1)
    assertThat(savedRequirements.get(0)).isEqualTo(duplicatedReq2);
    assertThat(savedRequirements.get(1)).isEqualTo(duplicatedReq1);

    // Verify manual scenario assignment
    savedRequirements.forEach(req ->
        assertThat(req.getManualScenario()).isEqualTo(duplicatedScenario));
  }

  @Test
  void shouldNotDuplicateRequirementsWhenOriginalHasNone() {
    // Given
    var originalScenario = createManualScenario();
    originalScenario.setRequirements(null);
    var duplicatedScenario = new TmsManualScenario();
    duplicatedScenario.setId(20L);

    // When
    tmsManualScenarioRequirementService.duplicateRequirements(originalScenario, duplicatedScenario);

    // Then
    verify(tmsManualScenarioRequirementRepository, never()).saveAll(any());
  }

  @Test
  void shouldNotDuplicateRequirementsWhenOriginalHasEmptySet() {
    // Given
    var originalScenario = createManualScenario();
    originalScenario.setRequirements(Collections.emptyList());
    var duplicatedScenario = new TmsManualScenario();
    duplicatedScenario.setId(20L);

    // When
    tmsManualScenarioRequirementService.duplicateRequirements(originalScenario, duplicatedScenario);

    // Then
    verify(tmsManualScenarioRequirementRepository, never()).saveAll(any());
  }

  // ===== Helper methods =====

  private TmsManualScenario createManualScenario() {
    var scenario = new TmsManualScenario();
    scenario.setId(1L);
    scenario.setExecutionEstimationTime(30);
    scenario.setType(
        com.epam.reportportal.base.infrastructure.persistence.entity.tms.enums.TmsManualScenarioType.TEXT);
    return scenario;
  }

  private TmsManualScenarioRequirement createRequirementEntity(String id, String value) {
    return TmsManualScenarioRequirement.builder()
        .id(id)
        .value(value)
        .build();
  }
}
