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
import com.epam.ta.reportportal.core.widget.content.BuildFilterStrategy;
import com.epam.ta.reportportal.database.dao.LaunchRepository;
import com.epam.ta.reportportal.database.entity.Launch;
import com.epam.ta.reportportal.database.entity.widget.ContentOptions;
import com.epam.ta.reportportal.ws.model.launch.Mode;
import com.epam.ta.reportportal.ws.model.widget.ChartObject;
import com.google.common.base.MoreObjects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author Pavel Bortnik
 */
@Service
public abstract class HistoryTestCasesStrategy implements BuildFilterStrategy {

	private static final String LAUNCH_NAME_FIELD = "launchNameFilter";

	private static final String LAST_FOUND_LAUNCH = "lastLaunch";

	static final int RESULTED_MAP_SIZE = 2;

	static final int ITEMS_COUNT_VALUE = 20;

	@Autowired
	protected LaunchRepository launchRepository;

	@Autowired
	private ITestItemsHistoryService historyServiceStrategy;

	List<Launch> getLaunchHistory(ContentOptions contentOptions, String projectName) {
		/*
		 * Return false response for absent filtering launch name parameter
		 */
		if (contentOptions.getWidgetOptions() == null || contentOptions.getWidgetOptions().get(LAUNCH_NAME_FIELD) == null) {
			return Collections.emptyList();
		}
		Optional<Launch> lastLaunchForProject = launchRepository.findLastLaunch(projectName,
				contentOptions.getWidgetOptions().get(LAUNCH_NAME_FIELD).get(0), Mode.DEFAULT.name()
		);
		if (!lastLaunchForProject.isPresent()) {
			return Collections.emptyList();
		}
		List<Launch> launchHistory = historyServiceStrategy.loadLaunches(contentOptions.getItemsCount(), lastLaunchForProject.get().getId(),
				projectName, false
		);
		if (launchHistory.isEmpty()) {
			return Collections.emptyList();
		}
		return launchHistory;
	}

	void addLastLaunch(Map<String, List<?>> result, Launch last) {
		ChartObject lastLaunch = new ChartObject();
		lastLaunch.setName(last.getName());
		lastLaunch.setNumber(last.getNumber().toString());
		lastLaunch.setId(last.getId());
		result.put(LAST_FOUND_LAUNCH, Collections.singletonList(lastLaunch));
	}

	String countPercentage(int amount, int total) {
		return String.format("%.2f", (double) amount / total * 100) + "%";
	}

	protected static class HistoryObject {

		protected int total;

		protected String name;

		protected Date lastTime;

		protected String percentage;

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
			return total == that.total && com.google.common.base.Objects.equal(name, that.name) && com.google.common.base.Objects.equal(
					lastTime, that.lastTime) && com.google.common.base.Objects.equal(percentage, that.percentage);
		}

		@Override
		public int hashCode() {
			return com.google.common.base.Objects.hashCode(total, name, lastTime, percentage);
		}

		@Override
		public String toString() {
			return MoreObjects.toStringHelper(this)
					.add("total", total)
					.add("name", name)
					.add("lastTime", lastTime)
					.add("percentage", percentage)
					.toString();
		}
	}

}
