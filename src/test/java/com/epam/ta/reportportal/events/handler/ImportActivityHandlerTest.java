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

package com.epam.ta.reportportal.events.handler;

import com.epam.ta.reportportal.auth.AuthConstants;
import com.epam.ta.reportportal.database.dao.ActivityRepository;
import com.epam.ta.reportportal.database.entity.item.Activity;
import com.epam.ta.reportportal.database.entity.item.ActivityEventType;
import com.epam.ta.reportportal.database.search.Condition;
import com.epam.ta.reportportal.database.search.Filter;
import com.epam.ta.reportportal.database.search.FilterCondition;
import com.epam.ta.reportportal.ws.BaseMvcTest;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.fileUpload;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Pavel Bortnik
 */
public class ImportActivityHandlerTest extends BaseMvcTest {

	@Autowired
	private ActivityRepository activityRepository;

	@Test
	public void onImportEvents() throws Exception {
		Path file = Paths.get("src/test/resources/test-results.zip");
		MockMultipartFile multipartFile = new MockMultipartFile("file", "test-results.zip", "application/zip", Files.readAllBytes(file));

		this.mvcMock.perform(fileUpload("/project1/launch/import").file(multipartFile)
				.contentType(MediaType.APPLICATION_JSON)
				.principal(authentication())).andExpect(status().is(200));

		String activityTypes = ActivityEventType.START_IMPORT.getValue() + "," + ActivityEventType.START_LAUNCH.getValue() + ","
				+ ActivityEventType.FINISH_IMPORT.getValue() + "," + ActivityEventType.FINISH_LAUNCH.getValue();
		Filter filter = new Filter(Activity.class, new HashSet<>(
				Arrays.asList(new FilterCondition(Condition.EQUALS, false, "project1", "projectRef"),
						new FilterCondition(Condition.IN, false, activityTypes, "actionType")
				)));
		List<Activity> activities = activityRepository.findByFilter(filter);
		List<String> newValues = activities.stream()
				.map(Activity::getHistory)
				.flatMap(Collection::stream)
				.map(Activity.FieldValues::getNewValue)
				.collect(toList());

		Assert.assertEquals("Some activities were not created", 4, activities.size());
		Assert.assertTrue("History doesn't contain file name value", newValues.contains("test-results.zip"));
		Assert.assertTrue("History doesn't contain launch name value", newValues.contains("test-results #1"));
	}

	@Override
	protected Authentication authentication() {
		return AuthConstants.ADMINISTRATOR;
	}
}