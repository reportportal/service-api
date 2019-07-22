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
import com.epam.ta.reportportal.ws.model.ParameterResource;
import com.epam.ta.reportportal.ws.model.StartTestItemRQ;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
class TestItemAsyncControllerTest extends BaseMvcTest {

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void startRootItemPositive() throws Exception {
		StartTestItemRQ rq = new StartTestItemRQ();
		rq.setLaunchId("a7b66ef2-db30-4db7-94df-f5f7786b398a");
		rq.setName("RootItem");
		rq.setType("SUITE");
		rq.setParameters(getParameters());
		rq.setUniqueId(UUID.randomUUID().toString());
		rq.setStartTime(Date.from(LocalDateTime.now().atZone(ZoneId.of("UTC")).toInstant()));
		mockMvc.perform(post(DEFAULT_PROJECT_BASE_URL + "/async/item").contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(rq))
				.with(token(oAuthHelper.getDefaultToken()))).andExpect(status().isCreated());
	}

	@Test
	void startRootItemWithoutUuid() throws Exception {
		StartTestItemRQ rq = new StartTestItemRQ();
		rq.setLaunchId("a7b66ef2-db30-4db7-94df-f5f7786b398a");
		rq.setName("RootItem");
		rq.setType("SUITE");
		rq.setParameters(getParameters());
		rq.setStartTime(Date.from(LocalDateTime.now().atZone(ZoneId.of("UTC")).toInstant()));
		mockMvc.perform(post(SUPERADMIN_PROJECT_BASE_URL + "/async/item").contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(rq))
				.with(token(oAuthHelper.getSuperadminToken()))).andExpect(status().isCreated());
	}

	@Test
	void startChildItemPositive() throws Exception {
		StartTestItemRQ rq = new StartTestItemRQ();
		rq.setLaunchId("a7b66ef2-db30-4db7-94df-f5f7786b398a");
		rq.setName("ChildItem");
		rq.setType("TEST");
		rq.setUniqueId(UUID.randomUUID().toString());
		rq.setParameters(getParameters());
		rq.setStartTime(Date.from(LocalDateTime.now().atZone(ZoneId.of("UTC")).toInstant()));
		mockMvc.perform(post(
				DEFAULT_PROJECT_BASE_URL + "/async/item/0f7ca5bc-cfae-4cc1-9682-e59c2860131e").content(objectMapper.writeValueAsBytes(rq))
				.contentType(APPLICATION_JSON)
				.with(token(oAuthHelper.getDefaultToken()))).andExpect(status().isCreated());
	}

	@Test
	void startChildItemWithoutUuid() throws Exception {
		StartTestItemRQ rq = new StartTestItemRQ();
		rq.setLaunchId("a7b66ef2-db30-4db7-94df-f5f7786b398a");
		rq.setName("ChildItem");
		rq.setType("TEST");
		rq.setParameters(getParameters());
		rq.setStartTime(Date.from(LocalDateTime.now().atZone(ZoneId.of("UTC")).toInstant()));
		mockMvc.perform(post(
				DEFAULT_PROJECT_BASE_URL + "/async/item/0f7ca5bc-cfae-4cc1-9682-e59c2860131e").content(objectMapper.writeValueAsBytes(rq))
				.contentType(APPLICATION_JSON)
				.with(token(oAuthHelper.getDefaultToken()))).andExpect(status().isCreated());
	}

	private List<ParameterResource> getParameters() {
		ParameterResource parameters = new ParameterResource();
		parameters.setKey("CardNumber");
		parameters.setValue("4444333322221111");
		ParameterResource parameters1 = new ParameterResource();
		parameters1.setKey("Stars");
		parameters1.setValue("2 stars");
		return ImmutableList.<ParameterResource>builder().add(parameters).add(parameters1).build();
	}

}