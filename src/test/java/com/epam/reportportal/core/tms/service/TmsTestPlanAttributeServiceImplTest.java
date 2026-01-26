package com.epam.reportportal.core.tms.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.epam.reportportal.core.tms.dto.TmsTestPlanAttributeRQ;
import com.epam.reportportal.core.tms.mapper.TmsTestPlanAttributeMapper;
import com.epam.reportportal.infrastructure.persistence.dao.ItemAttributeRepository;
import com.epam.reportportal.infrastructure.persistence.dao.tms.TmsTestPlanAttributeRepository;
import com.epam.reportportal.infrastructure.persistence.entity.ItemAttribute;
import com.epam.reportportal.infrastructure.persistence.entity.tms.TmsTestPlan;
import com.epam.reportportal.infrastructure.persistence.entity.tms.TmsTestPlanAttribute;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TmsTestPlanAttributeServiceImplTest {

  @Mock
  private TmsTestPlanAttributeMapper tmsTestPlanAttributeMapper;

  @Mock
  private TmsTestPlanAttributeRepository tmsTestPlanAttributeRepository;

  @Mock
  private ItemAttributeRepository itemAttributeRepository;

  @InjectMocks
  private TmsTestPlanAttributeServiceImpl sut;

  // ==================== createTestPlanAttributes tests ====================

  @Test
  void shouldNotCreateTestPlanAttributesWhenEmptyAttributes() {
    var tmsTestPlan = new TmsTestPlan();
    List<TmsTestPlanAttributeRQ> attributes = Collections.emptyList();

    assertDoesNotThrow(() -> sut.createTestPlanAttributes(tmsTestPlan, attributes));

    verifyNoInteractions(tmsTestPlanAttributeMapper, tmsTestPlanAttributeRepository,
        itemAttributeRepository);
  }

  @Test
  void shouldNotCreateTestPlanAttributesWhenNullAttributes() {
    var tmsTestPlan = new TmsTestPlan();

    assertDoesNotThrow(() -> sut.createTestPlanAttributes(tmsTestPlan, null));

    verifyNoInteractions(tmsTestPlanAttributeMapper, tmsTestPlanAttributeRepository,
        itemAttributeRepository);
  }

  @Test
  void shouldCreateTestPlanAttributes() {
    var tmsTestPlan = new TmsTestPlan();
    var attributeRQ1 = new TmsTestPlanAttributeRQ();
    var attributeRQ2 = new TmsTestPlanAttributeRQ();
    var attributesRQ = List.of(attributeRQ1, attributeRQ2);

    var itemAttribute1 = new ItemAttribute();
    var itemAttribute2 = new ItemAttribute();

    var tmsTestPlanAttribute1 = new TmsTestPlanAttribute();
    tmsTestPlanAttribute1.setItemAttribute(itemAttribute1);
    var tmsTestPlanAttribute2 = new TmsTestPlanAttribute();
    tmsTestPlanAttribute2.setItemAttribute(itemAttribute2);

    when(tmsTestPlanAttributeMapper.convertToTmsTestPlanAttribute(tmsTestPlan, attributeRQ1))
        .thenReturn(tmsTestPlanAttribute1);
    when(tmsTestPlanAttributeMapper.convertToTmsTestPlanAttribute(tmsTestPlan, attributeRQ2))
        .thenReturn(tmsTestPlanAttribute2);

    assertDoesNotThrow(() -> sut.createTestPlanAttributes(tmsTestPlan, attributesRQ));

    assertNotNull(tmsTestPlan.getAttributes());
    assertEquals(2, tmsTestPlan.getAttributes().size());
    assertTrue(tmsTestPlan.getAttributes().contains(tmsTestPlanAttribute1));
    assertTrue(tmsTestPlan.getAttributes().contains(tmsTestPlanAttribute2));

    verify(tmsTestPlanAttributeMapper).convertToTmsTestPlanAttribute(tmsTestPlan, attributeRQ1);
    verify(tmsTestPlanAttributeMapper).convertToTmsTestPlanAttribute(tmsTestPlan, attributeRQ2);
    verify(itemAttributeRepository).saveAll(any(List.class));
    verify(tmsTestPlanAttributeRepository).saveAll(any(Set.class));
  }

  @Test
  void shouldNotSaveAttributesWhenMapperReturnsNullForAllAttributes() {
    var tmsTestPlan = new TmsTestPlan();
    var attributeRQ = new TmsTestPlanAttributeRQ();
    var attributesRQ = List.of(attributeRQ);

    when(tmsTestPlanAttributeMapper.convertToTmsTestPlanAttribute(tmsTestPlan, attributeRQ))
        .thenReturn(null);

    assertDoesNotThrow(() -> sut.createTestPlanAttributes(tmsTestPlan, attributesRQ));

    verify(tmsTestPlanAttributeMapper).convertToTmsTestPlanAttribute(tmsTestPlan, attributeRQ);
    verify(itemAttributeRepository, never()).saveAll(any());
    verify(tmsTestPlanAttributeRepository, never()).saveAll(any());
  }

  @Test
  void shouldSkipNullAttributesAndSaveValidOnes() {
    var tmsTestPlan = new TmsTestPlan();
    var attributeRQ1 = new TmsTestPlanAttributeRQ();
    var attributeRQ2 = new TmsTestPlanAttributeRQ();
    var attributesRQ = List.of(attributeRQ1, attributeRQ2);

    var itemAttribute = new ItemAttribute();
    var tmsTestPlanAttribute = new TmsTestPlanAttribute();
    tmsTestPlanAttribute.setItemAttribute(itemAttribute);

    when(tmsTestPlanAttributeMapper.convertToTmsTestPlanAttribute(tmsTestPlan, attributeRQ1))
        .thenReturn(null);
    when(tmsTestPlanAttributeMapper.convertToTmsTestPlanAttribute(tmsTestPlan, attributeRQ2))
        .thenReturn(tmsTestPlanAttribute);

    assertDoesNotThrow(() -> sut.createTestPlanAttributes(tmsTestPlan, attributesRQ));

    assertNotNull(tmsTestPlan.getAttributes());
    assertEquals(1, tmsTestPlan.getAttributes().size());
    assertTrue(tmsTestPlan.getAttributes().contains(tmsTestPlanAttribute));

    verify(tmsTestPlanAttributeMapper, times(2)).convertToTmsTestPlanAttribute(any(), any());
    verify(itemAttributeRepository).saveAll(any(List.class));
    verify(tmsTestPlanAttributeRepository).saveAll(any(Set.class));
  }

  // ==================== updateTestPlanAttributes tests ====================

  @Test
  void shouldNotDeleteExistingAttributesWhenEmptyAttributesProvided() {
    var existingTestPlan = new TmsTestPlan();
    List<TmsTestPlanAttributeRQ> attributes = Collections.emptyList();

    assertDoesNotThrow(() -> sut.updateTestPlanAttributes(existingTestPlan, attributes));

    verify(tmsTestPlanAttributeRepository, never()).deleteAll(any());
    verifyNoInteractions(tmsTestPlanAttributeMapper, itemAttributeRepository);
  }

  @Test
  void shouldNotDeleteExistingAttributesWhenNullAttributesProvided() {
    var existingTestPlan = new TmsTestPlan();
    existingTestPlan.setId(1L);

    assertDoesNotThrow(() -> sut.updateTestPlanAttributes(existingTestPlan, null));

    verify(tmsTestPlanAttributeRepository, never()).deleteAll(any());
    verifyNoInteractions(tmsTestPlanAttributeMapper, itemAttributeRepository);
  }

  @Test
  void shouldUpdateTestPlanAttributesWithDeletingOld() {
    var existingTestPlan = new TmsTestPlan();
    var existingAttribute = new TmsTestPlanAttribute();
    Set<TmsTestPlanAttribute> existingAttributes = new HashSet<>();
    existingAttributes.add(existingAttribute);
    existingTestPlan.setAttributes(existingAttributes);

    var attributeRQ = new TmsTestPlanAttributeRQ();
    var attributesRQ = List.of(attributeRQ);

    var itemAttribute = new ItemAttribute();
    var newTmsTestPlanAttribute = new TmsTestPlanAttribute();
    newTmsTestPlanAttribute.setItemAttribute(itemAttribute);

    when(tmsTestPlanAttributeMapper.convertToTmsTestPlanAttribute(existingTestPlan, attributeRQ))
        .thenReturn(newTmsTestPlanAttribute);

    assertDoesNotThrow(() -> sut.updateTestPlanAttributes(existingTestPlan, attributesRQ));

    verify(tmsTestPlanAttributeRepository).deleteAll(any());
    verify(tmsTestPlanAttributeMapper).convertToTmsTestPlanAttribute(existingTestPlan, attributeRQ);
    verify(itemAttributeRepository).saveAll(any(List.class));
    verify(tmsTestPlanAttributeRepository).saveAll(any(Set.class));
  }

  @Test
  void shouldUpdateTestPlanAttributesWhenNoExistingAttributes() {
    var existingTestPlan = new TmsTestPlan();
    existingTestPlan.setAttributes(new HashSet<>());

    var attributeRQ = new TmsTestPlanAttributeRQ();
    var attributesRQ = List.of(attributeRQ);

    var itemAttribute = new ItemAttribute();
    var newTmsTestPlanAttribute = new TmsTestPlanAttribute();
    newTmsTestPlanAttribute.setItemAttribute(itemAttribute);

    when(tmsTestPlanAttributeMapper.convertToTmsTestPlanAttribute(existingTestPlan, attributeRQ))
        .thenReturn(newTmsTestPlanAttribute);

    assertDoesNotThrow(() -> sut.updateTestPlanAttributes(existingTestPlan, attributesRQ));

    verify(tmsTestPlanAttributeRepository, never()).deleteAll(any());
    verify(tmsTestPlanAttributeMapper).convertToTmsTestPlanAttribute(existingTestPlan, attributeRQ);
    verify(itemAttributeRepository).saveAll(any(List.class));
    verify(tmsTestPlanAttributeRepository).saveAll(any(Set.class));
  }

  // ==================== patchTestPlanAttributes tests ====================

  @Test
  void shouldNotPatchTestPlanAttributesWhenEmptyAttributes() {
    var existingTestPlan = new TmsTestPlan();
    Set<TmsTestPlanAttribute> existingAttributes = new HashSet<>();
    existingAttributes.add(new TmsTestPlanAttribute());
    existingTestPlan.setAttributes(existingAttributes);
    var initialSize = existingAttributes.size();

    List<TmsTestPlanAttributeRQ> attributes = Collections.emptyList();

    assertDoesNotThrow(() -> sut.patchTestPlanAttributes(existingTestPlan, attributes));

    assertEquals(initialSize, existingTestPlan.getAttributes().size());
    verifyNoInteractions(tmsTestPlanAttributeMapper, tmsTestPlanAttributeRepository,
        itemAttributeRepository);
  }

  @Test
  void shouldNotPatchTestPlanAttributesWhenNullAttributes() {
    var existingTestPlan = new TmsTestPlan();
    Set<TmsTestPlanAttribute> existingAttributes = new HashSet<>();
    existingAttributes.add(new TmsTestPlanAttribute());
    existingTestPlan.setAttributes(existingAttributes);
    var initialSize = existingAttributes.size();

    assertDoesNotThrow(() -> sut.patchTestPlanAttributes(existingTestPlan, null));

    assertEquals(initialSize, existingTestPlan.getAttributes().size());
    verifyNoInteractions(tmsTestPlanAttributeMapper, tmsTestPlanAttributeRepository,
        itemAttributeRepository);
  }

  @Test
  void shouldPatchTestPlanAttributes() {
    var existingTestPlan = new TmsTestPlan();
    var existingAttribute = new TmsTestPlanAttribute();
    Set<TmsTestPlanAttribute> existingAttributes = new HashSet<>();
    existingAttributes.add(existingAttribute);
    existingTestPlan.setAttributes(existingAttributes);

    var attributeRQ1 = new TmsTestPlanAttributeRQ();
    var attributeRQ2 = new TmsTestPlanAttributeRQ();
    var attributesRQ = List.of(attributeRQ1, attributeRQ2);

    var itemAttribute1 = new ItemAttribute();
    var itemAttribute2 = new ItemAttribute();

    var newAttribute1 = new TmsTestPlanAttribute();
    newAttribute1.setItemAttribute(itemAttribute1);
    var newAttribute2 = new TmsTestPlanAttribute();
    newAttribute2.setItemAttribute(itemAttribute2);

    when(tmsTestPlanAttributeMapper.convertToTmsTestPlanAttribute(existingTestPlan, attributeRQ1))
        .thenReturn(newAttribute1);
    when(tmsTestPlanAttributeMapper.convertToTmsTestPlanAttribute(existingTestPlan, attributeRQ2))
        .thenReturn(newAttribute2);

    assertDoesNotThrow(() -> sut.patchTestPlanAttributes(existingTestPlan, attributesRQ));

    // Should contain both existing and new attributes
    assertEquals(3, existingTestPlan.getAttributes().size());
    assertTrue(existingTestPlan.getAttributes().contains(existingAttribute));
    assertTrue(existingTestPlan.getAttributes().contains(newAttribute1));
    assertTrue(existingTestPlan.getAttributes().contains(newAttribute2));

    verify(tmsTestPlanAttributeMapper).convertToTmsTestPlanAttribute(existingTestPlan, attributeRQ1);
    verify(tmsTestPlanAttributeMapper).convertToTmsTestPlanAttribute(existingTestPlan, attributeRQ2);
    verify(itemAttributeRepository).saveAll(any(List.class));
    verify(tmsTestPlanAttributeRepository).saveAll(any(Set.class));
  }

  @Test
  void shouldNotPatchWhenMapperReturnsNullForAllAttributes() {
    var existingTestPlan = new TmsTestPlan();
    Set<TmsTestPlanAttribute> existingAttributes = new HashSet<>();
    existingTestPlan.setAttributes(existingAttributes);

    var attributeRQ = new TmsTestPlanAttributeRQ();
    var attributesRQ = List.of(attributeRQ);

    when(tmsTestPlanAttributeMapper.convertToTmsTestPlanAttribute(existingTestPlan, attributeRQ))
        .thenReturn(null);

    assertDoesNotThrow(() -> sut.patchTestPlanAttributes(existingTestPlan, attributesRQ));

    verify(tmsTestPlanAttributeMapper).convertToTmsTestPlanAttribute(existingTestPlan, attributeRQ);
    verify(itemAttributeRepository, never()).saveAll(any());
    verify(tmsTestPlanAttributeRepository, never()).saveAll(any());
  }

  // ==================== deleteAllByTestPlanId tests ====================

  @Test
  void shouldDeleteAllByTestPlanId() {
    var testPlanId = 123L;

    assertDoesNotThrow(() -> sut.deleteAllByTestPlanId(testPlanId));

    verify(tmsTestPlanAttributeRepository).deleteAllByTestPlanId(testPlanId);
  }

  // ==================== duplicateTestPlanAttributes tests ====================

  @Test
  void shouldNotDuplicateTestPlanAttributesWhenEmptyAttributes() {
    var originalTestPlan = new TmsTestPlan();
    originalTestPlan.setAttributes(Collections.emptySet());
    var newTestPlan = new TmsTestPlan();

    assertDoesNotThrow(() -> sut.duplicateTestPlanAttributes(originalTestPlan, newTestPlan));

    verifyNoInteractions(tmsTestPlanAttributeMapper, tmsTestPlanAttributeRepository,
        itemAttributeRepository);
  }

  @Test
  void shouldNotDuplicateTestPlanAttributesWhenNullAttributes() {
    var originalTestPlan = new TmsTestPlan();
    originalTestPlan.setAttributes(null);
    var newTestPlan = new TmsTestPlan();

    assertDoesNotThrow(() -> sut.duplicateTestPlanAttributes(originalTestPlan, newTestPlan));

    verifyNoInteractions(tmsTestPlanAttributeMapper, tmsTestPlanAttributeRepository,
        itemAttributeRepository);
  }

  @Test
  void shouldDuplicateTestPlanAttributesSuccessfully() {
    var originalTestPlan = new TmsTestPlan();
    originalTestPlan.setId(1L);

    var originalAttribute1 = new TmsTestPlanAttribute();
    var originalAttribute2 = new TmsTestPlanAttribute();

    Set<TmsTestPlanAttribute> originalAttributes = new HashSet<>();
    originalAttributes.add(originalAttribute1);
    originalAttributes.add(originalAttribute2);
    originalTestPlan.setAttributes(originalAttributes);

    var newTestPlan = new TmsTestPlan();
    newTestPlan.setId(2L);

    var itemAttribute1 = new ItemAttribute();
    var itemAttribute2 = new ItemAttribute();

    var duplicatedAttribute1 = new TmsTestPlanAttribute();
    duplicatedAttribute1.setItemAttribute(itemAttribute1);
    var duplicatedAttribute2 = new TmsTestPlanAttribute();
    duplicatedAttribute2.setItemAttribute(itemAttribute2);

    when(tmsTestPlanAttributeMapper.duplicateTestPlanAttribute(originalAttribute1, newTestPlan))
        .thenReturn(duplicatedAttribute1);
    when(tmsTestPlanAttributeMapper.duplicateTestPlanAttribute(originalAttribute2, newTestPlan))
        .thenReturn(duplicatedAttribute2);

    assertDoesNotThrow(() -> sut.duplicateTestPlanAttributes(originalTestPlan, newTestPlan));

    assertNotNull(newTestPlan.getAttributes());
    assertEquals(2, newTestPlan.getAttributes().size());
    assertTrue(newTestPlan.getAttributes().contains(duplicatedAttribute1));
    assertTrue(newTestPlan.getAttributes().contains(duplicatedAttribute2));

    verify(tmsTestPlanAttributeMapper).duplicateTestPlanAttribute(originalAttribute1, newTestPlan);
    verify(tmsTestPlanAttributeMapper).duplicateTestPlanAttribute(originalAttribute2, newTestPlan);
    verify(itemAttributeRepository).saveAll(any(List.class));
    verify(tmsTestPlanAttributeRepository).saveAll(any(Set.class));
  }

  @Test
  void shouldNotDuplicateWhenMapperReturnsNullForAllAttributes() {
    var originalTestPlan = new TmsTestPlan();
    originalTestPlan.setId(1L);

    var originalAttribute = new TmsTestPlanAttribute();
    Set<TmsTestPlanAttribute> originalAttributes = new HashSet<>();
    originalAttributes.add(originalAttribute);
    originalTestPlan.setAttributes(originalAttributes);

    var newTestPlan = new TmsTestPlan();
    newTestPlan.setId(2L);

    when(tmsTestPlanAttributeMapper.duplicateTestPlanAttribute(originalAttribute, newTestPlan))
        .thenReturn(null);

    assertDoesNotThrow(() -> sut.duplicateTestPlanAttributes(originalTestPlan, newTestPlan));

    assertNull(newTestPlan.getAttributes());

    verify(tmsTestPlanAttributeMapper).duplicateTestPlanAttribute(originalAttribute, newTestPlan);
    verify(itemAttributeRepository, never()).saveAll(any());
    verify(tmsTestPlanAttributeRepository, never()).saveAll(any());
  }

  @Test
  void shouldDuplicateTestPlanAttributesWithSingleAttribute() {
    var originalTestPlan = new TmsTestPlan();
    originalTestPlan.setId(1L);

    var originalAttribute = new TmsTestPlanAttribute();
    Set<TmsTestPlanAttribute> originalAttributes = new HashSet<>();
    originalAttributes.add(originalAttribute);
    originalTestPlan.setAttributes(originalAttributes);

    var newTestPlan = new TmsTestPlan();
    newTestPlan.setId(2L);

    var itemAttribute = new ItemAttribute();
    var duplicatedAttribute = new TmsTestPlanAttribute();
    duplicatedAttribute.setItemAttribute(itemAttribute);

    when(tmsTestPlanAttributeMapper.duplicateTestPlanAttribute(originalAttribute, newTestPlan))
        .thenReturn(duplicatedAttribute);

    assertDoesNotThrow(() -> sut.duplicateTestPlanAttributes(originalTestPlan, newTestPlan));

    assertNotNull(newTestPlan.getAttributes());
    assertEquals(1, newTestPlan.getAttributes().size());
    assertTrue(newTestPlan.getAttributes().contains(duplicatedAttribute));

    verify(tmsTestPlanAttributeMapper).duplicateTestPlanAttribute(originalAttribute, newTestPlan);
    verify(itemAttributeRepository).saveAll(any(List.class));
    verify(tmsTestPlanAttributeRepository).saveAll(any(Set.class));
  }

  @Test
  void shouldSkipNullDuplicatedAttributesAndSaveValidOnes() {
    var originalTestPlan = new TmsTestPlan();
    originalTestPlan.setId(1L);

    var originalAttribute1 = new TmsTestPlanAttribute();
    var originalAttribute2 = new TmsTestPlanAttribute();
    Set<TmsTestPlanAttribute> originalAttributes = new HashSet<>();
    originalAttributes.add(originalAttribute1);
    originalAttributes.add(originalAttribute2);
    originalTestPlan.setAttributes(originalAttributes);

    var newTestPlan = new TmsTestPlan();
    newTestPlan.setId(2L);

    var itemAttribute = new ItemAttribute();
    var duplicatedAttribute = new TmsTestPlanAttribute();
    duplicatedAttribute.setItemAttribute(itemAttribute);

    when(tmsTestPlanAttributeMapper.duplicateTestPlanAttribute(originalAttribute1, newTestPlan))
        .thenReturn(null);
    when(tmsTestPlanAttributeMapper.duplicateTestPlanAttribute(originalAttribute2, newTestPlan))
        .thenReturn(duplicatedAttribute);

    assertDoesNotThrow(() -> sut.duplicateTestPlanAttributes(originalTestPlan, newTestPlan));

    assertNotNull(newTestPlan.getAttributes());
    assertEquals(1, newTestPlan.getAttributes().size());
    assertTrue(newTestPlan.getAttributes().contains(duplicatedAttribute));

    verify(tmsTestPlanAttributeMapper, times(2)).duplicateTestPlanAttribute(any(), any());
    verify(itemAttributeRepository).saveAll(any(List.class));
    verify(tmsTestPlanAttributeRepository).saveAll(any(Set.class));
  }
}
