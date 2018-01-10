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
import com.epam.ta.reportportal.database.entity.history.status.FlakyHistory;
import com.epam.ta.reportportal.database.entity.widget.ContentOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

import static java.util.Collections.emptyMap;
import static java.util.Comparator.comparing;
import static java.util.Comparator.reverseOrder;
import static java.util.stream.Collectors.toList;
import static org.springframework.util.CollectionUtils.isEmpty;

/**
 * @author Pavel Bortnik
 */
@Service
public class FlakyTestCasesStrategy extends HistoryTestCasesStrategy {

	private static final String FLAKY = "flaky";

	@Autowired
	private TestItemRepository itemRepository;

	@Override
	public Map<String, List<?>> buildFilterAndLoadContent(UserFilter userFilter, ContentOptions contentOptions, String projectName) {
		List<Launch> launchHistory = getLaunchHistory(contentOptions, projectName);
		if (isEmpty(launchHistory)) {
			return emptyMap();
		}

		List<FlakyHistory> itemStatusHistory = itemRepository.getFlakyItemStatusHistory(
				launchHistory.stream().map(Launch::getId).collect(toList()));
		if (isEmpty(itemStatusHistory)) {
			return emptyMap();
		}
		Map<String, List<?>> result = new HashMap<>(RESULTED_MAP_SIZE);
		result = processHistory(result, itemStatusHistory);
		addLastLaunch(result, launchHistory);
		return result;
	}

	private Map<String, List<?>> processHistory(Map<String, List<?>> result, List<FlakyHistory> itemStatusHistory) {
		List<FlakyHistoryObject> flakyHistoryObjects = itemStatusHistory.stream().map(this::processItem).collect(toList());
		flakyHistoryObjects.sort(
				comparing(FlakyHistoryObject::getSwitchCounter, reverseOrder()).thenComparing(FlakyHistoryObject::getTotal));
		if (flakyHistoryObjects.size() > ITEMS_COUNT_VALUE) {
			flakyHistoryObjects = flakyHistoryObjects.subList(0, ITEMS_COUNT_VALUE);
		}
		result.put(FLAKY, flakyHistoryObjects);
		return result;
	}

	private FlakyHistoryObject processItem(FlakyHistory historyItem) {
		List<FlakyHistory.HistoryEntry> statusHistory = historyItem.getStatusHistory();
		Date lastSwitched = statusHistory.get(0).getStartTime();
		int potentialSwitches = historyItem.getTotal() - 1;
		int switchCounter = 0;
		List<String> statuses = new ArrayList<>();

		String prevStatus = statusHistory.get(0).getStatus();
		for (FlakyHistory.HistoryEntry entry : statusHistory) {
			if (!entry.getStatus().equals(prevStatus)) {
				lastSwitched = entry.getStartTime();
				switchCounter++;
			}
			statuses.add(entry.getStatus());
			prevStatus = entry.getStatus();
		}

		FlakyHistoryObject flakyHistoryObject = new FlakyHistoryObject();
		flakyHistoryObject.setUniqueId(historyItem.getUniqueId());
		flakyHistoryObject.setName(historyItem.getName());
		flakyHistoryObject.setTotal(potentialSwitches);
		flakyHistoryObject.setSwitchCounter(switchCounter);
		flakyHistoryObject.setPercentage(countPercentage(switchCounter, potentialSwitches));
		flakyHistoryObject.setLastTime(lastSwitched);
		flakyHistoryObject.setStatuses(statuses);
		return flakyHistoryObject;
	}

	private static class FlakyHistoryObject extends HistoryObject {

		private int switchCounter;

		private List<String> statuses;

		public int getSwitchCounter() {
			return switchCounter;
		}

		public void setSwitchCounter(int switchCounter) {
			this.switchCounter = switchCounter;
		}

		public List<String> getStatuses() {
			return statuses;
		}

		void setStatuses(List<String> statuses) {
			this.statuses = statuses;
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
			FlakyHistoryObject that = (FlakyHistoryObject) o;
			return switchCounter == that.switchCounter && Objects.equals(statuses, that.statuses);
		}

		@Override
		public int hashCode() {
			return Objects.hash(super.hashCode(), switchCounter, statuses);
		}
	}
}
