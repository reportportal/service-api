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
import com.epam.ta.reportportal.core.launch.impl.MergeLaunchHandler;
import com.epam.ta.reportportal.database.dao.LaunchRepository;
import com.epam.ta.reportportal.database.entity.Launch;
import com.epam.ta.reportportal.database.entity.Status;
import com.epam.ta.reportportal.database.entity.statistics.ExecutionCounter;
import com.epam.ta.reportportal.database.entity.statistics.IssueCounter;
import com.epam.ta.reportportal.database.entity.statistics.Statistics;
import com.epam.ta.reportportal.database.fixture.SpringFixture;
import com.epam.ta.reportportal.database.fixture.SpringFixtureRule;
import com.epam.ta.reportportal.exception.ReportPortalException;
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

import java.util.ArrayList;
import java.util.Date;
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
    private static final String IN_PROGRESS_ID = "51824cc1553de743b3e5aa2c";
    private static final String DIFF_PROJECT_LAUNCH_ID = "88624678053de743b3e5aa9e";
    private static final String NOT_OWNER = "customer";

    @Autowired
    private MergeLaunchHandler mergeLaunchHandler;

    @Autowired
    private LaunchRepository launchRepository;

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
        List<Launch> launchList = launchRepository.find(ImmutableList.<String>builder().add(IN_PROGRESS_ID).build());
        mergeLaunchHandler.mergeLaunches(PROJECT1, USER1, getMergeRequest(launchList));
    }

    @Test
    public void fromDifferentProjects() {
        thrown.expect(ReportPortalException.class);
        thrown.expectMessage("Impossible to merge launches from different projects.");
        List<Launch> launchList = launchRepository.find(ImmutableList.<String>builder().add(DIFF_PROJECT_LAUNCH_ID).build());
        mergeLaunchHandler.mergeLaunches(PROJECT1, USER1, getMergeRequest(launchList));
    }

    @Test
    public void notOwner() {
        thrown.expect(ReportPortalException.class);
        thrown.expectMessage("You are not an owner of launches");
        List<Launch> launchList = launchRepository.find(ImmutableList.<String>builder().add(MERGE_LAUNCH_1).build());
        mergeLaunchHandler.mergeLaunches(PROJECT1, NOT_OWNER, getMergeRequest(launchList));
    }

    @Test
    public void mergeLaunches() {
        ImmutableList<String> ids = ImmutableList.<String>builder().add(MERGE_LAUNCH_1).add(MERGE_LAUNCH_2).build();
        List<Launch> launchList = launchRepository.find(ids);
        LaunchResource launchResource = mergeLaunchHandler.mergeLaunches(PROJECT1, USER1, getMergeRequest(launchList));
        Assert.assertTrue(launchRepository.find(ids).isEmpty());
        Launch launch = launchRepository.findOne(launchResource.getLaunchId());
        Assert.assertEquals(launch, expectedLaunch(launch.getId(), launch.getLastModified()));
        Assert.assertNotNull(launchResource);
    }

    private MergeLaunchesRQ getMergeRequest(List<Launch> launches) {
        MergeLaunchesRQ mergeLaunchesRQ = new MergeLaunchesRQ();
        mergeLaunchesRQ.setLaunches(launches.stream().map(Launch::getId).collect(Collectors.toSet()));
        mergeLaunchesRQ.setStartTime(new Date(0));
        mergeLaunchesRQ.setEndTime(new Date(1000));
        mergeLaunchesRQ.setMode(Mode.DEFAULT);
        mergeLaunchesRQ.setName("Result");
        mergeLaunchesRQ.setTags(ImmutableSet.<String>builder().add("IOS").add("Android").build());
        mergeLaunchesRQ.setDescription("Description");
        return mergeLaunchesRQ;
    }

    private Launch expectedLaunch(String id, Date lastModified){
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
        ExecutionCounter executionCounter = new ExecutionCounter(2, 1, 1, 0);
        IssueCounter issueCounter = new IssueCounter();
        issueCounter.setToInvestigate("TI001", 1);
        issueCounter.setToInvestigate(GROUP_TOTAL, 1);
        Statistics statistics = new Statistics(executionCounter, issueCounter);
        launch.setStatistics(statistics);
        return launch;
    }

}