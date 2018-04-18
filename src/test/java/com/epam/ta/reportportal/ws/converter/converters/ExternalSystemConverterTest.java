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

package com.epam.ta.reportportal.ws.converter.converters;

import com.epam.ta.reportportal.database.entity.AuthType;
import com.epam.ta.reportportal.database.entity.ExternalSystem;
import com.epam.ta.reportportal.ws.model.externalsystem.ExternalSystemResource;
import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Pavel_Bortnik
 */
public class ExternalSystemConverterTest {

	@Test(expected = NullPointerException.class)
	public void testNull() {
		ExternalSystemConverter.TO_RESOURCE.apply(null);
	}

	@Test
	public void testConvertToResource() {
		ExternalSystem externalSystem = new ExternalSystem();
		externalSystem.setId("id");
		externalSystem.setProjectRef("project");
		externalSystem.setProject("extProject");
		externalSystem.setExternalSystemType("JIRA");
		externalSystem.setAccessKey("a31aa6de3e27c11d90762cad11936727d6b0759e");
		externalSystem.setDomain("github.com");
		externalSystem.setExternalSystemAuth(AuthType.OAUTH);
		externalSystem.setFields(Lists.newArrayList());
		externalSystem.setUsername("user");
		externalSystem.setUrl("url");
		validate(externalSystem, ExternalSystemConverter.TO_RESOURCE.apply(externalSystem));
	}

	private void validate(ExternalSystem db, ExternalSystemResource resource) {
		Assert.assertEquals(db.getProjectRef(), resource.getProjectRef());
		Assert.assertEquals(db.getProject(), resource.getProject());
		Assert.assertEquals(db.getUsername(), resource.getUsername());
		Assert.assertEquals(db.getExternalSystemType(), resource.getExternalSystemType());
		Assert.assertEquals(db.getUrl(), resource.getUrl());
		Assert.assertEquals(db.getDomain(), resource.getDomain());
		Assert.assertEquals(db.getAccessKey(), resource.getAccessKey());
		Assert.assertEquals(db.getExternalSystemAuth().name(), resource.getExternalSystemAuth());
		Assert.assertEquals(db.getId(), resource.getSystemId());
		Assert.assertEquals(db.getFields(), resource.getFields());
	}
}