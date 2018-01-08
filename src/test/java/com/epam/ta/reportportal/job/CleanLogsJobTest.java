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

package com.epam.ta.reportportal.job;

import com.epam.ta.reportportal.database.dao.*;
import com.epam.ta.reportportal.database.entity.Launch;
import com.epam.ta.reportportal.database.entity.Project;
import com.epam.ta.reportportal.database.entity.item.TestItem;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.time.Duration;
import java.util.Date;
import java.util.stream.Stream;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Created by Andrey_Ivanov1 on 31-May-17.
 */

@RunWith(SpringJUnit4ClassRunner.class)
public class CleanLogsJobTest {

	@InjectMocks
	private CleanLogsJob cleanLogsJob = new CleanLogsJob();
	@Mock
	private LogRepository logRepo;
	@Mock
	private LaunchRepository launchRepo;
	@Mock
	private TestItemRepository testItemRepo;
	@Mock
	private ProjectRepository projectRepository;
	@Mock
	private ActivityRepository activityRepository;

	@Test
	public void runTest() {
		String name = "name";
		Project project = new Project();
		Project.Configuration configuration = new Project.Configuration();

		configuration.setKeepLogs("1 month");
		project.setName(name);
		project.setConfiguration(configuration);

		Stream<Project> sp = Stream.of(project);

		Launch launch = new Launch();
		launch.setId(name);
		Stream<Launch> sl = Stream.of(launch);

		TestItem testItem = new TestItem();
		Stream<TestItem> st = Stream.of(testItem);

		when(projectRepository.streamAllIdsAndConfiguration()).thenReturn(sp);
		when(launchRepo.streamModifiedInRange(anyString(), any(Date.class), any(Date.class))).thenReturn(sl);
		when(testItemRepo.streamIdsByLaunch(anyString())).thenReturn(st);

		cleanLogsJob.execute(null);

		verify(activityRepository, times(1)).deleteModifiedLaterAgo(anyString(), any(Duration.class));
		verify(logRepo, times(1)).deleteByPeriodAndItemsRef(any(Duration.class), anyListOf(String.class));
	}

}
