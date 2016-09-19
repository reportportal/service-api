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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;

import com.epam.ta.reportportal.auth.AuthConstants;
import com.epam.ta.reportportal.ws.BaseMvcTest;
import com.epam.ta.reportportal.ws.model.CollectionsRQ;
import com.epam.ta.reportportal.ws.model.filter.*;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Dzmitry_Kavalets
 */
public class UserFilterControllerTest extends BaseMvcTest {

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	public void createFilterPositive() throws Exception {
		CreateUserFilterRQ createUserFilterRQ = new CreateUserFilterRQ();
		createUserFilterRQ.setName("userFilter");
		createUserFilterRQ.setObjectType("Launch");
		createUserFilterRQ.setIsLink(false);
		SelectionParameters selectionParameters = new SelectionParameters();
		selectionParameters.setIsAsc(false);
		selectionParameters.setSortingColumnName("start_time");
		selectionParameters.setQuantity(10);
		selectionParameters.setPageNumber(2);
		createUserFilterRQ.setSelectionParameters(selectionParameters);
		createUserFilterRQ.setEntities(generateFilterEntities());
		CollectionsRQ<CreateUserFilterRQ> rq = new CollectionsRQ<>();
		rq.setElements(Collections.singletonList(createUserFilterRQ));
		this.mvcMock.perform(post(PROJECT_BASE_URL + "/filter").principal(authentication()).content(objectMapper.writeValueAsBytes(rq))
				.contentType(MediaType.APPLICATION_JSON)).andExpect(status().isCreated());
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
		this.mvcMock
				.perform(put(PROJECT_BASE_URL + "/filter/566e1f3818177ca344439d38").principal(authentication())
						.content(objectMapper.writeValueAsBytes(updateUserFilterRQ)).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().is(200));
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
		bulkUpdateFilterRQ.setId("566e1f3818177ca344439d38");
		bulkUpdateFilterRQ.setObjectType("Launch");
		bulkUpdateFilterRQ.setEntities(generateFilterEntities());
		rq.setElements(Collections.singletonList(bulkUpdateFilterRQ));
		this.mvcMock.perform(put(PROJECT_BASE_URL + "/filter").principal(authentication()).contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(rq))).andExpect(status().is(200));
	}

	@Override
	protected Authentication authentication() {
		return AuthConstants.ADMINISTRATOR;
	}

	private static Set<UserFilterEntity> generateFilterEntities() {
		Set<UserFilterEntity> userFilterEntities = new LinkedHashSet<>();
		UserFilterEntity userFilterEntity = new UserFilterEntity();
		userFilterEntity.setValue("Api");
		userFilterEntity.setCondition("cnt");
		userFilterEntity.setFilteringField("name");
		userFilterEntity.setIsNegative(false);
		userFilterEntities.add(userFilterEntity);
		return userFilterEntities;
	}
}