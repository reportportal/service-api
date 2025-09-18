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

import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.ws.converter.utils.ResourceUpdater;
import com.epam.ta.reportportal.ws.converter.utils.ResourceUpdaterProvider;
import com.epam.ta.reportportal.ws.converter.utils.item.content.TestItemUpdaterContent;
import com.epam.ta.reportportal.ws.converter.utils.item.updater.NestedStepsUpdater;
import com.epam.ta.reportportal.ws.reporting.TestItemResource;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NestedStepsUpdaterProvider
    implements ResourceUpdaterProvider<TestItemUpdaterContent, TestItemResource> {

  private final TestItemRepository testItemRepository;

  @Autowired
  public NestedStepsUpdaterProvider(TestItemRepository testItemRepository) {
    this.testItemRepository = testItemRepository;
  }

  @Override
  public ResourceUpdater<TestItemResource> retrieve(TestItemUpdaterContent updaterContent) {
    var itemIds = updaterContent.getTestItems().stream()
        .map(TestItem::getItemId)
        .toList();
    var parents = testItemRepository.findParentsWithNestedSteps(itemIds);
    var hasNested = new HashSet<>(parents);
    Map<Long, Boolean> mapping = HashMap.newHashMap(itemIds.size());
    for (Long id : itemIds) {
      mapping.put(id, hasNested.contains(id));
    }
    return NestedStepsUpdater.of(mapping);
  }
}
