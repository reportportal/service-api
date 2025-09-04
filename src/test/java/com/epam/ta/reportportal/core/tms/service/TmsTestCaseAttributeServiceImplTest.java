package com.epam.ta.reportportal.core.tms.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
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
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
  private TmsTestCase testCase2;
  private TmsTestCase originalTestCase;
  private TmsTestCase newTestCase;
  private List<TmsTestCase> testCases;
  private List<TmsTestCaseAttributeRQ> attributeRQs;
  private Set<TmsTestCaseAttribute> testCaseAttributes;
  private Set<TmsTestCaseAttribute> existingAttributes;
  private Set<TmsTestCaseAttribute> duplicatedAttributes;

  @BeforeEach
  void setUp() {
    // Setup test case 1
    testCase = new TmsTestCase();
    testCase.setId(1L);
    testCase.setName("Test Case 1");
    testCase.setDescription("Description 1");

    // Setup test case 2
    testCase2 = new TmsTestCase();
    testCase2.setId(2L);
    testCase2.setName("Test Case 2");
    testCase2.setDescription("Description 2");

    // Setup original and new test cases for duplication
    originalTestCase = new TmsTestCase();
    originalTestCase.setId(3L);
    originalTestCase.setName("Original Test Case");
    originalTestCase.setDescription("Original Description");

    newTestCase = new TmsTestCase();
    newTestCase.setId(4L);
    newTestCase.setName("New Test Case");
    newTestCase.setDescription("New Description");

    // Setup test cases list
    testCases = Arrays.asList(testCase, testCase2);

    // Setup existing attributes for testCase
    existingAttributes = new HashSet<>();
    TmsTestCaseAttribute existingAttribute = new TmsTestCaseAttribute();
    TmsTestCaseAttributeId existingAttributeId = new TmsTestCaseAttributeId(1L, 3L);
    existingAttribute.setId(existingAttributeId);

    TmsAttribute existingTmsAttribute = new TmsAttribute();
    existingTmsAttribute.setId(3L);
    existingAttribute.setAttribute(existingTmsAttribute);

    existingAttribute.setValue("existing-value");
    existingAttributes.add(existingAttribute);

    testCase.setAttributes(new HashSet<>(existingAttributes));
    testCase2.setAttributes(new HashSet<>());

    // Setup original test case attributes for duplication
    Set<TmsTestCaseAttribute> originalAttributes = new HashSet<>();
    TmsTestCaseAttribute originalAttribute1 = new TmsTestCaseAttribute();
    TmsTestCaseAttributeId originalAttributeId1 = new TmsTestCaseAttributeId(3L, 5L);
    originalAttribute1.setId(originalAttributeId1);
    TmsAttribute originalTmsAttribute1 = new TmsAttribute();
    originalTmsAttribute1.setId(5L);
    originalAttribute1.setAttribute(originalTmsAttribute1);
    originalAttribute1.setValue("original-value-1");
    originalAttributes.add(originalAttribute1);

    TmsTestCaseAttribute originalAttribute2 = new TmsTestCaseAttribute();
    TmsTestCaseAttributeId originalAttributeId2 = new TmsTestCaseAttributeId(3L, 6L);
    originalAttribute2.setId(originalAttributeId2);
    TmsAttribute originalTmsAttribute2 = new TmsAttribute();
    originalTmsAttribute2.setId(6L);
    originalAttribute2.setAttribute(originalTmsAttribute2);
    originalAttribute2.setValue("original-value-2");
    originalAttributes.add(originalAttribute2);

    originalTestCase.setAttributes(originalAttributes);

    // Setup duplicated attributes
    duplicatedAttributes = new HashSet<>();
    TmsTestCaseAttribute duplicatedAttribute1 = new TmsTestCaseAttribute();
    TmsTestCaseAttributeId duplicatedAttributeId1 = new TmsTestCaseAttributeId(4L, 5L);
    duplicatedAttribute1.setId(duplicatedAttributeId1);
    duplicatedAttribute1.setAttribute(originalTmsAttribute1);
    duplicatedAttribute1.setValue("original-value-1");
    duplicatedAttributes.add(duplicatedAttribute1);

    TmsTestCaseAttribute duplicatedAttribute2 = new TmsTestCaseAttribute();
    TmsTestCaseAttributeId duplicatedAttributeId2 = new TmsTestCaseAttributeId(4L, 6L);
    duplicatedAttribute2.setId(duplicatedAttributeId2);
    duplicatedAttribute2.setAttribute(originalTmsAttribute2);
    duplicatedAttribute2.setValue("original-value-2");
    duplicatedAttributes.add(duplicatedAttribute2);

    // Setup attribute requests
    attributeRQs = new ArrayList<>();
    TmsTestCaseAttributeRQ attributeRQ = new TmsTestCaseAttributeRQ();
    attributeRQ.setValue("new-value");
    attributeRQ.setId(2L);
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
    assertEquals(testCaseAttributes, testCase.getAttributes());
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
    assertEquals(testCaseAttributes, testCase.getAttributes());
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
    assertEquals(existingAttributes, testCase.getAttributes());
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
    assertEquals(existingAttributes, testCase.getAttributes());
  }

  @Test
  void patchTestCaseAttributes_SingleTestCase_ShouldAddNewAttributesWithoutRemovingExisting() {
    // Given
    when(tmsTestCaseAttributeMapper.convertToTmsTestCaseAttributes(attributeRQs)).thenReturn(
        testCaseAttributes);

    // Make a copy of the existing attributes to verify they're still present after patching
    Set<TmsTestCaseAttribute> initialAttributes = new HashSet<>(testCase.getAttributes());

    // When
    sut.patchTestCaseAttributes(testCase, attributeRQs);

    // Then
    verify(tmsTestCaseAttributeMapper).convertToTmsTestCaseAttributes(attributeRQs);
    verify(tmsTestCaseAttributeRepository).saveAll(testCaseAttributes);

    // Assert the final state - should contain both existing and new attributes
    assertEquals(2, testCase.getAttributes().size());
    assertTrue(testCase.getAttributes().containsAll(initialAttributes));
    assertTrue(testCase.getAttributes().containsAll(testCaseAttributes));
  }

  @Test
  void patchTestCaseAttributes_SingleTestCase_WithNullAttributes_ShouldReturnEarly() {
    // Given
    Set<TmsTestCaseAttribute> initialAttributes = new HashSet<>(testCase.getAttributes());

    // When
    sut.patchTestCaseAttributes(testCase, null);

    // Then
    verifyNoInteractions(tmsTestCaseAttributeMapper, tmsTestCaseAttributeRepository);

    // Assert that the existing attributes are unchanged
    assertEquals(initialAttributes, testCase.getAttributes());
    assertEquals(initialAttributes.size(), testCase.getAttributes().size());
  }

  @Test
  void patchTestCaseAttributes_SingleTestCase_WithEmptyAttributes_ShouldReturnEarly() {
    // Given
    Set<TmsTestCaseAttribute> initialAttributes = new HashSet<>(testCase.getAttributes());

    // When
    sut.patchTestCaseAttributes(testCase, Collections.emptyList());

    // Then
    verifyNoInteractions(tmsTestCaseAttributeMapper, tmsTestCaseAttributeRepository);

    // Assert that the existing attributes are unchanged
    assertEquals(initialAttributes, testCase.getAttributes());
    assertEquals(initialAttributes.size(), testCase.getAttributes().size());
  }

  @Test
  void patchTestCaseAttributes_MultipleTestCases_ShouldAddAttributesToAllTestCases() {
    // Given
    when(tmsTestCaseAttributeMapper.convertToTmsTestCaseAttributes(attributeRQs)).thenReturn(
        testCaseAttributes);

    // Store initial states
    Set<TmsTestCaseAttribute> initialAttributesTestCase1 = new HashSet<>(testCase.getAttributes());
    Set<TmsTestCaseAttribute> initialAttributesTestCase2 = new HashSet<>(testCase2.getAttributes());

    // When
    sut.patchTestCaseAttributes(testCases, attributeRQs);

    // Then
    verify(tmsTestCaseAttributeMapper).convertToTmsTestCaseAttributes(attributeRQs);
    // saveAll should be called once for each test case
    verify(tmsTestCaseAttributeRepository, times(2)).saveAll(testCaseAttributes);

    // Assert that both test cases have the new attributes added
    assertTrue(testCase.getAttributes().containsAll(initialAttributesTestCase1));
    assertTrue(testCase.getAttributes().containsAll(testCaseAttributes));
    assertTrue(testCase2.getAttributes().containsAll(initialAttributesTestCase2));
    assertTrue(testCase2.getAttributes().containsAll(testCaseAttributes));
  }

  @Test
  void patchTestCaseAttributes_MultipleTestCases_WithNullAttributes_ShouldReturnEarly() {
    // Given
    Set<TmsTestCaseAttribute> initialAttributesTestCase1 = new HashSet<>(testCase.getAttributes());
    Set<TmsTestCaseAttribute> initialAttributesTestCase2 = new HashSet<>(testCase2.getAttributes());

    // When
    sut.patchTestCaseAttributes(testCases, null);

    // Then
    verifyNoInteractions(tmsTestCaseAttributeMapper, tmsTestCaseAttributeRepository);

    // Assert that both test cases' attributes are unchanged
    assertEquals(initialAttributesTestCase1, testCase.getAttributes());
    assertEquals(initialAttributesTestCase2, testCase2.getAttributes());
  }

  @Test
  void patchTestCaseAttributes_MultipleTestCases_WithEmptyAttributes_ShouldReturnEarly() {
    // Given
    Set<TmsTestCaseAttribute> initialAttributesTestCase1 = new HashSet<>(testCase.getAttributes());
    Set<TmsTestCaseAttribute> initialAttributesTestCase2 = new HashSet<>(testCase2.getAttributes());

    // When
    sut.patchTestCaseAttributes(testCases, Collections.emptyList());

    // Then
    verifyNoInteractions(tmsTestCaseAttributeMapper, tmsTestCaseAttributeRepository);

    // Assert that both test cases' attributes are unchanged
    assertEquals(initialAttributesTestCase1, testCase.getAttributes());
    assertEquals(initialAttributesTestCase2, testCase2.getAttributes());
  }

  @Test
  void patchTestCaseAttributes_MultipleTestCases_WithSingleTestCase_ShouldWorkCorrectly() {
    // Given
    List<TmsTestCase> singleTestCaseList = Collections.singletonList(testCase);
    when(tmsTestCaseAttributeMapper.convertToTmsTestCaseAttributes(attributeRQs)).thenReturn(
        testCaseAttributes);

    Set<TmsTestCaseAttribute> initialAttributes = new HashSet<>(testCase.getAttributes());

    // When
    sut.patchTestCaseAttributes(singleTestCaseList, attributeRQs);

    // Then
    verify(tmsTestCaseAttributeMapper).convertToTmsTestCaseAttributes(attributeRQs);
    verify(tmsTestCaseAttributeRepository, times(1)).saveAll(testCaseAttributes);

    // Assert the final state
    assertTrue(testCase.getAttributes().containsAll(initialAttributes));
    assertTrue(testCase.getAttributes().containsAll(testCaseAttributes));
  }

  @Test
  void patchTestCaseAttributes_MultipleTestCases_WithEmptyTestCasesList_ShouldNotCallRepository() {
    // Given
    List<TmsTestCase> emptyTestCasesList = Collections.emptyList();
    when(tmsTestCaseAttributeMapper.convertToTmsTestCaseAttributes(attributeRQs)).thenReturn(
        testCaseAttributes);

    // When
    sut.patchTestCaseAttributes(emptyTestCasesList, attributeRQs);

    // Then
    verify(tmsTestCaseAttributeMapper).convertToTmsTestCaseAttributes(attributeRQs);
    verifyNoInteractions(tmsTestCaseAttributeRepository);
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
  void deleteAllByTestFolderId_ShouldCallRepositoryDelete() {
    // Given
    Long projectId = 1L;
    Long testFolderId = 2L;

    // When
    sut.deleteAllByTestFolderId(projectId, testFolderId);

    // Then
    verify(tmsTestCaseAttributeRepository).deleteTestCaseAttributesByTestFolderId(projectId,
        testFolderId);
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

  @Test
  void deleteByTestCaseIdAndAttributeIds_ShouldCallRepositoryDelete() {
    // Given
    Long testCaseId = 2L;
    List<Long> attributeIds = Arrays.asList(3L, 4L, 5L);

    // When
    sut.deleteByTestCaseIdAndAttributeIds(testCaseId, attributeIds);

    // Then
    verify(tmsTestCaseAttributeRepository).deleteByTestCaseIdAndAttributeIds(testCaseId,
        attributeIds);
  }

  @Test
  void deleteByTestCaseIdAndAttributeIds_WithSingleAttribute_ShouldCallRepositoryDelete() {
    // Given
    Long testCaseId = 2L;
    List<Long> attributeIds = List.of(3L);

    // When
    sut.deleteByTestCaseIdAndAttributeIds(testCaseId, attributeIds);

    // Then
    verify(tmsTestCaseAttributeRepository).deleteByTestCaseIdAndAttributeIds(testCaseId,
        attributeIds);
  }

  @Test
  void deleteByTestCaseIdAndAttributeIds_WithEmptyAttributeIds_ShouldCallRepositoryDelete() {
    // Given
    Long testCaseId = 2L;
    List<Long> attributeIds = Collections.emptyList();

    // When
    sut.deleteByTestCaseIdAndAttributeIds(testCaseId, attributeIds);

    // Then
    verify(tmsTestCaseAttributeRepository).deleteByTestCaseIdAndAttributeIds(testCaseId,
        attributeIds);
  }

  @Test
  void deleteByTestCaseIdsAndAttributeIds_ShouldCallRepositoryDelete() {
    // Given
    List<Long> testCaseIds = Arrays.asList(2L, 3L, 4L);
    Collection<Long> attributeIds = Arrays.asList(5L, 6L, 7L);

    // When
    sut.deleteByTestCaseIdsAndAttributeIds(testCaseIds, attributeIds);

    // Then
    verify(tmsTestCaseAttributeRepository).deleteByTestCaseIdsAndAttributeIds(testCaseIds,
        attributeIds);
  }

  @Test
  void deleteByTestCaseIdsAndAttributeIds_WithSingleTestCaseAndAttribute_ShouldCallRepositoryDelete() {
    // Given
    List<Long> testCaseIds = List.of(2L);
    Collection<Long> attributeIds = List.of(3L);

    // When
    sut.deleteByTestCaseIdsAndAttributeIds(testCaseIds, attributeIds);

    // Then
    verify(tmsTestCaseAttributeRepository).deleteByTestCaseIdsAndAttributeIds(testCaseIds,
        attributeIds);
  }

  @Test
  void deleteByTestCaseIdsAndAttributeIds_WithMultipleTestCasesAndSingleAttribute_ShouldCallRepositoryDelete() {
    // Given
    List<Long> testCaseIds = Arrays.asList(2L, 3L, 4L, 5L);
    Collection<Long> attributeIds = List.of(6L);

    // When
    sut.deleteByTestCaseIdsAndAttributeIds(testCaseIds, attributeIds);

    // Then
    verify(tmsTestCaseAttributeRepository).deleteByTestCaseIdsAndAttributeIds(testCaseIds,
        attributeIds);
  }

  @Test
  void deleteByTestCaseIdsAndAttributeIds_WithSingleTestCaseAndMultipleAttributes_ShouldCallRepositoryDelete() {
    // Given
    List<Long> testCaseIds = List.of(2L);
    Collection<Long> attributeIds = Arrays.asList(3L, 4L, 5L, 6L);

    // When
    sut.deleteByTestCaseIdsAndAttributeIds(testCaseIds, attributeIds);

    // Then
    verify(tmsTestCaseAttributeRepository).deleteByTestCaseIdsAndAttributeIds(testCaseIds,
        attributeIds);
  }

  @Test
  void deleteByTestCaseIdsAndAttributeIds_WithEmptyTestCaseIds_ShouldCallRepositoryDelete() {
    // Given
    List<Long> testCaseIds = Collections.emptyList();
    Collection<Long> attributeIds = Arrays.asList(3L, 4L);

    // When
    sut.deleteByTestCaseIdsAndAttributeIds(testCaseIds, attributeIds);

    // Then
    verify(tmsTestCaseAttributeRepository).deleteByTestCaseIdsAndAttributeIds(testCaseIds,
        attributeIds);
  }

  @Test
  void deleteByTestCaseIdsAndAttributeIds_WithEmptyAttributeIds_ShouldCallRepositoryDelete() {
    // Given
    List<Long> testCaseIds = Arrays.asList(2L, 3L);
    Collection<Long> attributeIds = Collections.emptyList();

    // When
    sut.deleteByTestCaseIdsAndAttributeIds(testCaseIds, attributeIds);

    // Then
    verify(tmsTestCaseAttributeRepository).deleteByTestCaseIdsAndAttributeIds(testCaseIds,
        attributeIds);
  }

  @Test
  void deleteByTestCaseIdsAndAttributeIds_WithBothEmptyLists_ShouldCallRepositoryDelete() {
    // Given
    List<Long> testCaseIds = Collections.emptyList();
    Collection<Long> attributeIds = Collections.emptyList();

    // When
    sut.deleteByTestCaseIdsAndAttributeIds(testCaseIds, attributeIds);

    // Then
    verify(tmsTestCaseAttributeRepository).deleteByTestCaseIdsAndAttributeIds(testCaseIds,
        attributeIds);
  }

  @Test
  void duplicateTestCaseAttributes_WithExistingAttributes_ShouldDuplicateAndSaveAttributes() {
    // Given
    TmsTestCaseAttribute originalAttribute1 = originalTestCase.getAttributes().iterator().next();
    TmsTestCaseAttribute duplicatedAttribute1 = duplicatedAttributes.iterator().next();

    when(tmsTestCaseAttributeMapper.duplicateTestCaseAttribute(originalAttribute1, newTestCase))
        .thenReturn(duplicatedAttribute1);

    // We need to set up the mapper to return different duplicated attributes for each original
    originalTestCase.getAttributes().forEach(originalAttribute -> {
      TmsTestCaseAttribute duplicatedAttribute = duplicatedAttributes.stream()
          .filter(
              dup -> dup.getAttribute().getId().equals(originalAttribute.getAttribute().getId()))
          .findFirst()
          .orElse(null);
      when(tmsTestCaseAttributeMapper.duplicateTestCaseAttribute(originalAttribute, newTestCase))
          .thenReturn(duplicatedAttribute);
    });

    // When
    sut.duplicateTestCaseAttributes(originalTestCase, newTestCase);

    // Then
    verify(tmsTestCaseAttributeRepository).saveAll(duplicatedAttributes);

    // Verify that duplicateTestCaseAttribute was called for each original attribute
    originalTestCase.getAttributes().forEach(originalAttribute -> {
      verify(tmsTestCaseAttributeMapper).duplicateTestCaseAttribute(originalAttribute, newTestCase);
    });
  }

  @Test
  void duplicateTestCaseAttributes_WithNoAttributes_ShouldNotCallMapperOrRepository() {
    // Given
    TmsTestCase emptyOriginalTestCase = new TmsTestCase();
    emptyOriginalTestCase.setId(5L);
    emptyOriginalTestCase.setAttributes(null);

    // When
    sut.duplicateTestCaseAttributes(emptyOriginalTestCase, newTestCase);

    // Then
    verifyNoInteractions(tmsTestCaseAttributeMapper, tmsTestCaseAttributeRepository);
  }

  @Test
  void duplicateTestCaseAttributes_WithEmptyAttributes_ShouldNotCallMapperOrRepository() {
    // Given
    TmsTestCase emptyOriginalTestCase = new TmsTestCase();
    emptyOriginalTestCase.setId(5L);
    emptyOriginalTestCase.setAttributes(new HashSet<>());

    // When
    sut.duplicateTestCaseAttributes(emptyOriginalTestCase, newTestCase);

    // Then
    verifyNoInteractions(tmsTestCaseAttributeMapper, tmsTestCaseAttributeRepository);
  }

  @Test
  void duplicateTestCaseAttributes_WithSingleAttribute_ShouldDuplicateCorrectly() {
    // Given
    TmsTestCase singleAttributeOriginalTestCase = new TmsTestCase();
    singleAttributeOriginalTestCase.setId(6L);

    TmsTestCaseAttribute singleOriginalAttribute = new TmsTestCaseAttribute();
    TmsTestCaseAttributeId singleOriginalAttributeId = new TmsTestCaseAttributeId(6L, 7L);
    singleOriginalAttribute.setId(singleOriginalAttributeId);
    TmsAttribute singleTmsAttribute = new TmsAttribute();
    singleTmsAttribute.setId(7L);
    singleOriginalAttribute.setAttribute(singleTmsAttribute);
    singleOriginalAttribute.setValue("single-value");

    Set<TmsTestCaseAttribute> singleAttributeSet = Set.of(singleOriginalAttribute);
    singleAttributeOriginalTestCase.setAttributes(singleAttributeSet);

    TmsTestCaseAttribute singleDuplicatedAttribute = new TmsTestCaseAttribute();
    TmsTestCaseAttributeId singleDuplicatedAttributeId = new TmsTestCaseAttributeId(4L, 7L);
    singleDuplicatedAttribute.setId(singleDuplicatedAttributeId);
    singleDuplicatedAttribute.setAttribute(singleTmsAttribute);
    singleDuplicatedAttribute.setValue("single-value");

    Set<TmsTestCaseAttribute> singleDuplicatedAttributeSet = Set.of(singleDuplicatedAttribute);

    when(
        tmsTestCaseAttributeMapper.duplicateTestCaseAttribute(singleOriginalAttribute, newTestCase))
        .thenReturn(singleDuplicatedAttribute);

    // When
    sut.duplicateTestCaseAttributes(singleAttributeOriginalTestCase, newTestCase);

    // Then
    verify(tmsTestCaseAttributeMapper).duplicateTestCaseAttribute(singleOriginalAttribute,
        newTestCase);
    verify(tmsTestCaseAttributeRepository).saveAll(singleDuplicatedAttributeSet);
  }

  // Tests for addAttributesToTestCases method

  @Test
  void addAttributesToTestCases_WithMultipleTestCasesAndAttributes_ShouldCreateAllCombinations() {
    // Given
    List<Long> testCaseIds = Arrays.asList(1L, 2L);
    Collection<Long> attributeIds = Arrays.asList(10L, 20L);

    TmsTestCaseAttribute attr1TestCase1 = new TmsTestCaseAttribute();
    TmsTestCaseAttribute attr2TestCase1 = new TmsTestCaseAttribute();
    TmsTestCaseAttribute attr1TestCase2 = new TmsTestCaseAttribute();
    TmsTestCaseAttribute attr2TestCase2 = new TmsTestCaseAttribute();

    when(tmsTestCaseAttributeMapper.createTestCaseAttribute(1L, 10L)).thenReturn(attr1TestCase1);
    when(tmsTestCaseAttributeMapper.createTestCaseAttribute(1L, 20L)).thenReturn(attr2TestCase1);
    when(tmsTestCaseAttributeMapper.createTestCaseAttribute(2L, 10L)).thenReturn(attr1TestCase2);
    when(tmsTestCaseAttributeMapper.createTestCaseAttribute(2L, 20L)).thenReturn(attr2TestCase2);

    // Порядок важен - соответствует flatMap логике
    List<TmsTestCaseAttribute> expectedAttributes = Arrays.asList(
        attr1TestCase1, attr2TestCase1, attr1TestCase2, attr2TestCase2);

    // When
    sut.addAttributesToTestCases(testCaseIds, attributeIds);

    // Then
    verify(tmsTestCaseAttributeMapper).createTestCaseAttribute(1L, 10L);
    verify(tmsTestCaseAttributeMapper).createTestCaseAttribute(1L, 20L);
    verify(tmsTestCaseAttributeMapper).createTestCaseAttribute(2L, 10L);
    verify(tmsTestCaseAttributeMapper).createTestCaseAttribute(2L, 20L);
    verify(tmsTestCaseAttributeRepository).saveAll(expectedAttributes);
  }

  @Test
  void addAttributesToTestCases_WithSingleTestCaseAndMultipleAttributes_ShouldCreateAllAttributes() {
    // Given
    List<Long> testCaseIds = List.of(1L);
    Collection<Long> attributeIds = Arrays.asList(10L, 20L, 30L);

    TmsTestCaseAttribute attr1 = new TmsTestCaseAttribute();
    TmsTestCaseAttribute attr2 = new TmsTestCaseAttribute();
    TmsTestCaseAttribute attr3 = new TmsTestCaseAttribute();

    when(tmsTestCaseAttributeMapper.createTestCaseAttribute(1L, 10L)).thenReturn(attr1);
    when(tmsTestCaseAttributeMapper.createTestCaseAttribute(1L, 20L)).thenReturn(attr2);
    when(tmsTestCaseAttributeMapper.createTestCaseAttribute(1L, 30L)).thenReturn(attr3);

    List<TmsTestCaseAttribute> expectedAttributes = Arrays.asList(attr1, attr2, attr3);

    // When
    sut.addAttributesToTestCases(testCaseIds, attributeIds);

    // Then
    verify(tmsTestCaseAttributeMapper).createTestCaseAttribute(1L, 10L);
    verify(tmsTestCaseAttributeMapper).createTestCaseAttribute(1L, 20L);
    verify(tmsTestCaseAttributeMapper).createTestCaseAttribute(1L, 30L);
    verify(tmsTestCaseAttributeRepository).saveAll(expectedAttributes);
  }

  @Test
  void addAttributesToTestCases_WithMultipleTestCasesAndSingleAttribute_ShouldCreateAllTestCases() {
    // Given
    List<Long> testCaseIds = Arrays.asList(1L, 2L, 3L);
    Collection<Long> attributeIds = List.of(10L);

    TmsTestCaseAttribute attr1 = new TmsTestCaseAttribute();
    TmsTestCaseAttribute attr2 = new TmsTestCaseAttribute();
    TmsTestCaseAttribute attr3 = new TmsTestCaseAttribute();

    when(tmsTestCaseAttributeMapper.createTestCaseAttribute(1L, 10L)).thenReturn(attr1);
    when(tmsTestCaseAttributeMapper.createTestCaseAttribute(2L, 10L)).thenReturn(attr2);
    when(tmsTestCaseAttributeMapper.createTestCaseAttribute(3L, 10L)).thenReturn(attr3);

    List<TmsTestCaseAttribute> expectedAttributes = Arrays.asList(attr1, attr2, attr3);

    // When
    sut.addAttributesToTestCases(testCaseIds, attributeIds);

    // Then
    verify(tmsTestCaseAttributeMapper).createTestCaseAttribute(1L, 10L);
    verify(tmsTestCaseAttributeMapper).createTestCaseAttribute(2L, 10L);
    verify(tmsTestCaseAttributeMapper).createTestCaseAttribute(3L, 10L);
    verify(tmsTestCaseAttributeRepository).saveAll(expectedAttributes);
  }

  @Test
  void addAttributesToTestCases_WithSingleTestCaseAndSingleAttribute_ShouldCreateOneAttribute() {
    // Given
    List<Long> testCaseIds = List.of(1L);
    Collection<Long> attributeIds = List.of(10L);

    TmsTestCaseAttribute attribute = new TmsTestCaseAttribute();

    when(tmsTestCaseAttributeMapper.createTestCaseAttribute(1L, 10L)).thenReturn(attribute);

    List<TmsTestCaseAttribute> expectedAttributes = List.of(attribute);

    // When
    sut.addAttributesToTestCases(testCaseIds, attributeIds);

    // Then
    verify(tmsTestCaseAttributeMapper).createTestCaseAttribute(1L, 10L);
    verify(tmsTestCaseAttributeRepository).saveAll(expectedAttributes);
  }

  @Test
  void addAttributesToTestCases_WithSetAsAttributeIds_ShouldWorkCorrectly() {
    // Given
    List<Long> testCaseIds = List.of(1L);
    Collection<Long> attributeIds = Set.of(10L, 20L); // Using Set instead of List

    TmsTestCaseAttribute attr1 = new TmsTestCaseAttribute();
    TmsTestCaseAttribute attr2 = new TmsTestCaseAttribute();

    when(tmsTestCaseAttributeMapper.createTestCaseAttribute(1L, 10L)).thenReturn(attr1);
    when(tmsTestCaseAttributeMapper.createTestCaseAttribute(1L, 20L)).thenReturn(attr2);

    // When
    sut.addAttributesToTestCases(testCaseIds, attributeIds);

    // Then
    verify(tmsTestCaseAttributeMapper).createTestCaseAttribute(1L, 10L);
    verify(tmsTestCaseAttributeMapper).createTestCaseAttribute(1L, 20L);

    // Используем ArgumentCaptor для проверки содержимого списка,
    // так как порядок в Set может быть разным
    ArgumentCaptor<List<TmsTestCaseAttribute>> captor = ArgumentCaptor.forClass(List.class);
    verify(tmsTestCaseAttributeRepository).saveAll(captor.capture());

    List<TmsTestCaseAttribute> actualAttributes = captor.getValue();
    assertEquals(2, actualAttributes.size());
    assertTrue(actualAttributes.contains(attr1));
    assertTrue(actualAttributes.contains(attr2));
  }

  @Test
  void addAttributesToTestCases_WithLargerDataSet_ShouldHandleCorrectOrder() {
    // Given
    List<Long> testCaseIds = Arrays.asList(1L, 2L, 3L);
    Collection<Long> attributeIds = Arrays.asList(10L, 20L);

    // Создаем все возможные комбинации
    TmsTestCaseAttribute attr10TestCase1 = new TmsTestCaseAttribute();
    TmsTestCaseAttribute attr20TestCase1 = new TmsTestCaseAttribute();
    TmsTestCaseAttribute attr10TestCase2 = new TmsTestCaseAttribute();
    TmsTestCaseAttribute attr20TestCase2 = new TmsTestCaseAttribute();
    TmsTestCaseAttribute attr10TestCase3 = new TmsTestCaseAttribute();
    TmsTestCaseAttribute attr20TestCase3 = new TmsTestCaseAttribute();

    when(tmsTestCaseAttributeMapper.createTestCaseAttribute(1L, 10L)).thenReturn(attr10TestCase1);
    when(tmsTestCaseAttributeMapper.createTestCaseAttribute(1L, 20L)).thenReturn(attr20TestCase1);
    when(tmsTestCaseAttributeMapper.createTestCaseAttribute(2L, 10L)).thenReturn(attr10TestCase2);
    when(tmsTestCaseAttributeMapper.createTestCaseAttribute(2L, 20L)).thenReturn(attr20TestCase2);
    when(tmsTestCaseAttributeMapper.createTestCaseAttribute(3L, 10L)).thenReturn(attr10TestCase3);
    when(tmsTestCaseAttributeMapper.createTestCaseAttribute(3L, 20L)).thenReturn(attr20TestCase3);

    // Ожидаемый порядок согласно flatMap логике
    List<TmsTestCaseAttribute> expectedAttributes = Arrays.asList(
        attr10TestCase1, attr20TestCase1,
        attr10TestCase2, attr20TestCase2,
        attr10TestCase3, attr20TestCase3);

    // When
    sut.addAttributesToTestCases(testCaseIds, attributeIds);

    // Then
    verify(tmsTestCaseAttributeRepository).saveAll(expectedAttributes);
  }

  @Test
  void addAttributesToTestCases_WithEmptyCollections_ShouldCallSaveAllWithEmptyList() {
    // Given
    List<Long> testCaseIds = Collections.emptyList();
    Collection<Long> attributeIds = Collections.emptyList();

    // When
    sut.addAttributesToTestCases(testCaseIds, attributeIds);

    // Then
    verify(tmsTestCaseAttributeRepository).saveAll(Collections.emptyList());
    verifyNoInteractions(tmsTestCaseAttributeMapper);
  }
}
