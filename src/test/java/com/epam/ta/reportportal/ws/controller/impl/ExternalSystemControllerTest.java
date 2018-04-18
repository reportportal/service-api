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

import com.epam.ta.reportportal.auth.AuthConstants;
import com.epam.ta.reportportal.ws.BaseMvcTest;
import com.epam.ta.reportportal.ws.model.externalsystem.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;

import java.util.*;

import static com.epam.ta.reportportal.database.entity.AuthType.APIKEY;
import static java.util.Collections.singletonList;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Ignore
public class ExternalSystemControllerTest extends BaseMvcTest {

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	public void createExternalSystemTest() throws Exception {
		final CreateExternalSystemRQ rq = new CreateExternalSystemRQ();
		rq.setUrl("https://rp.epam.com/");
		rq.setExternalSystemType("RALLY");
		rq.setExternalSystemAuth(APIKEY.name());
		this.mvcMock.perform(post(PROJECT_BASE_URL + "/external-system").content(objectMapper.writeValueAsBytes(rq))
				.contentType(APPLICATION_JSON)
				.principal(authentication())).andExpect(status().isCreated());
	}

	@Test
	public void deleteExternalSystem() throws Exception {
		this.mvcMock.perform(delete(PROJECT_BASE_URL + "/external-system/54958aec4e84859227150765").principal(authentication()))
				.andExpect(status().is(200));
	}

	@Test
	public void getExternalSystem() throws Exception {
		this.mvcMock.perform(get(PROJECT_BASE_URL + "/external-system/54958aec4e84859227150765").principal(authentication()))
				.andExpect(status().is(200));
	}

	@Test
	public void deleteAllExternalSystems() throws Exception {
		this.mvcMock.perform(delete(PROJECT_BASE_URL + "/external-system/clear").principal(authentication())).andExpect(status().is(200));
	}

	@Test
	public void getTicketTest() throws Exception {
		this.mvcMock.perform(get(PROJECT_BASE_URL + "/external-system/54958aec4e84859227150766/ticket/DE1").principal(authentication()))
				.andExpect(status().is(200));
	}

	@Test
	public void createTicket() throws Exception {
		final PostTicketRQ rq = new PostTicketRQ();
		rq.setTestItemId("44534cc1553de743b3e5aa30");
		rq.setBackLinks(new HashMap<String, String>() {
			private static final long serialVersionUID = 1L;

			{
				put("", "");
			}
		});
		final PostFormField postFormField = getPostFormField();
		rq.setFields(Collections.singletonList(postFormField));
		this.mvcMock.perform(post(PROJECT_BASE_URL + "/external-system/54958aec4e84859227150766/ticket").contentType(APPLICATION_JSON)
				.principal(authentication())
				.content(objectMapper.writeValueAsBytes(rq))).andExpect(status().isCreated());
	}

	@Test
	public void getExternalSystemFields() throws Exception {
		this.mvcMock.perform(
				get(PROJECT_BASE_URL + "/external-system/54958aec4e84859227150766/fields-set?issuetype=type").principal(authentication()))
				.andExpect(status().is(200));
	}

	@Test
	public void updateExternalSystem() throws Exception {
		final UpdateExternalSystemRQ rq = new UpdateExternalSystemRQ();
		rq.setUrl("https://rp.epam.com/");
		rq.setExternalSystemType("RALLY");
		rq.setProject("project");
		rq.setFields(Collections.singletonList(getPostFormField()));
		rq.setExternalSystemAuth("APIKEY");
		this.mvcMock.perform(put(PROJECT_BASE_URL + "/external-system/54958aec4e84859227150766").contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(rq))
				.principal(authentication())).andExpect(status().is(200));
	}

	@Test
	public void testConnectionTest() throws Exception {
		final UpdateExternalSystemRQ rq = new UpdateExternalSystemRQ();
		rq.setExternalSystemType("RALLY");
		rq.setExternalSystemAuth("APIKEY");
		rq.setUrl("https://rp.epam.com/");
		this.mvcMock.perform(put(PROJECT_BASE_URL + "/external-system/54958aec4e84859227150766/connect").contentType(APPLICATION_JSON)
				.principal(authentication())
				.content(objectMapper.writeValueAsBytes(rq))).andExpect(status().is(200));
	}

	@Test
	public void submitTicketToJira() throws Exception {
		final PostTicketRQ rq = new PostTicketRQ();
		rq.setFields(getPostFormFields());
		final HashMap<String, String> backLinks = new HashMap<>();
		backLinks.put("44534cc1553de743b3e5aa33", "asdadasdadasd");
		rq.setBackLinks(backLinks);
		rq.setIsIncludeComments(true);
		rq.setIsIncludeScreenshots(true);
		rq.setIsIncludeLogs(true);
		rq.setTestItemId("44534cc1553de743b3e5aa33");
		rq.setNumberOfLogs(1);
		this.mvcMock.perform(post(PROJECT_BASE_URL + "/external-system/54958aec4e84859227150767/ticket").contentType(APPLICATION_JSON)
				.principal(authentication())
				.content(objectMapper.writeValueAsBytes(rq))).andExpect(status().isCreated());
	}

	@Override
	protected Authentication authentication() {
		return AuthConstants.ADMINISTRATOR;
	}

	private PostFormField getPostFormField() {
		PostFormField postFormField = new PostFormField();
		postFormField.setFieldName("Schedule State");
		postFormField.setId("ScheduleState");
		postFormField.setFieldType("string");
		postFormField.setIsRequired(true);
		postFormField.setValue(Collections.singletonList("Defined"));
		AllowedValue defined = new AllowedValue();
		defined.setValueName("Defined");
		AllowedValue inProgress = new AllowedValue();
		inProgress.setValueName("In-Progress");
		AllowedValue completed = new AllowedValue();
		completed.setValueName("Completed");
		AllowedValue accepted = new AllowedValue();
		accepted.setValueName("Accepted");
		postFormField.setDefinedValues(Arrays.asList(defined, inProgress, completed, accepted));
		return postFormField;
	}

	private List<PostFormField> getPostFormFields() {
		final ArrayList<PostFormField> postFormFields = new ArrayList<>();
		postFormFields.add(new PostFormField("issuetype", "issuetype", null, false, singletonList("bug"), null));
		postFormFields.add(new PostFormField("project", "project", null, false, singletonList(""), null));
		postFormFields.add(new PostFormField("description", "description", "string", false, singletonList("description"), null));
		postFormFields.add(new PostFormField("summary", "Summary", "string", true, singletonList("summary"), null));
		postFormFields.add(new PostFormField("priority", "Priority", "priority", false, singletonList("Blocker"), null));
		postFormFields.add(
				new PostFormField("components", "Component/s", "array", false, singletonList("Debug Component"), new ArrayList<>()));
		postFormFields.add(new PostFormField("assignee", "Assignee", "user", false, singletonList("assignee"), null));
		postFormFields.add(new PostFormField("customAssignee1", "CustomAssignee1", "user", false, singletonList("customAssignee1"), null));
		postFormFields.add(new PostFormField("fixVersions", "Fix Version/s", "array", false, singletonList("VERSION1"), new ArrayList<>()));
		postFormFields.add(
				new PostFormField("customArrayField1", "CustomArrayField1", "array", false, singletonList("CustomArrayField1"), null));
		postFormFields.add(
				new PostFormField("customArrayField2", "CustomArrayField2", "array", false, singletonList("CustomArrayField2"), null));
		postFormFields.add(new PostFormField("labels", "Labels", "array", false, singletonList("label"), null));
		postFormFields.add(
				new PostFormField("customComponentField1", "customComponentField1", "string", false, singletonList("Debug Component"),
						new ArrayList<>()
				));
		postFormFields.add(new PostFormField("duedate", "Due Date", "date", false, singletonList("2015-02-02"), null));
		postFormFields.add(new PostFormField("timetracking", "Time Tracking", "timetracking", false, singletonList("timetracking"), null));
		postFormFields.add(new PostFormField("attachment", "Attachment", "array", false, singletonList("attachment"), null));
		postFormFields.add(new PostFormField("Sprint", "Sprint", "sprint", false, singletonList("1"), null));
		return postFormFields;
	}
}