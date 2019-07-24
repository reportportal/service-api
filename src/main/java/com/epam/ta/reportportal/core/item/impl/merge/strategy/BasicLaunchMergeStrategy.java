/*
 * Copyright 2019 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.ta.reportportal.core.item.impl.merge.strategy;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.item.impl.TestItemUniqueIdGenerator;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.ws.model.launch.MergeLaunchesRQ;

import java.util.List;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class BasicLaunchMergeStrategy extends AbstractLaunchMergeStrategy {

	private final StatisticsCalculationFactory statisticsCalculationFactory;

	public BasicLaunchMergeStrategy(TestItemRepository testItemRepository, TestItemUniqueIdGenerator identifierGenerator,
			LaunchRepository launchRepository, StatisticsCalculationFactory statisticsCalculationFactory) {
		super(testItemRepository, identifierGenerator, launchRepository);
		this.statisticsCalculationFactory = statisticsCalculationFactory;
	}

	@Override
	public Launch mergeLaunches(ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user, MergeLaunchesRQ rq,
			List<Launch> launchesList) {

		Launch newLaunch = createNewLaunch(projectDetails, user, rq, launchesList);

		newLaunch.setStatistics(statisticsCalculationFactory.getStrategy(MergeStrategyType.BASIC)
				.recalculateLaunchStatistics(newLaunch, launchesList));

		return newLaunch;

	}
}
