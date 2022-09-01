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
import com.epam.ta.reportportal.ws.model.ErrorRS;
import com.epam.ta.reportportal.ws.model.filter.Order;
import com.epam.ta.reportportal.ws.model.filter.UpdateUserFilterRQ;
import com.epam.ta.reportportal.ws.model.filter.UserFilterCondition;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MvcResult;

import static com.epam.ta.reportportal.ws.controller.constants.ValidationTestsConstants.*;
import static com.epam.ta.reportportal.ws.model.ErrorType.INCORRECT_REQUEST;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author <a href="mailto:tatyana_gladysheva@epam.com">Tatyana Gladysheva</a>
 */
public class UserFilterControllerValidationTest extends BaseMvcTest {

	private static final String FILTER_PATH = "/filter";

	private static final String FIELD_NAME_SIZE_MESSAGE = String.format(FIELD_NAME_SIZE_MESSAGE_WITH_FORMAT, 3, 128);

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	public void createFilterShouldReturnErrorWhenNameIsNull() throws Exception {
		//GIVEN
		UpdateUserFilterRQ userFilterRQ = prepareFilter();

		//WHEN
		MvcResult mvcResult = mockMvc.perform(post(DEFAULT_SLUG_KEY_BASE_URL + FILTER_PATH)
				.with(token(oAuthHelper.getDefaultToken()))
				.content(objectMapper.writeValueAsBytes(userFilterRQ))
				.contentType(APPLICATION_JSON))
				.andExpect(status().isBadRequest()).andReturn();

		//THEN
		ErrorRS error = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ErrorRS.class);
		assertEquals(INCORRECT_REQUEST, error.getErrorType());
		assertEquals(INCORRECT_REQUEST_MESSAGE + FIELD_NAME_IS_NULL_MESSAGE, error.getMessage());
	}

	@Test
	public void createFilterShouldReturnErrorWhenNameIsEmpty() throws Exception {
		//GIVEN
		UpdateUserFilterRQ userFilterRQ = prepareFilter();
		userFilterRQ.setName(EMPTY);

		//WHEN
		MvcResult mvcResult = mockMvc.perform(post(DEFAULT_SLUG_KEY_BASE_URL + FILTER_PATH)
				.with(token(oAuthHelper.getDefaultToken()))
				.content(objectMapper.writeValueAsBytes(userFilterRQ))
				.contentType(APPLICATION_JSON))
				.andExpect(status().isBadRequest()).andReturn();

		//THEN
		ErrorRS error = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ErrorRS.class);
		assertEquals(INCORRECT_REQUEST, error.getErrorType());
		assertEquals(INCORRECT_REQUEST_MESSAGE + "[" + FIELD_NAME_IS_BLANK_MESSAGE + " " + FIELD_NAME_SIZE_MESSAGE + "] ", error.getMessage());
	}

	@Test
	public void createFilterShouldReturnErrorWhenNameConsistsOfWhitespaces() throws Exception {
		//GIVEN
		UpdateUserFilterRQ userFilterRQ = prepareFilter();
		userFilterRQ.setName(WHITESPACES_NAME_VALUE);

		//WHEN
		MvcResult mvcResult = mockMvc.perform(post(DEFAULT_SLUG_KEY_BASE_URL + FILTER_PATH)
				.with(token(oAuthHelper.getDefaultToken()))
				.content(objectMapper.writeValueAsBytes(userFilterRQ))
				.contentType(APPLICATION_JSON))
				.andExpect(status().isBadRequest()).andReturn();

		//THEN
		ErrorRS error = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ErrorRS.class);
		assertEquals(INCORRECT_REQUEST, error.getErrorType());
		assertEquals(INCORRECT_REQUEST_MESSAGE + "[" + FIELD_NAME_IS_BLANK_MESSAGE + " " + FIELD_NAME_SIZE_MESSAGE + "] ", error.getMessage());
	}

	@Test
	public void createFilterShouldReturnErrorWhenNameIsLessThanThreeCharacters() throws Exception {
		//GIVEN
		UpdateUserFilterRQ userFilterRQ = prepareFilter();
		userFilterRQ.setName(SHORT_NAME_VALUE);

		//WHEN
		MvcResult mvcResult = mockMvc.perform(post(DEFAULT_SLUG_KEY_BASE_URL + FILTER_PATH)
				.with(token(oAuthHelper.getDefaultToken()))
				.content(objectMapper.writeValueAsBytes(userFilterRQ))
				.contentType(APPLICATION_JSON))
				.andExpect(status().isBadRequest()).andReturn();

		//THEN
		ErrorRS error = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ErrorRS.class);
		assertEquals(INCORRECT_REQUEST, error.getErrorType());
		assertEquals(INCORRECT_REQUEST_MESSAGE + "[" + FIELD_NAME_SIZE_MESSAGE + "] ", error.getMessage());
	}

	@Test
	public void createFilterShouldReturnErrorWhenNameIsGreaterThanOneHundredAndTwentyEightCharacters() throws Exception {
		//GIVEN
		UpdateUserFilterRQ userFilterRQ = prepareFilter();
		userFilterRQ.setName(LONG_NAME_VALUE);

		//WHEN
		MvcResult mvcResult = mockMvc.perform(post(DEFAULT_SLUG_KEY_BASE_URL + FILTER_PATH)
				.with(token(oAuthHelper.getDefaultToken()))
				.content(objectMapper.writeValueAsBytes(userFilterRQ))
				.contentType(APPLICATION_JSON))
				.andExpect(status().isBadRequest()).andReturn();

		//THEN
		ErrorRS error = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ErrorRS.class);
		assertEquals(INCORRECT_REQUEST, error.getErrorType());
		assertEquals(INCORRECT_REQUEST_MESSAGE + "[" + FIELD_NAME_SIZE_MESSAGE + "] ", error.getMessage());
	}

	@Test
	public void updateFilterShouldReturnErrorWhenNameIsNull() throws Exception {
		//GIVEN
		UpdateUserFilterRQ userFilterRQ = prepareFilter();

		//WHEN
		MvcResult mvcResult = mockMvc.perform(put(DEFAULT_SLUG_KEY_BASE_URL + FILTER_PATH + ID_PATH)
				.with(token(oAuthHelper.getDefaultToken()))
				.content(objectMapper.writeValueAsBytes(userFilterRQ))
				.contentType(APPLICATION_JSON))
				.andExpect(status().isBadRequest()).andReturn();

		//THEN
		ErrorRS error = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ErrorRS.class);
		assertEquals(INCORRECT_REQUEST, error.getErrorType());
		assertEquals(INCORRECT_REQUEST_MESSAGE + FIELD_NAME_IS_NULL_MESSAGE, error.getMessage());
	}

	@Test
	public void updateFilterShouldReturnErrorWhenNameIsEmpty() throws Exception {
		//GIVEN
		UpdateUserFilterRQ userFilterRQ = prepareFilter();
		userFilterRQ.setName(EMPTY);

		//WHEN
		MvcResult mvcResult = mockMvc.perform(put(DEFAULT_SLUG_KEY_BASE_URL + FILTER_PATH + ID_PATH)
				.with(token(oAuthHelper.getDefaultToken()))
				.content(objectMapper.writeValueAsBytes(userFilterRQ))
				.contentType(APPLICATION_JSON))
				.andExpect(status().isBadRequest()).andReturn();

		//THEN
		ErrorRS error = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ErrorRS.class);
		assertEquals(INCORRECT_REQUEST, error.getErrorType());
		assertEquals(INCORRECT_REQUEST_MESSAGE + "[" + FIELD_NAME_IS_BLANK_MESSAGE + " " + FIELD_NAME_SIZE_MESSAGE + "] ", error.getMessage());
	}

	@Test
	public void updateFilterShouldReturnErrorWhenNameConsistsOfWhitespaces() throws Exception {
		//GIVEN
		UpdateUserFilterRQ userFilterRQ = prepareFilter();
		userFilterRQ.setName(WHITESPACES_NAME_VALUE);

		//WHEN
		MvcResult mvcResult = mockMvc.perform(put(DEFAULT_SLUG_KEY_BASE_URL + FILTER_PATH + ID_PATH)
				.with(token(oAuthHelper.getDefaultToken()))
				.content(objectMapper.writeValueAsBytes(userFilterRQ))
				.contentType(APPLICATION_JSON))
				.andExpect(status().isBadRequest()).andReturn();

		//THEN
		ErrorRS error = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ErrorRS.class);
		assertEquals(INCORRECT_REQUEST, error.getErrorType());
		assertEquals(INCORRECT_REQUEST_MESSAGE + "[" + FIELD_NAME_IS_BLANK_MESSAGE + " " + FIELD_NAME_SIZE_MESSAGE + "] ", error.getMessage());
	}

	@Test
	public void updateFilterShouldReturnErrorWhenNameIsLessThanThreeCharacters() throws Exception {
		//GIVEN
		UpdateUserFilterRQ userFilterRQ = prepareFilter();
		userFilterRQ.setName(SHORT_NAME_VALUE);

		//WHEN
		MvcResult mvcResult = mockMvc.perform(put(DEFAULT_SLUG_KEY_BASE_URL + FILTER_PATH + ID_PATH)
				.with(token(oAuthHelper.getDefaultToken()))
				.content(objectMapper.writeValueAsBytes(userFilterRQ))
				.contentType(APPLICATION_JSON))
				.andExpect(status().isBadRequest()).andReturn();

		//THEN
		ErrorRS error = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ErrorRS.class);
		assertEquals(INCORRECT_REQUEST, error.getErrorType());
		assertEquals(INCORRECT_REQUEST_MESSAGE + "[" + FIELD_NAME_SIZE_MESSAGE + "] ", error.getMessage());
	}

	@Test
	public void updateFilterShouldReturnErrorWhenNameIsGreaterThanOneHundredAndTwentyEightCharacters() throws Exception {
		//GIVEN
		UpdateUserFilterRQ userFilterRQ = prepareFilter();
		userFilterRQ.setName(LONG_NAME_VALUE);

		//WHEN
		MvcResult mvcResult = mockMvc.perform(put(DEFAULT_SLUG_KEY_BASE_URL + FILTER_PATH + ID_PATH)
				.with(token(oAuthHelper.getDefaultToken()))
				.content(objectMapper.writeValueAsBytes(userFilterRQ))
				.contentType(APPLICATION_JSON))
				.andExpect(status().isBadRequest()).andReturn();

		//THEN
		ErrorRS error = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ErrorRS.class);
		assertEquals(INCORRECT_REQUEST, error.getErrorType());
		assertEquals(INCORRECT_REQUEST_MESSAGE + "[" + FIELD_NAME_SIZE_MESSAGE + "] ", error.getMessage());
	}

	private UpdateUserFilterRQ prepareFilter() {
		UpdateUserFilterRQ userFilterRQ = new UpdateUserFilterRQ();
		userFilterRQ.setObjectType("Launch");

		Order order = new Order();
		order.setIsAsc(false);
		order.setSortingColumnName("startTime");

		userFilterRQ.setOrders(Lists.newArrayList(order));

		userFilterRQ.setDescription("description");
		userFilterRQ.setConditions(Sets.newHashSet(new UserFilterCondition("name", "cnt", "test")));

		return userFilterRQ;
	}
}
