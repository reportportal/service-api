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

package com.epam.reportportal.core.item.impl.retry;

import com.epam.reportportal.core.events.activity.item.ItemRetryEvent;
import com.epam.reportportal.infrastructure.persistence.dao.LaunchRepository;
import com.epam.reportportal.infrastructure.persistence.dao.TestItemRepository;
import com.epam.reportportal.infrastructure.persistence.entity.item.TestItem;
import com.epam.reportportal.infrastructure.persistence.entity.launch.Launch;
import com.epam.reportportal.infrastructure.persistence.jooq.enums.JStatusEnum;
import com.epam.reportportal.infrastructure.rules.commons.validation.BusinessRule;
import com.epam.reportportal.infrastructure.rules.exception.ErrorType;
import java.time.Instant;
import java.util.Objects;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class DefaultRetryHandler implements RetryHandler {

  private final TestItemRepository testItemRepository;
  private final LaunchRepository launchRepository;
  private final ApplicationEventPublisher eventPublisher;

  public DefaultRetryHandler(TestItemRepository testItemRepository,
      LaunchRepository launchRepository,
      ApplicationEventPublisher eventPublisher) {
    this.testItemRepository = testItemRepository;
    this.launchRepository = launchRepository;
    this.eventPublisher = eventPublisher;
  }

  @Override
  public void handleRetries(Launch launch, TestItem newRetryParent, Long previousParent) {
    handleRetries(launch, previousParent, newRetryParent);
    eventPublisher.publishEvent(
        ItemRetryEvent.of(launch.getProjectId(), launch.getId(), newRetryParent.getItemId()));
  }

  @Override
  public void finishRetries(Long retryParentId, JStatusEnum status, Instant endTime) {
    testItemRepository.updateStatusAndEndTimeByRetryOfId(retryParentId,
        JStatusEnum.IN_PROGRESS,
        JStatusEnum.valueOf(status.name()),
        endTime
    );
  }

  /**
   * Handles retry items with new explicitly provided parent
   *
   * @param launch         {@link Launch}
   * @param retryParent    {@link TestItem#getItemId()}
   * @param newRetryParent {@link TestItem}
   */
  private void handleRetries(Launch launch, Long retryParent, TestItem newRetryParent) {
    validateNewParent(retryParent, newRetryParent);
    testItemRepository.handleRetry(retryParent, newRetryParent.getItemId());
    updateLaunchRetriesState(launch);
  }

  private void validateNewParent(Long prevRetryParent, TestItem newRetryParent) {
    BusinessRule.expect(newRetryParent, i -> !i.getItemId().equals(prevRetryParent))
        .verify(ErrorType.RETRIES_HANDLER_ERROR,
            "Previous and new parent 'id' should not be equal");
    BusinessRule.expect(newRetryParent, i -> Objects.isNull(i.getRetryOf()))
        .verify(ErrorType.RETRIES_HANDLER_ERROR, "Parent item should not be a retry");
  }

  private void updateLaunchRetriesState(Launch launch) {
    if (!launch.isHasRetries()) {
      launch.setHasRetries(launchRepository.hasRetries(launch.getId()));
    }
  }

}
