package com.epam.ta.reportportal.ws.param;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.commons.querygen.Queryable;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.item.history.TestItemHistory;
import com.epam.ta.reportportal.ws.converter.TestItemResourceAssembler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class DefaultBaselineHistoryProvider implements HistoryProvider {

	private final TestItemRepository testItemRepository;

	@Autowired
	public DefaultBaselineHistoryProvider(TestItemRepository testItemRepository, TestItemResourceAssembler itemResourceAssembler) {
		this.testItemRepository = testItemRepository;
	}

	@Override
	public Page<TestItemHistory> provide(Queryable filter, Pageable pageable, ReportPortalUser.ProjectDetails projectDetails,
			HistoryRequestParams historyRequestParams) {
		return testItemRepository.loadItemsHistoryPage(filter,
				pageable,
				projectDetails.getProjectId(),
				historyRequestParams.getHistoryDepth()
		);
	}
}
