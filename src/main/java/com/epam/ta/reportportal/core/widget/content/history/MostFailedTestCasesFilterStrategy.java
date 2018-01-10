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

import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.core.widget.content.WidgetContentProvider;
import com.epam.ta.reportportal.database.dao.TestItemRepository;
import com.epam.ta.reportportal.database.entity.Launch;
import com.epam.ta.reportportal.database.entity.filter.UserFilter;
import com.epam.ta.reportportal.database.entity.history.status.MostFailedHistory;
import com.epam.ta.reportportal.database.entity.widget.ContentOptions;
import com.epam.ta.reportportal.database.search.CriteriaMapFactory;
import com.epam.ta.reportportal.ws.model.ErrorType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toList;
import static org.springframework.util.CollectionUtils.isEmpty;

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
	public Map<String, List<?>> buildFilterAndLoadContent(UserFilter userFilter, ContentOptions contentOptions, String projectName) {
		String criteria = getCriteria(contentOptions);

		List<Launch> launchHistory = getLaunchHistory(contentOptions, projectName);
		if (isEmpty(launchHistory)) {
			return emptyMap();
		}
		List<String> ids = launchHistory.stream().map(Launch::getId).collect(toList());

		List<MostFailedHistory> history = itemRepository.getMostFailedItemHistory(ids, criteria, ITEMS_COUNT_VALUE);
		if (isEmpty(history)) {
			return emptyMap();
		}

		Map<String, List<?>> result = new HashMap<>(RESULTED_MAP_SIZE);
		result = processHistory(result, history);
		addLastLaunch(result, launchHistory);
		return result;
	}

	private Map<String, List<?>> processHistory(Map<String, List<?>> result, List<MostFailedHistory> itemStatusHistory) {
		result.put(MOST_FAILED, itemStatusHistory.stream().map(this::processItem).collect(toList()));
		return result;
	}

	private MostFailedHistoryObject processItem(MostFailedHistory historyItem) {
		MostFailedHistoryObject mostFailed = new MostFailedHistoryObject();
		mostFailed.setUniqueId(historyItem.getUniqueId());
		mostFailed.setName(historyItem.getName());
		mostFailed.setTotal(historyItem.getTotal());
		mostFailed.setFailedCount(historyItem.getFailed());
		mostFailed.setPercentage(countPercentage(historyItem.getFailed(), historyItem.getTotal()));

		Date date = null;
		List<MostFailedHistory.HistoryEntry> historyEntries = Optional.ofNullable(historyItem.getStatusHistory())
				.orElse(Collections.emptyList());
		List<Boolean> statuses = new ArrayList<>(historyEntries.size());
		for (MostFailedHistory.HistoryEntry entry : historyEntries) {
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

	private String getCriteria(ContentOptions contentOptions) {
		String criteria = null;
		if (null != contentOptions.getContentFields() && contentOptions.getContentFields().size() == 1) {
			criteria = WidgetContentProvider.transformToDBStyle(criteriaMapFactory.getCriteriaMap(Launch.class),
					contentOptions.getContentFields()
			).get(0);
		}
		BusinessRule.expect(criteria, Objects::nonNull).verify(ErrorType.BAD_REQUEST_ERROR);
		return criteria;
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

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			}
			if (!super.equals(o)) {
				return false;
			}
			MostFailedHistoryObject that = (MostFailedHistoryObject) o;
			return failedCount == that.failedCount && Objects.equals(isFailed, that.isFailed);
		}

		@Override
		public int hashCode() {
			return Objects.hash(super.hashCode(), failedCount, isFailed);
		}
	}
}