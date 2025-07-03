package com.epam.ta.reportportal.core.tms.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.ta.reportportal.core.tms.db.entity.TmsManualScenario;
import com.epam.ta.reportportal.core.tms.db.entity.TmsManualScenarioAttribute;
import com.epam.ta.reportportal.core.tms.db.entity.TmsManualScenarioAttributeId;
import com.epam.ta.reportportal.core.tms.db.repository.TmsManualScenarioAttributeRepository;
import com.epam.ta.reportportal.core.tms.dto.TmsManualScenarioAttributeRQ;
import com.epam.ta.reportportal.core.tms.mapper.TmsManualScenarioAttributeMapper;
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
class TmsManualScenarioAttributeServiceImplTest {

  @Mock
  private TmsManualScenarioAttributeMapper tmsManualScenarioAttributeMapper;

  @Mock
  private TmsManualScenarioAttributeRepository tmsManualScenarioAttributeRepository;

  @InjectMocks
  private TmsManualScenarioAttributeServiceImpl tmsManualScenarioAttributeService;

  private TmsManualScenario manualScenario;
  private List<TmsManualScenarioAttributeRQ> attributeRQs;
  private Set<TmsManualScenarioAttribute> attributes;

  @BeforeEach
  void setUp() {
    manualScenario = createManualScenario();
    attributeRQs = createAttributeRQs();
    attributes = createAttributes();
  }

  @Test
  void shouldCreateAttributes() {
    // Given
    when(tmsManualScenarioAttributeMapper.convertToTmsManualScenarioAttributes(attributeRQs))
        .thenReturn(attributes);

    // When
    tmsManualScenarioAttributeService.createAttributes(manualScenario, attributeRQs);

    // Then
    verify(tmsManualScenarioAttributeMapper).convertToTmsManualScenarioAttributes(attributeRQs);
    verify(tmsManualScenarioAttributeRepository).saveAll(attributes);

    // Verify manual scenario is set on each attribute
    for (var attribute : attributes) {
      assertThat(attribute.getManualScenario()).isEqualTo(manualScenario);
    }
    assertThat(manualScenario.getAttributes()).isEqualTo(attributes);
  }

  @Test
  void shouldNotCreateAttributesWhenListIsEmpty() {
    // When
    tmsManualScenarioAttributeService.createAttributes(manualScenario, Collections.emptyList());

    // Then
    verify(tmsManualScenarioAttributeMapper, never()).convertToTmsManualScenarioAttributes(anyList());
    verify(tmsManualScenarioAttributeRepository, never()).saveAll(any());
  }

  @Test
  void shouldNotCreateAttributesWhenListIsNull() {
    // When
    tmsManualScenarioAttributeService.createAttributes(manualScenario, null);

    // Then
    verify(tmsManualScenarioAttributeMapper, never()).convertToTmsManualScenarioAttributes(anyList());
    verify(tmsManualScenarioAttributeRepository, never()).saveAll(any());
  }

  @Test
  void shouldUpdateAttributes() {
    // Given
    var existingAttributes = createExistingAttributes();
    manualScenario.setAttributes(existingAttributes);

    when(tmsManualScenarioAttributeMapper.convertToTmsManualScenarioAttributes(attributeRQs))
        .thenReturn(attributes);

    // When
    tmsManualScenarioAttributeService.updateAttributes(manualScenario, attributeRQs);

    // Then
    verify(tmsManualScenarioAttributeRepository).deleteAll(existingAttributes);
    verify(tmsManualScenarioAttributeMapper).convertToTmsManualScenarioAttributes(attributeRQs);
    verify(tmsManualScenarioAttributeRepository).saveAll(attributes);

    assertThat(manualScenario.getAttributes()).isEqualTo(attributes);
  }

  @Test
  void shouldUpdateAttributesWhenNoExistingAttributes() {
    // Given
    manualScenario.setAttributes(new HashSet<>());
    when(tmsManualScenarioAttributeMapper.convertToTmsManualScenarioAttributes(attributeRQs))
        .thenReturn(attributes);

    // When
    tmsManualScenarioAttributeService.updateAttributes(manualScenario, attributeRQs);

    // Then
    verify(tmsManualScenarioAttributeRepository, never()).deleteAll(any());
    verify(tmsManualScenarioAttributeMapper).convertToTmsManualScenarioAttributes(attributeRQs);
    verify(tmsManualScenarioAttributeRepository).saveAll(attributes);
  }

  @Test
  void shouldPatchAttributesWithNewAndUpdatedAttributes() {
    // Given
    var existingAttributes = createExistingAttributesForPatch();
    manualScenario.setAttributes(existingAttributes);

    var newAttributeRQs = createAttributeRQsForPatch();
    var newAttributes = createNewAttributesForPatch();

    when(tmsManualScenarioAttributeMapper.convertToTmsManualScenarioAttributes(newAttributeRQs))
        .thenReturn(newAttributes);

    // When
    tmsManualScenarioAttributeService.patchAttributes(manualScenario, newAttributeRQs);

    // Then
    verify(tmsManualScenarioAttributeRepository).saveAll(any());
    // Should update existing attribute with id 1 and add new attribute with id 3
    // Should delete attribute with id 2 (not in new list)
  }

  @Test
  void shouldPatchAttributesWhenNoExistingAttributes() {
    // Given
    manualScenario.setAttributes(new HashSet<>());
    when(tmsManualScenarioAttributeMapper.convertToTmsManualScenarioAttributes(attributeRQs))
        .thenReturn(attributes);

    // When
    tmsManualScenarioAttributeService.patchAttributes(manualScenario, attributeRQs);

    // Then
    verify(tmsManualScenarioAttributeMapper).convertToTmsManualScenarioAttributes(attributeRQs);
    verify(tmsManualScenarioAttributeRepository).saveAll(attributes);
  }

  @Test
  void shouldNotPatchAttributesWhenListIsEmpty() {
    // When
    tmsManualScenarioAttributeService.patchAttributes(manualScenario, Collections.emptyList());

    // Then
    verify(tmsManualScenarioAttributeMapper, never()).convertToTmsManualScenarioAttributes(anyList());
    verify(tmsManualScenarioAttributeRepository, never()).saveAll(any());
  }

  @Test
  void shouldDeleteAllByTestCaseId() {
    // When
    tmsManualScenarioAttributeService.deleteAllByTestCaseId(123L);

    // Then
    verify(tmsManualScenarioAttributeRepository).deleteAllByTestCaseId(123L);
  }

  @Test
  void shouldDeleteAllByTestCaseIds() {
    // Given
    var testCaseIds = List.of(1L, 2L, 3L);

    // When
    tmsManualScenarioAttributeService.deleteAllByTestCaseIds(testCaseIds);

    // Then
    verify(tmsManualScenarioAttributeRepository).deleteAllByTestCaseIds(testCaseIds);
  }

  @Test
  void shouldNotDeleteWhenTestCaseIdsIsEmpty() {
    // When
    tmsManualScenarioAttributeService.deleteAllByTestCaseIds(Collections.emptyList());

    // Then
    verify(tmsManualScenarioAttributeRepository, never()).deleteAllByTestCaseIds(any());
  }

  @Test
  void shouldDeleteAllByTestFolderId() {
    // When
    tmsManualScenarioAttributeService.deleteAllByTestFolderId(1L, 123L);

    // Then
    verify(tmsManualScenarioAttributeRepository).deleteManualScenarioAttributesByTestFolderId(1L, 123L);
  }

  // Helper methods
  private TmsManualScenario createManualScenario() {
    var scenario = new TmsManualScenario();
    scenario.setId(1L);
    scenario.setAttributes(new HashSet<>());
    return scenario;
  }

  private List<TmsManualScenarioAttributeRQ> createAttributeRQs() {
    var attr1 = new TmsManualScenarioAttributeRQ();
    attr1.setAttributeId(1L);
    attr1.setValue("Value 1");

    var attr2 = new TmsManualScenarioAttributeRQ();
    attr2.setAttributeId(2L);
    attr2.setValue("Value 2");

    return Arrays.asList(attr1, attr2);
  }

  private Set<TmsManualScenarioAttribute> createAttributes() {
    var attr1 = new TmsManualScenarioAttribute();
    var id1 = new TmsManualScenarioAttributeId();
    id1.setAttributeId(1L);
    id1.setManualScenarioId(1L);
    attr1.setId(id1);
    attr1.setValue("Value 1");

    var attr2 = new TmsManualScenarioAttribute();
    var id2 = new TmsManualScenarioAttributeId();
    id2.setAttributeId(2L);
    id2.setManualScenarioId(1L);
    attr2.setId(id2);
    attr2.setValue("Value 2");

    return new HashSet<>(Arrays.asList(attr1, attr2));
  }

  private Set<TmsManualScenarioAttribute> createExistingAttributes() {
    var attr1 = new TmsManualScenarioAttribute();
    var id1 = new TmsManualScenarioAttributeId();
    id1.setAttributeId(1L);
    id1.setManualScenarioId(1L);
    attr1.setId(id1);
    attr1.setValue("Old Value 1");

    return new HashSet<>(Collections.singletonList(attr1));
  }

  private Set<TmsManualScenarioAttribute> createExistingAttributesForPatch() {
    var attr1 = new TmsManualScenarioAttribute();
    var id1 = new TmsManualScenarioAttributeId();
    id1.setAttributeId(1L);
    id1.setManualScenarioId(1L);
    attr1.setId(id1);
    attr1.setValue("Old Value 1");

    var attr2 = new TmsManualScenarioAttribute();
    var id2 = new TmsManualScenarioAttributeId();
    id2.setAttributeId(2L);
    id2.setManualScenarioId(1L);
    attr2.setId(id2);
    attr2.setValue("Old Value 2");

    return new HashSet<>(Arrays.asList(attr1, attr2));
  }

  private List<TmsManualScenarioAttributeRQ> createAttributeRQsForPatch() {
    var attr1 = new TmsManualScenarioAttributeRQ();
    attr1.setAttributeId(1L);
    attr1.setValue("Updated Value 1");

    var attr3 = new TmsManualScenarioAttributeRQ();
    attr3.setAttributeId(3L);
    attr3.setValue("New Value 3");

    return Arrays.asList(attr1, attr3);
  }

  private Set<TmsManualScenarioAttribute> createNewAttributesForPatch() {
    var attr1 = new TmsManualScenarioAttribute();
    var id1 = new TmsManualScenarioAttributeId();
    id1.setAttributeId(1L);
    id1.setManualScenarioId(1L);
    attr1.setId(id1);
    attr1.setValue("Updated Value 1");

    var attr3 = new TmsManualScenarioAttribute();
    var id3 = new TmsManualScenarioAttributeId();
    id3.setAttributeId(3L);
    id3.setManualScenarioId(1L);
    attr3.setId(id3);
    attr3.setValue("New Value 3");

    return new HashSet<>(Arrays.asList(attr1, attr3));
  }
}
