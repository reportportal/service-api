/*
 * Copyright 2016 EPAM Systems
 * 
 * 
 * This file is part of EPAM Report Portal.
 * https://github.com/epam/ReportPortal
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

package com.epam.ta.reportportal.core.events;

import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import java.lang.reflect.InvocationTargetException;

import com.epam.ta.reportportal.events.handler.AddDemoProjectEventHandler;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.epam.ta.BaseTest;
import com.epam.ta.reportportal.database.dao.ProjectRepository;
import com.epam.ta.reportportal.database.dao.UserRepository;
import com.epam.ta.reportportal.database.entity.Project;
import com.epam.ta.reportportal.database.entity.user.User;

/**
 * Tests for adding demo project event handler
 * 
 * @author Andrei Varabyeu
 * 
 */
public class AddDemoProjectEventHandlerTest extends BaseTest {

	@Autowired
	private ProjectRepository projectRepository;

	@Autowired
	private UserRepository userRepository;

	@BeforeClass
	public static void reinitStartContextFlag()
			throws SecurityException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
		AddDemoProjectEventHandler.setImportedFlag(false);
	}

	@Test
	@Ignore
	public void testAddingDefaultProjectOnStart() {
		User user = userRepository.findOne(AddDemoProjectEventHandler.DEFAULT_ADMIN.get().getLogin());
		Project defaultProject = projectRepository.findOne(AddDemoProjectEventHandler.DEFAULT_PROJECT.get().getId());

		Assert.assertThat("Default User shouldn't be null", user, not(nullValue()));
		Assert.assertThat("Default Project shouldn't be null", defaultProject, not(nullValue()));
		Assert.assertThat("Default Project should contain user", defaultProject.getUsers(), hasKey(user.getId()));
	}
}