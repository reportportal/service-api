package com.epam.reportportal.base.core.tms.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.reportportal.base.core.tms.dto.TmsRequirementRQ;
import com.epam.reportportal.base.core.tms.mapper.TmsManualScenarioRequirementMapper;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsManualScenarioRequirementRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsManualScenario;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsManualScenarioRequirement;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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

    when(tmsManualScenarioRequirementMapper.toEntity(requirementRQ1)).thenReturn(entity1);
    when(tmsManualScenarioRequirementMapper.toEntity(requirementRQ2)).thenReturn(entity2);

    // When
    tmsManualScenarioRequirementService.createRequirements(manualScenario, requirements);

    // Then
    verify(tmsManualScenarioRequirementMapper).toEntity(requirementRQ1);
    verify(tmsManualScenarioRequirementMapper).toEntity(requirementRQ2);
    verify(tmsManualScenarioRequirementRepository).saveAll(requirementsCaptor.capture());

    var savedRequirements = requirementsCaptor.getValue();
    assertThat(savedRequirements).hasSize(2);
    savedRequirements.forEach(req -> assertThat(req.getManualScenario()).isEqualTo(manualScenario));
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
    var requirementRQ = TmsRequirementRQ.builder().id("REQ-NEW").value("New Requirement").build();
    var requirements = List.of(requirementRQ);
    var entity = createRequirementEntity("REQ-NEW", "New Requirement");

    when(tmsManualScenarioRequirementMapper.toEntity(requirementRQ)).thenReturn(entity);

    // When
    tmsManualScenarioRequirementService.updateRequirements(manualScenario, requirements);

    // Then
    verify(tmsManualScenarioRequirementRepository).deleteByManualScenarioId(manualScenario.getId());
    verify(tmsManualScenarioRequirementRepository).saveAll(requirementsCaptor.capture());

    var savedRequirements = requirementsCaptor.getValue();
    assertThat(savedRequirements).hasSize(1);
  }

  @Test
  void shouldDeleteExistingRequirementsOnUpdateWithNull() {
    // When
    tmsManualScenarioRequirementService.updateRequirements(manualScenario, null);

    // Then
    verify(tmsManualScenarioRequirementRepository).deleteByManualScenarioId(manualScenario.getId());
    verify(tmsManualScenarioRequirementRepository, never()).saveAll(any());
  }

  // ===== PATCH =====

  @Test
  void shouldPatchRequirementsWithNewEntries() {
    // Given
    var requirementRQ = TmsRequirementRQ.builder().id("REQ-NEW").value("New Requirement").build();
    var requirements = List.of(requirementRQ);
    var entity = createRequirementEntity("REQ-NEW", "New Requirement");

    when(tmsManualScenarioRequirementRepository.findById("REQ-NEW")).thenReturn(Optional.empty());
    when(tmsManualScenarioRequirementMapper.toEntity(requirementRQ)).thenReturn(entity);

    // When
    tmsManualScenarioRequirementService.patchRequirements(manualScenario, requirements);

    // Then
    verify(tmsManualScenarioRequirementRepository).saveAll(requirementsCaptor.capture());
    var savedRequirements = requirementsCaptor.getValue();
    assertThat(savedRequirements).hasSize(1);
    assertThat(savedRequirements.get(0).getId()).isEqualTo("REQ-NEW");
    assertThat(savedRequirements.get(0).getManualScenario()).isEqualTo(manualScenario);
  }

  @Test
  void shouldPatchRequirementsUpdatingExistingValue() {
    // Given
    var requirementRQ = TmsRequirementRQ.builder().id("REQ-001").value("Updated Value").build();
    var requirements = List.of(requirementRQ);
    var existingEntity = createRequirementEntity("REQ-001", "Old Value");
    existingEntity.setManualScenario(manualScenario);

    when(tmsManualScenarioRequirementRepository.findById("REQ-001"))
        .thenReturn(Optional.of(existingEntity));

    // When
    tmsManualScenarioRequirementService.patchRequirements(manualScenario, requirements);

    // Then
    verify(tmsManualScenarioRequirementMapper, never()).toEntity(any());
    verify(tmsManualScenarioRequirementRepository).saveAll(requirementsCaptor.capture());

    var savedRequirements = requirementsCaptor.getValue();
    assertThat(savedRequirements).hasSize(1);
    assertThat(savedRequirements.get(0).getValue()).isEqualTo("Updated Value");
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

  // ===== DUPLICATE =====

  @Test
  void shouldDuplicateRequirements() {
    // Given
    var originalScenario = createManualScenario();
    var duplicatedScenario = new TmsManualScenario();
    duplicatedScenario.setId(20L);

    var originalReq = createRequirementEntity("REQ-001", "Requirement 1");
    originalReq.setManualScenario(originalScenario);
    originalScenario.setRequirements(Set.of(originalReq));

    var duplicatedReq = createRequirementEntity("REQ-001", "Requirement 1");
    when(tmsManualScenarioRequirementMapper.duplicate(originalReq)).thenReturn(duplicatedReq);

    // When
    tmsManualScenarioRequirementService.duplicateRequirements(originalScenario, duplicatedScenario);

    // Then
    verify(tmsManualScenarioRequirementMapper).duplicate(originalReq);
    verify(tmsManualScenarioRequirementRepository).saveAll(requirementsCaptor.capture());

    var savedRequirements = requirementsCaptor.getValue();
    assertThat(savedRequirements).hasSize(1);
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
    originalScenario.setRequirements(Collections.emptySet());
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
