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

package com.epam.reportportal.base.core.item.impl.retry;

import static java.util.Optional.ofNullable;

import com.epam.reportportal.base.core.item.identity.IdentityUtil;
import com.epam.reportportal.base.core.item.identity.UniqueIdGenerator;
import com.epam.reportportal.base.infrastructure.persistence.dao.TestItemRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.item.TestItem;
import com.epam.reportportal.base.infrastructure.persistence.entity.launch.Launch;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Service;

/**
 * Finds an original test item to retry by unique id.
 *
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service("uniqueIdRetrySearcher")
public class UniqueIdRetrySearcher implements RetrySearcher {

  private final UniqueIdGenerator uniqueIdGenerator;
  private final TestItemRepository testItemRepository;

  public UniqueIdRetrySearcher(UniqueIdGenerator uniqueIdGenerator,
      TestItemRepository testItemRepository) {
    this.uniqueIdGenerator = uniqueIdGenerator;
    this.testItemRepository = testItemRepository;
  }

  @Override
  public Optional<Long> findPreviousRetry(Launch launch, TestItem newItem, TestItem parentItem) {
    if (Objects.isNull(newItem.getUniqueId())) {
      newItem.setUniqueId(
          uniqueIdGenerator.generate(newItem, IdentityUtil.getItemTreeIds(parentItem), launch));
    }
    return ofNullable(newItem.getItemId()).map(
            itemId -> testItemRepository.findLatestIdByUniqueIdAndLaunchIdAndParentIdAndItemIdNotEqual(
                newItem.getUniqueId(),
                launch.getId(),
                parentItem.getItemId(),
                itemId
            ))
        .orElseGet(() -> testItemRepository.findLatestIdByUniqueIdAndLaunchIdAndParentId(
            newItem.getUniqueId(),
            launch.getId(),
            parentItem.getItemId()
        ));
  }
}
