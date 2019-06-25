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
import com.epam.ta.reportportal.ws.model.dashboard.CreateDashboardRQ;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MvcResult;

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
class DashboardControllerValidationTest extends BaseMvcTest {

	private static final String DASHBOARD_PATH = "/dashboard";
	private static final String ID_PATH = "/555";

	private static final String INCORRECT_REQUEST_MESSAGE = "Incorrect Request. ";
	private static final String FIELD_NAME_IS_NULL_MESSAGE = "[Field 'name' should not be null.] ";
	private static final String FIELD_NAME_IS_BLANK_MESSAGE = "Field 'name' should not contain only white spaces and shouldn't be empty.";
	private static final String FIELD_NAME_SIZE_MESSAGE = "Field 'name' should have size from '3' to '128'.";

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	public void createDashboardShouldReturnErrorWhenNameIsNull() throws Exception {
		//GIVEN
		CreateDashboardRQ createDashboardRQ = new CreateDashboardRQ();

		//WHEN
		MvcResult mvcResult = mockMvc.perform(post(DEFAULT_PROJECT_BASE_URL + DASHBOARD_PATH)
				.with(token(oAuthHelper.getDefaultToken()))
				.content(objectMapper.writeValueAsBytes(createDashboardRQ))
				.contentType(APPLICATION_JSON))
				.andExpect(status().isBadRequest()).andReturn();

		//THEN
		ErrorRS error = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ErrorRS.class);
		assertEquals(INCORRECT_REQUEST, error.getErrorType());
		assertEquals(INCORRECT_REQUEST_MESSAGE + FIELD_NAME_IS_NULL_MESSAGE, error.getMessage());
	}

	@Test
	public void createDashboardShouldReturnErrorWhenNameIsEmpty() throws Exception {
		//GIVEN
		CreateDashboardRQ createDashboardRQ = new CreateDashboardRQ();
		createDashboardRQ.setName(EMPTY);

		//WHEN
		MvcResult mvcResult = mockMvc.perform(post(DEFAULT_PROJECT_BASE_URL + DASHBOARD_PATH)
				.with(token(oAuthHelper.getDefaultToken()))
				.content(objectMapper.writeValueAsBytes(createDashboardRQ))
				.contentType(APPLICATION_JSON))
				.andExpect(status().isBadRequest()).andReturn();

		//THEN
		ErrorRS error = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ErrorRS.class);
		assertEquals(INCORRECT_REQUEST, error.getErrorType());
		assertEquals(INCORRECT_REQUEST_MESSAGE + "[" + FIELD_NAME_IS_BLANK_MESSAGE + " " + FIELD_NAME_SIZE_MESSAGE + "] ", error.getMessage());
	}

	@Test
	public void createDashboardShouldReturnErrorWhenNameConsistsOfWhitespaces() throws Exception {
		//GIVEN
		CreateDashboardRQ createDashboardRQ = new CreateDashboardRQ();
		createDashboardRQ.setName("    ");

		//WHEN
		MvcResult mvcResult = mockMvc.perform(post(DEFAULT_PROJECT_BASE_URL + DASHBOARD_PATH)
				.with(token(oAuthHelper.getDefaultToken()))
				.content(objectMapper.writeValueAsBytes(createDashboardRQ))
				.contentType(APPLICATION_JSON))
				.andExpect(status().isBadRequest()).andReturn();

		//THEN
		ErrorRS error = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ErrorRS.class);
		assertEquals(INCORRECT_REQUEST, error.getErrorType());
		assertEquals(INCORRECT_REQUEST_MESSAGE + "[" + FIELD_NAME_IS_BLANK_MESSAGE + " " + FIELD_NAME_SIZE_MESSAGE + "] ", error.getMessage());
	}

	@Test
	public void createDashboardShouldReturnErrorWhenNameIsLessThanThreeCharacters() throws Exception {
		//GIVEN
		CreateDashboardRQ createDashboardRQ = new CreateDashboardRQ();
		createDashboardRQ.setName("cc");

		//WHEN
		MvcResult mvcResult = mockMvc.perform(post(DEFAULT_PROJECT_BASE_URL + DASHBOARD_PATH)
				.with(token(oAuthHelper.getDefaultToken()))
				.content(objectMapper.writeValueAsBytes(createDashboardRQ))
				.contentType(APPLICATION_JSON))
				.andExpect(status().isBadRequest()).andReturn();

		//THEN
		ErrorRS error = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ErrorRS.class);
		assertEquals(INCORRECT_REQUEST, error.getErrorType());
		assertEquals(INCORRECT_REQUEST_MESSAGE + "[" + FIELD_NAME_SIZE_MESSAGE + "] ", error.getMessage());
	}

	@Test
	public void createDashboardShouldReturnErrorWhenNameIsGreaterThanOneHundredAndEightCharacters() throws Exception {
		//GIVEN
		CreateDashboardRQ createDashboardRQ = new CreateDashboardRQ();
		createDashboardRQ.setName("ttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttt");

		//WHEN
		MvcResult mvcResult = mockMvc.perform(post(DEFAULT_PROJECT_BASE_URL + DASHBOARD_PATH)
				.with(token(oAuthHelper.getDefaultToken()))
				.content(objectMapper.writeValueAsBytes(createDashboardRQ))
				.contentType(APPLICATION_JSON))
				.andExpect(status().isBadRequest()).andReturn();

		//THEN
		ErrorRS error = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ErrorRS.class);
		assertEquals(INCORRECT_REQUEST, error.getErrorType());
		assertEquals(INCORRECT_REQUEST_MESSAGE + "[" + FIELD_NAME_SIZE_MESSAGE + "] ", error.getMessage());
	}

	@Test
	public void updateDashboardShouldReturnErrorWhenNameIsNull() throws Exception {
		//GIVEN
		CreateDashboardRQ createDashboardRQ = new CreateDashboardRQ();

		//WHEN
		MvcResult mvcResult = mockMvc.perform(put(DEFAULT_PROJECT_BASE_URL + DASHBOARD_PATH + ID_PATH)
				.with(token(oAuthHelper.getDefaultToken()))
				.content(objectMapper.writeValueAsBytes(createDashboardRQ))
				.contentType(APPLICATION_JSON))
				.andExpect(status().isBadRequest()).andReturn();

		//THEN
		ErrorRS error = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ErrorRS.class);
		assertEquals(INCORRECT_REQUEST, error.getErrorType());
		assertEquals(INCORRECT_REQUEST_MESSAGE + FIELD_NAME_IS_NULL_MESSAGE, error.getMessage());
	}

	@Test
	public void updateDashboardShouldReturnErrorWhenNameIsEmpty() throws Exception {
		//GIVEN
		CreateDashboardRQ createDashboardRQ = new CreateDashboardRQ();
		createDashboardRQ.setName(EMPTY);

		//WHEN
		MvcResult mvcResult = mockMvc.perform(put(DEFAULT_PROJECT_BASE_URL + DASHBOARD_PATH + ID_PATH)
				.with(token(oAuthHelper.getDefaultToken()))
				.content(objectMapper.writeValueAsBytes(createDashboardRQ))
				.contentType(APPLICATION_JSON))
				.andExpect(status().isBadRequest()).andReturn();

		//THEN
		ErrorRS error = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ErrorRS.class);
		assertEquals(INCORRECT_REQUEST, error.getErrorType());
		assertEquals(INCORRECT_REQUEST_MESSAGE + "[" + FIELD_NAME_IS_BLANK_MESSAGE + " " + FIELD_NAME_SIZE_MESSAGE + "] ", error.getMessage());
	}

	@Test
	public void updateDashboardShouldReturnErrorWhenNameConsistsOfWhitespaces() throws Exception {
		//GIVEN
		CreateDashboardRQ createDashboardRQ = new CreateDashboardRQ();
		createDashboardRQ.setName("    ");

		//WHEN
		MvcResult mvcResult = mockMvc.perform(put(DEFAULT_PROJECT_BASE_URL + DASHBOARD_PATH + ID_PATH)
				.with(token(oAuthHelper.getDefaultToken()))
				.content(objectMapper.writeValueAsBytes(createDashboardRQ))
				.contentType(APPLICATION_JSON))
				.andExpect(status().isBadRequest()).andReturn();

		//THEN
		ErrorRS error = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ErrorRS.class);
		assertEquals(INCORRECT_REQUEST, error.getErrorType());
		assertEquals(INCORRECT_REQUEST_MESSAGE + "[" + FIELD_NAME_IS_BLANK_MESSAGE + " " + FIELD_NAME_SIZE_MESSAGE + "] ", error.getMessage());
	}

	@Test
	public void updateDashboardShouldReturnErrorWhenNameIsLessThanThreeCharacters() throws Exception {
		//GIVEN
		CreateDashboardRQ createDashboardRQ = new CreateDashboardRQ();
		createDashboardRQ.setName("cc");

		//WHEN
		MvcResult mvcResult = mockMvc.perform(put(DEFAULT_PROJECT_BASE_URL + DASHBOARD_PATH + ID_PATH)
				.with(token(oAuthHelper.getDefaultToken()))
				.content(objectMapper.writeValueAsBytes(createDashboardRQ))
				.contentType(APPLICATION_JSON))
				.andExpect(status().isBadRequest()).andReturn();

		//THEN
		ErrorRS error = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ErrorRS.class);
		assertEquals(INCORRECT_REQUEST, error.getErrorType());
		assertEquals(INCORRECT_REQUEST_MESSAGE + "[" + FIELD_NAME_SIZE_MESSAGE + "] ", error.getMessage());
	}

	@Test
	public void updateDashboardShouldReturnErrorWhenNameIsGreaterThanOneHundredAndEightCharacters() throws Exception {
		//GIVEN
		CreateDashboardRQ createDashboardRQ = new CreateDashboardRQ();
		createDashboardRQ.setName("ttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttt");

		//WHEN
		MvcResult mvcResult = mockMvc.perform(put(DEFAULT_PROJECT_BASE_URL + DASHBOARD_PATH + ID_PATH)
				.with(token(oAuthHelper.getDefaultToken()))
				.content(objectMapper.writeValueAsBytes(createDashboardRQ))
				.contentType(APPLICATION_JSON))
				.andExpect(status().isBadRequest()).andReturn();

		//THEN
		ErrorRS error = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ErrorRS.class);
		assertEquals(INCORRECT_REQUEST, error.getErrorType());
		assertEquals(INCORRECT_REQUEST_MESSAGE + "[" + FIELD_NAME_SIZE_MESSAGE + "] ", error.getMessage());
	}
}