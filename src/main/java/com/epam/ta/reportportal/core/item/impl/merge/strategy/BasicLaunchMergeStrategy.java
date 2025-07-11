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
import com.epam.ta.reportportal.core.item.identity.TestItemUniqueIdGenerator;
import com.epam.ta.reportportal.dao.AttachmentRepository;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.LogRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.organization.MembershipDetails;
import com.epam.ta.reportportal.ws.reporting.MergeLaunchesRQ;
import java.util.List;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class BasicLaunchMergeStrategy extends AbstractLaunchMergeStrategy {

  private final StatisticsCalculationFactory statisticsCalculationFactory;

  public BasicLaunchMergeStrategy(LaunchRepository launchRepository,
      TestItemRepository testItemRepository, LogRepository logRepository,
      AttachmentRepository attachmentRepository, TestItemUniqueIdGenerator identifierGenerator,
      StatisticsCalculationFactory statisticsCalculationFactory) {
    super(launchRepository, testItemRepository, logRepository, attachmentRepository,
        identifierGenerator
    );
    this.statisticsCalculationFactory = statisticsCalculationFactory;
  }

  @Override
  public Launch mergeLaunches(MembershipDetails membershipDetails, ReportPortalUser user,
      MergeLaunchesRQ rq, List<Launch> launchesList) {

    Launch newLaunch = createNewLaunch(membershipDetails, user, rq, launchesList);

    newLaunch.setStatistics(statisticsCalculationFactory.getStrategy(MergeStrategyType.BASIC)
        .recalculateLaunchStatistics(newLaunch, launchesList));

    launchRepository.save(newLaunch);
    return newLaunch;

  }
}
