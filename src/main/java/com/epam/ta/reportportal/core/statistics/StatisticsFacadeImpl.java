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

package com.epam.ta.reportportal.core.statistics;

import com.epam.ta.reportportal.database.dao.LaunchRepository;
import com.epam.ta.reportportal.database.dao.ProjectRepository;
import com.epam.ta.reportportal.database.dao.TestItemRepository;
import com.epam.ta.reportportal.database.entity.Launch;
import com.epam.ta.reportportal.database.entity.Project;
import com.epam.ta.reportportal.database.entity.item.TestItem;
import com.epam.ta.reportportal.database.entity.statistics.ExecutionCounter;
import com.epam.ta.reportportal.database.entity.statistics.IssueCounter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Default implementation of {@link StatisticsFacade}
 *
 * @author Dzianis Shlychkou
 * @author Andrei_Ramanchuk
 */
@Service
public class StatisticsFacadeImpl implements StatisticsFacade {

	@Autowired
	protected TestItemRepository testItemRepository;

	@Autowired
	private LaunchRepository launchRepository;

	@Autowired
	private ProjectRepository projectRepository;

	@Override
	public TestItem updateExecutionStatistics(final TestItem testItem) {
		testItemRepository.updateExecutionStatistics(testItem);
		launchRepository.updateExecutionStatistics(testItem);
		return testItemRepository.findOne(testItem.getId());
	}

	@Override
	public TestItem updateIssueStatistics(final TestItem testItem) {
		Launch launch = launchRepository.findOne(testItem.getLaunchRef());
		Project project = projectRepository.findOne(launch.getProjectRef());
		testItemRepository.updateIssueStatistics(testItem, project.getConfiguration());
		launchRepository.updateIssueStatistics(testItem, project.getConfiguration());
		return testItemRepository.findOne(testItem.getId());
	}

	@Override
	public TestItem resetIssueStatistics(final TestItem testItem) {
		Launch launch = launchRepository.findOne(testItem.getLaunchRef());
		Project project = projectRepository.findOne(launch.getProjectRef());
		testItemRepository.resetIssueStatistics(testItem, project.getConfiguration());
		launchRepository.resetIssueStatistics(testItem, project.getConfiguration());
		return testItemRepository.findOne(testItem.getId());
	}

	@Override
	public TestItem resetExecutionStatistics(TestItem testItem) {
		testItemRepository.resetExecutionStatistics(testItem);
		launchRepository.resetExecutionStatistics(testItem);
		return testItemRepository.findOne(testItem.getId());
	}

	@Override
	public TestItem deleteIssueStatistics(TestItem testItem) {
		testItemRepository.deleteIssueStatistics(testItem);
		launchRepository.deleteIssueStatistics(testItem);
		return testItemRepository.findOne(testItem.getId());
	}

	@Override
	public TestItem deleteExecutionStatistics(TestItem testItem) {
		testItemRepository.deleteExecutionStatistics(testItem);
		launchRepository.deleteExecutionStatistics(testItem);
		return testItemRepository.findOne(testItem.getId());
	}

	@Override
	public void updateParentStatusFromStatistics(TestItem item) {
		item.setStatus(StatisticsHelper.getStatusFromStatistics(item.getStatistics()));
		testItemRepository.save(item);
		if (null != item.getParent()) {
			TestItem parent = testItemRepository.findOne(item.getParent());
			updateParentStatusFromStatistics(parent);
		}
	}

	@Override
	public void updateLaunchFromStatistics(Launch launch) {
		launch.setStatus(StatisticsHelper.getStatusFromStatistics(launch.getStatistics()));
		launchRepository.save(launch);
	}

	@Override
	public void recalculateStatistics(Launch launch) {
		deleteLaunchStatistics(launch);
		testItemRepository.findByLaunch(launch).forEach(this::recalculateTestItemStatistics);

		List<TestItem> withIssues = testItemRepository.findTestItemWithIssues(launch.getId());
		withIssues.forEach(this::updateIssueStatistics);
	}

	private void recalculateTestItemStatistics(TestItem item) {
		this.updateExecutionStatistics(item);
	}

	private void deleteLaunchStatistics(Launch launch) {
		testItemRepository.findByLaunch(launch).forEach(this::deleteTestItemStatistics);
		launch.getStatistics().setExecutionCounter(new ExecutionCounter(0, 0, 0, 0));
		launch.getStatistics().setIssueCounter(new IssueCounter());
		launchRepository.save(launch);
	}

	private void deleteTestItemStatistics(TestItem item) {
		item.getStatistics().setExecutionCounter(new ExecutionCounter(0, 0, 0, 0));
		item.getStatistics().setIssueCounter(new IssueCounter());
		testItemRepository.save(item);
	}

	@Override
	public boolean awareIssue(TestItem testItem) {
		return true;
	}

	@Override
	public TestItem identifyStatus(TestItem testItem) {
		testItem.setStatus(StatisticsHelper.getStatusFromStatistics(testItem.getStatistics()));
		return testItem;
	}
}