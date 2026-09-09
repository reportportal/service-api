/*
 * Copyright 2020 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.reportportal.base.core.item.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.reportportal.base.core.item.identity.IdentityUtil;
import com.epam.reportportal.base.core.item.identity.TestCaseHashGeneratorImpl;
import com.epam.reportportal.base.infrastructure.persistence.dao.TestItemRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.item.Parameter;
import com.epam.reportportal.base.infrastructure.persistence.entity.item.TestItem;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@ExtendWith(MockitoExtension.class)
class TestCaseHashGeneratorImplTest {

  @Mock
  private TestItemRepository testItemRepository;

  @InjectMocks
  private TestCaseHashGeneratorImpl testCaseHashGenerator;

  @Test
  void sameHashesForSameObjectsTest() {
    TestItem item = getItem();
    item.setItemId(3L);
    item.setPath("1.2.3");

    Map<Long, String> pathNames = new LinkedHashMap<>();
    pathNames.put(1L, "suite");
    pathNames.put(2L, "test");

    List<TestItem> parents = pathNames.entrySet().stream().map(entry -> {
      TestItem parent = new TestItem();
      parent.setItemId(entry.getKey());
      parent.setName(entry.getValue());
      return parent;
    }).collect(Collectors.toList());

    final List<Long> parentIds = IdentityUtil.getParentIds(item);

    when(testItemRepository.findAllById(parentIds)).thenReturn(parents);

    Integer first = testCaseHashGenerator.generate(item, parentIds, 100L);
    Integer second = testCaseHashGenerator.generate(item, parentIds, 100L);

    assertNotNull(first);
    assertNotNull(second);
    assertEquals(first, second);
  }

  @Test
  void hashGenerationWithCacheTest() {
    TestItem item = getItem();
    item.setItemId(3L);
    item.setPath("1.2.3");

    Map<Long, String> itemNamesCache = new HashMap<>();
    itemNamesCache.put(1L, "suite");
    itemNamesCache.put(2L, "test");

    final List<Long> parentIds = IdentityUtil.getParentIds(item);

    Integer hash1 = testCaseHashGenerator.generate(item, parentIds, 100L, itemNamesCache);
    Integer hash2 = testCaseHashGenerator.generate(item, parentIds, 100L, itemNamesCache);

    assertNotNull(hash1);
    assertEquals(hash1, hash2);
    verify(testItemRepository, never()).findAllById(any());
  }

  @Test
  void hashGenerationWithPartialCacheTest() {
    TestItem item = getItem();
    item.setItemId(3L);
    item.setPath("1.2.3");

    Map<Long, String> itemNamesCache = new HashMap<>();
    itemNamesCache.put(1L, "suite");
    // 2L is missing from cache

    TestItem parent2 = new TestItem();
    parent2.setItemId(2L);
    parent2.setName("test");

    when(testItemRepository.findAllById(List.of(2L))).thenReturn(List.of(parent2));

    final List<Long> parentIds = IdentityUtil.getParentIds(item);

    Integer hash1 = testCaseHashGenerator.generate(item, parentIds, 100L, itemNamesCache);

    assertNotNull(hash1);
    assertEquals("test", itemNamesCache.get(2L));
    verify(testItemRepository).findAllById(List.of(2L));
  }

  private TestItem getItem() {
    TestItem item = new TestItem();
    item.setName("item");
    HashSet<Parameter> parameters = new HashSet<>();
    Parameter parameter = new Parameter();
    parameter.setKey("key");
    parameter.setValue("value");
    parameters.add(parameter);
    item.setParameters(parameters);
    item.setPath("1.2.3");
    item.setLaunchId(1L);
    return item;
  }
}
