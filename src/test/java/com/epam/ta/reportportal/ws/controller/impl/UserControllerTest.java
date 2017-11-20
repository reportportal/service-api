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

import com.dumbster.smtp.ServerOptions;
import com.dumbster.smtp.SmtpServer;
import com.dumbster.smtp.SmtpServerFactory;
import com.epam.ta.reportportal.auth.AuthConstants;
import com.epam.ta.reportportal.database.dao.ActivityRepository;
import com.epam.ta.reportportal.database.dao.ProjectRepository;
import com.epam.ta.reportportal.database.entity.item.Activity;
import com.epam.ta.reportportal.database.entity.item.ActivityEventType;
import com.epam.ta.reportportal.database.personal.PersonalProjectService;
import com.epam.ta.reportportal.database.search.Condition;
import com.epam.ta.reportportal.database.search.Filter;
import com.epam.ta.reportportal.ws.BaseMvcTest;
import com.epam.ta.reportportal.ws.model.ValidationConstraints;
import com.epam.ta.reportportal.ws.model.user.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MvcResult;

import java.io.IOException;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Dzmitry_Kavalets
 */
public class UserControllerTest extends BaseMvcTest {

	private static SmtpServer SMTP;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private ProjectRepository projectRepository;

	@Autowired
	private ActivityRepository activityRepository;

	@BeforeClass
	public static void startSmtpServer() throws IOException {
		ServerOptions so = new ServerOptions();
		so.port = 10025;
		SMTP = SmtpServerFactory.startServer(so);
	}

	@AfterClass
	public static void shutdownSmtpServer() {
		SMTP.stop();
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
		rq.setDefaultProject("proJect1");
		this.mvcMock.perform(
				post("/user").principal(authentication()).contentType(APPLICATION_JSON).content(objectMapper.writeValueAsBytes(rq)))
				.andExpect(status().isCreated());
		Assert.assertTrue(
				projectRepository.findOne("project1").getUsers().stream().anyMatch(config -> config.getLogin().equals("testlogin")));
		Assert.assertTrue(
				"Personal project isn't created", projectRepository.exists("testlogin" + PersonalProjectService.PERSONAL_PROJECT_POSTFIX));
	}

	@Test
	// EPAM firewall issue with email sending
	public void createUserBidPositive() throws Exception {
		CreateUserRQ rq = new CreateUserRQ();
		rq.setDefaultProject("project1");
		rq.setEmail("tester@example.com");
		rq.setRole("MEMBER");
		MvcResult mvcResult = mvcMock.perform(
				post("/user/bid").principal(authentication()).contentType(APPLICATION_JSON).content(objectMapper.writeValueAsBytes(rq)))
				.andExpect(status().isCreated())
				.andReturn();
		CreateUserBidRS createUserBidRS = new Gson().fromJson(mvcResult.getResponse().getContentAsString(), CreateUserBidRS.class);
		Assert.assertNotNull(createUserBidRS.getBackLink());
		Assert.assertNotNull(createUserBidRS.getBid());
		Assert.assertTrue(createUserBidRS.getBackLink().contains("/ui/#registration?uuid=" + createUserBidRS.getBid()));
	}

	@Test
	public void createUserPositive() throws Exception {
		CreateUserRQConfirm rq = new CreateUserRQConfirm();
		rq.setLogin("testLogin1");
		rq.setPassword("testPassword");
		rq.setFullName("Test User");
		rq.setEmail("test1@test.com");
		mvcMock.perform(post("/user/registration?uuid=5c4b357a-923e-4b5b-8f90-ae95361550ae").contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(rq))).andExpect(status().isCreated());
		final List<Activity> activityList = activityRepository.findByFilter(
				new Filter(Activity.class, Condition.EQUALS, false, "testlogin1", "userRef"));
		Assert.assertFalse(activityList.isEmpty());
		Assert.assertEquals(1, activityList.size());
		Assert.assertEquals(ActivityEventType.CREATE_USER, activityList.get(0).getActionType());
	}

	@Test
	public void getUserBidInfoPositive() throws Exception {
		this.mvcMock.perform(get("/user/registration?uuid=5c4b357a-923e-4b5b-8f90-ae95361550ae").principal(authentication()))
				.andExpect(status().is(200));
	}

	@Test
	public void deleteUserPositive() throws Exception {
		/* Administrator cannot remove him/her-self */
		this.mvcMock.perform(delete("/user/user1").principal(authentication())).andExpect(status().is(400));
	}

	@Test
	public void editUserPositive() throws Exception {
		EditUserRQ rq = new EditUserRQ();
		rq.setFullName("Vasya Pupkin");
		rq.setEmail("user1email@epam.com");
		rq.setRole("USER");
		this.mvcMock.perform(
				put("/user/user1").principal(authentication()).contentType(APPLICATION_JSON).content(objectMapper.writeValueAsBytes(rq)))
				.andExpect(status().is(200));
	}

	@Test
	public void editUserShortName() throws Exception {
		EditUserRQ editUserRQ = new EditUserRQ();
		editUserRQ.setEmail("user2email@epam.com");
		editUserRQ.setFullName("1");
		this.mvcMock.perform(put("/user/user1").principal(authentication())
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(editUserRQ))).andExpect(status().is(400));
	}

	@Test
	public void editUserLongName() throws Exception {
		EditUserRQ editUserRQ = new EditUserRQ();
		editUserRQ.setEmail("user2email@epam.com");
		editUserRQ.setFullName(RandomStringUtils.randomAlphabetic(257));
		this.mvcMock.perform(put("/user/user1").principal(authentication())
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(editUserRQ))).andExpect(status().is(400));
	}

	@Test
	public void editUserNotUniqueEmail() throws Exception {
		EditUserRQ editUserRQ = new EditUserRQ();
		editUserRQ.setFullName("Vasya Pupkin");
		editUserRQ.setEmail("user2email@epam.com");
		this.mvcMock.perform(put("/user/user1").principal(authentication())
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(editUserRQ))).andExpect(status().is(409));
	}

	@Test
	public void editUserUniqueEmail() throws Exception {
		EditUserRQ editUserRQ = new EditUserRQ();
		editUserRQ.setFullName("Vasya Pupkin");
		editUserRQ.setEmail("user1uniquemail@epam.com");
		this.mvcMock.perform(put("/user/user1").principal(authentication())
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(editUserRQ))).andExpect(status().is(200));
	}

	@Test
	public void getUserPositive() throws Exception {
		this.mvcMock.perform(get("/user/user1").principal(authentication())).andExpect(status().is(200));
	}

	@Test
	public void getUsersPositive() throws Exception {
		this.mvcMock.perform(get("/user/all").principal(authentication())).andExpect(status().is(200));
	}

	@Test
	public void validateUserInfoUsernamePositive() throws Exception {
		this.mvcMock.perform(get("/user/registration/info?username=" + AuthConstants.TEST_USER).principal(authentication()))
				.andExpect(status().is(200));
	}

	@Test
	public void validateUserInfoEmailPositive() throws Exception {
		this.mvcMock.perform(get("/user/registration/info?email=user1email@epam.com").principal(authentication()))
				.andExpect(status().is(200));
	}

	@Test
	public void changePasswordWrongOldPassword() throws Exception {
		ChangePasswordRQ rq = new ChangePasswordRQ();
		rq.setOldPassword("password");
		rq.setNewPassword("12345");
		this.mvcMock.perform(post("/user/password/change").principal(authentication())
				.content(objectMapper.writeValueAsBytes(rq))
				.contentType(APPLICATION_JSON)).andExpect(status().is(400));
	}

	@Test
	public void changePasswordPositive() throws Exception {
		ChangePasswordRQ rq = new ChangePasswordRQ();
		rq.setOldPassword("1q2w3e");
		rq.setNewPassword("12345");
		this.mvcMock.perform(post("/user/password/change").principal(authentication())
				.content(objectMapper.writeValueAsBytes(rq))
				.contentType(APPLICATION_JSON)).andExpect(status().is(200));
	}

	@Test
	public void changePasswordLongNewPassword() throws Exception {
		ChangePasswordRQ rq = new ChangePasswordRQ();
		rq.setOldPassword("1q2w3e");
		rq.setNewPassword(RandomStringUtils.randomAlphabetic(ValidationConstraints.MAX_PASSWORD_LENGTH + 1));
		this.mvcMock.perform(post("/user/password/change").principal(authentication())
				.content(objectMapper.writeValueAsBytes(rq))
				.contentType(APPLICATION_JSON)).andExpect(status().is(400));
	}

	@Test
	public void changePasswordShortNewPassword() throws Exception {
		ChangePasswordRQ rq = new ChangePasswordRQ();
		rq.setOldPassword("1q2w3e");
		rq.setNewPassword(RandomStringUtils.randomAlphabetic(ValidationConstraints.MIN_PASSWORD_LENGTH - 1));
		this.mvcMock.perform(post("/user/password/change").principal(authentication())
				.content(objectMapper.writeValueAsBytes(rq))
				.contentType(APPLICATION_JSON)).andExpect(status().is(400));
	}

	@Test
	public void changeFullNameUpsaUser() throws Exception {
		EditUserRQ editUserRQ = new EditUserRQ();
		editUserRQ.setFullName("Vasya Pupkin");
		this.mvcMock.perform(put("/user/upsa_user").principal(authentication())
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(editUserRQ))).andExpect(status().isForbidden());
	}

	@Test
	public void changeEmailUpsaUser() throws Exception {
		EditUserRQ editUserRQ = new EditUserRQ();
		editUserRQ.setEmail("vasya.pupkin@gmail.com");
		this.mvcMock.perform(put("/user/upsa_user").principal(authentication())
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(editUserRQ))).andExpect(status().isForbidden());
	}

	@Test
	// TODO email service settings recognition issue
	public void restorePassword() throws Exception {
		final RestorePasswordRQ restorePasswordRQ = new RestorePasswordRQ();
		restorePasswordRQ.setEmail("user1email@epam.com");
		this.mvcMock.perform(post("/user/password/restore").principal(authentication())
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(restorePasswordRQ))).andExpect(status().is(200));
	}

	@Test
	public void resetPassword() throws Exception {
		final ResetPasswordRQ resetPasswordRQ = new ResetPasswordRQ();
		resetPasswordRQ.setPassword("password");
		resetPasswordRQ.setUuid("2ae88031-d4aa-40a8-9055-cd9d2551cd72");
		this.mvcMock.perform(post("/user/password/reset").principal(authentication())
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(resetPasswordRQ))).andExpect(status().is(200));
	}

	@Test
	public void isRestorePasswordBidExist() throws Exception {
		this.mvcMock.perform(
				get("/user/password/reset/2ae88031-d4aa-40a8-9055-cd9d2551cd72").principal(authentication()).contentType(APPLICATION_JSON))
				.andExpect(status().is(200));
	}

	@Test
	public void searchUsersPathVariableWithDot() throws Exception {
		mvcMock.perform(get("/user/search?term=.com").principal(authentication()).contentType(APPLICATION_JSON))
				.andExpect(status().is(200))
				.andExpect(jsonPath("$.page.totalElements").value(5));
	}

	@Override
	protected Authentication authentication() {
		return AuthConstants.ADMINISTRATOR;
	}
}
