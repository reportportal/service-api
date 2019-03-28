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

import com.epam.ta.reportportal.dao.IssueTypeRepository;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.entity.enums.ProjectAttributeEnum;
import com.epam.ta.reportportal.entity.integration.Integration;
import com.epam.ta.reportportal.entity.item.issue.IssueType;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.project.ProjectIssueType;
import com.epam.ta.reportportal.ws.BaseMvcTest;
import com.epam.ta.reportportal.ws.model.DeleteBulkRQ;
import com.epam.ta.reportportal.ws.model.Page;
import com.epam.ta.reportportal.ws.model.ValidationConstraints;
import com.epam.ta.reportportal.ws.model.user.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@Sql("/db/user/user-fill.sql")
class UserControllerTest extends BaseMvcTest {

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private ProjectRepository projectRepository;

	@Autowired
	private IssueTypeRepository issueTypeRepository;

	@Test
	void createUserByAdminPositive() throws Exception {
		CreateUserRQFull rq = new CreateUserRQFull();
		rq.setLogin("testLogin");
		rq.setPassword("testPassword");
		rq.setFullName("Test User");
		rq.setEmail("test@test.com");
		rq.setAccountRole("USER");
		rq.setProjectRole("MEMBER");
		rq.setDefaultProject("default_personal");

		mockMvc.perform(post("/user").with(token(oAuthHelper.getSuperadminToken()))
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(rq))).andExpect(status().isCreated());

		final Optional<Project> projectOptional = projectRepository.findByName("default_personal");
		assertTrue(projectOptional.isPresent());
		assertTrue(projectOptional.get().getUsers().stream().anyMatch(config -> config.getUser().getLogin().equals("testlogin")));

		Optional<Project> personalProject = projectRepository.findByName("testlogin_personal");
		assertTrue(personalProject.isPresent(), "Personal project isn't created");
		Project project = personalProject.get();

		List<IssueType> defaultIssueTypes = issueTypeRepository.getDefaultIssueTypes();

		project.getProjectAttributes()
				.forEach(projectAttribute -> assertTrue(projectAttribute.getValue()
						.equalsIgnoreCase(ProjectAttributeEnum.findByAttributeName(projectAttribute.getAttribute().getName())
								.get()
								.getDefaultValue())));

		assertTrue(defaultIssueTypes.containsAll(project.getProjectIssueTypes()
				.stream()
				.map(ProjectIssueType::getIssueType)
				.collect(Collectors.toList())));
	}

	@Test
	void createUserBidPositive() throws Exception {
		CreateUserRQ rq = new CreateUserRQ();
		rq.setDefaultProject("default_personal");
		rq.setEmail("test@domain.com");
		rq.setRole("PROJECT_MANAGER");

		when(mailServiceFactory.getEmailService(any(Integration.class), any(Boolean.class))).thenReturn(emailService);
		doNothing().when(emailService).sendCreateUserConfirmationEmail(any(), any(), any());

		MvcResult mvcResult = mockMvc.perform(post("/user/bid").with(token(oAuthHelper.getDefaultToken()))
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(rq))).andExpect(status().isCreated()).andReturn();
		CreateUserBidRS createUserBidRS = new Gson().fromJson(mvcResult.getResponse().getContentAsString(), CreateUserBidRS.class);
		assertNotNull(createUserBidRS.getBackLink());
		assertNotNull(createUserBidRS.getBid());
		assertTrue(createUserBidRS.getBackLink().contains("/ui/#registration?uuid=" + createUserBidRS.getBid()));
	}

	@Test
	void createUserPositive() throws Exception {
		CreateUserRQConfirm rq = new CreateUserRQConfirm();
		rq.setLogin("testLogin");
		rq.setPassword("testPassword");
		rq.setFullName("Test User");
		rq.setEmail("test@domain.com");
		mockMvc.perform(post("/user/registration?uuid=e5f98deb-8966-4b2d-ba2f-35bc69d30c06").contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(rq))).andExpect(status().isCreated());
	}

	@Test
	void getUserBidInfoPositive() throws Exception {
		mockMvc.perform(get("/user/registration?uuid=e5f98deb-8966-4b2d-ba2f-35bc69d30c06")).andExpect(status().isOk());
	}

	@Test
	void deleteUserNegative() throws Exception {
		/* Administrator cannot remove him/her-self */
		mockMvc.perform(delete("/user/1").with(token(oAuthHelper.getSuperadminToken()))).andExpect(status().is(400));
	}

	@Test
	void deleteUserPositive() throws Exception {
		mockMvc.perform(delete("/user/2").with(token(oAuthHelper.getSuperadminToken()))).andExpect(status().isOk());
	}

	@Test
	void deleteUsers() throws Exception {

		DeleteBulkRQ deleteBulkRQ = new DeleteBulkRQ();
		deleteBulkRQ.setIds(Lists.newArrayList(2L));

		mockMvc.perform(delete("/user").with(token(oAuthHelper.getSuperadminToken()))
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(deleteBulkRQ))).andExpect(status().isOk());
	}

	@Test
	void editUserPositive() throws Exception {
		EditUserRQ rq = new EditUserRQ();
		rq.setFullName("Vasya Pupkin");
		rq.setEmail("defaultemail@domain.com");
		rq.setRole("USER");
		mockMvc.perform(put("/user/default").with(token(oAuthHelper.getDefaultToken()))
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(rq))).andExpect(status().isOk());
	}

	@Test
	void editUserShortName() throws Exception {
		EditUserRQ editUserRQ = new EditUserRQ();
		editUserRQ.setEmail("defaltemail@domain.com");
		editUserRQ.setFullName("1");
		mockMvc.perform(put("/user/default").with(token(oAuthHelper.getDefaultToken()))
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(editUserRQ))).andExpect(status().isBadRequest());
	}

	@Test
	void editUserLongName() throws Exception {
		EditUserRQ editUserRQ = new EditUserRQ();
		editUserRQ.setEmail("defaltemail@domain.com");
		editUserRQ.setFullName(RandomStringUtils.randomAlphabetic(257));
		mockMvc.perform(put("/user/default").with(token(oAuthHelper.getDefaultToken()))
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(editUserRQ))).andExpect(status().isBadRequest());
	}

	@Test
	void editUserNotUniqueEmail() throws Exception {
		EditUserRQ editUserRQ = new EditUserRQ();
		editUserRQ.setFullName("Vasya Pupkin");
		editUserRQ.setEmail("superadminemail@domain.com");
		mockMvc.perform(put("/user/default").with(token(oAuthHelper.getDefaultToken()))
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(editUserRQ))).andExpect(status().is(409));
	}

	@Test
	void editUserUniqueEmail() throws Exception {
		EditUserRQ editUserRQ = new EditUserRQ();
		editUserRQ.setFullName("Vasya Pupkin");
		editUserRQ.setEmail("user1uniquemail@epam.com");
		mockMvc.perform(put("/user/default").with(token(oAuthHelper.getDefaultToken()))
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(editUserRQ))).andExpect(status().isOk());
	}

	@Test
	void getUserPositive() throws Exception {
		mockMvc.perform(get("/user/default").with(token(oAuthHelper.getSuperadminToken()))).andExpect(status().isOk());
	}

	@Test
	void getUserPositiveUsingApiToken() throws Exception {
		mockMvc.perform(get("/user/default").with(token("3a402a94-ed35-4be7-bbed-975fbde2f76d"))).andExpect(status().isOk());
	}

	@Test
	void getUsersPositive() throws Exception {
		mockMvc.perform(get("/user/all").with(token(oAuthHelper.getSuperadminToken()))).andExpect(status().isOk());
	}

	@Test
	void validateUserInfoUsernamePositive() throws Exception {
		mockMvc.perform(get("/user/registration/info?username=default").with(token(oAuthHelper.getSuperadminToken())))
				.andExpect(status().isOk());
	}

	@Test
	void changePasswordWrongOldPassword() throws Exception {
		ChangePasswordRQ rq = new ChangePasswordRQ();
		rq.setOldPassword("password");
		rq.setNewPassword("12345");
		mockMvc.perform(post("/user/password/change").with(token(oAuthHelper.getDefaultToken()))
				.content(objectMapper.writeValueAsBytes(rq))
				.contentType(APPLICATION_JSON)).andExpect(status().isBadRequest());
	}

	@Test
	void changePasswordPositive() throws Exception {
		ChangePasswordRQ rq = new ChangePasswordRQ();
		rq.setOldPassword("1q2w3e");
		rq.setNewPassword("12345");
		mockMvc.perform(post("/user/password/change").with(token(oAuthHelper.getDefaultToken()))
				.content(objectMapper.writeValueAsBytes(rq))
				.contentType(APPLICATION_JSON)).andExpect(status().isOk());
	}

	@Test
	void changePasswordLongNewPassword() throws Exception {
		ChangePasswordRQ rq = new ChangePasswordRQ();
		rq.setOldPassword("1q2w3e");
		rq.setNewPassword(RandomStringUtils.randomAlphabetic(ValidationConstraints.MAX_PASSWORD_LENGTH + 1));
		mockMvc.perform(post("/user/password/change").with(token(oAuthHelper.getDefaultToken()))
				.content(objectMapper.writeValueAsBytes(rq))
				.contentType(APPLICATION_JSON)).andExpect(status().isBadRequest());
	}

	@Test
	void changePasswordShortNewPassword() throws Exception {
		ChangePasswordRQ rq = new ChangePasswordRQ();
		rq.setOldPassword("1q2w3e");
		rq.setNewPassword(RandomStringUtils.randomAlphabetic(ValidationConstraints.MIN_PASSWORD_LENGTH - 1));
		mockMvc.perform(post("/user/password/change").with(token(oAuthHelper.getDefaultToken()))
				.content(objectMapper.writeValueAsBytes(rq))
				.contentType(APPLICATION_JSON)).andExpect(status().isBadRequest());
	}

	@Test
	void restorePassword() throws Exception {
		final RestorePasswordRQ restorePasswordRQ = new RestorePasswordRQ();
		restorePasswordRQ.setEmail("defaultemail@domain.com");

		when(mailServiceFactory.getDefaultEmailService(true)).thenReturn(emailService);
		doNothing().when(emailService).sendRestorePasswordEmail(any(), any(), any(), any());

		mockMvc.perform(post("/user/password/restore").with(token(oAuthHelper.getDefaultToken()))
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(restorePasswordRQ))).andExpect(status().isOk());
	}

	@Test
	void resetPassword() throws Exception {
		final ResetPasswordRQ resetPasswordRQ = new ResetPasswordRQ();
		resetPasswordRQ.setPassword("password");
		resetPasswordRQ.setUuid("e5f98deb-8966-4b2d-ba2f-35bc69d30c06");
		mockMvc.perform(post("/user/password/reset").with(token(oAuthHelper.getDefaultToken()))
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(resetPasswordRQ))).andExpect(status().isOk());
	}

	@Test
	void isRestorePasswordBidExist() throws Exception {
		mockMvc.perform(get("/user/password/reset/e5f98deb-8966-4b2d-ba2f-35bc69d30c06").with(token(oAuthHelper.getDefaultToken()))
				.contentType(APPLICATION_JSON)).andExpect(status().isOk());
	}

	@Test
	void getUserProjects() throws Exception {
		mockMvc.perform(get("/user/default/projects").with(token(oAuthHelper.getDefaultToken()))).andExpect(status().isOk());
	}

	@Test
	void getMyself() throws Exception {
		mockMvc.perform(get("/user").with(token(oAuthHelper.getDefaultToken()))).andExpect(status().isOk());
	}

	@Test
	void findUsers() throws Exception {
		MvcResult mvcResult = mockMvc.perform(get("/user/search?term=e").with(token(oAuthHelper.getSuperadminToken())))
				.andExpect(status().isOk())
				.andReturn();
		Page userResources = new Gson().fromJson(mvcResult.getResponse().getContentAsString(), Page.class);

		Assertions.assertNotNull(userResources);
		Assertions.assertEquals(2, userResources.getContent().size());
	}

	@Test
	void exportUsers() throws Exception {
		mockMvc.perform(get("/user/export").with(token(oAuthHelper.getSuperadminToken()))).andExpect(status().isOk());
	}
}