/*
 * Copyright 2019 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.ta.reportportal.ws.controller;

import com.epam.ta.reportportal.ws.BaseMvcTest;
import com.epam.ta.reportportal.ws.model.log.SaveLogRQ;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@Sql({ "/db/test-item/test-item-fill.sql", "/db/log/log-fill.sql" })
class LogControllerTest extends BaseMvcTest {

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void createLogPositive() throws Exception {
		SaveLogRQ rq = new SaveLogRQ();
		rq.setLaunchUuid(UUID.randomUUID().toString());
		rq.setItemUuid("f3960757-1a06-405e-9eb7-607c34683154");
		rq.setLevel("ERROR");
		rq.setMessage("log message");
		rq.setLogTime(Date.from(LocalDateTime.now().atZone(ZoneId.of("UTC")).toInstant()));
		mockMvc.perform(post(DEFAULT_PROJECT_BASE_URL + "/log").with(token(oAuthHelper.getDefaultToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(rq))).andExpect(status().isCreated());
	}

	@Test
	void createLogEntryPositive() throws Exception {
		SaveLogRQ rq = new SaveLogRQ();
		rq.setLaunchUuid(UUID.randomUUID().toString());
		rq.setItemUuid("f3960757-1a06-405e-9eb7-607c34683154");
		rq.setLevel("ERROR");
		rq.setMessage("log message");
		rq.setLogTime(Date.from(LocalDateTime.now().atZone(ZoneId.of("UTC")).toInstant()));
		mockMvc.perform(post(DEFAULT_PROJECT_BASE_URL + "/log/entry").with(token(oAuthHelper.getDefaultToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(rq))).andExpect(status().isCreated());
	}

	@Test
	void deleteLogPositive() throws Exception {
		mockMvc.perform(delete(DEFAULT_PROJECT_BASE_URL + "/log/1").with(token(oAuthHelper.getDefaultToken()))).andExpect(status().isOk());
	}

	@Test
	void getLogsPositive() throws Exception {
		mockMvc.perform(get(DEFAULT_PROJECT_BASE_URL + "/log?filter.eq.item=2").with(token(oAuthHelper.getDefaultToken())))
				.andExpect(status().isOk());
	}

	@Test
	void getLogPositive() throws Exception {
		mockMvc.perform(get(DEFAULT_PROJECT_BASE_URL + "/log/2").with(token(oAuthHelper.getDefaultToken()))).andExpect(status().isOk());
	}

	@Test
	void getLogStringPositive() throws Exception {
		mockMvc.perform(get(DEFAULT_PROJECT_BASE_URL + "/log/9ba98f41-2cde-4510-8503-d8eda901cc71").with(token(oAuthHelper.getDefaultToken()))).andExpect(status().isOk());
	}

	@Test
	void getLogUuidPositive() throws Exception {
		mockMvc.perform(get(DEFAULT_PROJECT_BASE_URL + "/log/uuid/9ba98f41-2cde-4510-8503-d8eda901cc71").with(token(oAuthHelper.getDefaultToken()))).andExpect(status().isOk());
	}

	@Test
	void getLogNegative() throws Exception {
		mockMvc.perform(get(DEFAULT_PROJECT_BASE_URL + "/log/100").with(token(oAuthHelper.getDefaultToken())))
				.andExpect(status().isNotFound());
	}
}