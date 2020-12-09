package com.epam.ta.reportportal.core.item.impl.retry;

import com.epam.ta.reportportal.core.item.identity.IdentityUtil;
import com.epam.ta.reportportal.core.item.identity.UniqueIdGenerator;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.launch.Launch;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

import static java.util.Optional.ofNullable;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service("uniqueIdRetriesHandler")
public class UniqueIdRetriesHandler extends AbstractRetriesHandler {

	private final UniqueIdGenerator uniqueIdGenerator;

	public UniqueIdRetriesHandler(TestItemRepository testItemRepository, LaunchRepository launchRepository,
			ApplicationEventPublisher eventPublisher, UniqueIdGenerator uniqueIdGenerator) {
		super(testItemRepository, launchRepository, eventPublisher);
		this.uniqueIdGenerator = uniqueIdGenerator;
	}

	@Override
	public Optional<Long> findPreviousRetry(Launch launch, TestItem newItem, TestItem parentItem) {
		if (Objects.isNull(newItem.getUniqueId())) {
			newItem.setUniqueId(uniqueIdGenerator.generate(newItem, IdentityUtil.getItemTreeIds(parentItem), launch));
		}
		return ofNullable(newItem.getItemId()).map(itemId -> testItemRepository.findLatestIdByUniqueIdAndLaunchIdAndParentIdAndItemIdNotEqual(
				newItem.getUniqueId(),
				launch.getId(),
				parentItem.getItemId(),
				itemId
		))
				.orElseGet(() -> testItemRepository.findLatestIdByUniqueIdAndLaunchIdAndParentId(newItem.getUniqueId(),
						launch.getId(),
						parentItem.getItemId()
				));
	}
}
