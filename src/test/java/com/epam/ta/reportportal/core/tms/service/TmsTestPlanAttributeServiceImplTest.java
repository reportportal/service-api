package com.epam.ta.reportportal.core.tms.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.epam.ta.reportportal.core.tms.db.entity.TmsTestPlan;
import com.epam.ta.reportportal.core.tms.db.entity.TmsTestPlanAttribute;
import com.epam.ta.reportportal.core.tms.db.repository.TmsTestPlanAttributeRepository;
import com.epam.ta.reportportal.core.tms.dto.TmsTestPlanAttributeRQ;
import com.epam.ta.reportportal.core.tms.mapper.TmsTestPlanAttributeMapper;
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

    // Assert
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

    // Act
    assertDoesNotThrow(() -> sut.createTestPlanAttributes(tmsTestPlan, attributesRQ));

    // Assert
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

    verify(tmsTestPlanAttributeRepository).deleteAllById_TestPlanId(existingTestPlan.getId());
    verifyNoInteractions(tmsTestPlanAttributeMapper);
  }

  @Test
  void shouldTestUpdateTestPlanAttributes() {
    var existingTestPlan = new TmsTestPlan();
    var attributesRQ = List.of(new TmsTestPlanAttributeRQ(), new TmsTestPlanAttributeRQ());
    var attributes = new HashSet<TmsTestPlanAttribute>();
    attributes.add(new TmsTestPlanAttribute());
    attributes.add(new TmsTestPlanAttribute());

    when(tmsTestPlanAttributeMapper.convertToTmsTestPlanAttributes(attributesRQ)).thenReturn(
        attributes);

    assertDoesNotThrow(() -> sut.updateTestPlanAttributes(existingTestPlan, attributesRQ));

    verify(tmsTestPlanAttributeRepository).deleteAllById_TestPlanId(existingTestPlan.getId());
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
}
