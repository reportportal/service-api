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
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service to provide a {@link ResourceUpdater} for test items that checks and maps whether they have nested steps.
 */
@Service
public class NestedStepsUpdaterProvider
    implements ResourceUpdaterProvider<TestItemUpdaterContent, TestItemResource> {

  private final TestItemRepository testItemRepository;

  /**
   * Constructs the {@code NestedStepsUpdaterProvider}.
   *
   * @param testItemRepository The repository used for fetching test item data related to nested steps.
   */
  @Autowired
  public NestedStepsUpdaterProvider(TestItemRepository testItemRepository) {
    this.testItemRepository = testItemRepository;
  }

  /**
   * Retrieves and constructs a {@code ResourceUpdater} for working with test items, mapping each item's ID to a flag
   * indicating whether it has nested steps.
   *
   * @param updaterContent The content containing test items to update.
   * @return A {@code ResourceUpdater} tailored to handle nested step mappings.
   */
  public ResourceUpdater<TestItemResource> retrieve(TestItemUpdaterContent updaterContent) {
    var itemIds = updaterContent.getTestItems().stream()
        .map(TestItem::getItemId)
        .toList();

    var hasNestedSteps = testItemRepository.findParentsWithNestedSteps(itemIds);
    var mapping = itemIds.stream()
        .collect(Collectors.toMap(
            id -> id,
            hasNestedSteps::contains
        ));
    return NestedStepsUpdater.of(mapping);
  }
}
