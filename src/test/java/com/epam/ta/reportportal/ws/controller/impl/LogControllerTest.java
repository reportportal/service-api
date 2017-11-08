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

package com.epam.ta.reportportal.ws.controller.impl;

import com.epam.ta.reportportal.auth.AuthConstants;
import com.epam.ta.reportportal.ws.BaseMvcTest;
import com.epam.ta.reportportal.ws.model.log.SaveLogRQ;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;

import java.util.Date;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Dzmitry_Kavalets
 */
public class LogControllerTest extends BaseMvcTest {
	@Autowired
	private ObjectMapper objectMapper;

	@SuppressWarnings("deprecation")
	@Test
	public void createLogPositive() throws Exception {
		SaveLogRQ rq = new SaveLogRQ();
		rq.setTestItemId("44524cc1553de753b3e5bb2f");
		rq.setLogTime(new Date(2014, 5, 7));
		this.mvcMock.perform(post(PROJECT_BASE_URL + "/log").principal(authentication())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(rq))).andExpect(status().isCreated());
	}

	@Test
	public void deleteLogPositive() throws Exception {
		this.mvcMock.perform(delete(PROJECT_BASE_URL + "/log/5187cba5553d2fdd93979773").principal(authentication()))
				.andExpect(status().is(200));
	}

	@Test
	public void getLogsPositive() throws Exception {
		this.mvcMock.perform(get(PROJECT_BASE_URL + "/log?filter.eq.item=44534cc1553de743b3e5aa33").principal(authentication()))
				.andExpect(status().is(200));
	}

	@Test
	public void getLogPositive() throws Exception {
		this.mvcMock.perform(get(PROJECT_BASE_URL + "/log/5187cba5553d2fdd93979773").principal(authentication()))
				.andExpect(status().is(200));
	}

	@Override
	protected Authentication authentication() {
		return AuthConstants.ADMINISTRATOR;
	}
}