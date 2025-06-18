package com.epam.ta.reportportal.core.tms.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.epam.ta.reportportal.core.tms.db.entity.TmsAttribute;
import com.epam.ta.reportportal.core.tms.db.entity.TmsTestCase;
import com.epam.ta.reportportal.core.tms.db.entity.TmsTestCaseAttribute;
import com.epam.ta.reportportal.core.tms.db.entity.TmsTestCaseAttributeId;
import com.epam.ta.reportportal.core.tms.db.repository.TmsTestCaseAttributeRepository;
import com.epam.ta.reportportal.core.tms.dto.TmsTestCaseAttributeRQ;
import com.epam.ta.reportportal.core.tms.mapper.TmsTestCaseAttributeMapper;
import java.util.ArrayList;
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
class TmsTestCaseAttributeServiceImplTest {

  @Mock
  private TmsTestCaseAttributeMapper tmsTestCaseAttributeMapper;

  @Mock
  private TmsTestCaseAttributeRepository tmsTestCaseAttributeRepository;

  @InjectMocks
  private TmsTestCaseAttributeServiceImpl sut;

  private TmsTestCase testCase;
  private List<TmsTestCaseAttributeRQ> attributeRQs;
  private Set<TmsTestCaseAttribute> testCaseAttributes;
  private Set<TmsTestCaseAttribute> existingAttributes;

  @BeforeEach
  void setUp() {
    // Setup test case
    testCase = new TmsTestCase();
    testCase.setId(1L);
    testCase.setName("Test Case");
    testCase.setDescription("Description");

    // Setup existing attributes
    existingAttributes = new HashSet<>();
    TmsTestCaseAttribute existingAttribute = new TmsTestCaseAttribute();
    TmsTestCaseAttributeId existingAttributeId = new TmsTestCaseAttributeId(1L, 3L);
    existingAttribute.setId(existingAttributeId);

    TmsAttribute existingTmsAttribute = new TmsAttribute();
    existingTmsAttribute.setId(3L);
    existingAttribute.setAttribute(existingTmsAttribute);

    existingAttribute.setValue("existing-value");
    existingAttributes.add(existingAttribute);

    testCase.setTags(new HashSet<>(existingAttributes));

    // Setup attribute requests
    attributeRQs = new ArrayList<>();
    TmsTestCaseAttributeRQ attributeRQ = new TmsTestCaseAttributeRQ();
    attributeRQ.setValue("new-value");
    attributeRQ.setAttributeId(2L);
    attributeRQs.add(attributeRQ);

    // Setup test case attributes
    testCaseAttributes = new HashSet<>();
    TmsTestCaseAttribute attribute = new TmsTestCaseAttribute();

    // Create and set the composite ID
    TmsTestCaseAttributeId attributeId = new TmsTestCaseAttributeId(1L, 2L);
    attribute.setId(attributeId);

    // Create and set the attribute
    TmsAttribute tmsAttribute = new TmsAttribute();
    tmsAttribute.setId(2L);
    attribute.setAttribute(tmsAttribute);

    attribute.setValue("new-value");
    testCaseAttributes.add(attribute);
  }

  @Test
  void createTestCaseAttributes_ShouldCreateAndSaveAttributes() {
    // Given
    when(tmsTestCaseAttributeMapper.convertToTmsTestCaseAttributes(attributeRQs)).thenReturn(
        testCaseAttributes);

    // When
    sut.createTestCaseAttributes(testCase, attributeRQs);

    // Then
    verify(tmsTestCaseAttributeMapper).convertToTmsTestCaseAttributes(attributeRQs);
    verify(tmsTestCaseAttributeRepository).saveAll(testCaseAttributes);

    // Assert the final state
    assertEquals(testCaseAttributes, testCase.getTags());
  }

  @Test
  void createTestCaseAttributes_WithNullAttributes_ShouldReturnEarly() {
    // When
    sut.createTestCaseAttributes(testCase, null);

    // Then
    verifyNoInteractions(tmsTestCaseAttributeMapper, tmsTestCaseAttributeRepository);

    // Assert that the test case's tags are unchanged
    assertEquals(existingAttributes, testCase.getTags());
  }

  @Test
  void createTestCaseAttributes_WithEmptyAttributes_ShouldReturnEarly() {
    // When
    sut.createTestCaseAttributes(testCase, Collections.emptyList());

    // Then
    verifyNoInteractions(tmsTestCaseAttributeMapper, tmsTestCaseAttributeRepository);

    // Assert that the test case's tags are unchanged
    assertEquals(existingAttributes, testCase.getTags());
  }

  @Test
  void updateTestCaseAttributes_ShouldDeleteOldAndCreateNewAttributes() {
    // Given
    when(tmsTestCaseAttributeMapper.convertToTmsTestCaseAttributes(attributeRQs)).thenReturn(
        testCaseAttributes);

    // When
    sut.updateTestCaseAttributes(testCase, attributeRQs);

    // Then
    verify(tmsTestCaseAttributeRepository).deleteAllByTestCaseId(testCase.getId());
    verify(tmsTestCaseAttributeMapper).convertToTmsTestCaseAttributes(attributeRQs);
    verify(tmsTestCaseAttributeRepository).saveAll(testCaseAttributes);

    // Assert the final state
    assertEquals(testCaseAttributes, testCase.getTags());
  }

  @Test
  void updateTestCaseAttributes_WithNullAttributes_ShouldOnlyDeleteOldAttributes() {
    // When
    sut.updateTestCaseAttributes(testCase, null);

    // Then
    verify(tmsTestCaseAttributeRepository).deleteAllByTestCaseId(testCase.getId());
    verifyNoInteractions(tmsTestCaseAttributeMapper);

    // Assert that the test case's tags are unchanged from the initial state
    // since createTestCaseAttributes with null will return early
    assertEquals(existingAttributes, testCase.getTags());
  }

  @Test
  void updateTestCaseAttributes_WithEmptyAttributes_ShouldOnlyDeleteOldAttributes() {
    // When
    sut.updateTestCaseAttributes(testCase, Collections.emptyList());

    // Then
    verify(tmsTestCaseAttributeRepository).deleteAllByTestCaseId(testCase.getId());
    verifyNoInteractions(tmsTestCaseAttributeMapper);

    // Assert that the test case's tags are unchanged from the initial state
    // since createTestCaseAttributes with empty list will return early
    assertEquals(existingAttributes, testCase.getTags());
  }

  @Test
  void patchTestCaseAttributes_ShouldAddNewAttributesWithoutRemovingExisting() {
    // Given
    when(tmsTestCaseAttributeMapper.convertToTmsTestCaseAttributes(attributeRQs)).thenReturn(
        testCaseAttributes);

    // Make a copy of the existing attributes to verify they're still present after patching
    Set<TmsTestCaseAttribute> initialAttributes = new HashSet<>(testCase.getTags());

    // When
    sut.patchTestCaseAttributes(testCase, attributeRQs);

    // Then
    verify(tmsTestCaseAttributeMapper).convertToTmsTestCaseAttributes(attributeRQs);
    verify(tmsTestCaseAttributeRepository).saveAll(testCaseAttributes);

    // Assert the final state - should contain both existing and new attributes
    assertEquals(2, testCase.getTags().size());
    assertTrue(testCase.getTags().containsAll(initialAttributes));
    assertTrue(testCase.getTags().containsAll(testCaseAttributes));
  }

  @Test
  void patchTestCaseAttributes_WithNullAttributes_ShouldReturnEarly() {
    // Given
    Set<TmsTestCaseAttribute> initialAttributes = new HashSet<>(testCase.getTags());

    // When
    sut.patchTestCaseAttributes(testCase, null);

    // Then
    verifyNoInteractions(tmsTestCaseAttributeMapper, tmsTestCaseAttributeRepository);

    // Assert that the existing attributes are unchanged
    assertEquals(initialAttributes, testCase.getTags());
    assertEquals(initialAttributes.size(), testCase.getTags().size());
  }

  @Test
  void patchTestCaseAttributes_WithEmptyAttributes_ShouldReturnEarly() {
    // Given
    Set<TmsTestCaseAttribute> initialAttributes = new HashSet<>(testCase.getTags());

    // When
    sut.patchTestCaseAttributes(testCase, Collections.emptyList());

    // Then
    verifyNoInteractions(tmsTestCaseAttributeMapper, tmsTestCaseAttributeRepository);

    // Assert that the existing attributes are unchanged
    assertEquals(initialAttributes, testCase.getTags());
    assertEquals(initialAttributes.size(), testCase.getTags().size());
  }

  @Test
  void deleteAllByTestCaseId_ShouldCallRepositoryDelete() {
    // Given
    Long testCaseId = 1L;

    // When
    sut.deleteAllByTestCaseId(testCaseId);

    // Then
    verify(tmsTestCaseAttributeRepository).deleteAllByTestCaseId(testCaseId);
  }

  @Test
  void deleteAllByTestCaseIds_WithValidIds_ShouldCallRepositoryDelete() {
    // Given
    List<Long> testCaseIds = Arrays.asList(1L, 2L, 3L);

    // When
    sut.deleteAllByTestCaseIds(testCaseIds);

    // Then
    verify(tmsTestCaseAttributeRepository).deleteAllByTestCaseIds(testCaseIds);
  }
}
