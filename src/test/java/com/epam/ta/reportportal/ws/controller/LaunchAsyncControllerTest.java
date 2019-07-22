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

import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.ws.BaseMvcTest;
import com.epam.ta.reportportal.ws.model.FinishExecutionRQ;
import com.epam.ta.reportportal.ws.model.attribute.ItemAttributesRQ;
import com.epam.ta.reportportal.ws.model.launch.StartLaunchRQ;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import static com.epam.ta.reportportal.ws.model.launch.Mode.DEFAULT;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
class LaunchAsyncControllerTest extends BaseMvcTest {

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void happyCreateLaunch() throws Exception {
		String name = "some launch name";
		StartLaunchRQ startLaunchRQ = new StartLaunchRQ();
		startLaunchRQ.setDescription("some description");
		startLaunchRQ.setName(name);
		startLaunchRQ.setStartTime(new Date());
		startLaunchRQ.setMode(DEFAULT);
		startLaunchRQ.setAttributes(Sets.newHashSet(new ItemAttributesRQ("key", "value")));

		mockMvc.perform(post(DEFAULT_PROJECT_BASE_URL + "/async/launch/").with(token(oAuthHelper.getDefaultToken()))
				.content(objectMapper.writeValueAsBytes(startLaunchRQ))
				.contentType(APPLICATION_JSON)).andExpect(status().isCreated());
	}

	@Test
	void finishLaunch() throws Exception {
		final FinishExecutionRQ finishExecutionRQ = new FinishExecutionRQ();
		finishExecutionRQ.setEndTime(Date.from(LocalDateTime.now().atZone(ZoneId.of("UTC")).toInstant()));
		finishExecutionRQ.setStatus(StatusEnum.PASSED.name());
		mockMvc.perform(put(DEFAULT_PROJECT_BASE_URL + "/async/launch/befef834-b2ef-4acf-aea3-b5a5b15fd93c/finish").contentType(APPLICATION_JSON)
				.with(token(oAuthHelper.getDefaultToken()))
				.content(objectMapper.writeValueAsBytes(finishExecutionRQ))).andExpect(status().is(200));
	}

}