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

package com.epam.reportportal.ws.converter.utils.item.provider;

import static com.epam.reportportal.ws.converter.converters.LaunchConverter.DELETED_USER;

import com.epam.reportportal.infrastructure.persistence.dao.UserRepository;
import com.epam.reportportal.infrastructure.persistence.entity.item.TestItem;
import com.epam.reportportal.infrastructure.persistence.entity.user.UserIdDisplayNameProjection;
import com.epam.reportportal.reporting.TestItemResource;
import com.epam.reportportal.ws.converter.utils.ResourceUpdater;
import com.epam.reportportal.ws.converter.utils.ResourceUpdaterProvider;
import com.epam.reportportal.ws.converter.utils.item.content.TestItemUpdaterContent;
import com.epam.reportportal.ws.converter.utils.item.updater.AnalysisOwnerUpdater;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Service to provide a {@link ResourceUpdater} for test items that populates analysis owner information. Performs batch
 * fetching of user full names (or logins) for optimal performance.
 *
 */
@Service
@RequiredArgsConstructor
public class AnalysisOwnerUpdaterProvider
    implements ResourceUpdaterProvider<TestItemUpdaterContent, TestItemResource> {

  private final UserRepository userRepository;

  /**
   * Retrieves and constructs a {@code ResourceUpdater} for working with test items, mapping each item's ID to the
   * analysis owner's full name (or login if full name is null).
   *
   * @param updaterContent The content containing test items to update.
   * @return A {@code ResourceUpdater} tailored to handle analysis owner mappings.
   */
  @Override
  public ResourceUpdater<TestItemResource> retrieve(TestItemUpdaterContent updaterContent) {
    var userIds = updaterContent.getTestItems().stream()
        .map(TestItem::getAnalysisOwnerId)
        .filter(Objects::nonNull)
        .distinct()
        .toList();

    if (userIds.isEmpty()) {
      return AnalysisOwnerUpdater.of(Map.of());
    }

    var userNames = getUserNamesMap(userIds);

    var itemIdToOwnerNameMap = updaterContent.getTestItems().stream()
        .filter(item -> Objects.nonNull(item.getAnalysisOwnerId()))
        .collect(Collectors.toMap(
            TestItem::getItemId,
            item -> userNames.getOrDefault(item.getAnalysisOwnerId(), DELETED_USER)
        ));

    return AnalysisOwnerUpdater.of(itemIdToOwnerNameMap);
  }

  private Map<Long, String> getUserNamesMap(List<Long> userIds) {
    return userRepository.findDisplayNamesByIds(userIds).stream()
        .collect(Collectors.toMap(
            UserIdDisplayNameProjection::id,
            UserIdDisplayNameProjection::displayName
        ));
  }
}
