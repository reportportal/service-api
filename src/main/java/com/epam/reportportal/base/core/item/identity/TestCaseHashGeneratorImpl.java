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

package com.epam.reportportal.base.core.item.identity;

import com.epam.reportportal.base.infrastructure.persistence.dao.TestItemRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.item.TestItem;
import com.google.api.client.util.Lists;
import com.google.common.base.Strings;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * Test case id hash for analytics and de-duplication.
 *
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@Service
public class TestCaseHashGeneratorImpl implements TestCaseHashGenerator {

  private final TestItemRepository testItemRepository;

  public TestCaseHashGeneratorImpl(TestItemRepository testItemRepository) {
    this.testItemRepository = testItemRepository;
  }

  @Override
  public Integer generate(TestItem item, List<Long> parentIds, Long projectId) {
    return generate(item, parentIds, projectId, null);
  }

  @Override
  public Integer generate(TestItem item, List<Long> parentIds, Long projectId,
      Map<Long, String> itemNamesCache) {
    return prepare(item, parentIds, projectId, itemNamesCache).hashCode();
  }

  private String prepare(TestItem item, List<Long> parentIds, Long projectId,
      Map<Long, String> itemNamesCache) {
    List<CharSequence> elements = Lists.newArrayList();

    elements.add(projectId.toString());
    getPathNames(parentIds, itemNamesCache).stream().filter(StringUtils::isNotEmpty).forEach(elements::add);
    elements.add(item.getName());
    item.getParameters()
        .stream()
        .map(parameter ->
            (!Strings.isNullOrEmpty(parameter.getKey()) ? parameter.getKey() + "=" : "")
                + parameter.getValue())
        .forEach(elements::add);

    return String.join(";", elements);
  }

  private List<String> getPathNames(List<Long> parentIds, Map<Long, String> itemNamesCache) {
    if (CollectionUtils.isEmpty(parentIds)) {
      return Collections.emptyList();
    }
    if (itemNamesCache != null) {
      List<Long> missingIds = parentIds.stream()
          .filter(id -> !itemNamesCache.containsKey(id))
          .toList();
      if (!missingIds.isEmpty()) {
        testItemRepository.findAllById(missingIds)
            .forEach(ti -> itemNamesCache.put(ti.getItemId(), ti.getName()));
      }
      return parentIds.stream()
          .sorted(Comparator.naturalOrder())
          .map(itemNamesCache::get)
          .filter(Objects::nonNull)
          .collect(Collectors.toList());
    }
    return testItemRepository.findAllById(parentIds)
        .stream()
        .sorted(Comparator.comparingLong(TestItem::getItemId))
        .map(TestItem::getName)
        .collect(Collectors.toList());
  }
}
