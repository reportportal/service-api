package com.epam.reportportal.core.tms.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.epam.reportportal.infrastructure.persistence.entity.tms.TmsAttribute;
import com.epam.reportportal.infrastructure.persistence.entity.tms.TmsTestPlan;
import com.epam.reportportal.infrastructure.persistence.entity.tms.TmsTestPlanAttribute;
import com.epam.reportportal.infrastructure.persistence.entity.tms.TmsTestPlanAttributeId;
import com.epam.reportportal.infrastructure.persistence.dao.tms.TmsTestPlanAttributeRepository;
import com.epam.reportportal.core.tms.dto.TmsTestPlanAttributeRQ;
import com.epam.reportportal.core.tms.mapper.TmsTestPlanAttributeMapper;
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

  @InjectMocks
  private TmsTestPlanAttributeServiceImpl sut;

  @Test
  void shouldNotCreateTestPlanAttributesWhenEmptyAttributes() {
    var tmsTestPlan = new TmsTestPlan();
    List<TmsTestPlanAttributeRQ> attributes = Collections.emptyList();

    assertDoesNotThrow(() -> sut.createTestPlanAttributes(tmsTestPlan, attributes));

    verifyNoInteractions(tmsTestPlanAttributeMapper, tmsTestPlanAttributeRepository);
  }

  @Test
  void shouldCreateTestPlanAttributes() {
    var tmsTestPlan = new TmsTestPlan();
    var attributesRQ = List.of(new TmsTestPlanAttributeRQ(), new TmsTestPlanAttributeRQ());
    var attributes = new HashSet<TmsTestPlanAttribute>();
    attributes.add(new TmsTestPlanAttribute());
    attributes.add(new TmsTestPlanAttribute());

    when(tmsTestPlanAttributeMapper.convertToTmsTestPlanAttributes(attributesRQ)).thenReturn(
        attributes);

    assertDoesNotThrow(() -> sut.createTestPlanAttributes(tmsTestPlan, attributesRQ));

    assertEquals(attributes, tmsTestPlan.getAttributes());
    attributes.forEach(attribute -> assertEquals(tmsTestPlan, attribute.getTestPlan()));

    verify(tmsTestPlanAttributeMapper).convertToTmsTestPlanAttributes(attributesRQ);
    verify(tmsTestPlanAttributeRepository).saveAll(attributes);
  }

  @Test
  void shouldRemoveAllTestPlanAttributesWhenEmptyAttributes() {
    var existingTestPlan = new TmsTestPlan();
    List<TmsTestPlanAttributeRQ> attributes = Collections.emptyList();

    assertDoesNotThrow(() -> sut.updateTestPlanAttributes(existingTestPlan, attributes));

    verify(tmsTestPlanAttributeRepository, never()).deleteAll(any());
    verifyNoInteractions(tmsTestPlanAttributeMapper);
  }

  @Test
  void shouldRemoveAllTestPlanAttributesWhenNullAttributes() {
    var existingTestPlan = new TmsTestPlan();
    existingTestPlan.setId(1L);

    assertDoesNotThrow(() -> sut.updateTestPlanAttributes(existingTestPlan, null));

    verify(tmsTestPlanAttributeRepository, never()).deleteAll(any());
    verifyNoInteractions(tmsTestPlanAttributeMapper);
  }

  @Test
  void shouldTestUpdateTestPlanAttributes() {
    var existingTestPlan = new TmsTestPlan();
    var attributesRQ = List.of(new TmsTestPlanAttributeRQ(), new TmsTestPlanAttributeRQ());
    var attributes = new HashSet<TmsTestPlanAttribute>();
    attributes.add(new TmsTestPlanAttribute());
    attributes.add(new TmsTestPlanAttribute());
    existingTestPlan.setAttributes(attributes);

    when(tmsTestPlanAttributeMapper.convertToTmsTestPlanAttributes(attributesRQ)).thenReturn(
        attributes);

    assertDoesNotThrow(() -> sut.updateTestPlanAttributes(existingTestPlan, attributesRQ));

    verify(tmsTestPlanAttributeRepository).deleteAll(existingTestPlan.getAttributes());
    assertEquals(attributes, existingTestPlan.getAttributes());
    attributes.forEach(attribute -> assertEquals(existingTestPlan, attribute.getTestPlan()));

    verify(tmsTestPlanAttributeMapper).convertToTmsTestPlanAttributes(attributesRQ);
    verify(tmsTestPlanAttributeRepository).saveAll(attributes);
  }

  @Test
  void shouldNotPatchTestPlanAttributesWhenEmptyAttributes() {
    var existingTestPlan = new TmsTestPlan();
    var existingAttributes = Set.of(new TmsTestPlanAttribute());
    existingTestPlan.setAttributes(existingAttributes);

    List<TmsTestPlanAttributeRQ> attributes = Collections.emptyList();

    assertDoesNotThrow(() -> sut.patchTestPlanAttributes(existingTestPlan, attributes));

    // Ensure no modifications are made
    assertEquals(existingAttributes, existingTestPlan.getAttributes());
    verifyNoInteractions(tmsTestPlanAttributeMapper, tmsTestPlanAttributeRepository);
  }

  @Test
  void shouldNotPatchTestPlanAttributesWhenNullAttributes() {
    var existingTestPlan = new TmsTestPlan();
    var existingAttributes = Set.of(new TmsTestPlanAttribute());
    existingTestPlan.setAttributes(existingAttributes);

    assertDoesNotThrow(() -> sut.patchTestPlanAttributes(existingTestPlan, null));

    // Ensure no modifications are made
    assertEquals(existingAttributes, existingTestPlan.getAttributes());
    verifyNoInteractions(tmsTestPlanAttributeMapper, tmsTestPlanAttributeRepository);
  }

  @Test
  void shouldPatchTestPlanAttributes() {
    var existingTestPlan = new TmsTestPlan();
    Set<TmsTestPlanAttribute> existingAttributes = new HashSet<>();
    existingAttributes.add(new TmsTestPlanAttribute());
    existingTestPlan.setAttributes(existingAttributes);

    var attributesRQ = List.of(new TmsTestPlanAttributeRQ(), new TmsTestPlanAttributeRQ());
    var newAttributes = new HashSet<TmsTestPlanAttribute>();
    newAttributes.add(new TmsTestPlanAttribute());
    newAttributes.add(new TmsTestPlanAttribute());

    when(tmsTestPlanAttributeMapper.convertToTmsTestPlanAttributes(attributesRQ)).thenReturn(
        newAttributes);

    assertDoesNotThrow(() -> sut.patchTestPlanAttributes(existingTestPlan, attributesRQ));

    assertTrue(existingTestPlan.getAttributes().containsAll(newAttributes));
    newAttributes.forEach(attribute -> assertEquals(existingTestPlan, attribute.getTestPlan()));

    verify(tmsTestPlanAttributeMapper).convertToTmsTestPlanAttributes(attributesRQ);
    verify(tmsTestPlanAttributeRepository).saveAll(newAttributes);
  }

  @Test
  void shouldDeleteAllByTestPlanId() {
    var testPlanId = 123L;

    assertDoesNotThrow(() -> sut.deleteAllByTestPlanId(testPlanId));

    verify(tmsTestPlanAttributeRepository).deleteAllByTestPlanId(testPlanId);
  }

  @Test
  void shouldNotDuplicateTestPlanAttributesWhenEmptyAttributes() {
    // Arrange
    var originalTestPlan = new TmsTestPlan();
    originalTestPlan.setAttributes(Collections.emptySet());
    var newTestPlan = new TmsTestPlan();

    // Act
    assertDoesNotThrow(() -> sut.duplicateTestPlanAttributes(originalTestPlan, newTestPlan));

    // Assert - no interactions with mapper or repository
    verifyNoInteractions(tmsTestPlanAttributeMapper, tmsTestPlanAttributeRepository);
  }

  @Test
  void shouldNotDuplicateTestPlanAttributesWhenNullAttributes() {
    // Arrange
    var originalTestPlan = new TmsTestPlan();
    originalTestPlan.setAttributes(null);
    var newTestPlan = new TmsTestPlan();

    // Act
    assertDoesNotThrow(() -> sut.duplicateTestPlanAttributes(originalTestPlan, newTestPlan));

    // Assert - no interactions with mapper or repository
    verifyNoInteractions(tmsTestPlanAttributeMapper, tmsTestPlanAttributeRepository);
  }

  @Test
  void shouldDuplicateTestPlanAttributesSuccessfully() {
    // Arrange
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
    originalAttribute1.setValue("value1");

    var originalAttribute2 = new TmsTestPlanAttribute();
    originalAttribute2.setId(new TmsTestPlanAttributeId(1L, 20L));
    originalAttribute2.setTestPlan(originalTestPlan);
    originalAttribute2.setAttribute(attribute2);
    originalAttribute2.setValue("value2");

    Set<TmsTestPlanAttribute> originalAttributes = new HashSet<>();
    originalAttributes.add(originalAttribute1);
    originalAttributes.add(originalAttribute2);
    originalTestPlan.setAttributes(originalAttributes);

    var newTestPlan = new TmsTestPlan();
    newTestPlan.setId(2L);

    var duplicatedAttribute1 = new TmsTestPlanAttribute();
    duplicatedAttribute1.setId(new TmsTestPlanAttributeId(2L, 10L));
    duplicatedAttribute1.setAttribute(attribute1);
    duplicatedAttribute1.setValue("value1");

    var duplicatedAttribute2 = new TmsTestPlanAttribute();
    duplicatedAttribute2.setId(new TmsTestPlanAttributeId(2L, 20L));
    duplicatedAttribute2.setAttribute(attribute2);
    duplicatedAttribute2.setValue("value2");

    when(tmsTestPlanAttributeMapper.duplicateTestPlanAttribute(originalAttribute1, newTestPlan))
        .thenReturn(duplicatedAttribute1);
    when(tmsTestPlanAttributeMapper.duplicateTestPlanAttribute(originalAttribute2, newTestPlan))
        .thenReturn(duplicatedAttribute2);

    // Act
    assertDoesNotThrow(() -> sut.duplicateTestPlanAttributes(originalTestPlan, newTestPlan));

    // Assert
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
    // Arrange
    var originalTestPlan = new TmsTestPlan();
    originalTestPlan.setId(1L);

    var attribute = new TmsAttribute();
    attribute.setId(10L);

    var originalAttribute = new TmsTestPlanAttribute();
    originalAttribute.setId(new TmsTestPlanAttributeId(1L, 10L));
    originalAttribute.setTestPlan(originalTestPlan);
    originalAttribute.setAttribute(attribute);
    originalAttribute.setValue("test-value");

    Set<TmsTestPlanAttribute> originalAttributes = new HashSet<>();
    originalAttributes.add(originalAttribute);
    originalTestPlan.setAttributes(originalAttributes);

    var newTestPlan = new TmsTestPlan();
    newTestPlan.setId(2L);

    var duplicatedAttribute = new TmsTestPlanAttribute();
    duplicatedAttribute.setId(new TmsTestPlanAttributeId(2L, 10L));
    duplicatedAttribute.setAttribute(attribute);
    duplicatedAttribute.setValue("test-value");

    when(tmsTestPlanAttributeMapper.duplicateTestPlanAttribute(originalAttribute, newTestPlan))
        .thenReturn(duplicatedAttribute);

    // Act
    assertDoesNotThrow(() -> sut.duplicateTestPlanAttributes(originalTestPlan, newTestPlan));

    // Assert
    assertNotNull(newTestPlan.getAttributes());
    assertEquals(1, newTestPlan.getAttributes().size());
    assertTrue(newTestPlan.getAttributes().contains(duplicatedAttribute));
    assertEquals(newTestPlan, duplicatedAttribute.getTestPlan());

    verify(tmsTestPlanAttributeMapper).duplicateTestPlanAttribute(originalAttribute, newTestPlan);
    verify(tmsTestPlanAttributeRepository).saveAll(any(Set.class));
  }

  @Test
  void shouldDuplicateTestPlanAttributesAndSetCorrectReferences() {
    // Arrange
    var originalTestPlan = new TmsTestPlan();
    originalTestPlan.setId(1L);

    var attribute = new TmsAttribute();
    attribute.setId(10L);

    var originalAttribute = new TmsTestPlanAttribute();
    originalAttribute.setId(new TmsTestPlanAttributeId(1L, 10L));
    originalAttribute.setTestPlan(originalTestPlan);
    originalAttribute.setAttribute(attribute);

    Set<TmsTestPlanAttribute> originalAttributes = new HashSet<>();
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

    // Act
    assertDoesNotThrow(() -> sut.duplicateTestPlanAttributes(originalTestPlan, newTestPlan));

    // Assert - verify that test plan reference is set correctly
    assertEquals(newTestPlan, duplicatedAttribute.getTestPlan());
    assertEquals(1, newTestPlan.getAttributes().size());

    verify(tmsTestPlanAttributeMapper).duplicateTestPlanAttribute(eq(originalAttribute), eq(newTestPlan));
    verify(tmsTestPlanAttributeRepository).saveAll(any(Set.class));
  }
}
