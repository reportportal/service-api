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

package com.epam.ta.reportportal.ws.converter.converters;

import com.epam.ta.reportportal.entity.statistics.Statistics;
import com.epam.ta.reportportal.ws.model.statistics.StatisticsResource;

import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.epam.ta.reportportal.dao.constant.WidgetContentRepositoryConstants.DEFECTS_KEY;
import static com.epam.ta.reportportal.dao.constant.WidgetContentRepositoryConstants.EXECUTIONS_KEY;

/**
 * @author Pavel Bortnik
 */
public final class StatisticsConverter {

	private StatisticsConverter() {
		//static only
	}

	public static final Function<Set<Statistics>, StatisticsResource> TO_RESOURCE = statistics -> {
		StatisticsResource statisticsResource = new StatisticsResource();
		statisticsResource.setDefects(statistics.stream()
				.filter(it -> it.getCounter() > 0 && it.getField().contains(DEFECTS_KEY))
				.collect(Collectors.groupingBy(
						it -> it.getField().split("\\$")[2],
						Collectors.groupingBy(it -> it.getField().split("\\$")[3], Collectors.summingInt(Statistics::getCounter))
				)));
		statisticsResource.setExecutions(statistics.stream()
				.filter(it -> it.getCounter() > 0 && it.getField().contains(EXECUTIONS_KEY))
				.collect(Collectors.groupingBy(it -> it.getField().split("\\$")[2], Collectors.summingInt(Statistics::getCounter))));
		return statisticsResource;

	};
}
