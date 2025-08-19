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
import com.epam.ta.reportportal.core.tms.dto.TmsAttributeRQ;
import com.epam.ta.reportportal.core.tms.mapper.TmsTestCaseAttributeMapper;
import java.util.ArrayList;
import java.util.Arrays;
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
class TmsTestCaseAttributeServiceImplTest {

  @Mock
  private TmsTestCaseAttributeMapper tmsTestCaseAttributeMapper;

  @Mock
  private TmsTestCaseAttributeRepository tmsTestCaseAttributeRepository;

  @Mock
  private TmsAttributeService tmsAttributeService;

  @InjectMocks
  private TmsTestCaseAttributeServiceImpl sut;

  private TmsTestCase testCase;
  private TmsTestCase testCase2;
  private List<TmsTestCase> testCases;
  private List<TmsAttributeRQ> attributeRQs;
  private Set<TmsTestCaseAttribute> testCaseAttributes;
  private Set<TmsTestCaseAttribute> existingAttributes;
  private Map<String, TmsAttribute> tmsAttributesMap;

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

    testCase.setTags(new HashSet<>(existingAttributes));
    testCase2.setTags(new HashSet<>());

    // Setup attribute requests
    attributeRQs = new ArrayList<>();
    TmsAttributeRQ attributeRQ = new TmsAttributeRQ();
    attributeRQ.setValue("new-value");
    attributeRQ.setId(2L);
    attributeRQs.add(attributeRQ);

    // Setup TmsAttribute map returned by TmsAttributeService
    TmsAttribute tmsAttribute = new TmsAttribute();
    tmsAttribute.setId(2L);
    tmsAttribute.setKey("test-key");
    tmsAttributesMap = Map.of("id:2", tmsAttribute);

    // Setup test case attributes
    testCaseAttributes = new HashSet<>();
    TmsTestCaseAttribute attribute = new TmsTestCaseAttribute();

    // Create and set the composite ID
    TmsTestCaseAttributeId attributeId = new TmsTestCaseAttributeId(1L, 2L);
    attribute.setId(attributeId);

    // Set the attribute
    attribute.setAttribute(tmsAttribute);
    attribute.setValue("new-value");
    testCaseAttributes.add(attribute);
  }

  @Test
  void createTestCaseAttributes_ShouldCreateAndSaveAttributes() {
    // Given
    when(tmsAttributeService.getTmsAttributes(attributeRQs)).thenReturn(tmsAttributesMap);
    when(tmsTestCaseAttributeMapper.convertToTmsTestCaseAttributes(tmsAttributesMap, attributeRQs))
        .thenReturn(testCaseAttributes);

    // When
    sut.createTestCaseAttributes(testCase, attributeRQs);

    // Then
    verify(tmsAttributeService).getTmsAttributes(attributeRQs);
    verify(tmsTestCaseAttributeMapper).convertToTmsTestCaseAttributes(tmsAttributesMap,
        attributeRQs);
    verify(tmsTestCaseAttributeRepository).saveAll(testCaseAttributes);

    // Assert the final state
    assertEquals(testCaseAttributes, testCase.getTags());
  }

  @Test
  void updateTestCaseAttributes_ShouldDeleteOldAndCreateNewAttributes() {
    // Given
    when(tmsAttributeService.getTmsAttributes(attributeRQs)).thenReturn(tmsAttributesMap);
    when(tmsTestCaseAttributeMapper.convertToTmsTestCaseAttributes(tmsAttributesMap, attributeRQs))
        .thenReturn(testCaseAttributes);

    // When
    sut.updateTestCaseAttributes(testCase, attributeRQs);

    // Then
    verify(tmsTestCaseAttributeRepository).deleteAllByTestCaseId(testCase.getId());
    verify(tmsAttributeService).getTmsAttributes(attributeRQs);
    verify(tmsTestCaseAttributeMapper).convertToTmsTestCaseAttributes(tmsAttributesMap,
        attributeRQs);
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
    verifyNoInteractions(tmsAttributeService, tmsTestCaseAttributeMapper);

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
    verifyNoInteractions(tmsAttributeService, tmsTestCaseAttributeMapper);

    // Assert that the test case's tags are unchanged from the initial state
    // since createTestCaseAttributes with empty list will return early
    assertEquals(existingAttributes, testCase.getTags());
  }

  @Test
  void patchTestCaseAttributes_SingleTestCase_ShouldAddNewAttributesWithoutRemovingExisting() {
    // Given
    when(tmsAttributeService.getTmsAttributes(attributeRQs)).thenReturn(tmsAttributesMap);
    when(tmsTestCaseAttributeMapper.convertToTmsTestCaseAttributes(tmsAttributesMap, attributeRQs))
        .thenReturn(testCaseAttributes);

    // Make a copy of the existing attributes to verify they're still present after patching
    Set<TmsTestCaseAttribute> initialAttributes = new HashSet<>(testCase.getTags());

    // When
    sut.patchTestCaseAttributes(testCase, attributeRQs);

    // Then
    verify(tmsAttributeService).getTmsAttributes(attributeRQs);
    verify(tmsTestCaseAttributeMapper).convertToTmsTestCaseAttributes(tmsAttributesMap,
        attributeRQs);
    verify(tmsTestCaseAttributeRepository).saveAll(testCaseAttributes);

    // Assert the final state - should contain both existing and new attributes
    assertEquals(2, testCase.getTags().size());
    assertTrue(testCase.getTags().containsAll(initialAttributes));
    assertTrue(testCase.getTags().containsAll(testCaseAttributes));
  }

  @Test
  void patchTestCaseAttributes_SingleTestCase_WithNullAttributes_ShouldReturnEarly() {
    // Given
    Set<TmsTestCaseAttribute> initialAttributes = new HashSet<>(testCase.getTags());

    // When
    sut.patchTestCaseAttributes(testCase, null);

    // Then
    verifyNoInteractions(tmsAttributeService, tmsTestCaseAttributeMapper,
        tmsTestCaseAttributeRepository);

    // Assert that the existing attributes are unchanged
    assertEquals(initialAttributes, testCase.getTags());
    assertEquals(initialAttributes.size(), testCase.getTags().size());
  }

  @Test
  void patchTestCaseAttributes_SingleTestCase_WithEmptyAttributes_ShouldReturnEarly() {
    // Given
    Set<TmsTestCaseAttribute> initialAttributes = new HashSet<>(testCase.getTags());

    // When
    sut.patchTestCaseAttributes(testCase, Collections.emptyList());

    // Then
    verifyNoInteractions(tmsAttributeService, tmsTestCaseAttributeMapper,
        tmsTestCaseAttributeRepository);

    // Assert that the existing attributes are unchanged
    assertEquals(initialAttributes, testCase.getTags());
    assertEquals(initialAttributes.size(), testCase.getTags().size());
  }

  @Test
  void patchTestCaseAttributes_MultipleTestCases_ShouldAddAttributesToAllTestCases() {
    // Given
    when(tmsAttributeService.getTmsAttributes(attributeRQs)).thenReturn(tmsAttributesMap);
    when(tmsTestCaseAttributeMapper.convertToTmsTestCaseAttributes(tmsAttributesMap, attributeRQs))
        .thenReturn(testCaseAttributes);

    // Store initial states
    Set<TmsTestCaseAttribute> initialAttributesTestCase1 = new HashSet<>(testCase.getTags());
    Set<TmsTestCaseAttribute> initialAttributesTestCase2 = new HashSet<>(testCase2.getTags());

    // When
    sut.patchTestCaseAttributes(testCases, attributeRQs);

    // Then
    verify(tmsAttributeService).getTmsAttributes(attributeRQs);
    verify(tmsTestCaseAttributeMapper).convertToTmsTestCaseAttributes(tmsAttributesMap,
        attributeRQs);
    // saveAll should be called once for each test case
    verify(tmsTestCaseAttributeRepository, times(2)).saveAll(testCaseAttributes);

    // Assert that both test cases have the new attributes added
    assertTrue(testCase.getTags().containsAll(initialAttributesTestCase1));
    assertTrue(testCase.getTags().containsAll(testCaseAttributes));
    assertTrue(testCase2.getTags().containsAll(initialAttributesTestCase2));
    assertTrue(testCase2.getTags().containsAll(testCaseAttributes));
  }

  @Test
  void patchTestCaseAttributes_MultipleTestCases_WithNullAttributes_ShouldReturnEarly() {
    // Given
    Set<TmsTestCaseAttribute> initialAttributesTestCase1 = new HashSet<>(testCase.getTags());
    Set<TmsTestCaseAttribute> initialAttributesTestCase2 = new HashSet<>(testCase2.getTags());

    // When
    sut.patchTestCaseAttributes(testCases, null);

    // Then
    verifyNoInteractions(tmsAttributeService, tmsTestCaseAttributeMapper,
        tmsTestCaseAttributeRepository);

    // Assert that both test cases' attributes are unchanged
    assertEquals(initialAttributesTestCase1, testCase.getTags());
    assertEquals(initialAttributesTestCase2, testCase2.getTags());
  }

  @Test
  void patchTestCaseAttributes_MultipleTestCases_WithEmptyAttributes_ShouldReturnEarly() {
    // Given
    Set<TmsTestCaseAttribute> initialAttributesTestCase1 = new HashSet<>(testCase.getTags());
    Set<TmsTestCaseAttribute> initialAttributesTestCase2 = new HashSet<>(testCase2.getTags());

    // When
    sut.patchTestCaseAttributes(testCases, Collections.emptyList());

    // Then
    verifyNoInteractions(tmsAttributeService, tmsTestCaseAttributeMapper,
        tmsTestCaseAttributeRepository);

    // Assert that both test cases' attributes are unchanged
    assertEquals(initialAttributesTestCase1, testCase.getTags());
    assertEquals(initialAttributesTestCase2, testCase2.getTags());
  }

  @Test
  void patchTestCaseAttributes_MultipleTestCases_WithSingleTestCase_ShouldWorkCorrectly() {
    // Given
    List<TmsTestCase> singleTestCaseList = Collections.singletonList(testCase);
    when(tmsAttributeService.getTmsAttributes(attributeRQs)).thenReturn(tmsAttributesMap);
    when(tmsTestCaseAttributeMapper.convertToTmsTestCaseAttributes(tmsAttributesMap, attributeRQs))
        .thenReturn(testCaseAttributes);

    Set<TmsTestCaseAttribute> initialAttributes = new HashSet<>(testCase.getTags());

    // When
    sut.patchTestCaseAttributes(singleTestCaseList, attributeRQs);

    // Then
    verify(tmsAttributeService).getTmsAttributes(attributeRQs);
    verify(tmsTestCaseAttributeMapper).convertToTmsTestCaseAttributes(tmsAttributesMap,
        attributeRQs);
    verify(tmsTestCaseAttributeRepository, times(1)).saveAll(testCaseAttributes);

    // Assert the final state
    assertTrue(testCase.getTags().containsAll(initialAttributes));
    assertTrue(testCase.getTags().containsAll(testCaseAttributes));
  }

  @Test
  void patchTestCaseAttributes_MultipleTestCases_WithEmptyTestCasesList_ShouldNotCallRepository() {
    // Given
    List<TmsTestCase> emptyTestCasesList = Collections.emptyList();
    when(tmsAttributeService.getTmsAttributes(attributeRQs)).thenReturn(tmsAttributesMap);
    when(tmsTestCaseAttributeMapper.convertToTmsTestCaseAttributes(tmsAttributesMap, attributeRQs))
        .thenReturn(testCaseAttributes);

    // When
    sut.patchTestCaseAttributes(emptyTestCasesList, attributeRQs);

    // Then
    verify(tmsAttributeService).getTmsAttributes(attributeRQs);
    verify(tmsTestCaseAttributeMapper).convertToTmsTestCaseAttributes(tmsAttributesMap,
        attributeRQs);
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
    List<Long> attributeIds = Arrays.asList(5L, 6L, 7L);

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
    List<Long> attributeIds = List.of(3L);

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
    List<Long> attributeIds = List.of(6L);

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
    List<Long> attributeIds = Arrays.asList(3L, 4L, 5L, 6L);

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
    List<Long> attributeIds = Arrays.asList(3L, 4L);

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
    List<Long> attributeIds = Collections.emptyList();

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
    List<Long> attributeIds = Collections.emptyList();

    // When
    sut.deleteByTestCaseIdsAndAttributeIds(testCaseIds, attributeIds);

    // Then
    verify(tmsTestCaseAttributeRepository).deleteByTestCaseIdsAndAttributeIds(testCaseIds,
        attributeIds);
  }
}
