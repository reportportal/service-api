/*
 *
 *  Copyright (C) 2018 EPAM Systems
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.epam.ta.reportportal.core.item.impl.merge.strategy;

import com.epam.ta.reportportal.core.item.merge.StatisticsCalculationStrategy;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.statistics.Statistics;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toMap;

/**
 * @author Ivan Budaev
 */
public class BasicStatisticsCalculationStrategy implements StatisticsCalculationStrategy {

	@Override
	public Set<Statistics> recalculateLaunchStatistics(Launch newLaunch, Collection<Launch> launches) {
		return launches.stream()
				.filter(l -> ofNullable(l.getStatistics()).isPresent())
				.flatMap(l -> l.getStatistics().stream())
				.filter(s -> ofNullable(s.getStatisticsField()).isPresent())
				.collect(toMap(Statistics::getStatisticsField, Statistics::getCounter, Integer::sum))
				.entrySet()
				.stream()
				.map(entry -> new Statistics(entry.getKey(), entry.getValue(), newLaunch.getId()))
				.collect(Collectors.toSet());
	}
}
