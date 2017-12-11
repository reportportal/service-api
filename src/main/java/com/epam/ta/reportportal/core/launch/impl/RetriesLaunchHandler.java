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

package com.epam.ta.reportportal.core.launch.impl;

import com.epam.ta.reportportal.core.analyzer.ILogIndexer;
import com.epam.ta.reportportal.core.launch.IRetriesLaunchHandler;
import com.epam.ta.reportportal.core.statistics.StatisticsFacade;
import com.epam.ta.reportportal.core.statistics.StatisticsFacadeFactory;
import com.epam.ta.reportportal.database.dao.ProjectRepository;
import com.epam.ta.reportportal.database.dao.TestItemRepository;
import com.epam.ta.reportportal.database.entity.Launch;
import com.epam.ta.reportportal.database.entity.Project;
import com.epam.ta.reportportal.database.entity.Status;
import com.epam.ta.reportportal.database.entity.history.status.RetryObject;
import com.epam.ta.reportportal.database.entity.item.TestItem;
import com.epam.ta.reportportal.ws.model.ErrorType;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static java.util.function.Predicate.isEqual;
import static org.apache.commons.lang3.BooleanUtils.isTrue;

/**
 * @author Pavel Bortnik
 */
@Service
public class RetriesLaunchHandler implements IRetriesLaunchHandler {

	private static final int MINIMUM_RETRIES_COUNT = 2;

	@Autowired
	private ProjectRepository projectRepository;

	@Autowired
	private TestItemRepository testItemRepository;

	@Autowired
	private StatisticsFacadeFactory statisticsFacadeFactory;

	@Autowired
	private ILogIndexer logIndexer;

	@Override
	public void collectRetries(Launch launch) {
		if (isTrue(launch.getHasRetries())) {
			Project project = projectRepository.findOne(launch.getProjectRef());
			StatisticsFacade statisticsFacade = statisticsFacadeFactory.getStatisticsFacade(
					project.getConfiguration().getStatisticsCalculationStrategy());

			List<RetryObject> retries = testItemRepository.findRetries(launch.getId());

			expect(CollectionUtils.isEmpty(retries), isEqual(false)).verify(
					ErrorType.FORBIDDEN_OPERATION, "Retries should exist for launch with 'hasRetries' flag.");

			retries.forEach(retry -> {
				List<TestItem> rtr = retry.getRetries();

				expect((retries.size() >= MINIMUM_RETRIES_COUNT), isEqual(true)).verify(
						ErrorType.FORBIDDEN_OPERATION, "Minimum retries' count is " + MINIMUM_RETRIES_COUNT);

				TestItem retryRoot = rtr.get(0);

				logIndexer.cleanIndex(project.getId(), Collections.singletonList(retryRoot.getId()));

				statisticsFacade.resetExecutionStatistics(retryRoot);
				if (retryRoot.getIssue() != null) {
					statisticsFacade.resetIssueStatistics(retryRoot);
				}

				TestItem lastRetry = rtr.get(rtr.size() - 1);
				rtr.remove(rtr.size() - 1);
				lastRetry.setRetryProcessed(Boolean.TRUE);
				lastRetry.setRetries(rtr);
				lastRetry.setParent(retryRoot.getParent());
				testItemRepository.delete(rtr);

				if (!lastRetry.getStatus().equals(Status.PASSED)) {
					lastRetry.setIssue(retryRoot.getIssue());
				}
				statisticsFacade.updateExecutionStatistics(lastRetry);
				if (lastRetry.getIssue() != null) {
					statisticsFacade.updateIssueStatistics(lastRetry);
				}
				testItemRepository.save(lastRetry);
				logIndexer.indexLogs(launch.getId(), Collections.singletonList(lastRetry));
			});
		}
	}
}
