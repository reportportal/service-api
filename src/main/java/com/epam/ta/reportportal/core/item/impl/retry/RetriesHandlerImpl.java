package com.epam.ta.reportportal.core.item.impl.retry;

import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.core.events.item.ItemRetryEvent;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.ws.model.ErrorType;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.util.Objects;

import static java.util.Optional.ofNullable;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class RetriesHandlerImpl implements RetriesHandler {

	private final LaunchRepository launchRepository;
	private final TestItemRepository testItemRepository;
	private final ApplicationEventPublisher eventPublisher;

	public RetriesHandlerImpl(LaunchRepository launchRepository, TestItemRepository testItemRepository,
			ApplicationEventPublisher eventPublisher) {
		this.launchRepository = launchRepository;
		this.testItemRepository = testItemRepository;
		this.eventPublisher = eventPublisher;
	}

	@Override
	public void handleRetries(Launch launch, TestItem newRetryParent, @Nullable String previousParent) {
		ofNullable(previousParent).flatMap(testItemRepository::findByUuid)
				.ifPresentOrElse(prev -> handleRetries(launch, prev, newRetryParent), () -> handleRetries(launch, newRetryParent));
		eventPublisher.publishEvent(ItemRetryEvent.of(launch.getProjectId(), launch.getId(), newRetryParent.getItemId()));
	}

	/**
	 * Handles retry items with new explicitly provided parent
	 *
	 * @param launch         {@link Launch}
	 * @param retryParent    {@link TestItem}
	 * @param newRetryParent {@link TestItem}
	 */
	private void handleRetries(Launch launch, TestItem retryParent, TestItem newRetryParent) {
		validateNewParent(retryParent, newRetryParent);
		testItemRepository.handleRetry(retryParent.getItemId(), newRetryParent.getItemId());
		updateLaunchRetriesState(launch);
	}

	private void validateNewParent(TestItem prevRetryParent, TestItem newRetryParent) {
		BusinessRule.expect(newRetryParent, i -> !i.getUuid().equals(prevRetryParent.getUuid()))
				.verify(ErrorType.RETRIES_HANDLER_ERROR, "Previous and new parent 'uuid' should not be equal");
		BusinessRule.expect(newRetryParent, i -> Objects.isNull(i.getRetryOf()))
				.verify(ErrorType.RETRIES_HANDLER_ERROR, "Parent item should not be a retry");
	}

	/**
	 * Handles retry items
	 *
	 * @param launch {@link Launch}
	 * @param item   {@link TestItem}
	 */
	private void handleRetries(Launch launch, TestItem item) {
		testItemRepository.handleRetries(item.getItemId());
		updateLaunchRetriesState(launch);
	}

	private void updateLaunchRetriesState(Launch launch) {
		if (!launch.isHasRetries()) {
			launch.setHasRetries(launchRepository.hasRetries(launch.getId()));
		}
	}

}
