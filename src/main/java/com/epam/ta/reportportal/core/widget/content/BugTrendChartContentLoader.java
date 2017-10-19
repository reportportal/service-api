/*
 * Copyright 2016 EPAM Systems
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

import com.epam.ta.reportportal.core.widget.impl.WidgetUtils;
import com.epam.ta.reportportal.database.StatisticsDocumentHandler;
import com.epam.ta.reportportal.database.dao.LaunchRepository;
import com.epam.ta.reportportal.database.entity.item.TestItem;
import com.epam.ta.reportportal.database.search.Filter;
import com.epam.ta.reportportal.ws.model.widget.ChartObject;
import com.google.common.collect.ImmutableList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * ContentLoader implementation for <b>Unique Bugs to Total test-cases
 * Widget</b>
 *
 * @author Dzmitry_Kavalets
 * @author Andrei_Ramanchuk
 */
@Service("BugTrendChartContentLoader")
public class BugTrendChartContentLoader extends StatisticBasedContentLoader implements IContentLoadingStrategy {

	private static final String COLLECTION_NAME = "launch";
	private static final String ISSUES = "issuesCount";

	@Autowired
	private LaunchRepository launchRepository;

	@Override
	public Map<String, List<ChartObject>> loadContent(String projectName, Filter filter, Sort sorting, int quantity,
			List<String> contentFields, List<String> metaDataFields, Map<String, List<String>> options) {

		StatisticsDocumentHandler statisticsDocumentHandler = new StatisticsDocumentHandler(contentFields, metaDataFields);
		if (filter.getTarget().equals(TestItem.class)) {
			return Collections.emptyMap();
		}
		List<String> allFields = ImmutableList.<String>builder().addAll(contentFields).addAll(metaDataFields).build();
		if (options.containsKey(LATEST_MODE)) {
			launchRepository.findLatestWithCallback(filter, sorting, allFields, quantity, statisticsDocumentHandler);
		} else {
			launchRepository.loadWithCallback(filter, sorting, quantity, allFields, statisticsDocumentHandler, COLLECTION_NAME);
		}
		List<ChartObject> result = statisticsDocumentHandler.getResult();
		if (WidgetUtils.needRevert(sorting)) {
			Collections.reverse(result);
		}
		return assembleWidgetData(result);
	}

	/**
	 * Calculate chart data (Total count of issues) for UI
	 *
	 * @param input
	 * @return
	 */
	private Map<String, List<ChartObject>> assembleWidgetData(List<ChartObject> input) {
		if (input.isEmpty()) {
			return Collections.emptyMap();
		}
		input.forEach(one -> one.getValues().put(ISSUES, String.valueOf(getIssuesCount(one))));
		return Collections.singletonMap(RESULT, input);
	}

	/**
	 * Calculate issues count for specified test item.<br>
	 * Following series are supposed to be:<br>
	 * - statistics.issueCounter.toInvestigate<br>
	 * - statistics.issueCounter.productBugs<br>
	 * - statistics.issueCounter.testBugs<br>
	 * - statistics.issueCounter.systemIssues
	 *
	 * @param single
	 * @return
	 */
	private static Integer getIssuesCount(ChartObject single) {
		return single.getValues().values().stream().mapToInt(Integer::parseInt).sum();
	}
}