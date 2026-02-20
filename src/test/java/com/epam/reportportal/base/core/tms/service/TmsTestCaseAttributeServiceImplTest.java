
package com.epam.reportportal.base.core.tms.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.epam.reportportal.base.core.tms.dto.TmsAttributeRS;
import com.epam.reportportal.base.core.tms.dto.TmsTestCaseAttributeRQ;
import com.epam.reportportal.base.core.tms.mapper.TmsAttributeMapper;
import com.epam.reportportal.base.core.tms.mapper.TmsTestCaseAttributeMapper;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsAttributeRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsTestCaseAttributeRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.Project;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsAttribute;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestCase;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestCaseAttribute;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestCaseAttributeId;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestFolder;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class TmsTestCaseAttributeServiceImplTest {

  private static final Long PROJECT_ID = 1L;

  @Mock
  private TmsTestCaseAttributeMapper tmsTestCaseAttributeMapper;

  @Mock
  private TmsTestCaseAttributeRepository tmsTestCaseAttributeRepository;

  @Mock
  private TmsAttributeService tmsAttributeService;

  @Mock
  private TmsAttributeRepository tmsAttributeRepository;

  @Mock
  private TmsAttributeMapper tmsAttributeMapper;

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
  private TmsAttribute tmsAttribute;
  private Pageable pageable;

  @BeforeEach
  void setUp() {
    // Setup pageable
    pageable = PageRequest.of(0, 20);

    // Setup project
    var project = new Project();
    project.setId(PROJECT_ID);

    // Setup test folder
    var testFolder = new TmsTestFolder();
    testFolder.setId(1L);
    testFolder.setProject(project);

    // Setup test case 1
    testCase = new TmsTestCase();
    testCase.setId(1L);
    testCase.setName("Test Case 1");
    testCase.setDescription("Description 1");
    testCase.setTestFolder(testFolder);

    // Setup test case 2
    testCase2 = new TmsTestCase();
    testCase2.setId(2L);
    testCase2.setName("Test Case 2");
    testCase2.setDescription("Description 2");
    testCase2.setTestFolder(testFolder);

    // Setup original and new test cases for duplication
    originalTestCase = new TmsTestCase();
    originalTestCase.setId(3L);
    originalTestCase.setName("Original Test Case");
    originalTestCase.setDescription("Original Description");
    originalTestCase.setTestFolder(testFolder);

    newTestCase = new TmsTestCase();
    newTestCase.setId(4L);
    newTestCase.setName("New Test Case");
    newTestCase.setDescription("New Description");
    newTestCase.setTestFolder(testFolder);

    // Setup test cases list
    testCases = Arrays.asList(testCase, testCase2);

    // Setup TmsAttribute
    tmsAttribute = new TmsAttribute();
    tmsAttribute.setId(2L);
    tmsAttribute.setKey("test-key");
    tmsAttribute.setProject(project);

    // Setup existing attributes for testCase
    existingAttributes = new HashSet<>();
    var existingAttribute = new TmsTestCaseAttribute();
    var existingAttributeId = new TmsTestCaseAttributeId(1L, 3L);
    existingAttribute.setId(existingAttributeId);

    var existingTmsAttribute = new TmsAttribute();
    existingTmsAttribute.setId(3L);
    existingTmsAttribute.setKey("existing-key");
    existingAttribute.setAttribute(existingTmsAttribute);

    existingAttributes.add(existingAttribute);

    testCase.setAttributes(new HashSet<>(existingAttributes));
    testCase2.setAttributes(new HashSet<>());

    // Setup original test case attributes for duplication
    var originalAttributes = new HashSet<TmsTestCaseAttribute>();
    var originalAttribute1 = new TmsTestCaseAttribute();
    var originalAttributeId1 = new TmsTestCaseAttributeId(3L, 5L);
    originalAttribute1.setId(originalAttributeId1);
    var originalTmsAttribute1 = new TmsAttribute();
    originalTmsAttribute1.setId(5L);
    originalTmsAttribute1.setKey("original-key-1");
    originalAttribute1.setAttribute(originalTmsAttribute1);
    originalAttributes.add(originalAttribute1);

    var originalAttribute2 = new TmsTestCaseAttribute();
    var originalAttributeId2 = new TmsTestCaseAttributeId(3L, 6L);
    originalAttribute2.setId(originalAttributeId2);
    var originalTmsAttribute2 = new TmsAttribute();
    originalTmsAttribute2.setId(6L);
    originalTmsAttribute2.setKey("original-key-2");
    originalAttribute2.setAttribute(originalTmsAttribute2);
    originalAttributes.add(originalAttribute2);

    originalTestCase.setAttributes(originalAttributes);

    // Setup duplicated attributes
    duplicatedAttributes = new HashSet<>();
    var duplicatedAttribute1 = new TmsTestCaseAttribute();
    var duplicatedAttributeId1 = new TmsTestCaseAttributeId(4L, 5L);
    duplicatedAttribute1.setId(duplicatedAttributeId1);
    duplicatedAttribute1.setAttribute(originalTmsAttribute1);
    duplicatedAttributes.add(duplicatedAttribute1);

    var duplicatedAttribute2 = new TmsTestCaseAttribute();
    var duplicatedAttributeId2 = new TmsTestCaseAttributeId(4L, 6L);
    duplicatedAttribute2.setId(duplicatedAttributeId2);
    duplicatedAttribute2.setAttribute(originalTmsAttribute2);
    duplicatedAttributes.add(duplicatedAttribute2);

    // Setup attribute requests
    attributeRQs = new ArrayList<>();
    var attributeRQ = new TmsTestCaseAttributeRQ();
    attributeRQ.setId(2L);
    attributeRQ.setKey("test-key");
    attributeRQs.add(attributeRQ);

    // Setup test case attributes
    testCaseAttributes = new HashSet<>();
    var attribute = new TmsTestCaseAttribute();
    var attributeId = new TmsTestCaseAttributeId(1L, 2L);
    attribute.setId(attributeId);
    attribute.setAttribute(tmsAttribute);
    testCaseAttributes.add(attribute);
  }

  @Test
  void createTestCaseAttributes_ShouldCreateAndSaveAttributesWithId() {
    // Given
    when(tmsAttributeService.getEntityById(PROJECT_ID, 2L)).thenReturn(tmsAttribute);
    when(tmsTestCaseAttributeMapper.createTestCaseAttribute(testCase, tmsAttribute))
        .thenReturn(testCaseAttributes.iterator().next());

    // When
    sut.createTestCaseAttributes(PROJECT_ID, testCase, attributeRQs);

    // Then
    verify(tmsAttributeService).getEntityById(PROJECT_ID, 2L);
    verify(tmsTestCaseAttributeMapper).createTestCaseAttribute(testCase, tmsAttribute);
    verify(tmsTestCaseAttributeRepository).saveAll(any(Set.class));

    // Assert the final state
    assertEquals(1, testCase.getAttributes().size());
  }

  @Test
  void createTestCaseAttributes_ShouldCreateAndSaveAttributesWithKey() {
    // Given
    var attributeRQ = new TmsTestCaseAttributeRQ();
    attributeRQ.setKey("new-tag");
    var attributeRQsWithKey = List.of(attributeRQ);

    when(tmsAttributeService.findOrCreateTag(PROJECT_ID, "new-tag")).thenReturn(tmsAttribute);
    when(tmsTestCaseAttributeMapper.createTestCaseAttribute(testCase, tmsAttribute))
        .thenReturn(testCaseAttributes.iterator().next());

    // When
    sut.createTestCaseAttributes(PROJECT_ID, testCase, attributeRQsWithKey);

    // Then
    verify(tmsAttributeService).findOrCreateTag(PROJECT_ID, "new-tag");
    verify(tmsTestCaseAttributeMapper).createTestCaseAttribute(testCase, tmsAttribute);
    verify(tmsTestCaseAttributeRepository).saveAll(any(Set.class));
  }

  @Test
  void createTestCaseAttributes_ShouldHandleMixedIdAndKey() {
    // Given
    var attributeRQ1 = new TmsTestCaseAttributeRQ();
    attributeRQ1.setId(2L);
    attributeRQ1.setKey("test-key");

    var attributeRQ2 = new TmsTestCaseAttributeRQ();
    attributeRQ2.setKey("new-tag");

    var mixedAttributeRQs = List.of(attributeRQ1, attributeRQ2);

    var tmsAttribute2 = new TmsAttribute();
    tmsAttribute2.setId(3L);
    tmsAttribute2.setKey("new-tag");

    var testCaseAttribute1 = new TmsTestCaseAttribute();
    var testCaseAttribute2 = new TmsTestCaseAttribute();

    when(tmsAttributeService.getEntityById(PROJECT_ID, 2L)).thenReturn(tmsAttribute);
    when(tmsAttributeService.findOrCreateTag(PROJECT_ID, "new-tag")).thenReturn(tmsAttribute2);
    when(tmsTestCaseAttributeMapper.createTestCaseAttribute(testCase, tmsAttribute))
        .thenReturn(testCaseAttribute1);
    when(tmsTestCaseAttributeMapper.createTestCaseAttribute(testCase, tmsAttribute2))
        .thenReturn(testCaseAttribute2);

    // When
    sut.createTestCaseAttributes(PROJECT_ID, testCase, mixedAttributeRQs);

    // Then
    verify(tmsAttributeService).getEntityById(PROJECT_ID, 2L);
    verify(tmsAttributeService).findOrCreateTag(PROJECT_ID, "new-tag");
    verify(tmsTestCaseAttributeMapper).createTestCaseAttribute(testCase, tmsAttribute);
    verify(tmsTestCaseAttributeMapper).createTestCaseAttribute(testCase, tmsAttribute2);
    verify(tmsTestCaseAttributeRepository).saveAll(any(Set.class));
  }

  @Test
  void updateTestCaseAttributes_ShouldDeleteOldAndCreateNewAttributes() {
    // Given
    var initialAttributes = new HashSet<>(existingAttributes);
    when(tmsAttributeService.getEntityById(PROJECT_ID, 2L)).thenReturn(tmsAttribute);
    when(tmsTestCaseAttributeMapper.createTestCaseAttribute(testCase, tmsAttribute))
        .thenReturn(testCaseAttributes.iterator().next());

    // When
    sut.updateTestCaseAttributes(PROJECT_ID, testCase, attributeRQs);

    // Then
    // Verify that deleteAll was called with the existing attributes
    var deleteCaptor = ArgumentCaptor.forClass(Collection.class);
    verify(tmsTestCaseAttributeRepository).deleteAll(deleteCaptor.capture());

    var deletedAttributes = deleteCaptor.getValue();
    assertEquals(initialAttributes.size(), deletedAttributes.size());
    assertTrue(deletedAttributes.containsAll(initialAttributes));

    // Verify that new attributes were created
    verify(tmsAttributeService).getEntityById(PROJECT_ID, 2L);
    verify(tmsTestCaseAttributeMapper).createTestCaseAttribute(testCase, tmsAttribute);
    verify(tmsTestCaseAttributeRepository).saveAll(any(Set.class));

    // Assert the final state
    assertEquals(1, testCase.getAttributes().size());
  }

  @Test
  void updateTestCaseAttributes_WithNullAttributes_ShouldOnlyDeleteOldAttributes() {
    // Given
    var initialAttributes = new HashSet<>(existingAttributes);

    // When
    sut.updateTestCaseAttributes(PROJECT_ID, testCase, null);

    // Then
    // Verify that deleteAll was called with existing attributes
    var deleteCaptor = ArgumentCaptor.forClass(Collection.class);
    verify(tmsTestCaseAttributeRepository).deleteAll(deleteCaptor.capture());

    var deletedAttributes = deleteCaptor.getValue();
    assertEquals(initialAttributes.size(), deletedAttributes.size());
    assertTrue(deletedAttributes.containsAll(initialAttributes));

    // Verify that attribute service was not called since attributes is null
    verifyNoInteractions(tmsAttributeService);
    verifyNoInteractions(tmsTestCaseAttributeMapper);

    // Assert that the test case's attributes collection is empty
    assertTrue(testCase.getAttributes().isEmpty());
  }

  @Test
  void updateTestCaseAttributes_WithEmptyAttributes_ShouldOnlyDeleteOldAttributes() {
    // Given
    var initialAttributes = new HashSet<>(existingAttributes);

    // When
    sut.updateTestCaseAttributes(PROJECT_ID, testCase, Collections.emptyList());

    // Then
    // Verify that deleteAll was called with existing attributes
    var deleteCaptor = ArgumentCaptor.forClass(Collection.class);
    verify(tmsTestCaseAttributeRepository).deleteAll(deleteCaptor.capture());

    var deletedAttributes = deleteCaptor.getValue();
    assertEquals(initialAttributes.size(), deletedAttributes.size());
    assertTrue(deletedAttributes.containsAll(initialAttributes));

    // Verify that services were not called since attributes list is empty
    verifyNoInteractions(tmsAttributeService);
    verifyNoInteractions(tmsTestCaseAttributeMapper);

    // Assert that the test case's attributes collection is empty
    assertTrue(testCase.getAttributes().isEmpty());
  }

  @Test
  void updateTestCaseAttributes_WithEmptyInitialAttributes_ShouldOnlyCreateNewAttributes() {
    // Given
    testCase.setAttributes(new HashSet<>()); // No existing attributes
    when(tmsAttributeService.getEntityById(PROJECT_ID, 2L)).thenReturn(tmsAttribute);
    when(tmsTestCaseAttributeMapper.createTestCaseAttribute(testCase, tmsAttribute))
        .thenReturn(testCaseAttributes.iterator().next());

    // When
    sut.updateTestCaseAttributes(PROJECT_ID, testCase, attributeRQs);

    // Then
    // Verify that deleteAll was NOT called since there were no existing attributes
    verify(tmsTestCaseAttributeRepository, times(0)).deleteAll(Collections.emptySet());

    // Verify that new attributes were created
    verify(tmsAttributeService).getEntityById(PROJECT_ID, 2L);
    verify(tmsTestCaseAttributeMapper).createTestCaseAttribute(testCase, tmsAttribute);
    verify(tmsTestCaseAttributeRepository).saveAll(any(Set.class));

    // Assert the final state
    assertEquals(1, testCase.getAttributes().size());
  }

  @Test
  void updateTestCaseAttributes_WithKeyInsteadOfId_ShouldCreateNewAttributes() {
    // Given
    var attributeRQ = new TmsTestCaseAttributeRQ();
    attributeRQ.setKey("new-tag");
    var attributeRQsWithKey = List.of(attributeRQ);

    when(tmsAttributeService.findOrCreateTag(PROJECT_ID, "new-tag")).thenReturn(tmsAttribute);
    when(tmsTestCaseAttributeMapper.createTestCaseAttribute(testCase, tmsAttribute))
        .thenReturn(testCaseAttributes.iterator().next());

    // When
    sut.updateTestCaseAttributes(PROJECT_ID, testCase, attributeRQsWithKey);

    // Then
    verify(tmsTestCaseAttributeRepository).deleteAll(any());
    verify(tmsAttributeService).findOrCreateTag(PROJECT_ID, "new-tag");
    verify(tmsTestCaseAttributeMapper).createTestCaseAttribute(testCase, tmsAttribute);
    verify(tmsTestCaseAttributeRepository).saveAll(any(Set.class));
  }

  @Test
  void patchTestCaseAttributes_MultipleTestCases_ShouldAddAttributesToAllTestCases() {
    // Given
    var testCaseAttribute1 = new TmsTestCaseAttribute();
    var testCaseAttribute2 = new TmsTestCaseAttribute();

    when(tmsAttributeService.getEntityById(PROJECT_ID, 2L)).thenReturn(tmsAttribute);
    when(tmsTestCaseAttributeMapper.createTestCaseAttribute(testCase, tmsAttribute))
        .thenReturn(testCaseAttribute1);
    when(tmsTestCaseAttributeMapper.createTestCaseAttribute(testCase2, tmsAttribute))
        .thenReturn(testCaseAttribute2);

    // Store initial states
    var initialAttributesTestCase1Size = testCase.getAttributes().size();
    var initialAttributesTestCase2Size = testCase2.getAttributes().size();

    // When
    sut.patchTestCaseAttributes(testCases, attributeRQs);

    // Then
    verify(tmsAttributeService, times(2)).getEntityById(PROJECT_ID, 2L);
    verify(tmsTestCaseAttributeMapper).createTestCaseAttribute(testCase, tmsAttribute);
    verify(tmsTestCaseAttributeMapper).createTestCaseAttribute(testCase2, tmsAttribute);
    // saveAll should be called once for each test case
    verify(tmsTestCaseAttributeRepository, times(2)).saveAll(any(Set.class));

    // Assert that both test cases have the new attributes added
    assertEquals(initialAttributesTestCase1Size + 1, testCase.getAttributes().size());
    assertEquals(initialAttributesTestCase2Size + 1, testCase2.getAttributes().size());
  }

  @Test
  void patchTestCaseAttributes_MultipleTestCases_WithNullAttributes_ShouldReturnEarly() {
    // Given
    var initialAttributesTestCase1 = new HashSet<>(testCase.getAttributes());
    var initialAttributesTestCase2 = new HashSet<>(testCase2.getAttributes());

    // When
    sut.patchTestCaseAttributes(testCases, null);

    // Then
    verifyNoInteractions(tmsAttributeService, tmsTestCaseAttributeMapper,
        tmsTestCaseAttributeRepository);

    // Assert that both test cases' attributes are unchanged
    assertEquals(initialAttributesTestCase1, testCase.getAttributes());
    assertEquals(initialAttributesTestCase2, testCase2.getAttributes());
  }

  @Test
  void patchTestCaseAttributes_MultipleTestCases_WithEmptyAttributes_ShouldReturnEarly() {
    // Given
    var initialAttributesTestCase1 = new HashSet<>(testCase.getAttributes());
    var initialAttributesTestCase2 = new HashSet<>(testCase2.getAttributes());

    // When
    sut.patchTestCaseAttributes(testCases, Collections.emptyList());

    // Then
    verifyNoInteractions(tmsAttributeService, tmsTestCaseAttributeMapper,
        tmsTestCaseAttributeRepository);

    // Assert that both test cases' attributes are unchanged
    assertEquals(initialAttributesTestCase1, testCase.getAttributes());
    assertEquals(initialAttributesTestCase2, testCase2.getAttributes());
  }

  @Test
  void patchTestCaseAttributes_MultipleTestCases_WithSingleTestCase_ShouldWorkCorrectly() {
    // Given
    var singleTestCaseList = Collections.singletonList(testCase);
    var testCaseAttribute = new TmsTestCaseAttribute();

    when(tmsAttributeService.getEntityById(PROJECT_ID, 2L)).thenReturn(tmsAttribute);
    when(tmsTestCaseAttributeMapper.createTestCaseAttribute(testCase, tmsAttribute))
        .thenReturn(testCaseAttribute);

    var initialSize = testCase.getAttributes().size();

    // When
    sut.patchTestCaseAttributes(singleTestCaseList, attributeRQs);

    // Then
    verify(tmsAttributeService).getEntityById(PROJECT_ID, 2L);
    verify(tmsTestCaseAttributeMapper).createTestCaseAttribute(testCase, tmsAttribute);
    verify(tmsTestCaseAttributeRepository, times(1)).saveAll(any(Set.class));

    // Assert the final state
    assertEquals(initialSize + 1, testCase.getAttributes().size());
  }

  @Test
  void patchTestCaseAttributes_MultipleTestCases_WithEmptyTestCasesList_ShouldNotCallRepository() {
    // Given
    var emptyTestCasesList = Collections.<TmsTestCase>emptyList();

    // When
    sut.patchTestCaseAttributes(emptyTestCasesList, attributeRQs);

    // Then
    verifyNoInteractions(tmsAttributeService);
    verifyNoInteractions(tmsTestCaseAttributeMapper);
    verifyNoInteractions(tmsTestCaseAttributeRepository);
  }

  @Test
  void patchTestCaseAttributes_MultipleTestCases_WithKeyAttribute_ShouldAddToAllTestCases() {
    // Given
    var attributeRQ = new TmsTestCaseAttributeRQ();
    attributeRQ.setKey("shared-tag");
    var attributeRQsWithKey = List.of(attributeRQ);

    var testCaseAttribute1 = new TmsTestCaseAttribute();
    var testCaseAttribute2 = new TmsTestCaseAttribute();

    when(tmsAttributeService.findOrCreateTag(PROJECT_ID, "shared-tag")).thenReturn(tmsAttribute);
    when(tmsTestCaseAttributeMapper.createTestCaseAttribute(testCase, tmsAttribute))
        .thenReturn(testCaseAttribute1);
    when(tmsTestCaseAttributeMapper.createTestCaseAttribute(testCase2, tmsAttribute))
        .thenReturn(testCaseAttribute2);

    // When
    sut.patchTestCaseAttributes(testCases, attributeRQsWithKey);

    // Then
    verify(tmsAttributeService, times(2)).findOrCreateTag(PROJECT_ID, "shared-tag");
    verify(tmsTestCaseAttributeMapper).createTestCaseAttribute(testCase, tmsAttribute);
    verify(tmsTestCaseAttributeMapper).createTestCaseAttribute(testCase2, tmsAttribute);
    verify(tmsTestCaseAttributeRepository, times(2)).saveAll(any(Set.class));
  }

  @Test
  void deleteAllByTestCaseId_ShouldCallRepositoryDelete() {
    // Given
    var testCaseId = 1L;

    // When
    sut.deleteAllByTestCaseId(testCaseId);

    // Then
    verify(tmsTestCaseAttributeRepository).deleteAllByTestCaseId(testCaseId);
  }

  @Test
  void deleteAllByTestFolderId_ShouldCallRepositoryDelete() {
    // Given
    var testFolderId = 2L;

    // When
    sut.deleteAllByTestFolderId(PROJECT_ID, testFolderId);

    // Then
    verify(tmsTestCaseAttributeRepository).deleteTestCaseAttributesByTestFolderId(PROJECT_ID,
        testFolderId);
  }

  @Test
  void deleteAllByTestCaseIds_WithValidIds_ShouldCallRepositoryDelete() {
    // Given
    var testCaseIds = Arrays.asList(1L, 2L, 3L);

    // When
    sut.deleteAllByTestCaseIds(testCaseIds);

    // Then
    verify(tmsTestCaseAttributeRepository).deleteAllByTestCaseIds(testCaseIds);
  }

  @Test
  void deleteByTestCaseIdAndAttributeIds_ShouldCallRepositoryDelete() {
    // Given
    var testCaseId = 2L;
    var attributeIds = Arrays.asList(3L, 4L, 5L);

    // When
    sut.deleteByTestCaseIdAndAttributeIds(testCaseId, attributeIds);

    // Then
    verify(tmsTestCaseAttributeRepository).deleteByTestCaseIdAndAttributeIds(testCaseId,
        attributeIds);
  }

  @Test
  void deleteByTestCaseIdAndAttributeIds_WithSingleAttribute_ShouldCallRepositoryDelete() {
    // Given
    var testCaseId = 2L;
    var attributeIds = List.of(3L);

    // When
    sut.deleteByTestCaseIdAndAttributeIds(testCaseId, attributeIds);

    // Then
    verify(tmsTestCaseAttributeRepository).deleteByTestCaseIdAndAttributeIds(testCaseId,
        attributeIds);
  }

  @Test
  void deleteByTestCaseIdAndAttributeIds_WithEmptyAttributeIds_ShouldCallRepositoryDelete() {
    // Given
    var testCaseId = 2L;
    var attributeIds = Collections.<Long>emptyList();

    // When
    sut.deleteByTestCaseIdAndAttributeIds(testCaseId, attributeIds);

    // Then
    verify(tmsTestCaseAttributeRepository).deleteByTestCaseIdAndAttributeIds(testCaseId,
        attributeIds);
  }

  @Test
  void deleteByTestCaseIdsAndAttributeIds_ShouldCallRepositoryDelete() {
    // Given
    var testCaseIds = Arrays.asList(2L, 3L, 4L);
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
    var testCaseIds = List.of(2L);
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
    var testCaseIds = Arrays.asList(2L, 3L, 4L, 5L);
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
    var testCaseIds = List.of(2L);
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
    var testCaseIds = Collections.<Long>emptyList();
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
    var testCaseIds = Arrays.asList(2L, 3L);
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
    var testCaseIds = Collections.<Long>emptyList();
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
    originalTestCase.getAttributes().forEach(originalAttribute -> {
      var duplicatedAttribute = duplicatedAttributes.stream()
          .filter(
              dup -> dup.getAttribute().getId().equals(originalAttribute.getAttribute().getId()))
          .findFirst()
          .orElse(null);
      when(tmsTestCaseAttributeMapper.duplicateTestCaseAttribute(originalAttribute, newTestCase))
          .thenReturn(duplicatedAttribute);
    });

    when(tmsTestCaseAttributeRepository.saveAll(any(Set.class))).thenReturn(
        new ArrayList<>(duplicatedAttributes));

    // When
    sut.duplicateTestCaseAttributes(originalTestCase, newTestCase);

    // Then
    verify(tmsTestCaseAttributeRepository).saveAll(any(Set.class));

    // Verify that duplicateTestCaseAttribute was called for each original attribute
    originalTestCase.getAttributes().forEach(originalAttribute -> {
      verify(tmsTestCaseAttributeMapper).duplicateTestCaseAttribute(originalAttribute, newTestCase);
    });

    assertEquals(2, newTestCase.getAttributes().size());
  }

  @Test
  void duplicateTestCaseAttributes_WithNoAttributes_ShouldNotCallMapperOrRepository() {
    // Given
    var emptyOriginalTestCase = new TmsTestCase();
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
    var emptyOriginalTestCase = new TmsTestCase();
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
    var singleAttributeOriginalTestCase = new TmsTestCase();
    singleAttributeOriginalTestCase.setId(6L);

    var singleOriginalAttribute = new TmsTestCaseAttribute();
    var singleOriginalAttributeId = new TmsTestCaseAttributeId(6L, 7L);
    singleOriginalAttribute.setId(singleOriginalAttributeId);
    var singleTmsAttribute = new TmsAttribute();
    singleTmsAttribute.setId(7L);
    singleOriginalAttribute.setAttribute(singleTmsAttribute);

    var singleAttributeSet = Set.of(singleOriginalAttribute);
    singleAttributeOriginalTestCase.setAttributes(singleAttributeSet);

    var singleDuplicatedAttribute = new TmsTestCaseAttribute();
    var singleDuplicatedAttributeId = new TmsTestCaseAttributeId(4L, 7L);
    singleDuplicatedAttribute.setId(singleDuplicatedAttributeId);
    singleDuplicatedAttribute.setAttribute(singleTmsAttribute);

    var singleDuplicatedAttributeSet = Set.of(singleDuplicatedAttribute);

    when(
        tmsTestCaseAttributeMapper.duplicateTestCaseAttribute(singleOriginalAttribute, newTestCase))
        .thenReturn(singleDuplicatedAttribute);

    when(tmsTestCaseAttributeRepository.saveAll(any(Set.class))).thenReturn(
        new ArrayList<>(singleDuplicatedAttributeSet));

    // When
    sut.duplicateTestCaseAttributes(singleAttributeOriginalTestCase, newTestCase);

    // Then
    verify(tmsTestCaseAttributeMapper).duplicateTestCaseAttribute(singleOriginalAttribute,
        newTestCase);
    verify(tmsTestCaseAttributeRepository).saveAll(any(Set.class));
    assertEquals(1, newTestCase.getAttributes().size());
  }

  // Tests for addAttributesToTestCases method

  @Test
  void addAttributesToTestCases_WithMultipleTestCasesAndAttributes_ShouldCreateAllCombinations() {
    // Given
    var testCaseIds = Arrays.asList(1L, 2L);
    Collection<Long> attributeIds = Arrays.asList(10L, 20L);

    var attr1TestCase1 = new TmsTestCaseAttribute();
    var attr2TestCase1 = new TmsTestCaseAttribute();
    var attr1TestCase2 = new TmsTestCaseAttribute();
    var attr2TestCase2 = new TmsTestCaseAttribute();

    when(tmsTestCaseAttributeMapper.createTestCaseAttribute(1L, 10L)).thenReturn(attr1TestCase1);
    when(tmsTestCaseAttributeMapper.createTestCaseAttribute(1L, 20L)).thenReturn(attr2TestCase1);
    when(tmsTestCaseAttributeMapper.createTestCaseAttribute(2L, 10L)).thenReturn(attr1TestCase2);
    when(tmsTestCaseAttributeMapper.createTestCaseAttribute(2L, 20L)).thenReturn(attr2TestCase2);

    // Order matters - matches flatMap logic
    var expectedAttributes = Arrays.asList(
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
    var testCaseIds = List.of(1L);
    Collection<Long> attributeIds = Arrays.asList(10L, 20L, 30L);

    var attr1 = new TmsTestCaseAttribute();
    var attr2 = new TmsTestCaseAttribute();
    var attr3 = new TmsTestCaseAttribute();

    when(tmsTestCaseAttributeMapper.createTestCaseAttribute(1L, 10L)).thenReturn(attr1);
    when(tmsTestCaseAttributeMapper.createTestCaseAttribute(1L, 20L)).thenReturn(attr2);
    when(tmsTestCaseAttributeMapper.createTestCaseAttribute(1L, 30L)).thenReturn(attr3);

    var expectedAttributes = Arrays.asList(attr1, attr2, attr3);

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
    var testCaseIds = Arrays.asList(1L, 2L, 3L);
    Collection<Long> attributeIds = List.of(10L);

    var attr1 = new TmsTestCaseAttribute();
    var attr2 = new TmsTestCaseAttribute();
    var attr3 = new TmsTestCaseAttribute();

    when(tmsTestCaseAttributeMapper.createTestCaseAttribute(1L, 10L)).thenReturn(attr1);
    when(tmsTestCaseAttributeMapper.createTestCaseAttribute(2L, 10L)).thenReturn(attr2);
    when(tmsTestCaseAttributeMapper.createTestCaseAttribute(3L, 10L)).thenReturn(attr3);

    var expectedAttributes = Arrays.asList(attr1, attr2, attr3);

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
    var testCaseIds = List.of(1L);
    Collection<Long> attributeIds = List.of(10L);

    var attribute = new TmsTestCaseAttribute();

    when(tmsTestCaseAttributeMapper.createTestCaseAttribute(1L, 10L)).thenReturn(attribute);

    var expectedAttributes = List.of(attribute);

    // When
    sut.addAttributesToTestCases(testCaseIds, attributeIds);

    // Then
    verify(tmsTestCaseAttributeMapper).createTestCaseAttribute(1L, 10L);
    verify(tmsTestCaseAttributeRepository).saveAll(expectedAttributes);
  }

  @Test
  void addAttributesToTestCases_WithSetAsAttributeIds_ShouldWorkCorrectly() {
    // Given
    var testCaseIds = List.of(1L);
    Collection<Long> attributeIds = Set.of(10L, 20L); // Using Set instead of List

    var attr1 = new TmsTestCaseAttribute();
    var attr2 = new TmsTestCaseAttribute();

    when(tmsTestCaseAttributeMapper.createTestCaseAttribute(1L, 10L)).thenReturn(attr1);
    when(tmsTestCaseAttributeMapper.createTestCaseAttribute(1L, 20L)).thenReturn(attr2);

    // When
    sut.addAttributesToTestCases(testCaseIds, attributeIds);

    // Then
    verify(tmsTestCaseAttributeMapper).createTestCaseAttribute(1L, 10L);
    verify(tmsTestCaseAttributeMapper).createTestCaseAttribute(1L, 20L);

    // Use ArgumentCaptor to verify the list contents since Set order may vary
    var captor = ArgumentCaptor.forClass(List.class);
    verify(tmsTestCaseAttributeRepository).saveAll(captor.capture());

    var actualAttributes = (List<TmsTestCaseAttribute>) captor.getValue();
    assertEquals(2, actualAttributes.size());
    assertTrue(actualAttributes.contains(attr1));
    assertTrue(actualAttributes.contains(attr2));
  }

  @Test
  void addAttributesToTestCases_WithLargerDataSet_ShouldHandleCorrectOrder() {
    // Given
    var testCaseIds = Arrays.asList(1L, 2L, 3L);
    Collection<Long> attributeIds = Arrays.asList(10L, 20L);

    // Create all possible combinations
    var attr10TestCase1 = new TmsTestCaseAttribute();
    var attr20TestCase1 = new TmsTestCaseAttribute();
    var attr10TestCase2 = new TmsTestCaseAttribute();
    var attr20TestCase2 = new TmsTestCaseAttribute();
    var attr10TestCase3 = new TmsTestCaseAttribute();
    var attr20TestCase3 = new TmsTestCaseAttribute();

    when(tmsTestCaseAttributeMapper.createTestCaseAttribute(1L, 10L)).thenReturn(attr10TestCase1);
    when(tmsTestCaseAttributeMapper.createTestCaseAttribute(1L, 20L)).thenReturn(attr20TestCase1);
    when(tmsTestCaseAttributeMapper.createTestCaseAttribute(2L, 10L)).thenReturn(attr10TestCase2);
    when(tmsTestCaseAttributeMapper.createTestCaseAttribute(2L, 20L)).thenReturn(attr20TestCase2);
    when(tmsTestCaseAttributeMapper.createTestCaseAttribute(3L, 10L)).thenReturn(attr10TestCase3);
    when(tmsTestCaseAttributeMapper.createTestCaseAttribute(3L, 20L)).thenReturn(attr20TestCase3);

    // Expected order according to flatMap logic
    var expectedAttributes = Arrays.asList(
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
    var testCaseIds = Collections.<Long>emptyList();
    Collection<Long> attributeIds = Collections.emptyList();

    // When
    sut.addAttributesToTestCases(testCaseIds, attributeIds);

    // Then
    verify(tmsTestCaseAttributeRepository).saveAll(Collections.emptyList());
    verifyNoInteractions(tmsTestCaseAttributeMapper);
  }

  // Tests for getAttributesByTestCaseIds method

  @Test
  void getAttributesByTestCaseIds_ShouldReturnPagedAttributes() {
    // Given
    var testCaseIds = Arrays.asList(1L, 2L);
    var attr1 = createTmsAttribute(10L, "browser", "Chrome");
    var attr2 = createTmsAttribute(11L, "priority", "High");
    var page = new PageImpl<>(List.of(attr1, attr2), pageable, 2);

    var rs1 = createTmsAttributeRS(10L, "browser", "Chrome");
    var rs2 = createTmsAttributeRS(11L, "priority", "High");

    when(tmsAttributeRepository.findDistinctByTestCaseIdsAndProjectId(PROJECT_ID, testCaseIds, pageable))
        .thenReturn(page);
    when(tmsAttributeMapper.convertToTmsAttributeRS(attr1)).thenReturn(rs1);
    when(tmsAttributeMapper.convertToTmsAttributeRS(attr2)).thenReturn(rs2);

    // When
    var result = sut.getAttributesByTestCaseIds(PROJECT_ID, testCaseIds, pageable);

    // Then
    assertEquals(2, result.getContent().size());
    var contentList = new ArrayList<>(result.getContent());
    assertEquals("browser", contentList.get(0).getKey());
    assertEquals("Chrome", contentList.get(0).getValue());
    assertEquals("priority", contentList.get(1).getKey());
    assertEquals("High", contentList.get(1).getValue());
    verify(tmsAttributeRepository).findDistinctByTestCaseIdsAndProjectId(PROJECT_ID, testCaseIds, pageable);
  }

  @Test
  void getAttributesByTestCaseIds_WithEmptyResult_ShouldReturnEmptyPage() {
    // Given
    var testCaseIds = List.of(999L);
    var emptyPage = new PageImpl<TmsAttribute>(Collections.emptyList(), pageable, 0);

    when(tmsAttributeRepository.findDistinctByTestCaseIdsAndProjectId(PROJECT_ID, testCaseIds, pageable))
        .thenReturn(emptyPage);

    // When
    var result = sut.getAttributesByTestCaseIds(PROJECT_ID, testCaseIds, pageable);

    // Then
    assertTrue(result.getContent().isEmpty());
    verify(tmsAttributeRepository).findDistinctByTestCaseIdsAndProjectId(PROJECT_ID, testCaseIds, pageable);
  }

  @Test
  void getAttributesByTestCaseIds_WithSingleTestCaseId_ShouldReturnAttributes() {
    // Given
    var testCaseIds = List.of(1L);
    var attr = createTmsAttribute(10L, "os", "Linux");
    var page = new PageImpl<>(List.of(attr), pageable, 1);

    var rs = createTmsAttributeRS(10L, "os", "Linux");

    when(tmsAttributeRepository.findDistinctByTestCaseIdsAndProjectId(PROJECT_ID, testCaseIds, pageable))
        .thenReturn(page);
    when(tmsAttributeMapper.convertToTmsAttributeRS(attr)).thenReturn(rs);

    // When
    var result = sut.getAttributesByTestCaseIds(PROJECT_ID, testCaseIds, pageable);

    // Then
    assertEquals(1, result.getContent().size());
    var contentList = new ArrayList<>(result.getContent());
    assertEquals("os", contentList.get(0).getKey());
    assertEquals("Linux", contentList.get(0).getValue());
    verify(tmsAttributeRepository).findDistinctByTestCaseIdsAndProjectId(PROJECT_ID, testCaseIds, pageable);
  }

  @Test
  void getAttributesByTestCaseIds_ShouldPassCorrectPageable() {
    // Given
    var testCaseIds = List.of(1L);
    var customPageable = PageRequest.of(2, 5);
    var emptyPage = new PageImpl<TmsAttribute>(Collections.emptyList(), customPageable, 0);

    when(tmsAttributeRepository.findDistinctByTestCaseIdsAndProjectId(PROJECT_ID, testCaseIds, customPageable))
        .thenReturn(emptyPage);

    // When
    sut.getAttributesByTestCaseIds(PROJECT_ID, testCaseIds, customPageable);

    // Then
    verify(tmsAttributeRepository).findDistinctByTestCaseIdsAndProjectId(PROJECT_ID, testCaseIds, customPageable);
  }

  @Test
  void getAttributesByTestCaseIds_WithMultiplePages_ShouldReturnCorrectPageMetadata() {
    // Given
    var testCaseIds = Arrays.asList(1L, 2L, 3L);
    var customPageable = PageRequest.of(0, 2);
    var attr1 = createTmsAttribute(10L, "key1", "value1");
    var attr2 = createTmsAttribute(11L, "key2", "value2");
    var page = new PageImpl<>(List.of(attr1, attr2), customPageable, 5);

    var rs1 = createTmsAttributeRS(10L, "key1", "value1");
    var rs2 = createTmsAttributeRS(11L, "key2", "value2");

    when(tmsAttributeRepository.findDistinctByTestCaseIdsAndProjectId(PROJECT_ID, testCaseIds, customPageable))
        .thenReturn(page);
    when(tmsAttributeMapper.convertToTmsAttributeRS(attr1)).thenReturn(rs1);
    when(tmsAttributeMapper.convertToTmsAttributeRS(attr2)).thenReturn(rs2);

    // When
    var result = sut.getAttributesByTestCaseIds(PROJECT_ID, testCaseIds, customPageable);

    // Then
    assertEquals(2, result.getContent().size());
    assertEquals(5, result.getPage().getTotalElements());
    assertEquals(3, result.getPage().getTotalPages());
    verify(tmsAttributeRepository).findDistinctByTestCaseIdsAndProjectId(PROJECT_ID, testCaseIds, customPageable);
  }

  private TmsAttribute createTmsAttribute(Long id, String key, String value) {
    var attr = new TmsAttribute();
    attr.setId(id);
    attr.setKey(key);
    attr.setValue(value);
    var project = new Project();
    project.setId(PROJECT_ID);
    attr.setProject(project);
    return attr;
  }

  private TmsAttributeRS createTmsAttributeRS(Long id, String key, String value) {
    var rs = new TmsAttributeRS();
    rs.setId(id);
    rs.setKey(key);
    rs.setValue(value);
    return rs;
  }
}