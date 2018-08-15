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

import com.epam.ta.reportportal.core.item.history.ITestItemsHistoryService;
import com.epam.ta.reportportal.database.dao.LaunchRepository;
import com.epam.ta.reportportal.database.entity.Launch;
import com.epam.ta.reportportal.database.entity.item.TestItem;
import com.epam.ta.reportportal.database.entity.widget.ContentOptions;
import com.epam.ta.reportportal.database.search.Condition;
import com.epam.ta.reportportal.database.search.Filter;
import com.epam.ta.reportportal.database.search.FilterCondition;
import com.epam.ta.reportportal.database.search.Queryable;
import com.epam.ta.reportportal.ws.model.widget.ChartObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.util.CollectionUtils.isEmpty;

/**
 * @author Pavel Bortnik
 */
@Service
public abstract class HistoryTestCasesStrategy extends LastLaunchFilterStrategy {

	@Autowired
	protected LaunchRepository launchRepository;

	@Autowired
	private ITestItemsHistoryService historyServiceStrategy;

	List<Launch> getLaunchHistory(ContentOptions contentOptions, String projectName) {

		Optional<Launch> lastLaunch = getLastLaunch(contentOptions, projectName);

		if (!lastLaunch.isPresent()) {
			return Collections.emptyList();
		}

		List<Launch> launchHistory = historyServiceStrategy.loadLaunches(contentOptions.getItemsCount(),
				lastLaunch.get().getId(),
				projectName,
				false
		);
		if (launchHistory.isEmpty()) {
			return Collections.emptyList();
		}
		return launchHistory;
	}

	void addLastLaunch(Map<String, List<?>> result, List<Launch> launches) {
		if (!isEmpty(launches)) {
			Launch last = launches.get(0);
			ChartObject lastLaunch = new ChartObject();
			lastLaunch.setName(last.getName());
			lastLaunch.setNumber(last.getNumber().toString());
			lastLaunch.setId(last.getId());
			result.put(LAST_FOUND_LAUNCH, Collections.singletonList(lastLaunch));
		}
	}

	Queryable buildHistoryFilter(ContentOptions contentOptions, List<Launch> launches) {
		Set<FilterCondition> filterConditions = new HashSet<>();

		filterConditions.add(new FilterCondition(Condition.IN,
				false,
				launches.stream().map(Launch::getId).collect(Collectors.joining(",")),
				TestItem.LAUNCH_CRITERIA
		));
		filterConditions.add(new FilterCondition(Condition.EQUALS, false, "false", "has_childs"));

		if (!contentOptions.getWidgetOptions().containsKey(INCLUDE_METHODS)) {
			filterConditions.add(new FilterCondition(Condition.EQUALS, false, "STEP", "type"));
		}
		return new Filter(TestItem.class, filterConditions);
	}

	String countPercentage(int amount, int total) {
		return String.format("%.2f", (double) amount / total * 100) + "%";
	}

	protected static class HistoryObject {

		private String uniqueId;

		protected int total;

		protected String name;

		protected Date lastTime;

		protected String percentage;

		public String getUniqueId() {
			return uniqueId;
		}

		public void setUniqueId(String uniqueId) {
			this.uniqueId = uniqueId;
		}

		public int getTotal() {
			return total;
		}

		public void setTotal(int total) {
			this.total = total;
		}

		public Date getLastTime() {
			return lastTime;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public void setLastTime(Date lastTime) {
			this.lastTime = lastTime;
		}

		public String getPercentage() {
			return percentage;
		}

		public void setPercentage(String percentage) {
			this.percentage = percentage;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			}
			HistoryObject that = (HistoryObject) o;
			return total == that.total && Objects.equals(uniqueId, that.uniqueId) && Objects.equals(name, that.name) && Objects.equals(lastTime,
					that.lastTime
			) && Objects.equals(percentage, that.percentage);
		}

		@Override
		public int hashCode() {
			return Objects.hash(uniqueId, total, name, lastTime, percentage);
		}
	}

}
