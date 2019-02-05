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
import com.epam.ta.reportportal.entity.integration.Integration;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.util.email.EmailService;
import com.epam.ta.reportportal.util.email.MailServiceFactory;
import com.epam.ta.reportportal.util.integration.email.EmailIntegrationService;
import com.epam.ta.reportportal.ws.BaseMvcTest;
import com.epam.ta.reportportal.ws.model.ValidationConstraints;
import com.epam.ta.reportportal.ws.model.user.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Optional;

import static org.junit.Assert.assertTrue;
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
public class UserControllerTest extends BaseMvcTest {

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private ProjectRepository projectRepository;

	@MockBean
	private MailServiceFactory mailServiceFactory;

	@Mock
	private EmailService emailService;

	@MockBean
	private EmailIntegrationService emailIntegrationService;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void createUserByAdminPositive() throws Exception {
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
		assertTrue("Personal project isn't created", projectRepository.existsByName("testlogin_personal"));
	}

	@Test
	public void createUserBidPositive() throws Exception {
		CreateUserRQ rq = new CreateUserRQ();
		rq.setDefaultProject("default_personal");
		rq.setEmail("test@domain.com");
		rq.setRole("MEMBER");

		when(mailServiceFactory.getEmailService(any(Integration.class), any(Boolean.class))).thenReturn(emailService);
		doNothing().when(emailService).sendCreateUserConfirmationEmail(any(), any(), any());

		MvcResult mvcResult = mockMvc.perform(post("/user/bid").with(token(oAuthHelper.getDefaultToken()))
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(rq))).andExpect(status().isCreated()).andReturn();
		CreateUserBidRS createUserBidRS = new Gson().fromJson(mvcResult.getResponse().getContentAsString(), CreateUserBidRS.class);
		Assert.assertNotNull(createUserBidRS.getBackLink());
		Assert.assertNotNull(createUserBidRS.getBid());
		Assert.assertTrue(createUserBidRS.getBackLink().contains("/ui/#registration?uuid=" + createUserBidRS.getBid()));
	}

	@Test
	public void createUserPositive() throws Exception {
		CreateUserRQConfirm rq = new CreateUserRQConfirm();
		rq.setLogin("testLogin");
		rq.setPassword("testPassword");
		rq.setFullName("Test User");
		rq.setEmail("test@domain.com");
		mockMvc.perform(post("/user/registration?uuid=e5f98deb-8966-4b2d-ba2f-35bc69d30c06").contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(rq))).andExpect(status().isCreated());
	}

	@Test
	public void getUserBidInfoPositive() throws Exception {
		mockMvc.perform(get("/user/registration?uuid=e5f98deb-8966-4b2d-ba2f-35bc69d30c06")).andExpect(status().isOk());
	}

	@Test
	public void deleteUserNegative() throws Exception {
		/* Administrator cannot remove him/her-self */
		mockMvc.perform(delete("/user/superadmin").with(token(oAuthHelper.getSuperadminToken()))).andExpect(status().is(400));
	}

	@Test
	public void deleteUserPositive() throws Exception {
		mockMvc.perform(delete("/user/default").with(token(oAuthHelper.getSuperadminToken()))).andExpect(status().isOk());
	}

	@Test
	public void editUserPositive() throws Exception {
		EditUserRQ rq = new EditUserRQ();
		rq.setFullName("Vasya Pupkin");
		rq.setEmail("defaultemail@domain.com");
		rq.setRole("USER");
		mockMvc.perform(put("/user/default").with(token(oAuthHelper.getDefaultToken()))
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(rq))).andExpect(status().isOk());
	}

	@Test
	public void editUserShortName() throws Exception {
		EditUserRQ editUserRQ = new EditUserRQ();
		editUserRQ.setEmail("defaltemail@domain.com");
		editUserRQ.setFullName("1");
		mockMvc.perform(put("/user/default").with(token(oAuthHelper.getDefaultToken()))
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(editUserRQ))).andExpect(status().isBadRequest());
	}

	@Test
	public void editUserLongName() throws Exception {
		EditUserRQ editUserRQ = new EditUserRQ();
		editUserRQ.setEmail("defaltemail@domain.com");
		editUserRQ.setFullName(RandomStringUtils.randomAlphabetic(257));
		mockMvc.perform(put("/user/default").with(token(oAuthHelper.getDefaultToken()))
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(editUserRQ))).andExpect(status().isBadRequest());
	}

	@Test
	public void editUserNotUniqueEmail() throws Exception {
		EditUserRQ editUserRQ = new EditUserRQ();
		editUserRQ.setFullName("Vasya Pupkin");
		editUserRQ.setEmail("superadminemail@domain.com");
		mockMvc.perform(put("/user/default").with(token(oAuthHelper.getDefaultToken()))
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(editUserRQ))).andExpect(status().is(409));
	}

	@Test
	public void editUserUniqueEmail() throws Exception {
		EditUserRQ editUserRQ = new EditUserRQ();
		editUserRQ.setFullName("Vasya Pupkin");
		editUserRQ.setEmail("user1uniquemail@epam.com");
		mockMvc.perform(put("/user/default").with(token(oAuthHelper.getDefaultToken()))
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(editUserRQ))).andExpect(status().isOk());
	}

	@Test
	public void getUserPositive() throws Exception {
		mockMvc.perform(get("/user/default").with(token(oAuthHelper.getSuperadminToken()))).andExpect(status().isOk());
	}

	@Test
	public void getUsersPositive() throws Exception {
		mockMvc.perform(get("/user/all").with(token(oAuthHelper.getSuperadminToken()))).andExpect(status().isOk());
	}

	@Test
	public void validateUserInfoUsernamePositive() throws Exception {
		mockMvc.perform(get("/user/registration/info?username=default").with(token(oAuthHelper.getSuperadminToken())))
				.andExpect(status().isOk());
	}

	@Test
	public void changePasswordWrongOldPassword() throws Exception {
		ChangePasswordRQ rq = new ChangePasswordRQ();
		rq.setOldPassword("password");
		rq.setNewPassword("12345");
		mockMvc.perform(post("/user/password/change").with(token(oAuthHelper.getDefaultToken()))
				.content(objectMapper.writeValueAsBytes(rq))
				.contentType(APPLICATION_JSON)).andExpect(status().isBadRequest());
	}

	@Test
	public void changePasswordPositive() throws Exception {
		ChangePasswordRQ rq = new ChangePasswordRQ();
		rq.setOldPassword("1q2w3e");
		rq.setNewPassword("12345");
		mockMvc.perform(post("/user/password/change").with(token(oAuthHelper.getDefaultToken()))
				.content(objectMapper.writeValueAsBytes(rq))
				.contentType(APPLICATION_JSON)).andExpect(status().isOk());
	}

	@Test
	public void changePasswordLongNewPassword() throws Exception {
		ChangePasswordRQ rq = new ChangePasswordRQ();
		rq.setOldPassword("1q2w3e");
		rq.setNewPassword(RandomStringUtils.randomAlphabetic(ValidationConstraints.MAX_PASSWORD_LENGTH + 1));
		mockMvc.perform(post("/user/password/change").with(token(oAuthHelper.getDefaultToken()))
				.content(objectMapper.writeValueAsBytes(rq))
				.contentType(APPLICATION_JSON)).andExpect(status().isBadRequest());
	}

	@Test
	public void changePasswordShortNewPassword() throws Exception {
		ChangePasswordRQ rq = new ChangePasswordRQ();
		rq.setOldPassword("1q2w3e");
		rq.setNewPassword(RandomStringUtils.randomAlphabetic(ValidationConstraints.MIN_PASSWORD_LENGTH - 1));
		mockMvc.perform(post("/user/password/change").with(token(oAuthHelper.getDefaultToken()))
				.content(objectMapper.writeValueAsBytes(rq))
				.contentType(APPLICATION_JSON)).andExpect(status().isBadRequest());
	}

	@Test
	public void restorePassword() throws Exception {
		final RestorePasswordRQ restorePasswordRQ = new RestorePasswordRQ();
		restorePasswordRQ.setEmail("defaultemail@domain.com");

		when(mailServiceFactory.getDefaultEmailService(true)).thenReturn(emailService);
		doNothing().when(emailService).sendRestorePasswordEmail(any(), any(), any(), any());

		mockMvc.perform(post("/user/password/restore").with(token(oAuthHelper.getDefaultToken()))
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(restorePasswordRQ))).andExpect(status().isOk());
	}

	@Test
	public void resetPassword() throws Exception {
		final ResetPasswordRQ resetPasswordRQ = new ResetPasswordRQ();
		resetPasswordRQ.setPassword("password");
		resetPasswordRQ.setUuid("e5f98deb-8966-4b2d-ba2f-35bc69d30c06");
		mockMvc.perform(post("/user/password/reset").with(token(oAuthHelper.getDefaultToken()))
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(resetPasswordRQ))).andExpect(status().isOk());
	}

	@Test
	public void isRestorePasswordBidExist() throws Exception {
		mockMvc.perform(get("/user/password/reset/e5f98deb-8966-4b2d-ba2f-35bc69d30c06").with(token(oAuthHelper.getDefaultToken()))
				.contentType(APPLICATION_JSON)).andExpect(status().isOk());
	}

	@Test
	public void getUserProjects() throws Exception {
		mockMvc.perform(get("/user/default/projects").with(token(oAuthHelper.getDefaultToken()))).andExpect(status().isOk());
	}

	@Test
	public void getMyself() throws Exception {
		mockMvc.perform(get("/user").with(token(oAuthHelper.getDefaultToken()))).andExpect(status().isOk());
	}

	@Test
	public void exportUsers() throws Exception {
		mockMvc.perform(get("/user/export").with(token(oAuthHelper.getSuperadminToken()))).andExpect(status().isOk());
	}
}