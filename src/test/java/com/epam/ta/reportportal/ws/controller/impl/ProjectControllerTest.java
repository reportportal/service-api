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
import com.epam.ta.reportportal.commons.SendCase;
import com.epam.ta.reportportal.database.dao.ProjectRepository;
import com.epam.ta.reportportal.database.entity.Project;
import com.epam.ta.reportportal.database.entity.project.email.EmailSenderCase;
import com.epam.ta.reportportal.database.entity.project.email.ProjectEmailConfig;
import com.epam.ta.reportportal.ws.BaseMvcTest;
import com.epam.ta.reportportal.ws.model.Page;
import com.epam.ta.reportportal.ws.model.project.*;
import com.epam.ta.reportportal.ws.model.project.email.EmailSenderCaseDTO;
import com.epam.ta.reportportal.ws.model.project.email.ProjectEmailConfigDTO;
import com.epam.ta.reportportal.ws.model.user.UserResource;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MvcResult;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Dzmitry_Kavalets
 */
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
		mvcMock.perform(
				post("/project").content(objectMapper.writeValueAsBytes(rq)).contentType(APPLICATION_JSON).principal(authentication()))
				.andExpect(status().isCreated());
		Project project = projectRepository.findOne("TestProject".toLowerCase());
		assertNotNull(project.getConfiguration());
		assertNotNull(project.getConfiguration().getSubTypes());
		assertFalse(project.getConfiguration().getSubTypes().isEmpty());
	}

	@Test
	public void updateProjectPositive() throws Exception {
		final UpdateProjectRQ rq = new UpdateProjectRQ();
		rq.setCustomer("customer");
		mvcMock.perform(put("/project/project1").content(objectMapper.writeValueAsBytes(rq))
				.contentType(APPLICATION_JSON)
				.principal(authentication())).andExpect(status().is(200));
	}

	@Test
	public void deleteProjectPositive() throws Exception {
		mvcMock.perform(delete("/project/project1").principal(authentication())).andExpect(status().is(200));
	}

	@Test
	public void getProjectUsersPositive() throws Exception {
		mvcMock.perform(get("/project/project1/users").principal(authentication())).andExpect(status().is(200));
	}

	@Test
	public void getProjectPositive() throws Exception {
		mvcMock.perform(get("/project/project1").principal(authentication())).andExpect(status().is(200));
	}

	@Test
	public void unassignProjectUsersPositive() throws Exception {
		UnassignUsersRQ rq = new UnassignUsersRQ();
		rq.setUsernames(singletonList("user2"));
		mvcMock.perform(put("/project/project1/unassign").principal(authentication())
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(rq))).andExpect(status().is(200));
	}

	@Test
	public void unassignUsersEmptyUserNames() throws Exception {
		UnassignUsersRQ rq = new UnassignUsersRQ();
		rq.setUsernames(new ArrayList<>());
		mvcMock.perform(put("/project/project1/unassign").principal(authentication())
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(rq))).andExpect(status().is(200));
	}

	@Test
	public void assignProjectUsersPositive() throws Exception {
		AssignUsersRQ rq = new AssignUsersRQ();
		Map<String, String> user = new HashMap<>();
		user.put("user3", "MEMBER");
		rq.setUserNames(user);
		mvcMock.perform(put("/project/project1/assign").principal(authentication())
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(rq))).andExpect(status().is(200));
	}

	@Test
	public void getUsersForAssignPositive() throws Exception {
		mvcMock.perform(get("/project/project1/assignable").principal(authentication())).andExpect(status().is(200));
	}

	@Test
	public void getProjectUsersTest() throws Exception {
		mvcMock.perform(get("/project/project1/usernames?filter.cnt.users=user").principal(authentication())).andExpect(status().is(200));
	}

	@Test
	public void updateUserPreferencePositive() throws Exception {
		UpdateProjectRQ rq = new UpdateProjectRQ();
		mvcMock.perform(put("/project/project1/preference/user1").principal(authentication())
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(rq))).andExpect(status().is(200));
	}

	@Test
	public void getUserPreferencePositive() throws Exception {
		mvcMock.perform(get("/project/project1/preference/user1").principal(authentication())).andExpect(status().is(200));
	}

	@Test
	public void getAllProjectsInfo() throws Exception {
		final MvcResult mvcResult = mvcMock.perform(
				get("/project/list?page.page=1&page.size=51&page.sort=name,DESC&filter.eq.configuration$entryType=INTERNAL").principal(
						authentication())).andExpect(status().is(200)).andReturn();
		Page<ProjectInfoResource> entries = objectMapper.readValue(mvcResult.getResponse().getContentAsString(),
				new TypeReference<Page<ProjectInfoResource>>() {
				}
		);
		final Collection<ProjectInfoResource> content = entries.getContent();
		assertThat(content).hasSize(2);
		assertThat(content.stream().map(ProjectInfoResource::getProjectId).collect(Collectors.toList())).containsSequence("project2",
				"project1"
		);
		content.stream().forEach(it -> assertThat(it.getEntryType()).isEqualTo("INTERNAL"));
	}

	@Test
	public void getAllProjectNames() throws Exception {
		mvcMock.perform(get("/project/names").principal(authentication())).andExpect(status().is(200));
	}

	@Test
	public void getProjectInfoPositive() throws Exception {
		mvcMock.perform(get("/project/list/project1").principal(authentication())).andExpect(status().is(200));
	}

	@Test
	public void getProjectWidget() throws Exception {
		mvcMock.perform(get("/project/project1/widget/investigated").principal(authentication())).andExpect(status().is(200));
	}

	@Test
	public void updateProjectEmailConfig() throws Exception {
		final EmailSenderCaseDTO emailSenderCase = new EmailSenderCaseDTO(Arrays.asList("OWNER", "user1", "user1email@epam.com"),
				SendCase.ALWAYS.name(), singletonList("launchName"), singletonList("tags")
		);
		final ProjectEmailConfigDTO config = new ProjectEmailConfigDTO(true, "from@fake.org", Lists.newArrayList(emailSenderCase));
		this.mvcMock.perform(put("/project/project1/emailconfig").principal(authentication())
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(config))).andExpect(status().is(200));
		final Project project = projectRepository.findOne("project1");
		final ProjectEmailConfig emailConfig = project.getConfiguration().getEmailConfig();
		assertThat(emailConfig).isNotNull();
		assertThat(emailConfig.getEmailCases()).hasSize(1);
		final EmailSenderCase senderCase = emailConfig.getEmailCases().get(0);
		assertThat(senderCase.getLaunchNames()).hasSize(1).contains("launchName");
		assertThat(senderCase.getTags()).hasSize(1).contains("tags");

	}

	@Test
	public void getUsersFilterByEmailTest() throws Exception {
		MvcResult mvcResult = mvcMock.perform(get("/project/project1/users?filter.cnt.email=user").principal(authentication()))
				.andExpect(status().is(200))
				.andReturn();
		Page<UserResource> userResources = new Gson().fromJson(mvcResult.getResponse().getContentAsString(),
				new TypeToken<Page<UserResource>>() {
				}.getType()
		);
		Map<String, UserResource> userResourceMap = userResources.getContent()
				.stream()
				.collect(Collectors.toMap(UserResource::getUserId, it -> it));
		Assert.assertEquals(3, userResourceMap.size());
		Assert.assertTrue(userResourceMap.containsKey("user1"));
		Assert.assertTrue(userResourceMap.containsKey("user2"));
		Assert.assertTrue(userResourceMap.containsKey("user4"));
	}

	@Override
	protected Authentication authentication() {
		return AuthConstants.ADMINISTRATOR;
	}
}