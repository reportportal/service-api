package com.epam.ta.reportportal.ws.param;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.commons.querygen.Queryable;
import com.epam.ta.reportportal.entity.item.history.TestItemHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public interface HistoryProvider {

	/**
	 * @param filter               - {@link Queryable}
	 * @param pageable             - {@link Pageable}
	 * @param projectDetails       - {@link com.epam.ta.reportportal.commons.ReportPortalUser.ProjectDetails}
	 * @param historyRequestParams - {@link HistoryRequestParams}
	 * @return {@link Page} with {@link TestItemHistory} content
	 */
	Page<TestItemHistory> provide(Queryable filter, Pageable pageable, ReportPortalUser.ProjectDetails projectDetails,
			HistoryRequestParams historyRequestParams);
}
