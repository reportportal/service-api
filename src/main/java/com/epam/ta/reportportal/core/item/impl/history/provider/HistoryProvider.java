package com.epam.ta.reportportal.core.item.impl.history.provider;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.commons.querygen.Queryable;
import com.epam.ta.reportportal.entity.item.history.TestItemHistory;
import com.epam.ta.reportportal.ws.param.HistoryRequestParams;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public interface HistoryProvider {

	/**
	 * @param filter               - {@link Queryable}
	 * @param pageable             - {@link Pageable}
	 * @param historyRequestParams - {@link HistoryRequestParams}
	 * @param projectDetails       - {@link com.epam.ta.reportportal.commons.ReportPortalUser.ProjectDetails}
	 * @param user                 - {@link ReportPortalUser}
	 * @return {@link Page} with {@link TestItemHistory} content
	 */
	Page<TestItemHistory> provide(Queryable filter, Pageable pageable, HistoryRequestParams historyRequestParams,
			ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user);
}
