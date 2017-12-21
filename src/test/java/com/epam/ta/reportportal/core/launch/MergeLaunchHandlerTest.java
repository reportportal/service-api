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

package com.epam.ta.reportportal.core.launch;

import com.epam.ta.BaseTest;
import com.epam.ta.reportportal.core.item.merge.MergeTestItemHandler;
import com.epam.ta.reportportal.core.launch.impl.MergeLaunchHandler;
import com.epam.ta.reportportal.database.dao.LaunchRepository;
import com.epam.ta.reportportal.database.dao.TestItemRepository;
import com.epam.ta.reportportal.database.entity.Launch;
import com.epam.ta.reportportal.database.entity.Status;
import com.epam.ta.reportportal.database.entity.item.TestItem;
import com.epam.ta.reportportal.database.entity.statistics.ExecutionCounter;
import com.epam.ta.reportportal.database.entity.statistics.IssueCounter;
import com.epam.ta.reportportal.database.entity.statistics.Statistics;
import com.epam.ta.reportportal.database.fixture.SpringFixture;
import com.epam.ta.reportportal.database.fixture.SpringFixtureRule;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.item.MergeTestItemRQ;
import com.epam.ta.reportportal.ws.model.launch.LaunchResource;
import com.epam.ta.reportportal.ws.model.launch.MergeLaunchesRQ;
import com.epam.ta.reportportal.ws.model.launch.Mode;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import static com.epam.ta.reportportal.database.entity.statistics.IssueCounter.GROUP_TOTAL;

/**
 * @author Pavel Bortnik
 */

@SpringFixture("mergeLaunches")
public class MergeLaunchHandlerTest extends BaseTest {

	private static final String USER1 = "user1";
	private static final String PROJECT1 = "project1";
	private static final String MERGE_LAUNCH_1 = "51824cc1553de743b3e5bb2c";
	private static final String MERGE_LAUNCH_2 = "51824cc1553de743b3e5cc2c";
	private static final String MERGE_SUITE_1 = "44524cc1553de743b3e5bb30";
	private static final String MERGE_SUITE_2 = "595e4e7bb909d331fc392fc7";
	private static final String SAME_SUITE = "14524cc1553de743abc5bb30";
	private static final String IN_PROGRESS_ID = "51824cc1553de743b3e5aa2c";
	private static final String DIFF_PROJECT_LAUNCH_ID = "88624678053de743b3e5aa9e";
	private static final String NOT_OWNER = "customer";
	private static final String LAUNCH_NAME = "Merged";

	@Autowired
	private MergeLaunchHandler mergeLaunchHandler;

	@Autowired
	private MergeTestItemHandler mergeTestItemHandler;

	@Autowired
	private LaunchRepository launchRepository;

	@Autowired
	private TestItemRepository testItemRepository;

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Rule
	@Autowired
	public SpringFixtureRule dfRule;

	@Test
	public void emptyLaunchesList() {
		thrown.expect(ReportPortalException.class);
		thrown.expectMessage("Error in handled Request");
		mergeLaunchHandler.mergeLaunches(PROJECT1, USER1, getMergeRequest(new ArrayList<>()));
	}

	@Test
	public void nonFinishedLaunch() {
		thrown.expect(ReportPortalException.class);
		thrown.expectMessage("Unable to perform operation for non-finished launch.");
		mergeLaunchHandler.mergeLaunches(
				PROJECT1, USER1, getMergeRequest(ImmutableList.<String>builder().add(IN_PROGRESS_ID).add(MERGE_LAUNCH_1).build()));
	}

	@Test
	public void fromDifferentProjects() {
		thrown.expect(ReportPortalException.class);
		thrown.expectMessage("Impossible to merge launches from different projects.");
		mergeLaunchHandler.mergeLaunches(
				PROJECT1, USER1, getMergeRequest(ImmutableList.<String>builder().add(DIFF_PROJECT_LAUNCH_ID).add(MERGE_LAUNCH_1).build()));
	}

	@Test
	public void notOwner() {
		thrown.expect(ReportPortalException.class);
		thrown.expectMessage("You are not an owner of launches");
		mergeLaunchHandler.mergeLaunches(
				PROJECT1, NOT_OWNER, getMergeRequest(ImmutableList.<String>builder().add(MERGE_LAUNCH_1).add(MERGE_LAUNCH_2).build()));
	}

	@Test
	public void mergeItselfLaunch() {
		thrown.expect(ReportPortalException.class);
		thrown.expectMessage("Error in handled Request. Please, check specified parameters:");

		mergeLaunchHandler.mergeLaunches(PROJECT1, USER1, getDeepMergeRequest(ImmutableList.<String>builder().add(MERGE_LAUNCH_2).build()));
	}

	@Test
	public void mergeLaunches() {
		ImmutableList<String> ids = ImmutableList.<String>builder().add(MERGE_LAUNCH_1).add(MERGE_LAUNCH_2).build();
		LaunchResource launchResource = mergeLaunchHandler.mergeLaunches(PROJECT1, USER1, getMergeRequest(ids));
		Assert.assertTrue(launchRepository.find(ids).isEmpty());
		Launch launch = launchRepository.findOne(launchResource.getLaunchId());
		Assert.assertEquals(launch, expectedMergedLaunch(launch.getId(), launch.getLastModified()));
		List<TestItem> items = testItemRepository.findByLaunch(launch);
		Assert.assertEquals(items.size(), 9);
		items.forEach(it -> Assert.assertEquals(it.getLaunchRef(), launch.getId()));
	}

	@Test
	public void deepMergeLaunches() throws Exception {
		ImmutableList<String> ids = ImmutableList.<String>builder().add(MERGE_LAUNCH_2).add(MERGE_LAUNCH_1).build();
		LaunchResource launchResource = mergeLaunchHandler.mergeLaunches(PROJECT1, USER1, getDeepMergeRequest(ids));
		Assert.assertTrue(launchRepository.find(ids).isEmpty());
		Launch launch = launchRepository.findOne(launchResource.getLaunchId());

		Assert.assertEquals(expectedDeepMergedLaunch(launch.getId(), launch.getLastModified()), launch);
		List<TestItem> items = testItemRepository.findByLaunch(launch);
		Assert.assertEquals(7, items.size());
		items.forEach(it -> Assert.assertEquals(it.getLaunchRef(), launch.getId()));
		TestItem itemWithTags = items.stream().filter(it -> it.getTags() != null).collect(Collectors.toList()).get(0);
		Assert.assertTrue(itemWithTags.getTags().containsAll(ImmutableList.<String>builder().add("ios").add("andr").build()));
		Assert.assertTrue(itemWithTags.getItemDescription().startsWith("suite for history validation2"));
		String startDate = "Thu May 02 14:13:00 MSK 2013";
		String endDate = "Thu May 02 14:43:00 MSK 2013";
		DateFormat df = new SimpleDateFormat("EEE MMM dd kk:mm:ss zzz yyyy");
		Date start = df.parse(startDate);
		Date end = df.parse(endDate);
		Assert.assertEquals(itemWithTags.getStartTime(), start);
		Assert.assertEquals(itemWithTags.getEndTime(), end);
	}

	@Test
	public void mergeSuits() {
		MergeTestItemRQ mergeTestItemRQ = new MergeTestItemRQ();
		mergeTestItemRQ.setMergeStrategyType("SUITE");
		mergeTestItemRQ.setItems(ImmutableList.<String>builder().add(MERGE_SUITE_2).add(SAME_SUITE).build());
		mergeTestItemHandler.mergeTestItem(PROJECT1, MERGE_SUITE_1, mergeTestItemRQ, USER1);
		TestItem item = testItemRepository.findOne(MERGE_SUITE_1);
		Assert.assertTrue(item.getTags().containsAll(ImmutableList.<String>builder().add("ios").add("andr").build()));
		Assert.assertEquals(testItemRepository.findAllDescendants(item.getId()).size(), 3);
	}

	private MergeLaunchesRQ getMergeRequest(List<String> launches) {
		MergeLaunchesRQ mergeLaunchesRQ = new MergeLaunchesRQ();
		mergeLaunchesRQ.setLaunches(new HashSet<>(launches));
		mergeLaunchesRQ.setStartTime(new Date(0));
		mergeLaunchesRQ.setEndTime(new Date(1000));
		mergeLaunchesRQ.setMode(Mode.DEFAULT);
		mergeLaunchesRQ.setMergeStrategyType("BASIC");
		mergeLaunchesRQ.setName("Result");
		mergeLaunchesRQ.setTags(ImmutableSet.<String>builder().add("IOS").add("Android").build());
		mergeLaunchesRQ.setDescription("Description");
		return mergeLaunchesRQ;
	}

	private MergeLaunchesRQ getDeepMergeRequest(List<String> launches) {
		MergeLaunchesRQ deepMergeLaunchesRQ = new MergeLaunchesRQ();
		deepMergeLaunchesRQ.setLaunches(new HashSet<>(launches));
		deepMergeLaunchesRQ.setStartTime(new Date(0));
		deepMergeLaunchesRQ.setEndTime(new Date(1000));
		deepMergeLaunchesRQ.setMode(Mode.DEFAULT);
		deepMergeLaunchesRQ.setName(LAUNCH_NAME);
		deepMergeLaunchesRQ.setTags(ImmutableSet.<String>builder().add("IOS").add("Android").build());
		deepMergeLaunchesRQ.setDescription("Description");
		deepMergeLaunchesRQ.setMergeStrategyType("DEEP");
		return deepMergeLaunchesRQ;
	}

	private Launch expectedMergedLaunch(String id, Date lastModified) {
		Launch launch = new Launch();
		launch.setId(id);
		launch.setProjectRef(PROJECT1);
		launch.setUserRef(USER1);
		launch.setStatus(Status.FAILED);
		launch.setStartTime(new Date(0));
		launch.setEndTime(new Date(1000));
		launch.setLastModified(lastModified);
		launch.setMode(Mode.DEFAULT);
		launch.setName("Result");
		launch.setTags(ImmutableSet.<String>builder().add("IOS").add("Android").build());
		launch.setDescription("Description");
		launch.setNumber(1l);
		ExecutionCounter executionCounter = new ExecutionCounter(3, 2, 1, 0);
		IssueCounter issueCounter = new IssueCounter();
		issueCounter.setToInvestigate("TI001", 1);
		issueCounter.setToInvestigate(GROUP_TOTAL, 1);
		Statistics statistics = new Statistics(executionCounter, issueCounter);
		launch.setStatistics(statistics);
		return launch;
	}

	private Launch expectedDeepMergedLaunch(String id, Date lastModified) {
		Launch launch = new Launch();
		launch.setId(id);
		launch.setProjectRef(PROJECT1);
		launch.setUserRef(USER1);
		launch.setStatus(Status.FAILED);
		launch.setStartTime(new Date(0));
		launch.setEndTime(new Date(1000));
		launch.setLastModified(lastModified);
		launch.setMode(Mode.DEFAULT);
		launch.setName("Merged");
		launch.setTags(ImmutableSet.<String>builder().add("IOS").add("Android").build());
		launch.setDescription("Description");
		launch.setNumber(1l);
		ExecutionCounter executionCounter = new ExecutionCounter(3, 2, 1, 0);
		IssueCounter issueCounter = new IssueCounter();
		issueCounter.setToInvestigate("TI001", 1);
		issueCounter.setToInvestigate(GROUP_TOTAL, 1);
		Statistics statistics = new Statistics(executionCounter, issueCounter);
		launch.setStatistics(statistics);
		return launch;
	}
}