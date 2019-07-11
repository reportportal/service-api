/*
 * Copyright 2018 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.ta.reportportal.ws.converter.converters;

import com.epam.ta.reportportal.entity.statistics.Statistics;
import com.epam.ta.reportportal.ws.model.statistics.StatisticsResource;
import org.apache.commons.lang3.StringUtils;

import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.epam.ta.reportportal.dao.constant.WidgetContentRepositoryConstants.DEFECTS_KEY;
import static com.epam.ta.reportportal.dao.constant.WidgetContentRepositoryConstants.EXECUTIONS_KEY;
import static java.util.Optional.ofNullable;

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
				.filter(it -> ofNullable(it.getStatisticsField()).isPresent() && StringUtils.isNotEmpty(it.getStatisticsField().getName()))
				.filter(it -> it.getCounter() > 0 && it.getStatisticsField().getName().contains(DEFECTS_KEY))
				.collect(Collectors.groupingBy(it -> it.getStatisticsField().getName().split("\\$")[2],
						Collectors.groupingBy(it -> it.getStatisticsField().getName().split("\\$")[3],
								Collectors.summingInt(Statistics::getCounter)
						)
				)));
		statisticsResource.setExecutions(statistics.stream()
				.filter(it -> ofNullable(it.getStatisticsField()).isPresent() && StringUtils.isNotEmpty(it.getStatisticsField().getName()))
				.filter(it -> it.getCounter() > 0 && it.getStatisticsField().getName().contains(EXECUTIONS_KEY))
				.collect(Collectors.groupingBy(it -> it.getStatisticsField().getName().split("\\$")[2],
						Collectors.summingInt(Statistics::getCounter)
				)));
		return statisticsResource;

	};
}
