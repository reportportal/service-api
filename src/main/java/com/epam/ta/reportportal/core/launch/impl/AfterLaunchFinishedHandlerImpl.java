package com.epam.ta.reportportal.core.launch.impl;

import com.epam.ta.reportportal.core.launch.AfterLaunchFinishedHandler;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.launch.Launch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class AfterLaunchFinishedHandlerImpl implements AfterLaunchFinishedHandler {

	private final TestItemRepository testItemRepository;

	@Autowired
	public AfterLaunchFinishedHandlerImpl(TestItemRepository testItemRepository) {
		this.testItemRepository = testItemRepository;
	}

	@Override
	public void handleRetriesStatistics(Launch launch) {
		testItemRepository.handleRetriesStatistics(launch.getId());
	}
}
