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
package com.epam.ta.reportportal.events.handler;

import com.epam.ta.reportportal.auth.AuthConstants;
import com.epam.ta.reportportal.database.dao.ActivityRepository;
import com.epam.ta.reportportal.database.entity.StatisticsCalculationStrategy;
import com.epam.ta.reportportal.database.entity.item.Activity;
import com.epam.ta.reportportal.database.entity.project.InterruptionJobDelay;
import com.epam.ta.reportportal.database.entity.project.KeepLogsDelay;
import com.epam.ta.reportportal.database.entity.project.KeepScreenshotsDelay;
import com.epam.ta.reportportal.database.search.Condition;
import com.epam.ta.reportportal.database.search.Filter;
import com.epam.ta.reportportal.database.search.FilterCondition;
import com.epam.ta.reportportal.ws.BaseMvcTest;
import com.epam.ta.reportportal.ws.model.project.ProjectConfiguration;
import com.epam.ta.reportportal.ws.model.project.UpdateProjectRQ;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import static com.epam.ta.reportportal.events.handler.ProjectActivityHandler.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author dzmitry_kavalets
 */
public class ProjectActivitiesListenerTest extends BaseMvcTest {

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private ActivityRepository activityRepository;

	@Test
	public void checkProjectActivitiesPositive() throws Exception {
		UpdateProjectRQ updateProjectRQ = new UpdateProjectRQ();
		ProjectConfiguration projectConfiguration = new ProjectConfiguration();
		projectConfiguration.setInterruptJobTime(InterruptionJobDelay.ONE_DAY.getValue());
		projectConfiguration.setKeepLogs(KeepLogsDelay.ONE_MONTH.getValue());
		projectConfiguration.setKeepScreenshots(KeepScreenshotsDelay.ONE_MONTH.getValue());
		projectConfiguration.setStatisticCalculationStrategy(StatisticsCalculationStrategy.TEST_BASED.name());
		projectConfiguration.setIsAutoAnalyzerEnabled(false);
		updateProjectRQ.setConfiguration(projectConfiguration);

		this.mvcMock.perform(put("/project/project1").content(objectMapper.writeValueAsBytes(updateProjectRQ))
				.contentType(MediaType.APPLICATION_JSON)
				.principal(authentication())).andExpect(status().is(200));

		Filter filter = new Filter(Activity.class, new HashSet<>(
				Arrays.asList(new FilterCondition(Condition.EQUALS, false, "project1", "projectRef"),
						new FilterCondition(Condition.EQUALS, false, "update_project", "actionType")
				)));
		List<Activity> activities = activityRepository.findByFilter(filter);
		List<Activity.FieldValues> history = activities.get(0).getHistory();

		List<String> fields = history.stream().map(Activity.FieldValues::getField).collect(Collectors.toList());
		Assert.assertTrue(fields.contains(LAUNCH_INACTIVITY));
		Assert.assertTrue(fields.contains(KEEP_LOGS));
		Assert.assertTrue(fields.contains(KEEP_SCREENSHOTS));
		Assert.assertTrue(fields.contains(AUTO_ANALYZE));
		Assert.assertTrue(fields.contains(STATISTICS_CALCULATION_STRATEGY));
	}

	@Override
	protected Authentication authentication() {
		return AuthConstants.ADMINISTRATOR;
	}
}