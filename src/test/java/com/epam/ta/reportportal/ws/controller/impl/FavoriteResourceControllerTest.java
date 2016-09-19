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

package com.epam.ta.reportportal.ws.controller.impl;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;

import com.epam.ta.reportportal.auth.AuthConstants;
import com.epam.ta.reportportal.ws.BaseMvcTest;
import com.epam.ta.reportportal.ws.model.favorites.AddFavoriteResourceRQ;
import com.epam.ta.reportportal.ws.model.favorites.FavoriteResourceTypes;
import com.fasterxml.jackson.databind.ObjectMapper;

public class FavoriteResourceControllerTest extends BaseMvcTest {

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	public void addFavoriteResource() throws Exception {
		String resourceId = "520e1f3818127ca383464348";
		final AddFavoriteResourceRQ rq = new AddFavoriteResourceRQ();
		rq.setResourceId(resourceId);
		rq.setType(FavoriteResourceTypes.DASHBOARD);
		this.mvcMock.perform(post(PROJECT_BASE_URL + "/favorites").contentType(APPLICATION_JSON).content(objectMapper.writeValueAsBytes(rq))
				.principal(authentication())).andExpect(status().is(200));
	}

	@Test
	public void removeFromFavorites() throws Exception {
		this.mvcMock.perform(delete(PROJECT_BASE_URL + "/favorites?resource_id=520e1f3818127ca383339f44&resource_type=DASHBOARD")
				.principal(authentication())).andExpect(status().is(200));
	}

	@Override
	protected Authentication authentication() {
		return AuthConstants.ADMINISTRATOR;
	}
}