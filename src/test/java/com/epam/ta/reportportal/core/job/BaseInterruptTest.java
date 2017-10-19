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

package com.epam.ta.reportportal.core.job;

import com.epam.ta.reportportal.database.dao.LaunchRepository;
import com.epam.ta.reportportal.database.dao.ProjectRepository;
import com.epam.ta.reportportal.database.dao.TestItemRepository;
import com.epam.ta.reportportal.database.entity.Launch;
import com.epam.ta.reportportal.database.entity.Project;
import com.epam.ta.reportportal.database.entity.StatisticsCalculationStrategy;
import com.epam.ta.reportportal.database.entity.Status;
import com.epam.ta.reportportal.database.entity.item.TestItem;
import com.epam.ta.reportportal.database.entity.item.TestItemType;
import com.epam.ta.reportportal.database.entity.project.InterruptionJobDelay;
import com.epam.ta.reportportal.database.entity.project.KeepLogsDelay;
import com.epam.ta.reportportal.database.entity.project.KeepScreenshotsDelay;
import com.epam.ta.reportportal.database.entity.statistics.ExecutionCounter;
import com.epam.ta.reportportal.database.entity.statistics.IssueCounter;
import com.epam.ta.reportportal.database.entity.statistics.Statistics;
import com.epam.ta.reportportal.job.InterruptBrokenLaunchesJob;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.bson.types.ObjectId;
import org.junit.Ignore;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Calendar;

/**
 * Base class for tests related to interrupt launches job. Contains several
 * convenience methods as well as injected beans<br>
 * Pay attention: mongo auditing and annotation-based job configuration is
 * disabled here. You have to start job manual if you want it to be executed
 *
 * @author Andrei Varabyeu
 */
// @RunWith(SpringJUnit4ClassRunner.class)
// @ContextConfiguration(locations = { "classpath:report-portal-ws-servlet.xml"
// })
// @ActiveProfiles({"unittest_noaudit", "epam"})
// @DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Ignore
public abstract class BaseInterruptTest {

	@Autowired
	protected InterruptBrokenLaunchesJob brokenLaunchesJob;

	@Autowired
	protected LaunchRepository launchRepository;

	@Autowired
	protected TestItemRepository testItemRepository;

	@Autowired
	protected ProjectRepository projectRepository;

	protected Launch insertLaunchInProgres() {
		String randomPart = RandomStringUtils.randomAlphabetic(5);

		Launch launch = new Launch();
		launch.setId(ObjectId.get().toString());
		launch.setStartTime(DateUtils.addDays(Calendar.getInstance().getTime(), -1));
		launch.setName("launch".concat(randomPart));
		launch.setDescription("description".concat(randomPart));
		launch.setStatus(Status.IN_PROGRESS);
		launch.setLastModified(DateUtils.addDays(Calendar.getInstance().getTime(), -1));
		launch.setStatistics(new Statistics(new ExecutionCounter(), new IssueCounter()));

		Project p = new Project();
		p.setName("test project");
		p.getConfiguration().setStatisticsCalculationStrategy(StatisticsCalculationStrategy.TEST_BASED);
		p.getConfiguration().setInterruptJobTime(InterruptionJobDelay.THREE_HOURS.getValue());
		p.getConfiguration().setKeepLogs(KeepLogsDelay.TWO_WEEKS.getValue());
		p.getConfiguration().setKeepScreenshots(KeepScreenshotsDelay.ONE_WEEK.getValue());

		projectRepository.save(p);

		launch.setProjectRef(p.getId());

		launchRepository.save(launch);

		return launch;
	}

	protected TestItem prepareTestItem(Launch launch) {
		String randomPart = RandomStringUtils.randomAlphabetic(5);

		TestItem item = new TestItem();
		item.setId(ObjectId.get().toString());
		item.setStartTime(DateUtils.addDays(Calendar.getInstance().getTime(), -1));
		item.setName("testItem".concat(randomPart));
		item.setLaunchRef(launch.getId());
		item.setStatus(Status.IN_PROGRESS);
		item.setLastModified(DateUtils.addDays(Calendar.getInstance().getTime(), -1));
		item.setStatistics(new Statistics(new ExecutionCounter(), new IssueCounter()));
		item.setType(TestItemType.TEST);

		return item;
	}
}