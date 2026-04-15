package com.epam.reportportal.base.core.tms.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.reportportal.base.core.item.identity.TestCaseHashGenerator;
import com.epam.reportportal.base.core.tms.dto.TmsTestCaseAttributeRS;
import com.epam.reportportal.base.core.tms.dto.TmsTestCaseRS;
import com.epam.reportportal.base.core.tms.mapper.TestCaseItemBuilder;
import com.epam.reportportal.base.infrastructure.persistence.dao.ItemAttributeRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.TestItemRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.ItemAttribute;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.StatusEnum;
import com.epam.reportportal.base.infrastructure.persistence.entity.item.TestItem;
import com.epam.reportportal.base.infrastructure.persistence.entity.launch.Launch;
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
class TestCaseItemServiceImplTest {

  private final Long launchId = 100L;
  private final Long projectId = 1L;
  private final Long suiteItemId = 200L;
  @Mock
  private TestItemRepository testItemRepository;
  @Mock
  private ItemAttributeRepository itemAttributeRepository;
  @Mock
  private TestCaseItemBuilder testCaseItemBuilder;
  @Mock
  private TestCaseHashGenerator testCaseHashGenerator;
  @InjectMocks
  private TestCaseItemServiceImpl sut;
  @Captor
  private ArgumentCaptor<List<ItemAttribute>> listCaptor;
  private Launch launch;
  private TestItem suiteItem;
  private TmsTestCaseRS testCaseRS;

  @BeforeEach
  void setUp() {
    launch = new Launch();
    launch.setId(launchId);
    launch.setProjectId(projectId);

    suiteItem = new TestItem();
    suiteItem.setItemId(suiteItemId);
    suiteItem.setPath(String.valueOf(suiteItemId));

    testCaseRS = new TmsTestCaseRS();
    testCaseRS.setId(300L);
    testCaseRS.setName("Test Case Name");
  }

  // -------------------------------------------------------------------------
  // CREATE TEST CASE ITEM
  // -------------------------------------------------------------------------

  @Test
  void createTestCaseItem_WithoutAttributes_ShouldCreateAndSave() {
    testCaseRS.setAttributes(Collections.emptySet());

    TestItem testItem = new TestItem();
    testItem.setItemId(400L);

    when(testCaseItemBuilder.buildTestCaseItem(testCaseRS, suiteItem, launch))
        .thenReturn(testItem);
    when(testCaseHashGenerator.generate(any(), anyList(), any())).thenReturn(12345);
    when(testItemRepository.save(testItem)).thenReturn(testItem);

    var result = sut.createTestCaseItem(testCaseRS, suiteItem, launch);

    assertNotNull(result);
    assertEquals("200.400", result.getPath());
    assertEquals(12345, result.getTestCaseHash());
    assertNotNull(result.getItemResults());
    assertEquals(StatusEnum.TO_RUN, result.getItemResults().getStatus());

    verify(testCaseItemBuilder).buildTestCaseItem(testCaseRS, suiteItem, launch);
    verify(testItemRepository).save(testItem);
    verify(itemAttributeRepository, never()).saveAll(anyList());
  }

  @Test
  void createTestCaseItem_WithAttributes_ShouldMapAndSaveAttributes() {
    TmsTestCaseAttributeRS attributeRS = new TmsTestCaseAttributeRS();
    attributeRS.setKey("P1"); // Should be mapped to value, key becomes "tag"
    testCaseRS.setAttributes(Set.of(attributeRS));

    TestItem testItem = new TestItem();
    testItem.setItemId(400L);
    testItem.setAttributes(new HashSet<>());

    when(testCaseItemBuilder.buildTestCaseItem(testCaseRS, suiteItem, launch))
        .thenReturn(testItem);
    when(testCaseHashGenerator.generate(any(), anyList(), any())).thenReturn(54321);
    when(testItemRepository.save(testItem)).thenReturn(testItem);

    var result = sut.createTestCaseItem(testCaseRS, suiteItem, launch);

    assertNotNull(result);
    assertEquals("200.400", result.getPath());

    verify(testItemRepository).save(testItem);
    verify(itemAttributeRepository).saveAll(listCaptor.capture());

    List<ItemAttribute> savedAttributes = listCaptor.getValue();
    assertEquals(1, savedAttributes.size());

    ItemAttribute attr = savedAttributes.get(0);
    assertEquals("tag", attr.getKey());
    assertEquals("P1", attr.getValue());
    assertFalse(attr.isSystem());
    assertEquals(testItem, attr.getTestItem());

    // Also assert that the attribute was added to the test item's collection
    assertEquals(1, testItem.getAttributes().size());
  }

  // -------------------------------------------------------------------------
  // MARK AS HAVING CHILDREN
  // -------------------------------------------------------------------------

  @Test
  void markAsHavingNestedChildren_WhenFalse_ShouldUpdateAndSave() {
    TestItem item = new TestItem();
    item.setHasChildren(false);

    sut.markAsHavingNestedChildren(item);

    assertTrue(item.isHasChildren());
    verify(testItemRepository).save(item);
  }

  @Test
  void markAsHavingNestedChildren_WhenAlreadyTrue_ShouldDoNothing() {
    TestItem item = new TestItem();
    item.setHasChildren(true);

    sut.markAsHavingNestedChildren(item);

    assertTrue(item.isHasChildren());
    verify(testItemRepository, never()).save(any());
  }
}
