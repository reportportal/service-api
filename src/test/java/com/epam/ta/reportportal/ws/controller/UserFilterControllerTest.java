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

import com.epam.ta.reportportal.dao.UserFilterRepository;
import com.epam.ta.reportportal.entity.filter.UserFilter;
import com.epam.ta.reportportal.ws.BaseMvcTest;
import com.epam.ta.reportportal.ws.model.EntryCreatedRS;
import com.epam.ta.reportportal.ws.model.filter.Order;
import com.epam.ta.reportportal.ws.model.filter.UpdateUserFilterRQ;
import com.epam.ta.reportportal.ws.model.filter.UserFilterCondition;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@Sql("/db/shareable/shareable-fill.sql")
class UserFilterControllerTest extends BaseMvcTest {

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private UserFilterRepository repository;

	@Test
	void createFilterPositive() throws Exception {
		String name = "userFilter";
		String description = "description";
		UpdateUserFilterRQ request = new UpdateUserFilterRQ();
		request.setName(name);
		request.setObjectType("Launch");

		final Order order = new Order();
		order.setIsAsc(false);
		order.setSortingColumnName("startTime");

		request.setOrders(Lists.newArrayList(order));
		request.setDescription(description);
		request.setConditions(Sets.newHashSet(new UserFilterCondition("name", "cnt", "test")));

		MvcResult mvcResult = mockMvc.perform(post(DEFAULT_PROJECT_BASE_URL + "/filter").with(token(oAuthHelper.getDefaultToken()))
				.content(objectMapper.writeValueAsBytes(request))
				.contentType(APPLICATION_JSON)).andExpect(status().isCreated()).andReturn();

		EntryCreatedRS response = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<EntryCreatedRS>() {
		});
		final Optional<UserFilter> optionalFilter = repository.findById(response.getId());

		assertTrue(optionalFilter.isPresent());
		assertEquals(name, optionalFilter.get().getName());
		assertEquals(description, optionalFilter.get().getDescription());
	}

	@Test
	void getFilterPositive() throws Exception {
		mockMvc.perform(get(DEFAULT_PROJECT_BASE_URL + "/filter/3").with(token(oAuthHelper.getDefaultToken()))).andExpect(status().isOk());
	}

	@Test
	void getFilterNegative() throws Exception {
		mockMvc.perform(get(DEFAULT_PROJECT_BASE_URL + "/filter/100").with(token(oAuthHelper.getDefaultToken())))
				.andExpect(status().isNotFound());
	}

	@Test
	void getFiltersByIds() throws Exception {
		mockMvc.perform(get(DEFAULT_PROJECT_BASE_URL + "/filter/filters?ids=3,4").with(token(oAuthHelper.getDefaultToken())))
				.andExpect(status().isOk());
	}

	@Test
	void getOwnFiltersPositive() throws Exception {
		mockMvc.perform(get(DEFAULT_PROJECT_BASE_URL + "/filter/own").with(token(oAuthHelper.getDefaultToken())))
				.andExpect(status().isOk());
	}

	@Test
	void getSharedFiltersPositive() throws Exception {
		mockMvc.perform(get(SUPERADMIN_PROJECT_BASE_URL + "/filter/shared").with(token(oAuthHelper.getSuperadminToken())))
				.andExpect(status().isOk());
	}

	@Test
	void getPermittedFilters() throws Exception {
		mockMvc.perform(get(SUPERADMIN_PROJECT_BASE_URL + "/filter").with(token(oAuthHelper.getSuperadminToken())))
				.andExpect(status().isOk());
	}

	@Test
	void deleteFilterPositive() throws Exception {
		mockMvc.perform(delete(DEFAULT_PROJECT_BASE_URL + "/filter/3").with(token(oAuthHelper.getDefaultToken())))
				.andExpect(status().isOk());
	}

	@Test
	void getAllFiltersNamesPositive() throws Exception {
		mockMvc.perform(get(DEFAULT_PROJECT_BASE_URL + "/filter/names").with(token(oAuthHelper.getDefaultToken())))
				.andExpect(status().isOk());
	}

	@Test
	void getAllSharedFiltersNames() throws Exception {
		mockMvc.perform(get(DEFAULT_PROJECT_BASE_URL + "/filter/names?share=true").with(token(oAuthHelper.getDefaultToken())))
				.andExpect(status().isOk());
	}

	@Test
	void updateUserFilterPositive() throws Exception {
		UpdateUserFilterRQ updateUserFilterRQ = new UpdateUserFilterRQ();
		updateUserFilterRQ.setName("new name");
		updateUserFilterRQ.setObjectType("Launch");
		updateUserFilterRQ.setDescription("new description");
		updateUserFilterRQ.setShare(true);
		Order order = new Order();
		order.setIsAsc(true);
		order.setSortingColumnName("name");
		updateUserFilterRQ.setOrders(Lists.newArrayList(order));
		updateUserFilterRQ.setConditions(Sets.newHashSet(new UserFilterCondition("name", "eq", "filter")));

		mockMvc.perform(put(DEFAULT_PROJECT_BASE_URL + "/filter/3").with(token(oAuthHelper.getDefaultToken()))
				.content(objectMapper.writeValueAsBytes(updateUserFilterRQ))
				.contentType(APPLICATION_JSON)).andExpect(status().isOk());
		final Optional<UserFilter> optionalUserFilter = repository.findById(3L);
		assertTrue(optionalUserFilter.isPresent());
		assertEquals("new description", optionalUserFilter.get().getDescription());
		assertEquals("new name", optionalUserFilter.get().getName());
	}

	@Test
	void createUserFiltersLongDescription() throws Exception {
		String name = "userFilter";
		UpdateUserFilterRQ request = new UpdateUserFilterRQ();
		request.setName(name);
		request.setObjectType("Launch");

		final Order order = new Order();
		order.setIsAsc(false);
		order.setSortingColumnName("startTime");

		request.setOrders(Lists.newArrayList(order));
		request.setDescription(StringUtils.leftPad("", 1501, "a"));
		request.setConditions(Sets.newHashSet(new UserFilterCondition("name", "cnt", "test")));

		mockMvc.perform(post(DEFAULT_PROJECT_BASE_URL + "/filter").with(token(oAuthHelper.getDefaultToken()))
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(request))).andExpect(status().is4xxClientError());
	}
}