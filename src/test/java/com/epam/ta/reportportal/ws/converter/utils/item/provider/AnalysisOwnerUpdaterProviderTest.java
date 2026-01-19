/*
 * Copyright 2025 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.ta.reportportal.ws.converter.utils.item.provider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.ta.reportportal.dao.UserRepository;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.user.UserIdDisplayNameProjection;
import com.epam.ta.reportportal.ws.converter.utils.ResourceUpdater;
import com.epam.ta.reportportal.ws.converter.utils.item.content.TestItemUpdaterContent;
import com.epam.ta.reportportal.ws.reporting.TestItemResource;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * @author Evelina Sarkisian
 */
@ExtendWith(MockitoExtension.class)
class AnalysisOwnerUpdaterProviderTest {

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private AnalysisOwnerUpdaterProvider provider;

  @Test
  void retrieveShouldReturnUpdaterWithEmptyMapWhenNoItemsHaveAnalysisOwner() {
    TestItem item1 = createTestItem(1L, null);
    TestItem item2 = createTestItem(2L, null);
    TestItemUpdaterContent content = createUpdaterContent(Arrays.asList(item1, item2));

    ResourceUpdater<TestItemResource> updater = provider.retrieve(content);

    assertNotNull(updater);
    verify(userRepository, never()).findDisplayNamesByIds(anyList());
  }

  @Test
  void retrieveShouldFetchUserFullNamesForItemsWithAnalysisOwner() {
    TestItem item1 = createTestItem(1L, 100L);
    TestItem item2 = createTestItem(2L, 200L);
    TestItemUpdaterContent content = createUpdaterContent(Arrays.asList(item1, item2));

    List<UserIdDisplayNameProjection> userProjections = Arrays.asList(
        new UserIdDisplayNameProjection(100L, "John Doe"),
        new UserIdDisplayNameProjection(200L, "Jane Smith")
    );

    when(userRepository.findDisplayNamesByIds(Arrays.asList(100L, 200L)))
        .thenReturn(userProjections);

    ResourceUpdater<TestItemResource> updater = provider.retrieve(content);

    assertNotNull(updater);
    verify(userRepository, times(1)).findDisplayNamesByIds(Arrays.asList(100L, 200L));

    TestItemResource resource1 = createTestItemResource(1L);
    TestItemResource resource2 = createTestItemResource(2L);
    updater.updateResource(resource1);
    updater.updateResource(resource2);

    assertEquals("John Doe", resource1.getAnalysisOwner());
    assertEquals("Jane Smith", resource2.getAnalysisOwner());
  }

  @Test
  void retrieveShouldHandleMixedItemsWithAndWithoutAnalysisOwner() {
    TestItem item1 = createTestItem(1L, 100L);
    TestItem item2 = createTestItem(2L, null);
    TestItem item3 = createTestItem(3L, 200L);
    TestItemUpdaterContent content = createUpdaterContent(Arrays.asList(item1, item2, item3));

    List<UserIdDisplayNameProjection> userProjections = Arrays.asList(
        new UserIdDisplayNameProjection(100L, "John Doe"),
        new UserIdDisplayNameProjection(200L, "Jane Smith")
    );

    when(userRepository.findDisplayNamesByIds(Arrays.asList(100L, 200L)))
        .thenReturn(userProjections);

    ResourceUpdater<TestItemResource> updater = provider.retrieve(content);

    assertNotNull(updater);
    verify(userRepository, times(1)).findDisplayNamesByIds(Arrays.asList(100L, 200L));

    TestItemResource resource1 = createTestItemResource(1L);
    TestItemResource resource2 = createTestItemResource(2L);
    TestItemResource resource3 = createTestItemResource(3L);
    updater.updateResource(resource1);
    updater.updateResource(resource2);
    updater.updateResource(resource3);

    assertEquals("John Doe", resource1.getAnalysisOwner());
    assertNull(resource2.getAnalysisOwner());
    assertEquals("Jane Smith", resource3.getAnalysisOwner());
  }

  @Test
  void retrieveShouldReturnDeletedUserWhenUserNotFound() {
    TestItem item1 = createTestItem(1L, 100L);
    TestItem item2 = createTestItem(2L, 999L);
    TestItemUpdaterContent content = createUpdaterContent(Arrays.asList(item1, item2));

    List<UserIdDisplayNameProjection> userProjections = List.of(
        new UserIdDisplayNameProjection(100L, "John Doe")
    );
    when(userRepository.findDisplayNamesByIds(Arrays.asList(100L, 999L)))
        .thenReturn(userProjections);

    ResourceUpdater<TestItemResource> updater = provider.retrieve(content);

    assertNotNull(updater);
    verify(userRepository, times(1)).findDisplayNamesByIds(Arrays.asList(100L, 999L));

    TestItemResource resource1 = createTestItemResource(1L);
    TestItemResource resource2 = createTestItemResource(2L);
    updater.updateResource(resource1);
    updater.updateResource(resource2);

    assertEquals("John Doe", resource1.getAnalysisOwner());
    assertEquals("deleted_user", resource2.getAnalysisOwner());
  }

  @Test
  void retrieveShouldHandleEmptyTestItemsList() {
    TestItemUpdaterContent content = createUpdaterContent(Collections.emptyList());

    ResourceUpdater<TestItemResource> updater = provider.retrieve(content);

    assertNotNull(updater);
    verify(userRepository, never()).findDisplayNamesByIds(anyList());
  }

  private TestItem createTestItem(Long itemId, Long analysisOwnerId) {
    TestItem item = new TestItem();
    item.setItemId(itemId);
    item.setAnalysisOwnerId(analysisOwnerId);
    return item;
  }

  private TestItemUpdaterContent createUpdaterContent(List<TestItem> testItems) {
    return TestItemUpdaterContent.of(1L, testItems);
  }

  private TestItemResource createTestItemResource(Long itemId) {
    TestItemResource resource = new TestItemResource();
    resource.setItemId(itemId);
    return resource;
  }
}
