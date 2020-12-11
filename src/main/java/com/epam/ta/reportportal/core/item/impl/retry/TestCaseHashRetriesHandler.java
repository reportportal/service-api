package com.epam.ta.reportportal.core.item.impl.retry;

import com.epam.ta.reportportal.core.item.identity.IdentityUtil;
import com.epam.ta.reportportal.core.item.identity.TestCaseHashGenerator;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.launch.Launch;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service("testCaseHashRetriesHandler")
public class TestCaseHashRetriesHandler extends AbstractRetriesHandler {

	private final TestCaseHashGenerator testCaseHashGenerator;

	public TestCaseHashRetriesHandler(TestItemRepository testItemRepository, LaunchRepository launchRepository,
			ApplicationEventPublisher eventPublisher, TestCaseHashGenerator testCaseHashGenerator) {
		super(testItemRepository, launchRepository, eventPublisher);
		this.testCaseHashGenerator = testCaseHashGenerator;
	}

	@Override
	public Optional<Long> findPreviousRetry(Launch launch, TestItem newItem, TestItem parentItem) {
		if (Objects.isNull(newItem.getTestCaseId())) {
			newItem.setTestCaseHash(testCaseHashGenerator.generate(newItem,
					IdentityUtil.getItemTreeIds(parentItem),
					launch.getProjectId()
			));
		}
		return testItemRepository.findLatestIdByTestCaseHashAndLaunchIdAndParentId(newItem.getTestCaseHash(),
				launch.getId(),
				parentItem.getItemId()
		);
	}
}
