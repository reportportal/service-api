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
import com.epam.ta.reportportal.ws.model.issue.DefineIssueRQ;
import com.epam.ta.reportportal.ws.model.issue.Issue;
import com.epam.ta.reportportal.ws.model.issue.IssueDefinition;
import com.epam.ta.reportportal.ws.model.item.LinkExternalIssueRQ;
import com.epam.ta.reportportal.ws.model.item.UnlinkExternalIssueRQ;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
public class TestItemControllerValidationTest extends BaseMvcTest {

	private static final String ITEM_PATH = "/item";
	private static final String PARENT_ID_PATH = "/555";

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
		startTestItemRQ.setName(WHITESPACES_NAME_VALUE);

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
		startTestItemRQ.setName(WHITESPACES_NAME_VALUE);

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

	@Test
	public void shouldReturnBadRequestWhenMoreThan300Issues() throws Exception {
		//GIVEN
		final DefineIssueRQ defineIssueRQ = new DefineIssueRQ();
		defineIssueRQ.setIssues(Stream.generate(() -> {
			final IssueDefinition issueDefinition = new IssueDefinition();
			issueDefinition.setId(1L);
			final Issue issue = new Issue();
			issue.setComment("comment");
			issue.setIssueType("ab001");
			issueDefinition.setIssue(issue);
			return issueDefinition;
		}).limit(301).collect(Collectors.toList()));

		//WHEN
		MvcResult mvcResult = mockMvc.perform(put(DEFAULT_PROJECT_BASE_URL + ITEM_PATH).with(token(oAuthHelper.getDefaultToken()))
				.content(objectMapper.writeValueAsBytes(defineIssueRQ))
				.contentType(APPLICATION_JSON)).andExpect(status().isBadRequest()).andReturn();

		//THEN
		ErrorRS error = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ErrorRS.class);
		assertEquals(INCORRECT_REQUEST, error.getErrorType());
		assertEquals(INCORRECT_REQUEST_MESSAGE + "[Field 'issues' should have size from '0' to '300'.] ",
				error.getMessage()
		);
	}

	@Test
	public void shouldReturnBadRequestWhenMoreThan300IssuesToLink() throws Exception {
		//GIVEN
		final LinkExternalIssueRQ linkExternalIssueRQ = new LinkExternalIssueRQ();
		final Issue.ExternalSystemIssue externalSystemIssue = getExternalSystemIssue();
		linkExternalIssueRQ.setIssues(Stream.generate(() -> externalSystemIssue).limit(301).collect(Collectors.toList()));
		linkExternalIssueRQ.setTestItemIds(List.of(1L));

		//WHEN
		MvcResult mvcResult = mockMvc.perform(put(
				DEFAULT_PROJECT_BASE_URL + ITEM_PATH + "/issue/link").with(token(oAuthHelper.getDefaultToken()))
				.content(objectMapper.writeValueAsBytes(linkExternalIssueRQ))
				.contentType(APPLICATION_JSON)).andExpect(status().isBadRequest()).andReturn();

		//THEN
		ErrorRS error = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ErrorRS.class);
		assertEquals(INCORRECT_REQUEST, error.getErrorType());
		assertEquals(INCORRECT_REQUEST_MESSAGE + "[Field 'issues' should have size from '0' to '300'.] ",
				error.getMessage()
		);
	}

	private Issue.ExternalSystemIssue getExternalSystemIssue() {
		final Issue.ExternalSystemIssue externalSystemIssue = new Issue.ExternalSystemIssue();
		externalSystemIssue.setBtsProject("prj");
		externalSystemIssue.setUrl("url");
		externalSystemIssue.setBtsUrl("btsUrl");
		externalSystemIssue.setSubmitDate(123L);
		externalSystemIssue.setTicketId("id");
		return externalSystemIssue;
	}

	@Test
	public void shouldReturnBadRequestWhenMoreThan300ItemIdsToLink() throws Exception {
		//GIVEN
		final LinkExternalIssueRQ linkExternalIssueRQ = new LinkExternalIssueRQ();
		final Issue.ExternalSystemIssue externalSystemIssue = getExternalSystemIssue();
		linkExternalIssueRQ.setIssues(List.of(externalSystemIssue));
		final List<Long> itemIds = Stream.generate(() -> 1L).limit(301).collect(Collectors.toList());
		linkExternalIssueRQ.setTestItemIds(itemIds);

		//WHEN
		MvcResult mvcResult = mockMvc.perform(put(
				DEFAULT_PROJECT_BASE_URL + ITEM_PATH + "/issue/link").with(token(oAuthHelper.getDefaultToken()))
				.content(objectMapper.writeValueAsBytes(linkExternalIssueRQ))
				.contentType(APPLICATION_JSON)).andExpect(status().isBadRequest()).andReturn();

		//THEN
		ErrorRS error = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ErrorRS.class);
		assertEquals(INCORRECT_REQUEST, error.getErrorType());
		assertEquals(INCORRECT_REQUEST_MESSAGE + "[Field 'testItemIds' should have size from '0' to '300'.] ",
				error.getMessage()
		);
	}

	@Test
	public void shouldReturnBadRequestWhenMoreThan300TicketsToUnlink() throws Exception {
		//GIVEN
		final UnlinkExternalIssueRQ unlinkExternalIssueRQ = new UnlinkExternalIssueRQ();
		unlinkExternalIssueRQ.setTicketIds(Stream.generate(() -> "id").limit(301).collect(Collectors.toList()));
		unlinkExternalIssueRQ.setTestItemIds(List.of(1L));

		//WHEN
		MvcResult mvcResult = mockMvc.perform(put(
				DEFAULT_PROJECT_BASE_URL + ITEM_PATH + "/issue/unlink").with(token(oAuthHelper.getDefaultToken()))
				.content(objectMapper.writeValueAsBytes(unlinkExternalIssueRQ))
				.contentType(APPLICATION_JSON)).andExpect(status().isBadRequest()).andReturn();

		//THEN
		ErrorRS error = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ErrorRS.class);
		assertEquals(INCORRECT_REQUEST, error.getErrorType());
		assertEquals(INCORRECT_REQUEST_MESSAGE + "[Field 'ticketIds' should have size from '0' to '300'.] ",
				error.getMessage()
		);
	}

	@Test
	public void shouldReturnBadRequestWhenMoreThan300ItemIdsToUnlink() throws Exception {
		//GIVEN
		final UnlinkExternalIssueRQ unlinkExternalIssueRQ = new UnlinkExternalIssueRQ();
		unlinkExternalIssueRQ.setTicketIds(List.of("id"));
		unlinkExternalIssueRQ.setTestItemIds(Stream.generate(() -> 1L).limit(301).collect(Collectors.toList()));

		//WHEN
		MvcResult mvcResult = mockMvc.perform(put(
				DEFAULT_PROJECT_BASE_URL + ITEM_PATH + "/issue/unlink").with(token(oAuthHelper.getDefaultToken()))
				.content(objectMapper.writeValueAsBytes(unlinkExternalIssueRQ))
				.contentType(APPLICATION_JSON)).andExpect(status().isBadRequest()).andReturn();

		//THEN
		ErrorRS error = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ErrorRS.class);
		assertEquals(INCORRECT_REQUEST, error.getErrorType());
		assertEquals(INCORRECT_REQUEST_MESSAGE + "[Field 'testItemIds' should have size from '0' to '300'.] ",
				error.getMessage()
		);
	}

	private StartTestItemRQ prepareTestItem() {
		StartTestItemRQ startTestItemRQ = new StartTestItemRQ();
		startTestItemRQ.setLaunchUuid("a7b66ef2-db30-4db7-94df-f5f7786b398a");
		startTestItemRQ.setType("SUITE");
		startTestItemRQ.setUniqueId(UUID.randomUUID().toString());
		startTestItemRQ.setStartTime(Date.from(LocalDateTime.now().atZone(ZoneId.of("UTC")).toInstant()));
		return startTestItemRQ;
	}
}
