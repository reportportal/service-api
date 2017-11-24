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

import com.epam.ta.reportportal.database.dao.UserFilterRepository;
import com.epam.ta.reportportal.database.entity.filter.UserFilter;
import com.epam.ta.reportportal.ws.BaseMvcTest;
import com.epam.ta.reportportal.ws.model.CollectionsRQ;
import com.epam.ta.reportportal.ws.model.EntryCreatedRS;
import com.epam.ta.reportportal.ws.model.filter.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static com.epam.ta.reportportal.auth.AuthConstants.ADMINISTRATOR;
import static com.epam.ta.reportportal.auth.AuthConstants.USER_PROJECT;
import static java.util.stream.Collectors.joining;
import static java.util.stream.IntStream.range;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Dzmitry_Kavalets
 */
public class UserFilterControllerTest extends BaseMvcTest {

	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private UserFilterRepository userFilterRepository;

	@Test
	public void createFilterPositive() throws Exception {
		String name = "userFilter";
		String description = "description";
		CreateUserFilterRQ createUserFilterRQ = new CreateUserFilterRQ();
		createUserFilterRQ.setName(name);
		createUserFilterRQ.setObjectType("Launch");
		createUserFilterRQ.setIsLink(false);
		createUserFilterRQ.setDescription(description);
		SelectionParameters selectionParameters = selectionParameters();
		createUserFilterRQ.setSelectionParameters(selectionParameters);
		createUserFilterRQ.setEntities(generateFilterEntities());
		CollectionsRQ<CreateUserFilterRQ> rq = new CollectionsRQ<>();
		rq.setElements(Collections.singletonList(createUserFilterRQ));
		MvcResult mvcResult = this.mvcMock.perform(post(PROJECT_BASE_URL + "/filter").principal(authentication())
				.content(objectMapper.writeValueAsBytes(rq))
				.contentType(APPLICATION_JSON)).andExpect(status().isCreated()).andReturn();
		List<EntryCreatedRS> entries = objectMapper.readValue(mvcResult.getResponse().getContentAsString(),
				new TypeReference<List<EntryCreatedRS>>() {
				}
		);
		Assert.assertEquals(1, entries.size());
		UserFilter userFilter = userFilterRepository.findOne(entries.get(0).getId());
		Assert.assertEquals(name, userFilter.getName());
		Assert.assertEquals(description, userFilter.getDescription());
		Assert.assertEquals(USER_PROJECT, userFilter.getProjectName());
	}

	@Test
	public void getFilterPositive() throws Exception {
		this.mvcMock.perform(get(PROJECT_BASE_URL + "/filter/566e1f3818177ca344439d38").principal(authentication()))
				.andExpect(status().is(200));
	}

	@Test
	public void getAllFiltersPositive() throws Exception {
		this.mvcMock.perform(get(PROJECT_BASE_URL + "/filter").principal(authentication())).andExpect(status().is(200));
	}

	@Test
	public void getOwnFiltersPositive() throws Exception {
		this.mvcMock.perform(get(PROJECT_BASE_URL + "/filter/own").principal(authentication())).andExpect(status().is(200));
	}

	@Test
	public void getSharedFiltersPositive() throws Exception {
		this.mvcMock.perform(get(PROJECT_BASE_URL + "/filter/shared").principal(authentication())).andExpect(status().is(200));
	}

	@Test
	public void deleteFilterPositive() throws Exception {
		this.mvcMock.perform(delete(PROJECT_BASE_URL + "/filter/566e1f3818177ca344439d38").principal(authentication()))
				.andExpect(status().is(200));
	}

	@Test
	public void getAllFiltersNamesPositive() throws Exception {
		this.mvcMock.perform(get(PROJECT_BASE_URL + "/filter/names").principal(authentication())).andExpect(status().is(200));
	}

	@Test
	public void updateUserFilterPositive() throws Exception {
		UpdateUserFilterRQ updateUserFilterRQ = new UpdateUserFilterRQ();
		updateUserFilterRQ.setObjectType("Launch");
		updateUserFilterRQ.setEntities(generateFilterEntities());
		updateUserFilterRQ.setDescription("new description");
		SelectionParameters selectionParameters = new SelectionParameters();
		Order order = new Order();
		order.setIsAsc(true);
		order.setSortingColumnName("start_time");
		selectionParameters.setOrders(Collections.singletonList(order));
		updateUserFilterRQ.setSelectionParameters(selectionParameters);
		this.mvcMock.perform(put(PROJECT_BASE_URL + "/filter/566e1f3818177ca344439d38").principal(authentication())
				.content(objectMapper.writeValueAsBytes(updateUserFilterRQ))
				.contentType(APPLICATION_JSON)).andExpect(status().is(200));
		Assert.assertEquals("new description", userFilterRepository.findOne("566e1f3818177ca344439d38").getDescription());
	}

	@Test
	public void getUserFiltersPositive() throws Exception {
		this.mvcMock.perform(get(PROJECT_BASE_URL + "/filter/filters?ids=566e1f3818177ca344439d38").principal(authentication()))
				.andExpect(status().is(200));
	}

	@Test
	public void updateUserFiltersPositive() throws Exception {
		CollectionsRQ<BulkUpdateFilterRQ> rq = new CollectionsRQ<>();
		BulkUpdateFilterRQ bulkUpdateFilterRQ = new BulkUpdateFilterRQ();
		String id = "566e1f3818177ca344439d38";
		bulkUpdateFilterRQ.setId(id);
		bulkUpdateFilterRQ.setObjectType("Launch");
		bulkUpdateFilterRQ.setDescription("new description");
		bulkUpdateFilterRQ.setEntities(generateFilterEntities());
		rq.setElements(Collections.singletonList(bulkUpdateFilterRQ));
		this.mvcMock.perform(put(PROJECT_BASE_URL + "/filter").principal(authentication())
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(rq))).andExpect(status().is(200));
		UserFilter userFilter = userFilterRepository.findOne(id);
		Assert.assertEquals("new description", userFilter.getDescription());
	}

	@Test
	public void createUserFiltersLongDescription() throws Exception {
		CollectionsRQ<CreateUserFilterRQ> createRq = new CollectionsRQ<>();
		CreateUserFilterRQ filterRQ = new CreateUserFilterRQ();
		filterRQ.setName("filterName");
		filterRQ.setObjectType("Launch");
		filterRQ.setEntities(generateFilterEntities());
		filterRQ.setSelectionParameters(selectionParameters());
		filterRQ.setIsLink(false);
		filterRQ.setDescription(range(0, 257).mapToObj(String::valueOf).collect(joining()));
		createRq.setElements(Collections.singletonList(filterRQ));
		mvcMock.perform(post(PROJECT_BASE_URL + "/filter").principal(authentication())
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(createRq))).andExpect(status().is4xxClientError());
	}

	@Test
	public void createUserFiltersZeroDescription() throws Exception {
		CollectionsRQ<CreateUserFilterRQ> createRq = new CollectionsRQ<>();
		CreateUserFilterRQ filterRQ = new CreateUserFilterRQ();
		filterRQ.setName("filterName");
		filterRQ.setObjectType("Launch");
		filterRQ.setEntities(generateFilterEntities());
		filterRQ.setSelectionParameters(selectionParameters());
		filterRQ.setIsLink(false);
		filterRQ.setDescription("");
		createRq.setElements(Collections.singletonList(filterRQ));
		mvcMock.perform(post(PROJECT_BASE_URL + "/filter").principal(authentication())
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(createRq))).andExpect(status().is4xxClientError());
	}

	private SelectionParameters selectionParameters() {
		SelectionParameters selectionParameters = new SelectionParameters();
		Order order = new Order();
		order.setIsAsc(false);
		order.setSortingColumnName("start_time");
		selectionParameters.setPageNumber(2);
		selectionParameters.setOrders(Collections.singletonList(order));
		return selectionParameters;
	}

	@Override
	protected Authentication authentication() {
		return ADMINISTRATOR;
	}

	private static Set<UserFilterEntity> generateFilterEntities() {
		Set<UserFilterEntity> userFilterEntities = new LinkedHashSet<>();
		UserFilterEntity userFilterEntity = new UserFilterEntity();
		userFilterEntity.setValue("Api");
		userFilterEntity.setCondition("cnt");
		userFilterEntity.setFilteringField("name");
		userFilterEntities.add(userFilterEntity);
		return userFilterEntities;
	}
}