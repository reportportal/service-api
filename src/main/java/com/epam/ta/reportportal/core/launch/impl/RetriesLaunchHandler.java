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

import com.epam.ta.reportportal.commons.Preconditions;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

	@Override
	public void handleRetries(Launch launch) {
		if (isTrue(launch.getHasRetries())) {
			Project project = projectRepository.findOne(launch.getProjectRef());
			StatisticsFacade statisticsFacade = statisticsFacadeFactory.getStatisticsFacade(
					project.getConfiguration().getStatisticsCalculationStrategy());

			List<RetryObject> retries = testItemRepository.findRetries(launch.getId());
			expect(retries, Preconditions.NOT_EMPTY_COLLECTION).verify(
					ErrorType.RETRIES_HANDLER_ERROR, "There are no retries in the launch.");

			retries.forEach(retry -> handleRetry(retry, statisticsFacade));
		}
	}

	/**
	 * Process aggregated retry object
	 *
	 * @param retry            Retry object
	 * @param statisticsFacade Statistics facade
	 */
	private void handleRetry(RetryObject retry, StatisticsFacade statisticsFacade) {
		List<TestItem> retries = retry.getRetries();
		expect((retries.size() >= MINIMUM_RETRIES_COUNT), isEqual(true)).verify(
				ErrorType.RETRIES_HANDLER_ERROR, "Minimum retries count is " + MINIMUM_RETRIES_COUNT);
		TestItem lastRetry = moveRetries(retries, statisticsFacade);
		testItemRepository.delete(retries);
		testItemRepository.save(lastRetry);
	}

	/**
	 * Resets retry statistics
	 *
	 * @param retry            Retry to be reseted
	 * @param statisticsFacade Statistics facade
	 */
	private void resetRetryStatistics(TestItem retry, StatisticsFacade statisticsFacade) {
		statisticsFacade.resetExecutionStatistics(retry);
		if (retry.getIssue() != null) {
			statisticsFacade.resetIssueStatistics(retry);
		}
	}

	/**
	 * Move all retries from test item collection into a embedded collection
	 * inside the last retry
	 *
	 * @param retries          Retries to be processed
	 * @param statisticsFacade Statistics facade
	 * @return Last retry
	 */
	private TestItem moveRetries(List<TestItem> retries, StatisticsFacade statisticsFacade) {
		retries.forEach(it -> it.setRetryProcessed(Boolean.TRUE));
		TestItem retryRoot = retries.get(0);
		resetRetryStatistics(retryRoot, statisticsFacade);

		TestItem lastRetry = retries.get(retries.size() - 1);
		retries.remove(retries.size() - 1);
		lastRetry.setStartTime(retryRoot.getStartTime());
		lastRetry.setRetries(retries);
		lastRetry = updateRetryStatistics(lastRetry, retryRoot, statisticsFacade);
		return lastRetry;
	}

	/**
	 * Updates statistics of last retry using info from the retry root
	 *
	 * @param lastRetry        Last retry
	 * @param retryRoot        Retry root
	 * @param statisticsFacade Statistics facade
	 * @return Updated last retry
	 */
	private TestItem updateRetryStatistics(TestItem lastRetry, TestItem retryRoot, StatisticsFacade statisticsFacade) {
		if (!lastRetry.getStatus().equals(Status.PASSED)) {
			lastRetry.setIssue(retryRoot.getIssue());
		}
		statisticsFacade.updateExecutionStatistics(lastRetry);
		if (lastRetry.getIssue() != null) {
			statisticsFacade.updateIssueStatistics(lastRetry);
		}
		return lastRetry;
	}

}
