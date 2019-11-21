package com.epam.ta.reportportal.core.item.impl.history.provider.impl;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.commons.querygen.Queryable;
import com.epam.ta.reportportal.core.item.TestItemService;
import com.epam.ta.reportportal.core.item.impl.LaunchAccessValidator;
import com.epam.ta.reportportal.core.item.impl.history.provider.HistoryProvider;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.item.history.TestItemHistory;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.param.HistoryRequestParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class TestItemBaselineHistoryProvider implements HistoryProvider {

	private final TestItemService testItemService;
	private final LaunchAccessValidator launchAccessValidator;
	private final TestItemRepository testItemRepository;

	@Autowired
	public TestItemBaselineHistoryProvider(TestItemService testItemService, LaunchAccessValidator launchAccessValidator,
			TestItemRepository testItemRepository) {
		this.testItemService = testItemService;
		this.launchAccessValidator = launchAccessValidator;
		this.testItemRepository = testItemRepository;
	}

	@Override
	public Page<TestItemHistory> provide(Queryable filter, Pageable pageable, HistoryRequestParams historyRequestParams,
			ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user) {

		return historyRequestParams.getParentId()
				.map(itemId -> loadHistory(filter, pageable, itemId, historyRequestParams, projectDetails, user))
				.orElseGet(() -> historyRequestParams.getItemId()
						.map(itemId -> loadHistory(filter, pageable, itemId, historyRequestParams, projectDetails, user))
						.orElseGet(() -> Page.empty(pageable)));
	}

	private Page<TestItemHistory> loadHistory(Queryable filter, Pageable pageable, Long itemId, HistoryRequestParams historyRequestParams,
			ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user) {
		TestItem testItem = testItemRepository.findById(itemId)
				.orElseThrow(() -> new ReportPortalException(ErrorType.TEST_ITEM_NOT_FOUND, itemId));
		Launch launch = testItemService.getEffectiveLaunch(testItem);
		launchAccessValidator.validate(launch.getId(), projectDetails, user);

		return historyRequestParams.getHistoryType()
				.filter(HistoryRequestParams.HistoryTypeEnum.LINE::equals)
				.map(type -> testItemRepository.loadItemsHistoryPage(filter,
						pageable,
						projectDetails.getProjectId(),
						launch.getName(),
						historyRequestParams.getHistoryDepth()
				))
				.orElseGet(() -> testItemRepository.loadItemsHistoryPage(filter,
						pageable,
						projectDetails.getProjectId(),
						historyRequestParams.getHistoryDepth()
				));

	}
}
