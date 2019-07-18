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
import com.epam.ta.reportportal.ws.model.StartTestItemRQ;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;

import static com.epam.ta.reportportal.ws.model.ErrorType.INCORRECT_REQUEST;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author <a href="mailto:tatyana_gladysheva@epam.com">Tatyana Gladysheva</a>
 */
public class TestItemControllerValidationTest extends BaseMvcTest {

	private static final String ITEM_PATH = "/item";
	private static final String PARENT_ID_PATH = "/555";

	private static final String INCORRECT_REQUEST_MESSAGE = "Incorrect Request. ";
	private static final String FIELD_NAME_IS_NULL_MESSAGE = "[Field 'name' should not be null.] ";
	private static final String FIELD_NAME_IS_BLANK_MESSAGE = "Field 'name' should not contain only white spaces and shouldn't be empty.";
	private static final String FIELD_NAME_SIZE_MESSAGE = "Field 'name' should have size from '1' to '1,024'.";

	private static final String LONG_NAME_VALUE = "ttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttt"
			+ "ttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttt"
			+ "ttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttt"
			+ "ttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttt"
			+ "ttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttt"
			+ "ttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttt"
			+ "ttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttt"
			+ "ttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttt"
			+ "ttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttt";

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	public void startRootTestItemShouldReturnErrorWhenNameIsNull() throws Exception {
		//GIVEN
		StartTestItemRQ startTestItemRQ = prepareTestItem();

		//WHEN
		MvcResult mvcResult = mockMvc.perform(post(DEFAULT_PROJECT_BASE_URL + ITEM_PATH)
				.with(token(oAuthHelper.getDefaultToken()))
				.content(objectMapper.writeValueAsBytes(startTestItemRQ))
				.contentType(APPLICATION_JSON))
				.andExpect(status().isBadRequest()).andReturn();

		//THEN
		ErrorRS error = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ErrorRS.class);
		assertEquals(INCORRECT_REQUEST, error.getErrorType());
		assertEquals(INCORRECT_REQUEST_MESSAGE + FIELD_NAME_IS_NULL_MESSAGE, error.getMessage());
	}

	@Test
	public void startRootTestItemShouldReturnErrorWhenNameIsEmpty() throws Exception {
		//GIVEN
		StartTestItemRQ startTestItemRQ = prepareTestItem();
		startTestItemRQ.setName(EMPTY);

		//WHEN
		MvcResult mvcResult = mockMvc.perform(post(DEFAULT_PROJECT_BASE_URL + ITEM_PATH)
				.with(token(oAuthHelper.getDefaultToken()))
				.content(objectMapper.writeValueAsBytes(startTestItemRQ))
				.contentType(APPLICATION_JSON))
				.andExpect(status().isBadRequest()).andReturn();

		//THEN
		ErrorRS error = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ErrorRS.class);
		assertEquals(INCORRECT_REQUEST, error.getErrorType());
		assertEquals(INCORRECT_REQUEST_MESSAGE + "[" + FIELD_NAME_IS_BLANK_MESSAGE + " " + FIELD_NAME_SIZE_MESSAGE + "] ", error.getMessage());
	}

	@Test
	public void startRootTestItemShouldReturnErrorWhenNameConsistsOfWhitespaces() throws Exception {
		//GIVEN
		StartTestItemRQ startTestItemRQ = prepareTestItem();
		startTestItemRQ.setName("    ");

		//WHEN
		MvcResult mvcResult = mockMvc.perform(post(DEFAULT_PROJECT_BASE_URL + ITEM_PATH)
				.with(token(oAuthHelper.getDefaultToken()))
				.content(objectMapper.writeValueAsBytes(startTestItemRQ))
				.contentType(APPLICATION_JSON))
				.andExpect(status().isBadRequest()).andReturn();

		//THEN
		ErrorRS error = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ErrorRS.class);
		assertEquals(INCORRECT_REQUEST, error.getErrorType());
		assertEquals(INCORRECT_REQUEST_MESSAGE + "[" + FIELD_NAME_IS_BLANK_MESSAGE + " " + FIELD_NAME_SIZE_MESSAGE + "] ", error.getMessage());
	}

	@Test
	public void startRootTestItemShouldReturnErrorWhenNameIsGreaterThanOneThousandTwentyFourCharacters() throws Exception {
		//GIVEN
		StartTestItemRQ startTestItemRQ = prepareTestItem();
		startTestItemRQ.setName(LONG_NAME_VALUE);

		//WHEN
		MvcResult mvcResult = mockMvc.perform(post(DEFAULT_PROJECT_BASE_URL + ITEM_PATH)
				.with(token(oAuthHelper.getDefaultToken()))
				.content(objectMapper.writeValueAsBytes(startTestItemRQ))
				.contentType(APPLICATION_JSON))
				.andExpect(status().isBadRequest()).andReturn();

		//THEN
		ErrorRS error = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ErrorRS.class);
		assertEquals(INCORRECT_REQUEST, error.getErrorType());
		assertEquals(INCORRECT_REQUEST_MESSAGE + "[" + FIELD_NAME_SIZE_MESSAGE + "] ", error.getMessage());
	}

	@Test
	public void startChildTestItemShouldReturnErrorWhenNameIsNull() throws Exception {
		//GIVEN
		StartTestItemRQ startTestItemRQ = prepareTestItem();

		//WHEN
		MvcResult mvcResult = mockMvc.perform(post(DEFAULT_PROJECT_BASE_URL + ITEM_PATH + PARENT_ID_PATH)
				.with(token(oAuthHelper.getDefaultToken()))
				.content(objectMapper.writeValueAsBytes(startTestItemRQ))
				.contentType(APPLICATION_JSON))
				.andExpect(status().isBadRequest()).andReturn();

		//THEN
		ErrorRS error = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ErrorRS.class);
		assertEquals(INCORRECT_REQUEST, error.getErrorType());
		assertEquals(INCORRECT_REQUEST_MESSAGE + FIELD_NAME_IS_NULL_MESSAGE, error.getMessage());
	}

	@Test
	public void startChildTestItemShouldReturnErrorWhenNameIsEmpty() throws Exception {
		//GIVEN
		StartTestItemRQ startTestItemRQ = prepareTestItem();
		startTestItemRQ.setName(EMPTY);

		//WHEN
		MvcResult mvcResult = mockMvc.perform(post(DEFAULT_PROJECT_BASE_URL + ITEM_PATH + PARENT_ID_PATH)
				.with(token(oAuthHelper.getDefaultToken()))
				.content(objectMapper.writeValueAsBytes(startTestItemRQ))
				.contentType(APPLICATION_JSON))
				.andExpect(status().isBadRequest()).andReturn();

		//THEN
		ErrorRS error = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ErrorRS.class);
		assertEquals(INCORRECT_REQUEST, error.getErrorType());
		assertEquals(INCORRECT_REQUEST_MESSAGE + "[" + FIELD_NAME_IS_BLANK_MESSAGE + " " + FIELD_NAME_SIZE_MESSAGE + "] ", error.getMessage());
	}

	@Test
	public void startChildTestItemShouldReturnErrorWhenNameConsistsOfWhitespaces() throws Exception {
		//GIVEN
		StartTestItemRQ startTestItemRQ = prepareTestItem();
		startTestItemRQ.setName("    ");

		//WHEN
		MvcResult mvcResult = mockMvc.perform(post(DEFAULT_PROJECT_BASE_URL + ITEM_PATH + PARENT_ID_PATH)
				.with(token(oAuthHelper.getDefaultToken()))
				.content(objectMapper.writeValueAsBytes(startTestItemRQ))
				.contentType(APPLICATION_JSON))
				.andExpect(status().isBadRequest()).andReturn();

		//THEN
		ErrorRS error = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ErrorRS.class);
		assertEquals(INCORRECT_REQUEST, error.getErrorType());
		assertEquals(INCORRECT_REQUEST_MESSAGE + "[" + FIELD_NAME_IS_BLANK_MESSAGE + " " + FIELD_NAME_SIZE_MESSAGE + "] ", error.getMessage());
	}

	@Test
	public void startChildTestItemShouldReturnErrorWhenNameIsGreaterThanOneThousandTwentyFourCharacters() throws Exception {
		//GIVEN
		StartTestItemRQ startTestItemRQ = prepareTestItem();
		startTestItemRQ.setName(LONG_NAME_VALUE);

		//WHEN
		MvcResult mvcResult = mockMvc.perform(post(DEFAULT_PROJECT_BASE_URL + ITEM_PATH + PARENT_ID_PATH)
				.with(token(oAuthHelper.getDefaultToken()))
				.content(objectMapper.writeValueAsBytes(startTestItemRQ))
				.contentType(APPLICATION_JSON))
				.andExpect(status().isBadRequest()).andReturn();

		//THEN
		ErrorRS error = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ErrorRS.class);
		assertEquals(INCORRECT_REQUEST, error.getErrorType());
		assertEquals(INCORRECT_REQUEST_MESSAGE + "[" + FIELD_NAME_SIZE_MESSAGE + "] ", error.getMessage());
	}

	private StartTestItemRQ prepareTestItem() {
		StartTestItemRQ startTestItemRQ = new StartTestItemRQ();
		startTestItemRQ.setLaunchId("a7b66ef2-db30-4db7-94df-f5f7786b398a");
		startTestItemRQ.setType("SUITE");
		startTestItemRQ.setUniqueId(UUID.randomUUID().toString());
		startTestItemRQ.setStartTime(Date.from(LocalDateTime.now().atZone(ZoneId.of("UTC")).toInstant()));
		return startTestItemRQ;
	}
}
