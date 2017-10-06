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

import com.epam.ta.reportportal.database.dao.TestItemRepository;
import com.epam.ta.reportportal.database.entity.Launch;
import com.epam.ta.reportportal.database.entity.filter.UserFilter;
import com.epam.ta.reportportal.database.entity.item.ItemStatusHistory;
import com.epam.ta.reportportal.database.entity.widget.ContentOptions;
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

	private static final int ITEMS_COUNT_VALUE = 20;

	@Autowired
	private TestItemRepository itemRepository;

	@Override
	public Map<String, List<?>> buildFilterAndLoadContent(UserFilter userFilter, ContentOptions contentOptions, String projectName) {

		List<Launch> launchHistory = getLaunchHistory(contentOptions, projectName);
		if (CollectionUtils.isEmpty(launchHistory)) {
			return Collections.emptyMap();
		}

		List<ItemStatusHistory> itemStatusHistory = itemRepository.getFlakyItemStatusHistory(
				launchHistory.stream().map(Launch::getId).collect(Collectors.toList()));

		if (CollectionUtils.isEmpty(itemStatusHistory)) {
			return Collections.emptyMap();
		}

		Map<String, List<?>> result = processHistory(itemStatusHistory);
		addLastLaunch(result, launchHistory.get(0));
		return result;
	}

	private Map<String, List<?>> processHistory(List<ItemStatusHistory> itemStatusHistory) {
		Map<String, List<?>> result = new HashMap<>();
		List<FlakinessObject> flakinessObjects = new ArrayList<>();
		for (ItemStatusHistory historyItem : itemStatusHistory) {
			FlakinessObject item = processItem(historyItem);
			flakinessObjects.add(item);
		}
		flakinessObjects.sort(Comparator.comparing(FlakinessObject::getSwitchCounter));
		if (flakinessObjects.size() > ITEMS_COUNT_VALUE) {
			flakinessObjects = flakinessObjects.subList(0, ITEMS_COUNT_VALUE);
		}
		result.put("content", flakinessObjects);
		return result;
	}

	private FlakinessObject processItem(ItemStatusHistory historyItem) {
		LinkedList<ItemStatusHistory.Entry> statusHistory = historyItem.getStatusHistory();
		Date lastSwitched = statusHistory.get(0).getTime();
		Long switchCounter = 0L;
		List<String> statuses = new ArrayList<>();

		String prevStatus = statusHistory.get(0).getStatus();
		for (ItemStatusHistory.Entry entry : statusHistory) {
			if (!entry.getStatus().equals(prevStatus)) {
				lastSwitched = entry.getTime();
				switchCounter++;
			}
			statuses.add(entry.getStatus());
			prevStatus = entry.getStatus();
		}

		FlakinessObject flakinessObject = new FlakinessObject();
		flakinessObject.setName(historyItem.getName());
		flakinessObject.setTotal(historyItem.getTotal());
		flakinessObject.setSwitchCounter(switchCounter);
		flakinessObject.setPercentage(String.format("%.2f", (double) switchCounter / historyItem.getTotal() * 100));
		flakinessObject.setLastTime(lastSwitched);
		flakinessObject.setStatuses(statuses);
		return flakinessObject;
	}

	private static class FlakinessObject extends HistoryObject {

		private Long switchCounter;

		private List<String> statuses;

		public Long getSwitchCounter() {
			return switchCounter;
		}

		public void setSwitchCounter(Long switchCounter) {
			this.switchCounter = switchCounter;
		}

		public List<String> getStatuses() {
			return statuses;
		}

		public void setStatuses(List<String> statuses) {
			this.statuses = statuses;
		}
	}
}
