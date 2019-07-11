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
import com.epam.ta.reportportal.ws.model.widget.ContentParameters;
import com.epam.ta.reportportal.ws.model.widget.WidgetRQ;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Arrays;

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
class WidgetControllerValidationTest extends BaseMvcTest {

	private static final String WIDGET_PATH = "/widget";

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	public void createWidgetShouldReturnErrorWhenNameIsNull() throws Exception {
		//GIVEN
		WidgetRQ widgetRQ = prepareWidget();

		//WHEN
		MvcResult mvcResult = mockMvc.perform(post(DEFAULT_PROJECT_BASE_URL + WIDGET_PATH)
				.with(token(oAuthHelper.getDefaultToken()))
				.content(objectMapper.writeValueAsBytes(widgetRQ))
				.contentType(APPLICATION_JSON))
				.andExpect(status().isBadRequest()).andReturn();

		//THEN
		ErrorRS error = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ErrorRS.class);
		assertEquals(INCORRECT_REQUEST, error.getErrorType());
		assertEquals(INCORRECT_REQUEST_MESSAGE + FIELD_NAME_IS_NULL_MESSAGE, error.getMessage());
	}

	@Test
	public void createWidgetShouldReturnErrorWhenNameIsEmpty() throws Exception {
		//GIVEN
		WidgetRQ widgetRQ = prepareWidget();
		widgetRQ.setName(EMPTY);

		//WHEN
		MvcResult mvcResult = mockMvc.perform(post(DEFAULT_PROJECT_BASE_URL + WIDGET_PATH)
				.with(token(oAuthHelper.getDefaultToken()))
				.content(objectMapper.writeValueAsBytes(widgetRQ))
				.contentType(APPLICATION_JSON))
				.andExpect(status().isBadRequest()).andReturn();

		//THEN
		ErrorRS error = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ErrorRS.class);
		assertEquals(INCORRECT_REQUEST, error.getErrorType());
		assertEquals(INCORRECT_REQUEST_MESSAGE + "[" + FIELD_NAME_IS_BLANK_MESSAGE + " " + FIELD_NAME_SIZE_MESSAGE + "] ", error.getMessage());
	}

	@Test
	public void createWidgetShouldReturnErrorWhenNameConsistsOfWhitespaces() throws Exception {
		//GIVEN
		WidgetRQ widgetRQ = prepareWidget();
		widgetRQ.setName("    ");

		//WHEN
		MvcResult mvcResult = mockMvc.perform(post(DEFAULT_PROJECT_BASE_URL + WIDGET_PATH)
				.with(token(oAuthHelper.getDefaultToken()))
				.content(objectMapper.writeValueAsBytes(widgetRQ))
				.contentType(APPLICATION_JSON))
				.andExpect(status().isBadRequest()).andReturn();

		//THEN
		ErrorRS error = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ErrorRS.class);
		assertEquals(INCORRECT_REQUEST, error.getErrorType());
		assertEquals(INCORRECT_REQUEST_MESSAGE + "[" + FIELD_NAME_IS_BLANK_MESSAGE + " " + FIELD_NAME_SIZE_MESSAGE + "] ", error.getMessage());
	}

	@Test
	public void createWidgetShouldReturnErrorWhenNameIsLessThanThreeCharacters() throws Exception {
		//GIVEN
		WidgetRQ widgetRQ = prepareWidget();
		widgetRQ.setName("cc");

		//WHEN
		MvcResult mvcResult = mockMvc.perform(post(DEFAULT_PROJECT_BASE_URL + WIDGET_PATH)
				.with(token(oAuthHelper.getDefaultToken()))
				.content(objectMapper.writeValueAsBytes(widgetRQ))
				.contentType(APPLICATION_JSON))
				.andExpect(status().isBadRequest()).andReturn();

		//THEN
		ErrorRS error = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ErrorRS.class);
		assertEquals(INCORRECT_REQUEST, error.getErrorType());
		assertEquals(INCORRECT_REQUEST_MESSAGE + "[" + FIELD_NAME_SIZE_MESSAGE + "] ", error.getMessage());
	}

	@Test
	public void createWidgetShouldReturnErrorWhenNameIsGreaterThanOneHundredAndEightCharacters() throws Exception {
		//GIVEN
		WidgetRQ widgetRQ = prepareWidget();
		widgetRQ.setName("ttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttt");

		//WHEN
		MvcResult mvcResult = mockMvc.perform(post(DEFAULT_PROJECT_BASE_URL + WIDGET_PATH)
				.with(token(oAuthHelper.getDefaultToken()))
				.content(objectMapper.writeValueAsBytes(widgetRQ))
				.contentType(APPLICATION_JSON))
				.andExpect(status().isBadRequest()).andReturn();

		//THEN
		ErrorRS error = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ErrorRS.class);
		assertEquals(INCORRECT_REQUEST, error.getErrorType());
		assertEquals(INCORRECT_REQUEST_MESSAGE + "[" + FIELD_NAME_SIZE_MESSAGE + "] ", error.getMessage());
	}

	@Test
	public void updateWidgetShouldReturnErrorWhenNameIsNull() throws Exception {
		//GIVEN
		WidgetRQ widgetRQ = prepareWidget();

		//WHEN
		MvcResult mvcResult = mockMvc.perform(put(DEFAULT_PROJECT_BASE_URL + WIDGET_PATH + ID_PATH)
				.with(token(oAuthHelper.getDefaultToken()))
				.content(objectMapper.writeValueAsBytes(widgetRQ))
				.contentType(APPLICATION_JSON))
				.andExpect(status().isBadRequest()).andReturn();

		//THEN
		ErrorRS error = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ErrorRS.class);
		assertEquals(INCORRECT_REQUEST, error.getErrorType());
		assertEquals(INCORRECT_REQUEST_MESSAGE + FIELD_NAME_IS_NULL_MESSAGE, error.getMessage());
	}

	@Test
	public void updateWidgetShouldReturnErrorWhenNameIsEmpty() throws Exception {
		//GIVEN
		WidgetRQ widgetRQ = prepareWidget();
		widgetRQ.setName(EMPTY);

		//WHEN
		MvcResult mvcResult = mockMvc.perform(put(DEFAULT_PROJECT_BASE_URL + WIDGET_PATH + ID_PATH)
				.with(token(oAuthHelper.getDefaultToken()))
				.content(objectMapper.writeValueAsBytes(widgetRQ))
				.contentType(APPLICATION_JSON))
				.andExpect(status().isBadRequest()).andReturn();

		//THEN
		ErrorRS error = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ErrorRS.class);
		assertEquals(INCORRECT_REQUEST, error.getErrorType());
		assertEquals(INCORRECT_REQUEST_MESSAGE + "[" + FIELD_NAME_IS_BLANK_MESSAGE + " " + FIELD_NAME_SIZE_MESSAGE + "] ", error.getMessage());
	}

	@Test
	public void updateWidgetShouldReturnErrorWhenNameConsistsOfWhitespaces() throws Exception {
		//GIVEN
		WidgetRQ widgetRQ = prepareWidget();
		widgetRQ.setName("    ");

		//WHEN
		MvcResult mvcResult = mockMvc.perform(put(DEFAULT_PROJECT_BASE_URL + WIDGET_PATH + ID_PATH)
				.with(token(oAuthHelper.getDefaultToken()))
				.content(objectMapper.writeValueAsBytes(widgetRQ))
				.contentType(APPLICATION_JSON))
				.andExpect(status().isBadRequest()).andReturn();

		//THEN
		ErrorRS error = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ErrorRS.class);
		assertEquals(INCORRECT_REQUEST, error.getErrorType());
		assertEquals(INCORRECT_REQUEST_MESSAGE + "[" + FIELD_NAME_IS_BLANK_MESSAGE + " " + FIELD_NAME_SIZE_MESSAGE + "] ", error.getMessage());
	}

	@Test
	public void updateWidgetShouldReturnErrorWhenNameIsLessThanThreeCharacters() throws Exception {
		//GIVEN
		WidgetRQ widgetRQ = prepareWidget();
		widgetRQ.setName("cc");

		//WHEN
		MvcResult mvcResult = mockMvc.perform(put(DEFAULT_PROJECT_BASE_URL + WIDGET_PATH + ID_PATH)
				.with(token(oAuthHelper.getDefaultToken()))
				.content(objectMapper.writeValueAsBytes(widgetRQ))
				.contentType(APPLICATION_JSON))
				.andExpect(status().isBadRequest()).andReturn();

		//THEN
		ErrorRS error = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ErrorRS.class);
		assertEquals(INCORRECT_REQUEST, error.getErrorType());
		assertEquals(INCORRECT_REQUEST_MESSAGE + "[" + FIELD_NAME_SIZE_MESSAGE + "] ", error.getMessage());
	}

	@Test
	public void updateWidgetShouldReturnErrorWhenNameIsGreaterThanOneHundredAndEightCharacters() throws Exception {
		//GIVEN
		WidgetRQ widgetRQ = prepareWidget();
		widgetRQ.setName("ttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttt");

		//WHEN
		MvcResult mvcResult = mockMvc.perform(put(DEFAULT_PROJECT_BASE_URL + WIDGET_PATH + ID_PATH)
				.with(token(oAuthHelper.getDefaultToken()))
				.content(objectMapper.writeValueAsBytes(widgetRQ))
				.contentType(APPLICATION_JSON))
				.andExpect(status().isBadRequest()).andReturn();

		//THEN
		ErrorRS error = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ErrorRS.class);
		assertEquals(INCORRECT_REQUEST, error.getErrorType());
		assertEquals(INCORRECT_REQUEST_MESSAGE + "[" + FIELD_NAME_SIZE_MESSAGE + "] ", error.getMessage());
	}

	private WidgetRQ prepareWidget() {
		WidgetRQ widgetRQ = new WidgetRQ();
		widgetRQ.setDescription("description");
		widgetRQ.setWidgetType("oldLineChart");
		ContentParameters contentParameters = new ContentParameters();
		contentParameters.setContentFields(Arrays.asList("number", "name", "user", "statistics$defects$automation_bug$AB002"));
		contentParameters.setItemsCount(50);
		widgetRQ.setContentParameters(contentParameters);
		widgetRQ.setShare(true);
		return widgetRQ;
	}
}