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
import com.epam.ta.reportportal.database.dao.TestItemRepository;
import com.epam.ta.reportportal.database.entity.Launch;
import com.epam.ta.reportportal.database.entity.filter.UserFilter;
import com.epam.ta.reportportal.database.entity.item.ItemStatusHistory;
import com.epam.ta.reportportal.database.entity.widget.ContentOptions;
import com.epam.ta.reportportal.ws.model.widget.ChartObject;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Pavel Bortnik
 */
@Service
public class FlakyTestCasesStrategy extends HistoryTestCasesStrategy {

	private static final String TOTAL = "total";
	private static final String SWITCHED = "switched";
	private static final String AFFECTED_BY = "percentage";
	private static final String LAST_SWITCH = "lastSwitch";
	private static final String STATUSES = "statuses";

	private static final String LAST_FOUND_LAUNCH = "lastLaunch";

	private static final String LAUNCH_NAME_FIELD = "launchNameFilter";

	private static final int ITEMS_COUNT_VALUE = 20;

	@Autowired
	private LaunchRepository launchRepository;

	@Autowired
	private TestItemRepository itemRepository;

	@Autowired
	private ITestItemsHistoryService historyServiceStrategy;

	@Override
	public Map<String, List<ChartObject>> buildFilterAndLoadContent(UserFilter userFilter, ContentOptions contentOptions,
			String projectName) {

		List<Launch> launchHistory = getLaunchHistory(contentOptions, projectName);
		if (CollectionUtils.isEmpty(launchHistory)) {
			return Collections.emptyMap();
		}

		List<ItemStatusHistory> itemStatusHistory = itemRepository.getFlakyItemStatusHistory(
				launchHistory.stream().map(Launch::getId).collect(Collectors.toList()));

		if (CollectionUtils.isEmpty(itemStatusHistory)) {
			return Collections.emptyMap();
		}

		Map<String, List<ChartObject>> result = processHistory(itemStatusHistory);
		addLastLaunch(result, launchHistory.get(0));
		return result;
	}


	private Map<String, List<ChartObject>> processHistory(List<ItemStatusHistory> itemStatusHistory) {
		Map<String, List<ChartObject>> result = new HashMap<>();
		List<ProcessedObject> processedObjects = new ArrayList<>();
		for (ItemStatusHistory historyItem : itemStatusHistory) {
			ProcessedObject item = processItem(historyItem);
			processedObjects.add(item);
		}
		processedObjects.sort(Comparator.comparing(ProcessedObject::getSwitchCounter));
		if (processedObjects.size() > ITEMS_COUNT_VALUE) {
			processedObjects = processedObjects.subList(0, ITEMS_COUNT_VALUE);
		}

		processedObjects.forEach(it -> {
			ChartObject chartObject = new ChartObject();
			Map<String, String> values = new HashMap<>();
			values.put(TOTAL, String.valueOf(it.getTotal()));
			values.put(SWITCHED, String.valueOf(it.getSwitchCounter()));
			values.put(AFFECTED_BY, String.format( "%.2f", (double) it.getSwitchCounter() / it.getTotal() * 100));
			values.put(LAST_SWITCH, String.valueOf(it.getLastSwitched().getTime()));
			values.put(STATUSES, it.getStatuses());
			chartObject.setValues(values);
			result.put(it.getName(), Collections.singletonList(chartObject));
		});
		return result;
	}

	private ProcessedObject processItem(ItemStatusHistory historyItem) {
		LinkedList<ItemStatusHistory.Entry> statusHistory = historyItem.getStatusHistory();
		Date lastSwitched = statusHistory.get(0).getTime();
		Long switchCounter = 0L;
		StringJoiner statuses = new StringJoiner(", ");

		String prevStatus = statusHistory.get(0).getStatus();
		for (ItemStatusHistory.Entry entry : statusHistory) {
			if (!entry.getStatus().equals(prevStatus)) {
				lastSwitched = entry.getTime();
				switchCounter++;
			}
			statuses.add(entry.getStatus());
			prevStatus = entry.getStatus();
		}

		ProcessedObject processedObject = new ProcessedObject();
		processedObject.setName(historyItem.getName());
		processedObject.setTotal(historyItem.getTotal());
		processedObject.setLastSwitched(lastSwitched);
		processedObject.setStatuses(statuses.toString());
		processedObject.setSwitchCounter(switchCounter);
		return processedObject;
	}

	private static class ProcessedObject {

		private Long total;

		private String name;

		private Date lastSwitched;

		private Long switchCounter;

		private String statuses;

		public Long getTotal() {
			return total;
		}

		public void setTotal(Long total) {
			this.total = total;
		}

		public Date getLastSwitched() {
			return lastSwitched;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public void setLastSwitched(Date lastSwitched) {
			this.lastSwitched = lastSwitched;
		}

		public Long getSwitchCounter() {
			return switchCounter;
		}

		public void setSwitchCounter(Long switchCounter) {
			this.switchCounter = switchCounter;
		}

		public String getStatuses() {
			return statuses;
		}

		public void setStatuses(String statuses) {
			this.statuses = statuses;
		}
	}
}
