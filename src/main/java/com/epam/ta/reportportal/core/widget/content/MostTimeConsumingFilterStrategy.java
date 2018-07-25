/*
 * Copyright 2018 EPAM Systems
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

package com.epam.ta.reportportal.core.widget.content;

import com.epam.ta.reportportal.core.widget.content.history.LastLaunchFilterStrategy;
import com.epam.ta.reportportal.database.dao.TestItemRepository;
import com.epam.ta.reportportal.database.entity.Launch;
import com.epam.ta.reportportal.database.entity.filter.UserFilter;
import com.epam.ta.reportportal.database.entity.item.TestItem;
import com.epam.ta.reportportal.database.entity.widget.ContentOptions;
import com.epam.ta.reportportal.database.search.Condition;
import com.epam.ta.reportportal.database.search.Filter;
import com.epam.ta.reportportal.database.search.FilterCondition;
import com.epam.ta.reportportal.ws.model.widget.ChartObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static com.epam.ta.reportportal.core.widget.content.StatisticBasedContentLoader.RESULT;

/**
 * @author Pavel Bortnik
 */
@Service
public class MostTimeConsumingFilterStrategy extends LastLaunchFilterStrategy {

	private final static String INCLUDE_METHODS = "include_methods";

	private TestItemRepository testItemRepository;

	@Autowired
	public void setTestItemRepository(TestItemRepository testItemRepository) {
		this.testItemRepository = testItemRepository;
	}

	@Override
	public Map<String, ?> buildFilterAndLoadContent(UserFilter userFilter, ContentOptions contentOptions, String projectName) {
		Optional<Launch> lastLaunch = getLastLaunch(contentOptions, projectName);
		if (!lastLaunch.isPresent()) {
			return Collections.emptyMap();
		}
		Map<String, List<?>> res = new HashMap<>(RESULTED_MAP_SIZE);
		Launch last = lastLaunch.get();

		Filter filter = createFilter(last.getId(), contentOptions);
		res.put(RESULT, testItemRepository.findMostTimeConsumingTestItems(filter, contentOptions.getItemsCount()));

		ChartObject lastLaunchChartObject = new ChartObject();
		lastLaunchChartObject.setName(last.getName());
		lastLaunchChartObject.setNumber(last.getNumber().toString());
		lastLaunchChartObject.setId(last.getId());
		res.put(LAST_FOUND_LAUNCH, Collections.singletonList(lastLaunchChartObject));
		return res;
	}

	private Filter createFilter(String lastId, ContentOptions contentOptions) {
		Set<FilterCondition> filterConditions = new HashSet<>();
		filterConditions.add(new FilterCondition(
				Condition.IN,
				false,
				contentOptions.getContentFields().stream().map(String::toUpperCase).collect(Collectors.joining(",")),
				TestItem.STATUS
		));
		filterConditions.add(new FilterCondition(Condition.EQUALS, false, lastId, TestItem.LAUNCH_CRITERIA));
		filterConditions.add(new FilterCondition(Condition.EQUALS, false, "false", "has_childs"));

		if (!contentOptions.getWidgetOptions().containsKey(INCLUDE_METHODS)) {
			filterConditions.add(new FilterCondition(Condition.EQUALS, false, "STEP", "type"));
		}

		return new Filter(TestItem.class, filterConditions);
	}
}
