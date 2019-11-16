package com.epam.ta.reportportal.ws.param;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.commons.querygen.Queryable;
import com.epam.ta.reportportal.core.item.utils.DefaultLaunchFilterProvider;
import com.epam.ta.reportportal.core.shareable.GetShareableEntityHandler;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.filter.UserFilter;
import com.epam.ta.reportportal.entity.item.history.TestItemHistory;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class FilterBaselineHistoryProvider implements HistoryProvider {

	private final TestItemRepository testItemRepository;
	private final GetShareableEntityHandler<UserFilter> getShareableEntityHandler;

	@Autowired
	public FilterBaselineHistoryProvider(TestItemRepository testItemRepository,
			GetShareableEntityHandler<UserFilter> getShareableEntityHandler) {
		this.testItemRepository = testItemRepository;
		this.getShareableEntityHandler = getShareableEntityHandler;
	}

	@Override
	public Page<TestItemHistory> provide(Queryable filter, Pageable pageable, ReportPortalUser.ProjectDetails projectDetails,
			HistoryRequestParams historyRequestParams) {
		return historyRequestParams.getFilterParams()
				.map(filterParams -> this.getItemsWithLaunchesFiltering(filter,
						pageable,
						projectDetails,
						filterParams,
						historyRequestParams.getHistoryDepth()
				))
				.orElseGet(() -> Page.empty(pageable));

	}

	private Page<TestItemHistory> getItemsWithLaunchesFiltering(Queryable testItemFilter, Pageable testItemPageable,
			ReportPortalUser.ProjectDetails projectDetails, HistoryRequestParams.FilterParams filterParams, int historyDepth) {
		Pair<Queryable, Pageable> queryablePair = DefaultLaunchFilterProvider.createDefaultLaunchQueryablePair(projectDetails,
				getShareableEntityHandler.getPermitted(filterParams.getFilterId(), projectDetails),
				filterParams.getLaunchesLimit()
		);

		return testItemRepository.loadItemsHistoryPage(filterParams.isLatest(),
				queryablePair.getKey(),
				testItemFilter,
				queryablePair.getValue(),
				testItemPageable,
				projectDetails.getProjectId(),
				historyDepth
		);
	}
}
