package com.epam.ta.reportportal.core.item.impl.retry;

import static java.util.Optional.ofNullable;

import com.epam.reportportal.rules.commons.validation.BusinessRule;
import com.epam.ta.reportportal.core.events.activity.item.ItemRetryEvent;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.jooq.enums.JStatusEnum;
import com.epam.reportportal.rules.exception.ErrorType;
import java.time.Instant;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
@RequiredArgsConstructor
public class DefaultRetryHandler implements RetryHandler {

  private final TestItemRepository testItemRepository;
  private final LaunchRepository launchRepository;
  private final ApplicationEventPublisher eventPublisher;
  private final RetrySearcher retrySearcher;

  @Override
  public void handleRetry(Launch launch, TestItem lastTry, String retryOf) {
    Long previousParentId = getPreviousParentId(launch, lastTry, retryOf);
    handleRetries(launch, previousParentId, lastTry);
    eventPublisher.publishEvent(
        ItemRetryEvent.of(launch.getProjectId(), launch.getId(), lastTry.getItemId()));
  }

  private Long getPreviousParentId(Launch launch, TestItem lastTry, String retryOf) {
    return ofNullable(retryOf)
        .flatMap(testItemRepository::findIdByUuid)
        .orElseGet(
            () -> retrySearcher.findPreviousTry(launch, lastTry).orElse(null));
  }

  @Override
  public void finishRetries(TestItem retryParent, JStatusEnum status, Instant endTime) {
    testItemRepository.updateStatusAndEndTimeByRetryOfId(retryParent.getItemId(),
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
