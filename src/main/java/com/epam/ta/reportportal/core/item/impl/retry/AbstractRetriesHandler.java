package com.epam.ta.reportportal.core.item.impl.retry;

import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.core.events.item.ItemRetryEvent;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.ws.model.ErrorType;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Objects;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public abstract class AbstractRetriesHandler implements RetriesHandler {

	protected final TestItemRepository testItemRepository;
	private final LaunchRepository launchRepository;
	private final ApplicationEventPublisher eventPublisher;

	public AbstractRetriesHandler(TestItemRepository testItemRepository, LaunchRepository launchRepository,
			ApplicationEventPublisher eventPublisher) {
		this.testItemRepository = testItemRepository;
		this.launchRepository = launchRepository;
		this.eventPublisher = eventPublisher;
	}

	@Override
	public void handleRetries(Launch launch, TestItem newRetryParent, Long previousParent) {
		handleRetries(launch, previousParent, newRetryParent);
		eventPublisher.publishEvent(ItemRetryEvent.of(launch.getProjectId(), launch.getId(), newRetryParent.getItemId()));
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
				.verify(ErrorType.RETRIES_HANDLER_ERROR, "Previous and new parent 'id' should not be equal");
		BusinessRule.expect(newRetryParent, i -> Objects.isNull(i.getRetryOf()))
				.verify(ErrorType.RETRIES_HANDLER_ERROR, "Parent item should not be a retry");
	}

	private void updateLaunchRetriesState(Launch launch) {
		if (!launch.isHasRetries()) {
			launch.setHasRetries(launchRepository.hasRetries(launch.getId()));
		}
	}

}
