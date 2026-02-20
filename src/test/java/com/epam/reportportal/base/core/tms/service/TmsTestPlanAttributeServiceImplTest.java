package com.epam.reportportal.base.core.tms.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsAttribute;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestPlan;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestPlanAttribute;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestPlanAttributeId;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsTestPlanAttributeRepository;
import com.epam.reportportal.base.core.tms.dto.TmsTestPlanAttributeRQ;
import com.epam.reportportal.base.core.tms.mapper.TmsTestPlanAttributeMapper;
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
class TmsTestPlanAttributeServiceImplTest {

  private static final Long PROJECT_ID = 1L;

  @Mock
  private TmsTestPlanAttributeMapper tmsTestPlanAttributeMapper;

  @Mock
  private TmsTestPlanAttributeRepository tmsTestPlanAttributeRepository;

  @Mock
  private TmsAttributeService tmsAttributeService;

  @InjectMocks
  private TmsTestPlanAttributeServiceImpl sut;

  private TmsTestPlan tmsTestPlan;
  private TmsAttribute tmsAttribute1;
  private TmsAttribute tmsAttribute2;
  private TmsTestPlanAttribute testPlanAttribute1;
  private TmsTestPlanAttribute testPlanAttribute2;
  private TmsTestPlanAttributeRQ attributeRQ1;
  private TmsTestPlanAttributeRQ attributeRQ2;

  @BeforeEach
  void setUp() {
    // Setup TmsTestPlan
    tmsTestPlan = new TmsTestPlan();
    tmsTestPlan.setId(1L);
    tmsTestPlan.setAttributes(new HashSet<>());

    // Setup TmsAttributes
    tmsAttribute1 = new TmsAttribute();
    tmsAttribute1.setId(10L);
    tmsAttribute1.setKey("key1");

    tmsAttribute2 = new TmsAttribute();
    tmsAttribute2.setId(20L);
    tmsAttribute2.setKey("key2");

    // Setup TmsTestPlanAttributes
    testPlanAttribute1 = new TmsTestPlanAttribute();
    testPlanAttribute1.setId(new TmsTestPlanAttributeId(1L, 10L));
    testPlanAttribute1.setAttribute(tmsAttribute1);
    testPlanAttribute1.setTestPlan(tmsTestPlan);

    testPlanAttribute2 = new TmsTestPlanAttribute();
    testPlanAttribute2.setId(new TmsTestPlanAttributeId(1L, 20L));
    testPlanAttribute2.setAttribute(tmsAttribute2);
    testPlanAttribute2.setTestPlan(tmsTestPlan);

    // Setup TmsTestPlanAttributeRQs
    attributeRQ1 = new TmsTestPlanAttributeRQ();
    attributeRQ1.setId(10L);
    attributeRQ1.setKey("key1");

    attributeRQ2 = new TmsTestPlanAttributeRQ();
    attributeRQ2.setId(20L);
    attributeRQ2.setKey("key2");
  }

  @Test
  void shouldNotCreateTestPlanAttributesWhenEmptyAttributes() {
    // Given
    var testPlan = new TmsTestPlan();
    var attributes = Collections.<TmsTestPlanAttributeRQ>emptyList();

    // When
    assertDoesNotThrow(() -> sut.createTestPlanAttributes(PROJECT_ID, testPlan, attributes));

    // Then
    verifyNoInteractions(tmsAttributeService, tmsTestPlanAttributeMapper,
        tmsTestPlanAttributeRepository);
  }

  @Test
  void shouldNotCreateTestPlanAttributesWhenNullAttributes() {
    // Given
    var testPlan = new TmsTestPlan();

    // When
    assertDoesNotThrow(() -> sut.createTestPlanAttributes(PROJECT_ID, testPlan, null));

    // Then
    verifyNoInteractions(tmsAttributeService, tmsTestPlanAttributeMapper,
        tmsTestPlanAttributeRepository);
  }

  @Test
  void shouldCreateTestPlanAttributesWithId() {
    // Given
    var attributesRQ = List.of(attributeRQ1, attributeRQ2);

    when(tmsAttributeService.getEntityById(PROJECT_ID, 10L)).thenReturn(tmsAttribute1);
    when(tmsAttributeService.getEntityById(PROJECT_ID, 20L)).thenReturn(tmsAttribute2);
    when(tmsTestPlanAttributeMapper.createTestPlanAttribute(tmsTestPlan, tmsAttribute1))
        .thenReturn(testPlanAttribute1);
    when(tmsTestPlanAttributeMapper.createTestPlanAttribute(tmsTestPlan, tmsAttribute2))
        .thenReturn(testPlanAttribute2);

    // When
    assertDoesNotThrow(() -> sut.createTestPlanAttributes(PROJECT_ID, tmsTestPlan, attributesRQ));

    // Then
    verify(tmsAttributeService).getEntityById(PROJECT_ID, 10L);
    verify(tmsAttributeService).getEntityById(PROJECT_ID, 20L);
    verify(tmsTestPlanAttributeMapper).createTestPlanAttribute(tmsTestPlan, tmsAttribute1);
    verify(tmsTestPlanAttributeMapper).createTestPlanAttribute(tmsTestPlan, tmsAttribute2);
    verify(tmsTestPlanAttributeRepository).saveAll(any(Set.class));

    assertNotNull(tmsTestPlan.getAttributes());
    assertEquals(2, tmsTestPlan.getAttributes().size());
  }

  @Test
  void shouldCreateTestPlanAttributesWithKeyValue() {
    // Given
    var attributeRQ = new TmsTestPlanAttributeRQ();
    attributeRQ.setKey("newKey");
    attributeRQ.setValue("newValue");

    var attributesRQ = List.of(attributeRQ);
    var newAttribute = new TmsAttribute();
    newAttribute.setId(30L);
    newAttribute.setKey("newKey");

    var newTestPlanAttribute = new TmsTestPlanAttribute();
    newTestPlanAttribute.setAttribute(newAttribute);

    when(tmsAttributeService.findOrCreateAttribute(PROJECT_ID, "newKey", "newValue"))
        .thenReturn(newAttribute);
    when(tmsTestPlanAttributeMapper.createTestPlanAttribute(tmsTestPlan, newAttribute))
        .thenReturn(newTestPlanAttribute);

    // When
    assertDoesNotThrow(() -> sut.createTestPlanAttributes(PROJECT_ID, tmsTestPlan, attributesRQ));

    // Then
    verify(tmsAttributeService).findOrCreateAttribute(PROJECT_ID, "newKey", "newValue");
    verify(tmsTestPlanAttributeMapper).createTestPlanAttribute(tmsTestPlan, newAttribute);
    verify(tmsTestPlanAttributeRepository).saveAll(any(Set.class));

    assertNotNull(tmsTestPlan.getAttributes());
    assertEquals(1, tmsTestPlan.getAttributes().size());
  }

  @Test
  void shouldCreateTestPlanAttributesWithMixedIdAndKeyValue() {
    // Given
    var attributeRQWithId = new TmsTestPlanAttributeRQ();
    attributeRQWithId.setId(10L);

    var attributeRQWithKeyValue = new TmsTestPlanAttributeRQ();
    attributeRQWithKeyValue.setKey("newKey");
    attributeRQWithKeyValue.setValue("newValue");

    var attributesRQ = List.of(attributeRQWithId, attributeRQWithKeyValue);

    var newAttribute = new TmsAttribute();
    newAttribute.setId(30L);
    newAttribute.setKey("newKey");

    var newTestPlanAttribute = new TmsTestPlanAttribute();
    newTestPlanAttribute.setAttribute(newAttribute);

    when(tmsAttributeService.getEntityById(PROJECT_ID, 10L)).thenReturn(tmsAttribute1);
    when(tmsAttributeService.findOrCreateAttribute(PROJECT_ID, "newKey", "newValue"))
        .thenReturn(newAttribute);
    when(tmsTestPlanAttributeMapper.createTestPlanAttribute(tmsTestPlan, tmsAttribute1))
        .thenReturn(testPlanAttribute1);
    when(tmsTestPlanAttributeMapper.createTestPlanAttribute(tmsTestPlan, newAttribute))
        .thenReturn(newTestPlanAttribute);

    // When
    assertDoesNotThrow(() -> sut.createTestPlanAttributes(PROJECT_ID, tmsTestPlan, attributesRQ));

    // Then
    verify(tmsAttributeService).getEntityById(PROJECT_ID, 10L);
    verify(tmsAttributeService).findOrCreateAttribute(PROJECT_ID, "newKey", "newValue");
    verify(tmsTestPlanAttributeMapper).createTestPlanAttribute(tmsTestPlan, tmsAttribute1);
    verify(tmsTestPlanAttributeMapper).createTestPlanAttribute(tmsTestPlan, newAttribute);
    verify(tmsTestPlanAttributeRepository).saveAll(any(Set.class));

    assertEquals(2, tmsTestPlan.getAttributes().size());
  }

  @Test
  void shouldRemoveAllTestPlanAttributesWhenEmptyAttributes() {
    // Given
    var existingTestPlan = new TmsTestPlan();
    var attributes = Collections.<TmsTestPlanAttributeRQ>emptyList();

    // When
    assertDoesNotThrow(
        () -> sut.updateTestPlanAttributes(PROJECT_ID, existingTestPlan, attributes));

    // Then
    verify(tmsTestPlanAttributeRepository, never()).deleteAll(any());
    verifyNoInteractions(tmsAttributeService, tmsTestPlanAttributeMapper);
  }

  @Test
  void shouldRemoveAllTestPlanAttributesWhenNullAttributes() {
    // Given
    var existingTestPlan = new TmsTestPlan();
    existingTestPlan.setId(1L);

    // When
    assertDoesNotThrow(() -> sut.updateTestPlanAttributes(PROJECT_ID, existingTestPlan, null));

    // Then
    verify(tmsTestPlanAttributeRepository, never()).deleteAll(any());
    verifyNoInteractions(tmsAttributeService, tmsTestPlanAttributeMapper);
  }

  @Test
  void shouldUpdateTestPlanAttributes() {
    // Given
    var existingTestPlan = new TmsTestPlan();
    existingTestPlan.setId(1L);

    var existingAttribute = new TmsTestPlanAttribute();
    existingAttribute.setId(new TmsTestPlanAttributeId(1L, 5L));
    var existingAttributes = new HashSet<TmsTestPlanAttribute>();
    existingAttributes.add(existingAttribute);
    existingTestPlan.setAttributes(existingAttributes);

    var attributesRQ = List.of(attributeRQ1);

    when(tmsAttributeService.getEntityById(PROJECT_ID, 10L)).thenReturn(tmsAttribute1);
    when(tmsTestPlanAttributeMapper.createTestPlanAttribute(existingTestPlan, tmsAttribute1))
        .thenReturn(testPlanAttribute1);

    // When
    assertDoesNotThrow(
        () -> sut.updateTestPlanAttributes(PROJECT_ID, existingTestPlan, attributesRQ));

    // Then
    verify(tmsTestPlanAttributeRepository).deleteAll(existingAttributes);
    verify(tmsAttributeService).getEntityById(PROJECT_ID, 10L);
    verify(tmsTestPlanAttributeMapper).createTestPlanAttribute(existingTestPlan, tmsAttribute1);
    verify(tmsTestPlanAttributeRepository).saveAll(any(Set.class));

    assertEquals(1, existingTestPlan.getAttributes().size());
  }

  @Test
  void shouldUpdateTestPlanAttributesWhenNoExistingAttributes() {
    // Given
    var existingTestPlan = new TmsTestPlan();
    existingTestPlan.setId(1L);
    existingTestPlan.setAttributes(new HashSet<>());

    var attributesRQ = List.of(attributeRQ1, attributeRQ2);

    when(tmsAttributeService.getEntityById(PROJECT_ID, 10L)).thenReturn(tmsAttribute1);
    when(tmsAttributeService.getEntityById(PROJECT_ID, 20L)).thenReturn(tmsAttribute2);
    when(tmsTestPlanAttributeMapper.createTestPlanAttribute(existingTestPlan, tmsAttribute1))
        .thenReturn(testPlanAttribute1);
    when(tmsTestPlanAttributeMapper.createTestPlanAttribute(existingTestPlan, tmsAttribute2))
        .thenReturn(testPlanAttribute2);

    // When
    assertDoesNotThrow(
        () -> sut.updateTestPlanAttributes(PROJECT_ID, existingTestPlan, attributesRQ));

    // Then
    verify(tmsTestPlanAttributeRepository, never()).deleteAll(any());
    verify(tmsAttributeService).getEntityById(PROJECT_ID, 10L);
    verify(tmsAttributeService).getEntityById(PROJECT_ID, 20L);
    verify(tmsTestPlanAttributeRepository).saveAll(any(Set.class));

    assertEquals(2, existingTestPlan.getAttributes().size());
  }

  @Test
  void shouldDeleteAllByTestPlanId() {
    // Given
    var testPlanId = 123L;

    // When
    assertDoesNotThrow(() -> sut.deleteAllByTestPlanId(testPlanId));

    // Then
    verify(tmsTestPlanAttributeRepository).deleteAllByTestPlanId(testPlanId);
  }

  @Test
  void shouldNotDuplicateTestPlanAttributesWhenEmptyAttributes() {
    // Given
    var originalTestPlan = new TmsTestPlan();
    originalTestPlan.setAttributes(Collections.emptySet());
    var newTestPlan = new TmsTestPlan();

    // When
    assertDoesNotThrow(() -> sut.duplicateTestPlanAttributes(originalTestPlan, newTestPlan));

    // Then
    verifyNoInteractions(tmsTestPlanAttributeMapper, tmsTestPlanAttributeRepository);
  }

  @Test
  void shouldNotDuplicateTestPlanAttributesWhenNullAttributes() {
    // Given
    var originalTestPlan = new TmsTestPlan();
    originalTestPlan.setAttributes(null);
    var newTestPlan = new TmsTestPlan();

    // When
    assertDoesNotThrow(() -> sut.duplicateTestPlanAttributes(originalTestPlan, newTestPlan));

    // Then
    verifyNoInteractions(tmsTestPlanAttributeMapper, tmsTestPlanAttributeRepository);
  }

  @Test
  void shouldDuplicateTestPlanAttributesSuccessfully() {
    // Given
    var originalTestPlan = new TmsTestPlan();
    originalTestPlan.setId(1L);

    var attribute1 = new TmsAttribute();
    attribute1.setId(10L);
    var attribute2 = new TmsAttribute();
    attribute2.setId(20L);

    var originalAttribute1 = new TmsTestPlanAttribute();
    originalAttribute1.setId(new TmsTestPlanAttributeId(1L, 10L));
    originalAttribute1.setTestPlan(originalTestPlan);
    originalAttribute1.setAttribute(attribute1);

    var originalAttribute2 = new TmsTestPlanAttribute();
    originalAttribute2.setId(new TmsTestPlanAttributeId(1L, 20L));
    originalAttribute2.setTestPlan(originalTestPlan);
    originalAttribute2.setAttribute(attribute2);

    var originalAttributes = new HashSet<TmsTestPlanAttribute>();
    originalAttributes.add(originalAttribute1);
    originalAttributes.add(originalAttribute2);
    originalTestPlan.setAttributes(originalAttributes);

    var newTestPlan = new TmsTestPlan();
    newTestPlan.setId(2L);

    var duplicatedAttribute1 = new TmsTestPlanAttribute();
    duplicatedAttribute1.setId(new TmsTestPlanAttributeId(2L, 10L));
    duplicatedAttribute1.setAttribute(attribute1);

    var duplicatedAttribute2 = new TmsTestPlanAttribute();
    duplicatedAttribute2.setId(new TmsTestPlanAttributeId(2L, 20L));
    duplicatedAttribute2.setAttribute(attribute2);

    when(tmsTestPlanAttributeMapper.duplicateTestPlanAttribute(originalAttribute1, newTestPlan))
        .thenReturn(duplicatedAttribute1);
    when(tmsTestPlanAttributeMapper.duplicateTestPlanAttribute(originalAttribute2, newTestPlan))
        .thenReturn(duplicatedAttribute2);

    // When
    assertDoesNotThrow(() -> sut.duplicateTestPlanAttributes(originalTestPlan, newTestPlan));

    // Then
    assertNotNull(newTestPlan.getAttributes());
    assertEquals(2, newTestPlan.getAttributes().size());
    assertTrue(newTestPlan.getAttributes().contains(duplicatedAttribute1));
    assertTrue(newTestPlan.getAttributes().contains(duplicatedAttribute2));

    // Verify all attributes have correct test plan reference
    newTestPlan.getAttributes().forEach(attr -> assertEquals(newTestPlan, attr.getTestPlan()));

    verify(tmsTestPlanAttributeMapper).duplicateTestPlanAttribute(originalAttribute1, newTestPlan);
    verify(tmsTestPlanAttributeMapper).duplicateTestPlanAttribute(originalAttribute2, newTestPlan);
    verify(tmsTestPlanAttributeRepository).saveAll(any(Set.class));
  }

  @Test
  void shouldDuplicateTestPlanAttributesWithSingleAttribute() {
    // Given
    var originalTestPlan = new TmsTestPlan();
    originalTestPlan.setId(1L);

    var attribute = new TmsAttribute();
    attribute.setId(10L);

    var originalAttribute = new TmsTestPlanAttribute();
    originalAttribute.setId(new TmsTestPlanAttributeId(1L, 10L));
    originalAttribute.setTestPlan(originalTestPlan);
    originalAttribute.setAttribute(attribute);

    var originalAttributes = new HashSet<TmsTestPlanAttribute>();
    originalAttributes.add(originalAttribute);
    originalTestPlan.setAttributes(originalAttributes);

    var newTestPlan = new TmsTestPlan();
    newTestPlan.setId(2L);

    var duplicatedAttribute = new TmsTestPlanAttribute();
    duplicatedAttribute.setId(new TmsTestPlanAttributeId(2L, 10L));
    duplicatedAttribute.setAttribute(attribute);

    when(tmsTestPlanAttributeMapper.duplicateTestPlanAttribute(originalAttribute, newTestPlan))
        .thenReturn(duplicatedAttribute);

    // When
    assertDoesNotThrow(() -> sut.duplicateTestPlanAttributes(originalTestPlan, newTestPlan));

    // Then
    assertNotNull(newTestPlan.getAttributes());
    assertEquals(1, newTestPlan.getAttributes().size());
    assertTrue(newTestPlan.getAttributes().contains(duplicatedAttribute));
    assertEquals(newTestPlan, duplicatedAttribute.getTestPlan());

    verify(tmsTestPlanAttributeMapper).duplicateTestPlanAttribute(originalAttribute, newTestPlan);
    verify(tmsTestPlanAttributeRepository).saveAll(any(Set.class));
  }

  @Test
  void shouldDuplicateTestPlanAttributesAndSetCorrectReferences() {
    // Given
    var originalTestPlan = new TmsTestPlan();
    originalTestPlan.setId(1L);

    var attribute = new TmsAttribute();
    attribute.setId(10L);

    var originalAttribute = new TmsTestPlanAttribute();
    originalAttribute.setId(new TmsTestPlanAttributeId(1L, 10L));
    originalAttribute.setTestPlan(originalTestPlan);
    originalAttribute.setAttribute(attribute);

    var originalAttributes = new HashSet<TmsTestPlanAttribute>();
    originalAttributes.add(originalAttribute);
    originalTestPlan.setAttributes(originalAttributes);

    var newTestPlan = new TmsTestPlan();
    newTestPlan.setId(2L);

    var duplicatedAttribute = new TmsTestPlanAttribute();
    duplicatedAttribute.setId(new TmsTestPlanAttributeId(2L, 10L));
    duplicatedAttribute.setAttribute(attribute);
    // Initially, the duplicated attribute might not have the test plan set

    when(tmsTestPlanAttributeMapper.duplicateTestPlanAttribute(originalAttribute, newTestPlan))
        .thenReturn(duplicatedAttribute);

    // When
    assertDoesNotThrow(() -> sut.duplicateTestPlanAttributes(originalTestPlan, newTestPlan));

    // Then - verify that test plan reference is set correctly
    assertEquals(newTestPlan, duplicatedAttribute.getTestPlan());
    assertEquals(1, newTestPlan.getAttributes().size());

    verify(tmsTestPlanAttributeMapper).duplicateTestPlanAttribute(eq(originalAttribute),
        eq(newTestPlan));
    verify(tmsTestPlanAttributeRepository).saveAll(any(Set.class));
  }

  @Test
  void shouldCreateTestPlanAttributesWithSingleAttribute() {
    // Given
    var attributesRQ = List.of(attributeRQ1);

    when(tmsAttributeService.getEntityById(PROJECT_ID, 10L)).thenReturn(tmsAttribute1);
    when(tmsTestPlanAttributeMapper.createTestPlanAttribute(tmsTestPlan, tmsAttribute1))
        .thenReturn(testPlanAttribute1);

    // When
    assertDoesNotThrow(() -> sut.createTestPlanAttributes(PROJECT_ID, tmsTestPlan, attributesRQ));

    // Then
    verify(tmsAttributeService).getEntityById(PROJECT_ID, 10L);
    verify(tmsTestPlanAttributeMapper).createTestPlanAttribute(tmsTestPlan, tmsAttribute1);
    verify(tmsTestPlanAttributeRepository).saveAll(any(Set.class));

    assertEquals(1, tmsTestPlan.getAttributes().size());
  }
}
