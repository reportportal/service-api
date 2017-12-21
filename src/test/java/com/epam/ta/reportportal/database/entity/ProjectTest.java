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

package com.epam.ta.reportportal.database.entity;

import org.junit.Assert;
import org.junit.Test;

import static org.hamcrest.Matchers.*;

/**
 * Validates stuff related to Project entity
 *
 * @author Andrei Varabyeu
 */
public class ProjectTest {

	@Test
	public void validatePMRole() {
		Assert.assertThat(ProjectRole.PROJECT_MANAGER, greaterThan(ProjectRole.CUSTOMER));
		Assert.assertThat(ProjectRole.MEMBER, lessThan(ProjectRole.PROJECT_MANAGER));
	}

	@Test
	public void validateWorkerRole() {
		Assert.assertThat(ProjectRole.MEMBER, greaterThan(ProjectRole.CUSTOMER));
		Assert.assertThat(ProjectRole.MEMBER, lessThan(ProjectRole.PROJECT_MANAGER));
	}

	@Test
	public void validateCustomerRole() {
		Assert.assertThat(ProjectRole.CUSTOMER, lessThan(ProjectRole.MEMBER));
	}

	@Test
	public void validateProjectManagerRole() {
		Assert.assertThat(ProjectRole.PROJECT_MANAGER, greaterThan(ProjectRole.OPERATOR));
	}

	@Test
	public void validateNullSaveUsers() {
		Assert.assertThat(new Project().getUsers(), notNullValue());
	}
}
