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
package com.epam.ta.reportportal.core.configs;

import com.epam.ta.reportportal.auth.permissions.ProjectAuthority;
import com.google.common.collect.ImmutableMap;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;

/**
 * @author Andrei Varabyeu
 */
public class ReportPortalAuthorityExtractorTest {

	@Test
	public void extractAuthorities() throws Exception {
		Map<String, Object> authorities = ImmutableMap.<String, Object>builder().put(
				"projects", ImmutableMap.builder().put("some_project", "PROJECT_MANAGER").build()).build();
		List<GrantedAuthority> grantedAuthorities = new SecurityConfiguration.ReportPortalAuthorityExtractor().extractAuthorities(
				authorities);

		Assert.assertThat(grantedAuthorities, hasItem(is(new ProjectAuthority("some_project", "PROJECT_MANAGER"))));
	}

}