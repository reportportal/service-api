package com.epam.reportportal.base.core.tms.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.reportportal.base.core.tms.dto.TmsManualScenarioAttributeRQ;
import com.epam.reportportal.base.core.tms.mapper.TmsManualScenarioAttributeMapper;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsManualScenarioAttributeRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.Project;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsAttribute;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsManualScenario;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsManualScenarioAttribute;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsManualScenarioAttributeId;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
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
class TmsManualScenarioAttributeServiceImplTest {

  private static final Long PROJECT_ID = 1L;

  @Mock
  private TmsManualScenarioAttributeMapper tmsManualScenarioAttributeMapper;

  @Mock
  private TmsManualScenarioAttributeRepository tmsManualScenarioAttributeRepository;

  @Mock
  private TmsAttributeService tmsAttributeService;

  @InjectMocks
  private TmsManualScenarioAttributeServiceImpl tmsManualScenarioAttributeService;

  @Captor
  private ArgumentCaptor<Set<TmsManualScenarioAttribute>> attributesCaptor;

  private TmsManualScenario manualScenario;

  @BeforeEach
  void setUp() {
    manualScenario = createManualScenario();
  }

  @Test
  void shouldCreateAttributesWithExistingAttributeIds() {
    // Given
    var attributeRQ1 = createAttributeRQWithId(1L);
    var attributeRQ2 = createAttributeRQWithId(2L);
    var attributeRQs = List.of(attributeRQ1, attributeRQ2);

    var tmsAttribute1 = createTmsAttribute(1L, "key1", "value1");
    var tmsAttribute2 = createTmsAttribute(2L, "key2", "value2");

    when(tmsAttributeService.getEntityById(PROJECT_ID, 1L)).thenReturn(tmsAttribute1);
    when(tmsAttributeService.getEntityById(PROJECT_ID, 2L)).thenReturn(tmsAttribute2);

    // When
    tmsManualScenarioAttributeService.createAttributes(PROJECT_ID, manualScenario, attributeRQs);

    // Then
    verify(tmsAttributeService).getEntityById(PROJECT_ID, 1L);
    verify(tmsAttributeService).getEntityById(PROJECT_ID, 2L);
    verify(tmsAttributeService, never()).findOrCreateAttribute(anyLong(), anyString(), anyString());
    verify(tmsManualScenarioAttributeRepository).saveAll(attributesCaptor.capture());

    var savedAttributes = attributesCaptor.getValue();
    assertThat(savedAttributes).hasSize(2);
    assertThat(manualScenario.getAttributes()).hasSize(2);

    for (var attribute : savedAttributes) {
      assertThat(attribute.getManualScenario()).isEqualTo(manualScenario);
    }
  }

  @Test
  void shouldCreateAttributesWithKeyAndValue() {
    // Given
    var attributeRQ1 = createAttributeRQWithKeyAndValue("key1", "value1");
    var attributeRQ2 = createAttributeRQWithKeyAndValue("key2", "value2");
    var attributeRQs = List.of(attributeRQ1, attributeRQ2);

    var tmsAttribute1 = createTmsAttribute(1L, "key1", "value1");
    var tmsAttribute2 = createTmsAttribute(2L, "key2", "value2");

    when(tmsAttributeService.findOrCreateAttribute(PROJECT_ID, "key1", "value1"))
        .thenReturn(tmsAttribute1);
    when(tmsAttributeService.findOrCreateAttribute(PROJECT_ID, "key2", "value2"))
        .thenReturn(tmsAttribute2);

    // When
    tmsManualScenarioAttributeService.createAttributes(PROJECT_ID, manualScenario, attributeRQs);

    // Then
    verify(tmsAttributeService).findOrCreateAttribute(PROJECT_ID, "key1", "value1");
    verify(tmsAttributeService).findOrCreateAttribute(PROJECT_ID, "key2", "value2");
    verify(tmsAttributeService, never()).getEntityById(anyLong(), anyLong());
    verify(tmsManualScenarioAttributeRepository).saveAll(attributesCaptor.capture());

    var savedAttributes = attributesCaptor.getValue();
    assertThat(savedAttributes).hasSize(2);
    assertThat(manualScenario.getAttributes()).hasSize(2);
  }

  @Test
  void shouldCreateAttributesMixed() {
    // Given
    var attributeRQ1 = createAttributeRQWithId(1L);
    var attributeRQ2 = createAttributeRQWithKeyAndValue("key2", "value2");
    var attributeRQs = List.of(attributeRQ1, attributeRQ2);

    var tmsAttribute1 = createTmsAttribute(1L, "key1", "value1");
    var tmsAttribute2 = createTmsAttribute(2L, "key2", "value2");

    when(tmsAttributeService.getEntityById(PROJECT_ID, 1L)).thenReturn(tmsAttribute1);
    when(tmsAttributeService.findOrCreateAttribute(PROJECT_ID, "key2", "value2"))
        .thenReturn(tmsAttribute2);

    // When
    tmsManualScenarioAttributeService.createAttributes(PROJECT_ID, manualScenario, attributeRQs);

    // Then
    verify(tmsAttributeService).getEntityById(PROJECT_ID, 1L);
    verify(tmsAttributeService).findOrCreateAttribute(PROJECT_ID, "key2", "value2");
    verify(tmsManualScenarioAttributeRepository).saveAll(attributesCaptor.capture());

    var savedAttributes = attributesCaptor.getValue();
    assertThat(savedAttributes).hasSize(2);
    assertThat(manualScenario.getAttributes()).hasSize(2);
  }

  @Test
  void shouldNotCreateAttributesWhenListIsEmpty() {
    // When
    tmsManualScenarioAttributeService.createAttributes(PROJECT_ID, manualScenario,
        Collections.emptyList());

    // Then
    verify(tmsAttributeService, never()).getEntityById(anyLong(), anyLong());
    verify(tmsAttributeService, never()).findOrCreateAttribute(anyLong(), anyString(), anyString());
    verify(tmsManualScenarioAttributeRepository, never()).saveAll(any());
  }

  @Test
  void shouldNotCreateAttributesWhenListIsNull() {
    // When
    tmsManualScenarioAttributeService.createAttributes(PROJECT_ID, manualScenario, null);

    // Then
    verify(tmsAttributeService, never()).getEntityById(anyLong(), anyLong());
    verify(tmsAttributeService, never()).findOrCreateAttribute(anyLong(), anyString(), anyString());
    verify(tmsManualScenarioAttributeRepository, never()).saveAll(any());
  }

  @Test
  void shouldUpdateAttributesWithExistingAttributes() {
    // Given
    var existingAttributes = createExistingManualScenarioAttributes();
    manualScenario.setAttributes(existingAttributes);

    var attributeRQ = createAttributeRQWithId(3L);
    var attributeRQs = List.of(attributeRQ);

    var tmsAttribute = createTmsAttribute(3L, "key3", "value3");

    when(tmsAttributeService.getEntityById(PROJECT_ID, 3L)).thenReturn(tmsAttribute);

    // When
    tmsManualScenarioAttributeService.updateAttributes(PROJECT_ID, manualScenario, attributeRQs);

    // Then
    verify(tmsManualScenarioAttributeRepository).deleteAll(existingAttributes);
    verify(tmsAttributeService).getEntityById(PROJECT_ID, 3L);
    verify(tmsManualScenarioAttributeRepository).saveAll(any());

    assertThat(manualScenario.getAttributes()).hasSize(1);
  }

  @Test
  void shouldUpdateAttributesWhenNoExistingAttributes() {
    // Given
    manualScenario.setAttributes(new HashSet<>());

    var attributeRQ = createAttributeRQWithKeyAndValue("key1", "value1");
    var attributeRQs = List.of(attributeRQ);

    var tmsAttribute = createTmsAttribute(1L, "key1", "value1");

    when(tmsAttributeService.findOrCreateAttribute(PROJECT_ID, "key1", "value1"))
        .thenReturn(tmsAttribute);

    // When
    tmsManualScenarioAttributeService.updateAttributes(PROJECT_ID, manualScenario, attributeRQs);

    // Then
    verify(tmsManualScenarioAttributeRepository, never()).deleteAll(any());
    verify(tmsAttributeService).findOrCreateAttribute(PROJECT_ID, "key1", "value1");
    verify(tmsManualScenarioAttributeRepository).saveAll(any());
  }

  @Test
  void shouldUpdateAttributesReplacingAllAttributes() {
    // Given
    var existingAttributes = createExistingManualScenarioAttributes();
    manualScenario.setAttributes(existingAttributes);

    var attributeRQ1 = createAttributeRQWithKeyAndValue("new-key1", "new-value1");
    var attributeRQ2 = createAttributeRQWithKeyAndValue("new-key2", "new-value2");
    var attributeRQs = List.of(attributeRQ1, attributeRQ2);

    var tmsAttribute1 = createTmsAttribute(10L, "new-key1", "new-value1");
    var tmsAttribute2 = createTmsAttribute(11L, "new-key2", "new-value2");

    when(tmsAttributeService.findOrCreateAttribute(PROJECT_ID, "new-key1", "new-value1"))
        .thenReturn(tmsAttribute1);
    when(tmsAttributeService.findOrCreateAttribute(PROJECT_ID, "new-key2", "new-value2"))
        .thenReturn(tmsAttribute2);

    // When
    tmsManualScenarioAttributeService.updateAttributes(PROJECT_ID, manualScenario, attributeRQs);

    // Then
    verify(tmsManualScenarioAttributeRepository).deleteAll(existingAttributes);
    verify(tmsAttributeService).findOrCreateAttribute(PROJECT_ID, "new-key1", "new-value1");
    verify(tmsAttributeService).findOrCreateAttribute(PROJECT_ID, "new-key2", "new-value2");
    verify(tmsManualScenarioAttributeRepository).saveAll(attributesCaptor.capture());

    var savedAttributes = attributesCaptor.getValue();
    assertThat(savedAttributes).hasSize(2);
  }

  @Test
  void shouldDuplicateAttributes() {
    // Given
    var originalScenario = createOriginalScenario();
    var newScenario = createNewScenario();
    var originalAttributes = createOriginalManualScenarioAttributes();
    var duplicatedAttributes = createDuplicatedManualScenarioAttributes();

    originalScenario.setAttributes(originalAttributes);

    var originalAttributesList = originalAttributes.stream().toList();
    var duplicatedAttributesList = duplicatedAttributes.stream().toList();

    when(tmsManualScenarioAttributeMapper.duplicateAttribute(originalAttributesList.get(0),
        newScenario))
        .thenReturn(duplicatedAttributesList.get(0));
    when(tmsManualScenarioAttributeMapper.duplicateAttribute(originalAttributesList.get(1),
        newScenario))
        .thenReturn(duplicatedAttributesList.get(1));

    // When
    tmsManualScenarioAttributeService.duplicateAttributes(originalScenario, newScenario);

    // Then
    verify(tmsManualScenarioAttributeMapper).duplicateAttribute(originalAttributesList.get(0),
        newScenario);
    verify(tmsManualScenarioAttributeMapper).duplicateAttribute(originalAttributesList.get(1),
        newScenario);
    verify(tmsManualScenarioAttributeRepository).saveAll(attributesCaptor.capture());

    var savedAttributes = attributesCaptor.getValue();
    assertThat(savedAttributes).hasSize(2);
    assertThat(newScenario.getAttributes()).isEqualTo(duplicatedAttributes);
  }

  @Test
  void shouldNotDuplicateAttributesWhenOriginalAttributesIsEmpty() {
    // Given
    var originalScenario = createOriginalScenario();
    var newScenario = createNewScenario();

    originalScenario.setAttributes(new HashSet<>());

    // When
    tmsManualScenarioAttributeService.duplicateAttributes(originalScenario, newScenario);

    // Then
    verify(tmsManualScenarioAttributeMapper, never()).duplicateAttribute(any(), any());
    verify(tmsManualScenarioAttributeRepository, never()).saveAll(any());
  }

  @Test
  void shouldNotDuplicateAttributesWhenOriginalAttributesIsNull() {
    // Given
    var originalScenario = createOriginalScenario();
    var newScenario = createNewScenario();

    originalScenario.setAttributes(null);

    // When
    tmsManualScenarioAttributeService.duplicateAttributes(originalScenario, newScenario);

    // Then
    verify(tmsManualScenarioAttributeMapper, never()).duplicateAttribute(any(), any());
    verify(tmsManualScenarioAttributeRepository, never()).saveAll(any());
  }

  @Test
  void shouldDuplicateAttributesWithSingleAttribute() {
    // Given
    var originalScenario = createOriginalScenario();
    var newScenario = createNewScenario();

    var originalAttribute = createManualScenarioAttribute(2L, 1L, "key1", "value1");
    var duplicatedAttribute = createManualScenarioAttribute(3L, 1L, "key1", "value1");

    originalScenario.setAttributes(Set.of(originalAttribute));

    when(tmsManualScenarioAttributeMapper.duplicateAttribute(originalAttribute, newScenario))
        .thenReturn(duplicatedAttribute);

    // When
    tmsManualScenarioAttributeService.duplicateAttributes(originalScenario, newScenario);

    // Then
    verify(tmsManualScenarioAttributeMapper).duplicateAttribute(originalAttribute, newScenario);
    verify(tmsManualScenarioAttributeRepository).saveAll(attributesCaptor.capture());

    var savedAttributes = attributesCaptor.getValue();
    assertThat(savedAttributes).hasSize(1);
    assertThat(savedAttributes).contains(duplicatedAttribute);
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
  void shouldNotDeleteWhenTestCaseIdsIsNull() {
    // When
    tmsManualScenarioAttributeService.deleteAllByTestCaseIds(null);

    // Then
    verify(tmsManualScenarioAttributeRepository, never()).deleteAllByTestCaseIds(any());
  }

  @Test
  void shouldDeleteAllByTestFolderId() {
    // When
    tmsManualScenarioAttributeService.deleteAllByTestFolderId(PROJECT_ID, 123L);

    // Then
    verify(tmsManualScenarioAttributeRepository).deleteManualScenarioAttributesByTestFolderId(
        PROJECT_ID, 123L);
  }

  @Test
  void shouldCreateAttributesWithMultipleAttributes() {
    // Given
    var attributeRQ1 = createAttributeRQWithId(1L);
    var attributeRQ2 = createAttributeRQWithKeyAndValue("key2", "value2");
    var attributeRQ3 = createAttributeRQWithId(3L);
    var attributeRQs = List.of(attributeRQ1, attributeRQ2, attributeRQ3);

    var tmsAttribute1 = createTmsAttribute(1L, "key1", "value1");
    var tmsAttribute2 = createTmsAttribute(2L, "key2", "value2");
    var tmsAttribute3 = createTmsAttribute(3L, "key3", "value3");

    when(tmsAttributeService.getEntityById(PROJECT_ID, 1L)).thenReturn(tmsAttribute1);
    when(tmsAttributeService.findOrCreateAttribute(PROJECT_ID, "key2", "value2"))
        .thenReturn(tmsAttribute2);
    when(tmsAttributeService.getEntityById(PROJECT_ID, 3L)).thenReturn(tmsAttribute3);

    // When
    tmsManualScenarioAttributeService.createAttributes(PROJECT_ID, manualScenario, attributeRQs);

    // Then
    verify(tmsAttributeService).getEntityById(PROJECT_ID, 1L);
    verify(tmsAttributeService).findOrCreateAttribute(PROJECT_ID, "key2", "value2");
    verify(tmsAttributeService).getEntityById(PROJECT_ID, 3L);
    verify(tmsManualScenarioAttributeRepository).saveAll(attributesCaptor.capture());

    var savedAttributes = attributesCaptor.getValue();
    assertThat(savedAttributes).hasSize(3);
  }

  @Test
  void shouldUpdateAttributesWithEmptyList() {
    // Given
    var existingAttributes = createExistingManualScenarioAttributes();
    manualScenario.setAttributes(existingAttributes);

    // When
    tmsManualScenarioAttributeService.updateAttributes(PROJECT_ID, manualScenario,
        Collections.emptyList());

    // Then
    verify(tmsManualScenarioAttributeRepository).deleteAll(existingAttributes);
    verify(tmsAttributeService, never()).getEntityById(anyLong(), anyLong());
    verify(tmsAttributeService, never()).findOrCreateAttribute(anyLong(), anyString(), anyString());
    verify(tmsManualScenarioAttributeRepository, never()).saveAll(any());
  }

  @Test
  void shouldUpdateAttributesWithNullList() {
    // Given
    var existingAttributes = createExistingManualScenarioAttributes();
    manualScenario.setAttributes(existingAttributes);

    // When
    tmsManualScenarioAttributeService.updateAttributes(PROJECT_ID, manualScenario, null);

    // Then
    verify(tmsManualScenarioAttributeRepository).deleteAll(existingAttributes);
    verify(tmsAttributeService, never()).getEntityById(anyLong(), anyLong());
    verify(tmsAttributeService, never()).findOrCreateAttribute(anyLong(), anyString(), anyString());
    verify(tmsManualScenarioAttributeRepository, never()).saveAll(any());
  }

  @Test
  void shouldDuplicateAttributesWithMultipleAttributes() {
    // Given
    var originalScenario = createOriginalScenario();
    var newScenario = createNewScenario();

    var originalAttr1 = createManualScenarioAttribute(2L, 1L, "key1", "value1");
    var originalAttr2 = createManualScenarioAttribute(2L, 2L, "key2", "value2");
    var originalAttr3 = createManualScenarioAttribute(2L, 3L, "key3", "value3");

    var duplicatedAttr1 = createManualScenarioAttribute(3L, 1L, "key1", "value1");
    var duplicatedAttr2 = createManualScenarioAttribute(3L, 2L, "key2", "value2");
    var duplicatedAttr3 = createManualScenarioAttribute(3L, 3L, "key3", "value3");

    originalScenario.setAttributes(Set.of(originalAttr1, originalAttr2, originalAttr3));

    when(tmsManualScenarioAttributeMapper.duplicateAttribute(originalAttr1, newScenario))
        .thenReturn(duplicatedAttr1);
    when(tmsManualScenarioAttributeMapper.duplicateAttribute(originalAttr2, newScenario))
        .thenReturn(duplicatedAttr2);
    when(tmsManualScenarioAttributeMapper.duplicateAttribute(originalAttr3, newScenario))
        .thenReturn(duplicatedAttr3);

    // When
    tmsManualScenarioAttributeService.duplicateAttributes(originalScenario, newScenario);

    // Then
    verify(tmsManualScenarioAttributeMapper, times(3)).duplicateAttribute(any(), eq(newScenario));
    verify(tmsManualScenarioAttributeRepository).saveAll(attributesCaptor.capture());

    var savedAttributes = attributesCaptor.getValue();
    assertThat(savedAttributes).hasSize(3);
  }

  // Helper methods
  private TmsManualScenario createManualScenario() {
    var scenario = new TmsManualScenario();
    scenario.setId(1L);
    scenario.setAttributes(new HashSet<>());
    return scenario;
  }

  private TmsManualScenario createOriginalScenario() {
    var scenario = new TmsManualScenario();
    scenario.setId(2L);
    scenario.setAttributes(new HashSet<>());
    return scenario;
  }

  private TmsManualScenario createNewScenario() {
    var scenario = new TmsManualScenario();
    scenario.setId(3L);
    scenario.setAttributes(new HashSet<>());
    return scenario;
  }

  private TmsManualScenarioAttributeRQ createAttributeRQWithId(Long id) {
    var rq = new TmsManualScenarioAttributeRQ();
    rq.setId(id);
    return rq;
  }

  private TmsManualScenarioAttributeRQ createAttributeRQWithKeyAndValue(String key, String value) {
    var rq = new TmsManualScenarioAttributeRQ();
    rq.setKey(key);
    rq.setValue(value);
    return rq;
  }

  private TmsAttribute createTmsAttribute(Long id, String key, String value) {
    var attribute = new TmsAttribute();
    attribute.setId(id);
    attribute.setKey(key);
    attribute.setValue(value);
    var project = new Project();
    project.setId(PROJECT_ID);
    attribute.setProject(project);
    return attribute;
  }

  private Set<TmsManualScenarioAttribute> createExistingManualScenarioAttributes() {
    var attr1 = createManualScenarioAttribute(1L, 1L, "old-key1", "old-value1");
    var attr2 = createManualScenarioAttribute(1L, 2L, "old-key2", "old-value2");
    return new HashSet<>(Arrays.asList(attr1, attr2));
  }

  private Set<TmsManualScenarioAttribute> createOriginalManualScenarioAttributes() {
    var attr1 = createManualScenarioAttribute(2L, 1L, "original-key1", "original-value1");
    var attr2 = createManualScenarioAttribute(2L, 2L, "original-key2", "original-value2");
    return new HashSet<>(Arrays.asList(attr1, attr2));
  }

  private Set<TmsManualScenarioAttribute> createDuplicatedManualScenarioAttributes() {
    var attr1 = createManualScenarioAttribute(3L, 1L, "original-key1", "original-value1");
    var attr2 = createManualScenarioAttribute(3L, 2L, "original-key2", "original-value2");
    return new HashSet<>(Arrays.asList(attr1, attr2));
  }

  private TmsManualScenarioAttribute createManualScenarioAttribute(Long scenarioId,
      Long attributeId, String key, String value) {
    var manualScenarioAttribute = new TmsManualScenarioAttribute();
    var id = new TmsManualScenarioAttributeId();
    id.setManualScenarioId(scenarioId);
    id.setAttributeId(attributeId);
    manualScenarioAttribute.setId(id);

    var tmsAttribute = createTmsAttribute(attributeId, key, value);
    manualScenarioAttribute.setAttribute(tmsAttribute);

    var scenario = new TmsManualScenario();
    scenario.setId(scenarioId);
    manualScenarioAttribute.setManualScenario(scenario);

    return manualScenarioAttribute;
  }
}
