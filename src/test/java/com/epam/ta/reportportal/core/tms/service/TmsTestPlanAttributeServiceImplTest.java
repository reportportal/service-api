package com.epam.ta.reportportal.core.tms.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.epam.ta.reportportal.core.tms.db.entity.TmsAttribute;
import com.epam.ta.reportportal.core.tms.db.entity.TmsTestPlan;
import com.epam.ta.reportportal.core.tms.db.entity.TmsTestPlanAttribute;
import com.epam.ta.reportportal.core.tms.db.repository.TmsTestPlanAttributeRepository;
import com.epam.ta.reportportal.core.tms.dto.TmsAttributeRQ;
import com.epam.ta.reportportal.core.tms.mapper.TmsTestPlanAttributeMapper;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
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
  private TmsAttributeService tmsAttributeService;

  @InjectMocks
  private TmsTestPlanAttributeServiceImpl sut;

  private Map<String, TmsAttribute> tmsAttributesMap;
  private List<TmsAttributeRQ> attributesRQ;
  private Set<TmsTestPlanAttribute> testPlanAttributes;

  @BeforeEach
  void setUp() {
    tmsAttributesMap = createTmsAttributesMap();
    attributesRQ = createAttributeRQs();
    testPlanAttributes = createTestPlanAttributes();
  }

  @Test
  void shouldNotCreateTestPlanAttributesWhenEmptyAttributes() {
    var tmsTestPlan = new TmsTestPlan();
    List<TmsAttributeRQ> attributes = Collections.emptyList();

    assertDoesNotThrow(() -> sut.createTestPlanAttributes(tmsTestPlan, attributes));

    // Assert
    verifyNoInteractions(tmsAttributeService, tmsTestPlanAttributeMapper, tmsTestPlanAttributeRepository);
  }

  @Test
  void shouldNotCreateTestPlanAttributesWhenNullAttributes() {
    var tmsTestPlan = new TmsTestPlan();

    assertDoesNotThrow(() -> sut.createTestPlanAttributes(tmsTestPlan, null));

    // Assert
    verifyNoInteractions(tmsAttributeService, tmsTestPlanAttributeMapper, tmsTestPlanAttributeRepository);
  }

  @Test
  void shouldCreateTestPlanAttributes() {
    var tmsTestPlan = new TmsTestPlan();

    when(tmsAttributeService.getTmsAttributes(attributesRQ)).thenReturn(tmsAttributesMap);
    when(tmsTestPlanAttributeMapper.convertToTmsTestPlanAttributes(tmsAttributesMap, attributesRQ))
        .thenReturn(testPlanAttributes);

    // Act
    assertDoesNotThrow(() -> sut.createTestPlanAttributes(tmsTestPlan, attributesRQ));

    // Assert
    assertEquals(testPlanAttributes, tmsTestPlan.getAttributes());
    testPlanAttributes.forEach(attribute -> assertEquals(tmsTestPlan, attribute.getTestPlan()));

    verify(tmsAttributeService).getTmsAttributes(attributesRQ);
    verify(tmsTestPlanAttributeMapper).convertToTmsTestPlanAttributes(tmsAttributesMap, attributesRQ);
    verify(tmsTestPlanAttributeRepository).saveAll(testPlanAttributes);
  }

  @Test
  void shouldRemoveAllTestPlanAttributesWhenEmptyAttributes() {
    var existingTestPlan = new TmsTestPlan();
    existingTestPlan.setId(1L);
    List<TmsAttributeRQ> attributes = Collections.emptyList();

    assertDoesNotThrow(() -> sut.updateTestPlanAttributes(existingTestPlan, attributes));

    verify(tmsTestPlanAttributeRepository).deleteAllById_TestPlanId(existingTestPlan.getId());
    verifyNoInteractions(tmsAttributeService, tmsTestPlanAttributeMapper);
  }

  @Test
  void shouldRemoveAllTestPlanAttributesWhenNullAttributes() {
    var existingTestPlan = new TmsTestPlan();
    existingTestPlan.setId(1L);

    assertDoesNotThrow(() -> sut.updateTestPlanAttributes(existingTestPlan, null));

    verify(tmsTestPlanAttributeRepository).deleteAllById_TestPlanId(existingTestPlan.getId());
    verifyNoInteractions(tmsAttributeService, tmsTestPlanAttributeMapper);
  }

  @Test
  void shouldTestUpdateTestPlanAttributes() {
    var existingTestPlan = new TmsTestPlan();
    existingTestPlan.setId(1L);

    when(tmsAttributeService.getTmsAttributes(attributesRQ)).thenReturn(tmsAttributesMap);
    when(tmsTestPlanAttributeMapper.convertToTmsTestPlanAttributes(tmsAttributesMap, attributesRQ))
        .thenReturn(testPlanAttributes);

    assertDoesNotThrow(() -> sut.updateTestPlanAttributes(existingTestPlan, attributesRQ));

    verify(tmsTestPlanAttributeRepository).deleteAllById_TestPlanId(existingTestPlan.getId());
    assertEquals(testPlanAttributes, existingTestPlan.getAttributes());
    testPlanAttributes.forEach(attribute -> assertEquals(existingTestPlan, attribute.getTestPlan()));

    verify(tmsAttributeService).getTmsAttributes(attributesRQ);
    verify(tmsTestPlanAttributeMapper).convertToTmsTestPlanAttributes(tmsAttributesMap, attributesRQ);
    verify(tmsTestPlanAttributeRepository).saveAll(testPlanAttributes);
  }

  @Test
  void shouldNotPatchTestPlanAttributesWhenEmptyAttributes() {
    var existingTestPlan = new TmsTestPlan();
    var existingAttributes = Set.of(new TmsTestPlanAttribute());
    existingTestPlan.setAttributes(existingAttributes);

    List<TmsAttributeRQ> attributes = Collections.emptyList();

    assertDoesNotThrow(() -> sut.patchTestPlanAttributes(existingTestPlan, attributes));

    // Ensure no modifications are made
    assertEquals(existingAttributes, existingTestPlan.getAttributes());
    verifyNoInteractions(tmsAttributeService, tmsTestPlanAttributeMapper, tmsTestPlanAttributeRepository);
  }

  @Test
  void shouldNotPatchTestPlanAttributesWhenNullAttributes() {
    var existingTestPlan = new TmsTestPlan();
    var existingAttributes = Set.of(new TmsTestPlanAttribute());
    existingTestPlan.setAttributes(existingAttributes);

    assertDoesNotThrow(() -> sut.patchTestPlanAttributes(existingTestPlan, null));

    // Ensure no modifications are made
    assertEquals(existingAttributes, existingTestPlan.getAttributes());
    verifyNoInteractions(tmsAttributeService, tmsTestPlanAttributeMapper, tmsTestPlanAttributeRepository);
  }

  @Test
  void shouldPatchTestPlanAttributes() {
    var existingTestPlan = new TmsTestPlan();
    Set<TmsTestPlanAttribute> existingAttributes = new HashSet<>();
    existingAttributes.add(new TmsTestPlanAttribute());
    existingTestPlan.setAttributes(existingAttributes);

    when(tmsAttributeService.getTmsAttributes(attributesRQ)).thenReturn(tmsAttributesMap);
    when(tmsTestPlanAttributeMapper.convertToTmsTestPlanAttributes(tmsAttributesMap, attributesRQ))
        .thenReturn(testPlanAttributes);

    assertDoesNotThrow(() -> sut.patchTestPlanAttributes(existingTestPlan, attributesRQ));

    assertTrue(existingTestPlan.getAttributes().containsAll(testPlanAttributes));
    testPlanAttributes.forEach(attribute -> assertEquals(existingTestPlan, attribute.getTestPlan()));

    verify(tmsAttributeService).getTmsAttributes(attributesRQ);
    verify(tmsTestPlanAttributeMapper).convertToTmsTestPlanAttributes(tmsAttributesMap, attributesRQ);
    verify(tmsTestPlanAttributeRepository).saveAll(testPlanAttributes);
  }

  @Test
  void shouldDeleteAllByTestPlanId() {
    var testPlanId = 123L;

    assertDoesNotThrow(() -> sut.deleteAllByTestPlanId(testPlanId));

    verify(tmsTestPlanAttributeRepository).deleteAllById_TestPlanId(testPlanId);
  }

  // Helper methods
  private Map<String, TmsAttribute> createTmsAttributesMap() {
    var tmsAttr1 = new TmsAttribute();
    tmsAttr1.setId(1L);
    tmsAttr1.setKey("key1");

    var tmsAttr2 = new TmsAttribute();
    tmsAttr2.setId(2L);
    tmsAttr2.setKey("key2");

    return Map.of("id:1", tmsAttr1, "id:2", tmsAttr2);
  }

  private List<TmsAttributeRQ> createAttributeRQs() {
    var attr1 = new TmsAttributeRQ();
    attr1.setId(1L);
    attr1.setValue("Value 1");

    var attr2 = new TmsAttributeRQ();
    attr2.setId(2L);
    attr2.setValue("Value 2");

    return List.of(attr1, attr2);
  }

  private Set<TmsTestPlanAttribute> createTestPlanAttributes() {
    var attr1 = new TmsTestPlanAttribute();
    var attr2 = new TmsTestPlanAttribute();
    return new HashSet<>(List.of(attr1, attr2));
  }
}
