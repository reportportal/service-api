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
import com.epam.ta.reportportal.database.entity.item.ItemStatusHistory;
import com.epam.ta.reportportal.database.entity.widget.ContentOptions;
import com.epam.ta.reportportal.database.search.CriteriaMapFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

/**
 * Most failed test-cases widget content loader<br> <b>Slow widget because history based</b>
 *
 * @author Andrei_Ramanchuk
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
		List<ItemStatusHistory> history = itemRepository.getMostFailedItemHistory(ids, criteria);
		Map<String, List<?>> result = processHistory(history);
		addLastLaunch(result, launchHistory.get(0));
		return result;
	}

	private Map<String, List<?>> processHistory(List<ItemStatusHistory> itemStatusHistory) {
		Map<String, List<?>> result = new HashMap<>();
		result.put(MOST_FAILED, itemStatusHistory.stream().map(this::processItem).collect(toList()));
		return result;
	}

	private MostFailedHistoryObject processItem(ItemStatusHistory historyItem) {
		MostFailedHistoryObject mostFailedHistoryObject = new MostFailedHistoryObject();
		mostFailedHistoryObject.setName(historyItem.getName());
		mostFailedHistoryObject.setTotal(historyItem.getTotal());
		mostFailedHistoryObject.setFailedCount(historyItem.getCount());
		mostFailedHistoryObject.setPercentage(String.format("%.2f", (double) historyItem.getCount() / historyItem.getTotal() * 100));
		List<Statuses> statuses = historyItem.getStatusHistory()
				.stream()
				.map(it -> new Statuses(it.getStatus(), it.getIssue()))
				.collect(toList());
		mostFailedHistoryObject.setStatuses(statuses);
		return mostFailedHistoryObject;
	}

	private static class MostFailedHistoryObject extends HistoryObject {

		private Long failedCount;

		private List<Statuses> statuses;

		public List<Statuses> getStatuses() {
			return statuses;
		}

		public void setStatuses(List<Statuses> statuses) {
			this.statuses = statuses;
		}

		public Long getFailedCount() {
			return failedCount;
		}

		public void setFailedCount(Long failedCount) {
			this.failedCount = failedCount;
		}
	}

	private static class Statuses {

		public Statuses(String status, String issue) {
			this.status = status;
			this.issue = issue;
		}

		private String status;

		private String issue;

		public String getStatus() {
			return status;
		}

		public void setStatus(String status) {
			this.status = status;
		}

		public String getIssue() {
			return issue;
		}

		public void setIssue(String issue) {
			this.issue = issue;
		}
	}
}