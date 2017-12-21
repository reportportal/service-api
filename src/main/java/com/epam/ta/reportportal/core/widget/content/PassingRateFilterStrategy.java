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
package com.epam.ta.reportportal.core.widget.content;

import com.epam.ta.reportportal.database.dao.LaunchRepository;
import com.epam.ta.reportportal.database.entity.Launch;
import com.epam.ta.reportportal.database.entity.filter.UserFilter;
import com.epam.ta.reportportal.database.entity.widget.ContentOptions;
import com.epam.ta.reportportal.ws.model.launch.Mode;
import com.epam.ta.reportportal.ws.model.widget.ChartObject;
import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.epam.ta.reportportal.core.widget.content.StatisticBasedContentLoader.*;

/**
 * Loads total and failed content for last run of
 * specified launch
 *
 * @author Pavel_Bortnik
 */

@Service
public class PassingRateFilterStrategy implements BuildFilterStrategy {

	private static final String LAUNCH_NAME_FIELD = "launchNameFilter";

	@Autowired
	private LaunchRepository launchRepository;

	@Override
	public Map<String, List<ChartObject>> buildFilterAndLoadContent(UserFilter userFilter, ContentOptions contentOptions,
			String projectName) {
		Map<String, List<ChartObject>> emptyResult = Collections.emptyMap();
		if (contentOptions.getWidgetOptions() == null || contentOptions.getWidgetOptions().get(LAUNCH_NAME_FIELD) == null) {
			return emptyResult;
		}
		Launch lastLaunchForProject = launchRepository.findLatestLaunch(
				projectName, contentOptions.getWidgetOptions().get(LAUNCH_NAME_FIELD).get(0), Mode.DEFAULT.name()).orElse(null);
		if (null == lastLaunchForProject) {
			return emptyResult;
		}
		ChartObject chartObject = processStatistics(lastLaunchForProject);
		return ImmutableMap.<String, List<ChartObject>>builder().put(RESULT, Collections.singletonList(chartObject)).build();
	}

	private ChartObject processStatistics(Launch lastLaunch) {
		ChartObject chartObject = new ChartObject();
		chartObject.setValues(ImmutableMap.<String, String>builder().put(TOTAL_FIELD,
				String.valueOf(lastLaunch.getStatistics().getExecutionCounter().getTotal())
		)
				.put(PASSED_FIELD, String.valueOf(lastLaunch.getStatistics().getExecutionCounter().getPassed()))
				.build());
		return chartObject;
	}

}
