/*
 * Copyright 2017 EPAM Systems
 *
 *
 * This file is part of EPAM Report Portal.
 * https://github.com/reportportal/service-api
 *
 * Report Portal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Report Portal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Report Portal.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.epam.ta.reportportal.core.widget.content.history;

import com.epam.ta.reportportal.core.widget.content.StatisticBasedContentLoader;
import com.epam.ta.reportportal.core.widget.content.WidgetContentProvider;
import com.epam.ta.reportportal.database.dao.TestItemRepository;
import com.epam.ta.reportportal.database.entity.Launch;
import com.epam.ta.reportportal.database.entity.filter.UserFilter;
import com.epam.ta.reportportal.database.entity.history.status.MostFailedHistory;
import com.epam.ta.reportportal.database.entity.widget.ContentOptions;
import com.epam.ta.reportportal.database.search.CriteriaMapFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

import static java.util.stream.Collectors.toList;

/**
 * Most failed test-cases widget content loader<br> <b>Slow widget because history based</b>
 *
 * @author Pavel Bortnik
 */
@Service
public class MostFailedTestCasesFilterStrategy extends HistoryTestCasesStrategy {

	private static final String MOST_FAILED = "most_failed";

	@Autowired
	private TestItemRepository itemRepository;

	@Autowired
	private CriteriaMapFactory criteriaMapFactory;

	@Override
	public Map<String, List<?>> buildFilterAndLoadContent(UserFilter userFilter, ContentOptions contentOptions,
			String projectName) {
		String criteria = new StatisticBasedContentLoader().getSystemIssueFieldName();
		if (null != contentOptions.getContentFields() && contentOptions.getContentFields().size() >= 1) {
			criteria = WidgetContentProvider.transformToDBStyle(criteriaMapFactory.getCriteriaMap(Launch.class),
					contentOptions.getContentFields()
			).get(0);
		}
		List<Launch> launchHistory = getLaunchHistory(contentOptions, projectName);
		List<String> ids = launchHistory.stream().map(Launch::getId).collect(toList());
		List<MostFailedHistory> history = itemRepository.getMostFailedItemHistory(ids, criteria, ITEMS_COUNT_VALUE);

		Map<String, List<?>> result = new HashMap<>(RESULTED_MAP_SIZE);
		processHistory(result, history);
		addLastLaunch(result, launchHistory.get(0));
		return result;
	}

	private Map<String, List<?>> processHistory(Map<String, List<?>> result, List<MostFailedHistory> itemStatusHistory) {
		result.put(MOST_FAILED, itemStatusHistory.stream().map(this::processItem).collect(toList()));
		return result;
	}

	private MostFailedHistoryObject processItem(MostFailedHistory historyItem) {
		MostFailedHistoryObject mostFailed = new MostFailedHistoryObject();
		mostFailed.setName(historyItem.getName());
		mostFailed.setTotal(historyItem.getTotal());
		mostFailed.setFailedCount(historyItem.getFailed());
		mostFailed.setPercentage(countPercentage(historyItem.getFailed(), historyItem.getTotal()));
		Date date = null;
		List<Boolean> statuses = new ArrayList<>(historyItem.getStatusHistory().size());
		for (MostFailedHistory.HistoryEntry entry : historyItem.getStatusHistory()) {
			boolean isFailed = false;
			if (entry.getCriteriaAmount() > 0) {
				date = entry.getStartTime();
				isFailed = true;
			}
			statuses.add(isFailed);
		}
		mostFailed.setLastTime(date);
		mostFailed.setIsFailed(statuses);
		return mostFailed;
	}

	private static class MostFailedHistoryObject extends HistoryObject {

		private int failedCount;

		private List<Boolean> isFailed;

		public int getFailedCount() {
			return failedCount;
		}

		public void setFailedCount(int failedCount) {
			this.failedCount = failedCount;
		}

		public List<Boolean> getIsFailed() {
			return isFailed;
		}

		public void setIsFailed(List<Boolean> isFailed) {
			this.isFailed = isFailed;
		}
	}
}