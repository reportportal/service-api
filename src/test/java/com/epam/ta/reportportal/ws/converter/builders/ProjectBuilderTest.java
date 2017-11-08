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

package com.epam.ta.reportportal.ws.converter.builders;

import com.epam.ta.BaseTest;
import com.epam.ta.reportportal.database.entity.Project;
import com.epam.ta.reportportal.database.entity.project.EntryType;
import com.epam.ta.reportportal.ws.converter.converters.ProjectConverter;
import com.epam.ta.reportportal.ws.model.project.CreateProjectRQ;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

public class ProjectBuilderTest extends BaseTest {

	@Autowired
	private ApplicationContext applicationContext;

	@Test
	@Ignore
	public void testBeanScope() {
		Assert.assertTrue(
				"Test builder should be prototype bean because it's not stateless",
				applicationContext.isPrototype(applicationContext.getBeanNamesForType(ProjectConverter.class)[0])
		);
	}

	@Test
	public void testValues() {
		Project actualValue = ProjectConverter.TO_MODEL.apply(getCreateProjectRQ());
		validateProjects(getProject(), actualValue);
	}

	private Project getProject() {
		Project project = new Project();
		project.setAddInfo(BuilderTestsConstants.TAG);
		project.setCustomer(BuilderTestsConstants.NAME);
		project.getConfiguration().setEntryType(EntryType.valueOf(BuilderTestsConstants.ENTRY_TYPE));
		project.setName(BuilderTestsConstants.ID);
		return project;
	}

	private CreateProjectRQ getCreateProjectRQ() {
		CreateProjectRQ request = new CreateProjectRQ();
		request.setAddInfo(BuilderTestsConstants.TAG);
		request.setCustomer(BuilderTestsConstants.NAME);
		request.setProjectName(BuilderTestsConstants.ID);
		request.setEntryType(BuilderTestsConstants.ENTRY_TYPE);
		return request;
	}

	private void validateProjects(Project expectedValue, Project actualValue) {
		Assert.assertEquals(expectedValue.getAddInfo(), actualValue.getAddInfo());
		Assert.assertEquals(expectedValue.getCustomer(), actualValue.getCustomer());
		Assert.assertEquals(expectedValue.getId(), actualValue.getId());
		Assert.assertEquals(expectedValue.getName(), actualValue.getName());
		Assert.assertEquals(expectedValue.getUsers(), actualValue.getUsers());
		if ((null != expectedValue.getConfiguration().getEntryType()) && (null != actualValue.getConfiguration().getEntryType())) {
			Assert.assertEquals(expectedValue.getConfiguration().getEntryType(), actualValue.getConfiguration().getEntryType());
		}
	}

}