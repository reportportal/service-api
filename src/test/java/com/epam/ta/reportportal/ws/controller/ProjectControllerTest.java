/*
 * Copyright 2018 EPAM Systems
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

import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.ws.BaseMvcTest;
import com.epam.ta.reportportal.ws.model.BulkRQ;
import com.epam.ta.reportportal.ws.model.project.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.ResultActions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@Sql("/db/project/project-fill.sql")
public class ProjectControllerTest extends BaseMvcTest {

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private ProjectRepository projectRepository;

	@Test
	public void createProjectPositive() throws Exception {
		CreateProjectRQ rq = new CreateProjectRQ();
		rq.setProjectName("TestProject");
		rq.setEntryType("INTERNAL");
		mockMvc.perform(post("/project").content(objectMapper.writeValueAsBytes(rq))
				.contentType(APPLICATION_JSON)
				.with(token(oAuthHelper.getSuperadminToken()))).andExpect(status().isCreated());
		final Optional<Project> createdProjectOptional = projectRepository.findByName("TestProject" .toLowerCase());
		assertTrue(createdProjectOptional.isPresent());
		assertEquals(11, createdProjectOptional.get().getProjectAttributes().size());
		assertEquals(5, createdProjectOptional.get().getProjectIssueTypes().size());
	}

	@Test
	public void updateProjectPositive() throws Exception {
		final UpdateProjectRQ rq = new UpdateProjectRQ();
		mockMvc.perform(put("/project/test_project").content(objectMapper.writeValueAsBytes(rq))
				.contentType(APPLICATION_JSON)
				.with(token(oAuthHelper.getSuperadminToken()))).andExpect(status().isOk());
	}

	@Test
	public void deleteProjectPositive() throws Exception {
		mockMvc.perform(delete("/project/test_project").with(token(oAuthHelper.getSuperadminToken()))).andExpect(status().isOk());
	}

	@Test
	public void bulkDeleteProjects() throws Exception {
		BulkRQ<DeleteProjectRQ> bulkRQ = new BulkRQ<>();
		Map<Long, DeleteProjectRQ> entities = new HashMap<>();
		DeleteProjectRQ first = new DeleteProjectRQ();
		first.setProjectName("default_personal");
		entities.put(2L, first);
		DeleteProjectRQ second = new DeleteProjectRQ();
		second.setProjectName("test_project");
		entities.put(3L, second);
		bulkRQ.setEntities(entities);
		mockMvc.perform(delete("/project").with(token(oAuthHelper.getSuperadminToken()))
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(bulkRQ))).andExpect(status().isOk());
	}

	@Test
	public void getProjectUsersPositive() throws Exception {
		mockMvc.perform(get("/project/test_project/users").with(token(oAuthHelper.getSuperadminToken()))).andExpect(status().isOk());
	}

	@Test
	public void getProjectPositive() throws Exception {
		mockMvc.perform(get("/project/test_project").with(token(oAuthHelper.getSuperadminToken()))).andExpect(status().isOk());
	}

	@Test
	public void unassignProjectUsersPositive() throws Exception {
		UnassignUsersRQ rq = new UnassignUsersRQ();
		rq.setUsernames(singletonList("test_user"));
		mockMvc.perform(put("/project/test_project/unassign").with(token(oAuthHelper.getSuperadminToken()))
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(rq))).andExpect(status().isOk());
	}

	@Test
	public void unassignUsersEmptyUserNames() throws Exception {
		UnassignUsersRQ rq = new UnassignUsersRQ();
		rq.setUsernames(new ArrayList<>());
		mockMvc.perform(put("/project/test_project/unassign").with(token(oAuthHelper.getSuperadminToken()))
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(rq))).andExpect(status().isBadRequest());
	}

	@Test
	public void assignProjectUsersPositive() throws Exception {
		AssignUsersRQ rq = new AssignUsersRQ();
		Map<String, String> user = new HashMap<>();
		user.put("default", "MEMBER");
		rq.setUserNames(user);
		mockMvc.perform(put("/project/test_project/assign").with(token(oAuthHelper.getSuperadminToken()))
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(rq))).andExpect(status().isOk());
	}

	@Test
	public void getUsersForAssignPositive() throws Exception {
		mockMvc.perform(get("/project/test_project/assignable").with(token(oAuthHelper.getSuperadminToken()))).andExpect(status().isOk());
	}

	@Test
	public void getProjectUsersTest() throws Exception {
		mockMvc.perform(get("/project/test_project/usernames?filter.cnt.users=user").with(token(oAuthHelper.getSuperadminToken())))
				.andExpect(status().isOk());
	}

	@Test
	public void addUserPreference() throws Exception {
		mockMvc.perform(put("/project/test_project/preference/superadmin/2").with(token(oAuthHelper.getSuperadminToken())))
				.andExpect(status().isOk());
	}

	@Test
	public void getUserPreferencePositive() throws Exception {
		mockMvc.perform(get("/project/test_project/preference/superadmin").with(token(oAuthHelper.getSuperadminToken())))
				.andExpect(status().isOk());
	}

	@Test
	public void deleteUserPreference() throws Exception {
		mockMvc.perform(delete("/project/test_project/preference/superadmin/1").with(token(oAuthHelper.getSuperadminToken())))
				.andExpect(status().isOk());
	}

	@Test
	public void getAllProjectNames() throws Exception {
		mockMvc.perform(get("/project/names").with(token(oAuthHelper.getSuperadminToken()))).andExpect(status().isOk());
	}

	@Test
	public void getProjectInfoPositive() throws Exception {
		mockMvc.perform(get("/project/list/test_project").with(token(oAuthHelper.getSuperadminToken()))).andExpect(status().isOk());
	}

	@Test
	public void searchForUsername() throws Exception {
		mockMvc.perform(get("/project/test_project/usernames/search?term=user").with(token(oAuthHelper.getSuperadminToken())))
				.andExpect(status().isOk());
	}

	@Test
	public void export() throws Exception {
		final ResultActions resultActions = mockMvc.perform(get("/project/export").with(token(oAuthHelper.getSuperadminToken())));
		resultActions.andExpect(status().isOk());
		assertEquals("text/csv", resultActions.andReturn().getResponse().getContentType());
	}
}