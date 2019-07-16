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
import com.epam.ta.reportportal.ws.model.attribute.ItemAttributesRQ;
import com.epam.ta.reportportal.ws.model.launch.MergeLaunchesRQ;
import com.epam.ta.reportportal.ws.model.launch.StartLaunchRQ;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Date;
import java.util.HashSet;

import static com.epam.ta.reportportal.ws.model.ErrorType.INCORRECT_REQUEST;
import static com.epam.ta.reportportal.ws.model.launch.Mode.DEFAULT;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author <a href="mailto:tatyana_gladysheva@epam.com">Tatyana Gladysheva</a>
 */
public class LaunchControllerValidationTest extends BaseMvcTest {

	private static final String LAUNCH_PATH = "/launch";
	private static final String MERGE_PATH = "/merge";

	private static final String INCORRECT_REQUEST_MESSAGE = "Incorrect Request. ";
	private static final String FIELD_NAME_IS_NULL_MESSAGE = "[Field 'name' should not be null.] ";
	private static final String FIELD_NAME_IS_BLANK_MESSAGE = "Field 'name' should not contain only white spaces and shouldn't be empty.";
	private static final String FIELD_NAME_SIZE_MESSAGE = "Field 'name' should have size from '1' to '256'.";

	private static final String LONG_NAME_VALUE = "tttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttt"
			+ "tttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttt"
			+ "ttttttttttttttttttttttttttttttttttttttttttttt";

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	public void createLaunchShouldReturnErrorWhenNameIsNull() throws Exception {
		//GIVEN
		StartLaunchRQ startLaunchRQ = prepareLaunch();

		//WHEN
		MvcResult mvcResult = mockMvc.perform(post(DEFAULT_PROJECT_BASE_URL + LAUNCH_PATH)
				.with(token(oAuthHelper.getDefaultToken()))
				.content(objectMapper.writeValueAsBytes(startLaunchRQ))
				.contentType(APPLICATION_JSON))
				.andExpect(status().isBadRequest()).andReturn();

		//THEN
		ErrorRS error = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ErrorRS.class);
		assertEquals(INCORRECT_REQUEST, error.getErrorType());
		assertEquals(INCORRECT_REQUEST_MESSAGE + FIELD_NAME_IS_NULL_MESSAGE, error.getMessage());
	}

	@Test
	public void createLaunchShouldReturnErrorWhenNameIsEmpty() throws Exception {
		//GIVEN
		StartLaunchRQ startLaunchRQ = prepareLaunch();
		startLaunchRQ.setName(EMPTY);

		//WHEN
		MvcResult mvcResult = mockMvc.perform(post(DEFAULT_PROJECT_BASE_URL + LAUNCH_PATH)
				.with(token(oAuthHelper.getDefaultToken()))
				.content(objectMapper.writeValueAsBytes(startLaunchRQ))
				.contentType(APPLICATION_JSON))
				.andExpect(status().isBadRequest()).andReturn();

		//THEN
		ErrorRS error = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ErrorRS.class);
		assertEquals(INCORRECT_REQUEST, error.getErrorType());
		assertEquals(INCORRECT_REQUEST_MESSAGE + "[" + FIELD_NAME_IS_BLANK_MESSAGE + " " + FIELD_NAME_SIZE_MESSAGE + "] ", error.getMessage());
	}

	@Test
	public void createLaunchShouldReturnErrorWhenNameConsistsOfWhitespaces() throws Exception {
		//GIVEN
		StartLaunchRQ startLaunchRQ = prepareLaunch();
		startLaunchRQ.setName("    ");

		//WHEN
		MvcResult mvcResult = mockMvc.perform(post(DEFAULT_PROJECT_BASE_URL + LAUNCH_PATH)
				.with(token(oAuthHelper.getDefaultToken()))
				.content(objectMapper.writeValueAsBytes(startLaunchRQ))
				.contentType(APPLICATION_JSON))
				.andExpect(status().isBadRequest()).andReturn();

		//THEN
		ErrorRS error = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ErrorRS.class);
		assertEquals(INCORRECT_REQUEST, error.getErrorType());
		assertEquals(INCORRECT_REQUEST_MESSAGE + "[" + FIELD_NAME_IS_BLANK_MESSAGE + " " + FIELD_NAME_SIZE_MESSAGE + "] ", error.getMessage());
	}

	@Test
	public void createLaunchShouldReturnErrorWhenNameIsGreaterThanTwoHundredAndFiftySixCharacters() throws Exception {
		//GIVEN
		StartLaunchRQ startLaunchRQ = prepareLaunch();
		startLaunchRQ.setName(LONG_NAME_VALUE);

		//WHEN
		MvcResult mvcResult = mockMvc.perform(post(DEFAULT_PROJECT_BASE_URL + LAUNCH_PATH)
				.with(token(oAuthHelper.getDefaultToken()))
				.content(objectMapper.writeValueAsBytes(startLaunchRQ))
				.contentType(APPLICATION_JSON))
				.andExpect(status().isBadRequest()).andReturn();

		//THEN
		ErrorRS error = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ErrorRS.class);
		assertEquals(INCORRECT_REQUEST, error.getErrorType());
		assertEquals(INCORRECT_REQUEST_MESSAGE + "[" + FIELD_NAME_SIZE_MESSAGE + "] ", error.getMessage());
	}

	private StartLaunchRQ prepareLaunch() {
		StartLaunchRQ startLaunchRQ = new StartLaunchRQ();
		startLaunchRQ.setDescription("some description");
		startLaunchRQ.setStartTime(new Date());
		startLaunchRQ.setMode(DEFAULT);
		startLaunchRQ.setAttributes(Sets.newHashSet(new ItemAttributesRQ("key", "value")));
		return startLaunchRQ;
	}

	@Test
	public void mergeLaunchShouldReturnErrorWhenNameIsNull() throws Exception {
		//GIVEN
		MergeLaunchesRQ mergeLaunchesRQ = prepareLaunchesMerge();

		//WHEN
		MvcResult mvcResult = mockMvc.perform(post(DEFAULT_PROJECT_BASE_URL + LAUNCH_PATH + MERGE_PATH)
				.with(token(oAuthHelper.getDefaultToken()))
				.content(objectMapper.writeValueAsBytes(mergeLaunchesRQ))
				.contentType(APPLICATION_JSON))
				.andExpect(status().isBadRequest()).andReturn();

		//THEN
		ErrorRS error = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ErrorRS.class);
		assertEquals(INCORRECT_REQUEST, error.getErrorType());
		assertEquals(INCORRECT_REQUEST_MESSAGE + FIELD_NAME_IS_NULL_MESSAGE, error.getMessage());
	}

	@Test
	public void mergeLaunchShouldReturnErrorWhenNameIsEmpty() throws Exception {
		//GIVEN
		MergeLaunchesRQ mergeLaunchesRQ = prepareLaunchesMerge();
		mergeLaunchesRQ.setName(EMPTY);

		//WHEN
		MvcResult mvcResult = mockMvc.perform(post(DEFAULT_PROJECT_BASE_URL + LAUNCH_PATH + MERGE_PATH)
				.with(token(oAuthHelper.getDefaultToken()))
				.content(objectMapper.writeValueAsBytes(mergeLaunchesRQ))
				.contentType(APPLICATION_JSON))
				.andExpect(status().isBadRequest()).andReturn();

		//THEN
		ErrorRS error = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ErrorRS.class);
		assertEquals(INCORRECT_REQUEST, error.getErrorType());
		assertEquals(INCORRECT_REQUEST_MESSAGE + "[" + FIELD_NAME_IS_BLANK_MESSAGE + " " + FIELD_NAME_SIZE_MESSAGE + "] ", error.getMessage());
	}

	@Test
	public void mergeLaunchShouldReturnErrorWhenNameConsistsOfWhitespaces() throws Exception {
		//GIVEN
		MergeLaunchesRQ mergeLaunchesRQ = prepareLaunchesMerge();
		mergeLaunchesRQ.setName("    ");

		//WHEN
		MvcResult mvcResult = mockMvc.perform(post(DEFAULT_PROJECT_BASE_URL + LAUNCH_PATH + MERGE_PATH)
				.with(token(oAuthHelper.getDefaultToken()))
				.content(objectMapper.writeValueAsBytes(mergeLaunchesRQ))
				.contentType(APPLICATION_JSON))
				.andExpect(status().isBadRequest()).andReturn();

		//THEN
		ErrorRS error = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ErrorRS.class);
		assertEquals(INCORRECT_REQUEST, error.getErrorType());
		assertEquals(INCORRECT_REQUEST_MESSAGE + "[" + FIELD_NAME_IS_BLANK_MESSAGE + " " + FIELD_NAME_SIZE_MESSAGE + "] ", error.getMessage());
	}

	@Test
	public void mergeLaunchShouldReturnErrorWhenNameIsGreaterThanTwoHundredAndFiftySixCharacters() throws Exception {
		//GIVEN
		MergeLaunchesRQ mergeLaunchesRQ = prepareLaunchesMerge();
		mergeLaunchesRQ.setName(LONG_NAME_VALUE);

		//WHEN
		MvcResult mvcResult = mockMvc.perform(post(DEFAULT_PROJECT_BASE_URL + LAUNCH_PATH + MERGE_PATH)
				.with(token(oAuthHelper.getDefaultToken()))
				.content(objectMapper.writeValueAsBytes(mergeLaunchesRQ))
				.contentType(APPLICATION_JSON))
				.andExpect(status().isBadRequest()).andReturn();

		//THEN
		ErrorRS error = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ErrorRS.class);
		assertEquals(INCORRECT_REQUEST, error.getErrorType());
		assertEquals(INCORRECT_REQUEST_MESSAGE + "[" + FIELD_NAME_SIZE_MESSAGE + "] ", error.getMessage());
	}

	private MergeLaunchesRQ prepareLaunchesMerge() {
		MergeLaunchesRQ mergeLaunchesRQ = new MergeLaunchesRQ();

		HashSet<Long> set = new HashSet<>();
		set.add(1L);
		set.add(2L);

		mergeLaunchesRQ.setLaunches(set);
		mergeLaunchesRQ.setMergeStrategyType("BASIC");
		mergeLaunchesRQ.setStartTime(new Date());
		mergeLaunchesRQ.setEndTime(new Date());

		return mergeLaunchesRQ;
	}
}
