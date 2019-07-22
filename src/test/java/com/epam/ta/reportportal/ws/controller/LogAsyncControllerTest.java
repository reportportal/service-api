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

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
class LogAsyncControllerTest extends BaseMvcTest {

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void createLog() throws Exception {
		SaveLogRQ rq = new SaveLogRQ();
		rq.setItemId("f3960757-1a06-405e-9eb7-607c34683154");
		rq.setLevel("ERROR");
		rq.setMessage("log message");
		rq.setLogTime(Date.from(LocalDateTime.now().atZone(ZoneId.of("UTC")).toInstant()));
		mockMvc.perform(post(DEFAULT_PROJECT_BASE_URL + "/async/log").with(token(oAuthHelper.getDefaultToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(rq)));
	}
}