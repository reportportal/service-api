package com.epam.ta.reportportal.core.item.impl.history.provider.impl;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.commons.querygen.Queryable;
import com.epam.ta.reportportal.core.item.impl.LaunchAccessValidator;
import com.epam.ta.reportportal.core.item.impl.history.provider.HistoryProvider;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.item.history.TestItemHistory;
import com.epam.ta.reportportal.ws.param.HistoryRequestParams;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class LaunchBaselineHistoryProvider implements HistoryProvider {

	private final LaunchAccessValidator launchAccessValidator;
	private final TestItemRepository testItemRepository;

	public LaunchBaselineHistoryProvider(LaunchAccessValidator launchAccessValidator, TestItemRepository testItemRepository) {
		this.launchAccessValidator = launchAccessValidator;
		this.testItemRepository = testItemRepository;
	}

	@Override
	public Page<TestItemHistory> provide(Queryable filter, Pageable pageable, HistoryRequestParams historyRequestParams,
			ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user) {
		return historyRequestParams.getLaunchId().map(launchId -> {
			launchAccessValidator.validate(launchId, projectDetails, user);

			return testItemRepository.loadItemsHistoryPage(filter,
					pageable,
					projectDetails.getProjectId(),
					historyRequestParams.getHistoryDepth()
			);
		}).orElseGet(() -> Page.empty(pageable));
	}

}
