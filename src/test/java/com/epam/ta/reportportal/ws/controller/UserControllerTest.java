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

import static com.epam.ta.reportportal.commons.EntityUtils.normalizeId;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epam.reportportal.model.ValidationConstraints;
import com.epam.ta.reportportal.dao.IssueTypeRepository;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.dao.UserRepository;
import com.epam.ta.reportportal.entity.enums.ProjectAttributeEnum;
import com.epam.ta.reportportal.entity.integration.Integration;
import com.epam.ta.reportportal.entity.item.issue.IssueType;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.project.ProjectIssueType;
import com.epam.ta.reportportal.entity.user.User;
import com.epam.ta.reportportal.entity.user.UserType;
import com.epam.ta.reportportal.model.DeleteBulkRQ;
import com.epam.ta.reportportal.model.Page;
import com.epam.ta.reportportal.model.user.ChangePasswordRQ;
import com.epam.ta.reportportal.model.user.CreateUserBidRS;
import com.epam.ta.reportportal.model.user.CreateUserRQ;
import com.epam.ta.reportportal.model.user.CreateUserRQConfirm;
import com.epam.ta.reportportal.model.user.CreateUserRQFull;
import com.epam.ta.reportportal.model.user.CreateUserRS;
import com.epam.ta.reportportal.model.user.EditUserRQ;
import com.epam.ta.reportportal.model.user.ResetPasswordRQ;
import com.epam.ta.reportportal.model.user.RestorePasswordRQ;
import com.epam.ta.reportportal.ws.BaseMvcTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MvcResult;

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
  private UserRepository userRepository;

  @Autowired
  private IssueTypeRepository issueTypeRepository;

  @Test
  void createdUserByIdentityProvider() throws Exception  {
    CreateUserRQFull rq = new CreateUserRQFull();
    rq.setLogin("testLogin");
    rq.setFullName("Test User");
    rq.setEmail("test@test.com");
    rq.setAccountRole("USER");
    rq.setProjectRole("EDITOR");
    rq.setActive(true);
    rq.setAccountType(UserType.INTERNAL);
    rq.setAccountType("SCIM");

    MvcResult mvcResult = mockMvc.perform(
            post("/users").with(token(oAuthHelper.getSuperadminToken()))
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(rq))).andExpect(status().isCreated())
        .andReturn();

    CreateUserRS createUserRS = objectMapper.readValue(mvcResult.getResponse().getContentAsString(),
        CreateUserRS.class);

    assertNotNull(createUserRS.getId());
    assertEquals(normalizeId(rq.getLogin()), createUserRS.getLogin());
    var user = userRepository.findById(createUserRS.getId());
    assertTrue(user.isPresent());
    assertEquals(user.get().getUserType(), UserType.SCIM);
    assertNull(user.get().getPassword());

    var projectOptional = projectRepository.findByName("default_personal");
    assertTrue(projectOptional.isPresent());
    assertFalse(projectOptional.get().getUsers().stream()
        .anyMatch(config -> config.getUser().getLogin().equals("testlogin")));

  }

  @Test
  void createUserByAdminPositive() throws Exception {
    CreateUserRQFull rq = new CreateUserRQFull();
    rq.setLogin("testLogin");
    rq.setPassword("testPassword");
    rq.setFullName("Test User");
    rq.setEmail("test@test.com");
    rq.setAccountRole("USER");
    rq.setProjectRole("EDITOR");
    rq.setDefaultProject("default_personal");
    rq.setActive(true);
    rq.setAccountType(UserType.INTERNAL);

    MvcResult mvcResult = mockMvc.perform(
            post("/users").with(token(oAuthHelper.getSuperadminToken()))
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(rq))).andExpect(status().isCreated())
        .andReturn();

    CreateUserRS createUserRS = objectMapper.readValue(mvcResult.getResponse().getContentAsString(),
        CreateUserRS.class);

    assertNotNull(createUserRS.getId());
    assertEquals(normalizeId(rq.getLogin()), createUserRS.getLogin());
    assertTrue(userRepository.findById(createUserRS.getId()).isPresent());

    // TODO: move to the new organization endpoints test
    /*final Optional<Project> projectOptional = projectRepository.findByName("default_personal");
    assertTrue(projectOptional.isPresent());
    assertTrue(projectOptional.get().getUsers().stream()
        .anyMatch(config -> config.getUser().getLogin().equals("testlogin")));

    Optional<Project> personalProject = projectRepository.findByName("testlogin_personal");
    assertTrue(personalProject.isPresent(), "Personal project isn't created");
    Project project = personalProject.get();

    List<IssueType> defaultIssueTypes = issueTypeRepository.getDefaultIssueTypes();

    project.getProjectAttributes()
        .forEach(projectAttribute -> assertTrue(projectAttribute.getValue()
            .equalsIgnoreCase(
                ProjectAttributeEnum.findByAttributeName(projectAttribute.getAttribute().getName())
                    .get()
                    .getDefaultValue())));

    assertTrue(defaultIssueTypes.containsAll(project.getProjectIssueTypes()
        .stream()
        .map(ProjectIssueType::getIssueType)
        .collect(Collectors.toList())));*/
  }

  @Test
  @Disabled("to be deleted")
  void createUserBidPositive() throws Exception {
    CreateUserRQ rq = new CreateUserRQ();
    rq.setDefaultProject("default_personal");
    rq.setEmail("test@domain.com");
    rq.setRole("EDITOR");

    when(mailServiceFactory.getEmailService(any(Integration.class), any(Boolean.class))).thenReturn(
        emailService);
    doNothing().when(emailService).sendCreateUserConfirmationEmail(any(), any(), any());

    MvcResult mvcResult = mockMvc.perform(
            post("/users/bid").with(token(oAuthHelper.getDefaultToken()))
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(rq))).andExpect(status().isCreated())
        .andReturn();

    CreateUserBidRS createUserBidRS = objectMapper.readValue(
        mvcResult.getResponse().getContentAsString(), CreateUserBidRS.class);
    assertNotNull(createUserBidRS.getBackLink());
    assertNotNull(createUserBidRS.getBid());
    assertTrue(createUserBidRS.getBackLink()
        .contains("/ui/#registration?uuid=" + createUserBidRS.getBid()));
  }

  @Test
  @Disabled("to be deleted")
  void createUserPositive() throws Exception {
    CreateUserRQConfirm rq = new CreateUserRQConfirm();
    rq.setLogin("testLogin");
    rq.setPassword("testPassword");
    rq.setFullName("Test User");
    rq.setEmail("test@domain.com");
    MvcResult mvcResult = mockMvc.perform(
            post("/users/registration?uuid=e5f98deb-8966-4b2d-ba2f-35bc69d30c06").contentType(
                APPLICATION_JSON).content(objectMapper.writeValueAsBytes(rq)))
        .andExpect(status().isCreated()).andReturn();

    CreateUserRS createUserRS = objectMapper.readValue(mvcResult.getResponse().getContentAsString(),
        CreateUserRS.class);

    assertNotNull(createUserRS.getId());
    assertEquals(normalizeId(rq.getLogin()), createUserRS.getLogin());
    assertTrue(userRepository.findById(createUserRS.getId()).isPresent());
  }

  @Test
  void getUserBidInfoPositive() throws Exception {
    mockMvc.perform(get("/users/registration?uuid=e5f98deb-8966-4b2d-ba2f-35bc69d30c06"))
        .andExpect(status().isOk());
  }

  @Test
  void deleteUserNegative() throws Exception {
    /* Administrator cannot remove him/her-self */
    mockMvc.perform(delete("/users/1").with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isForbidden());
  }

  @Test
  void deleteUserPositive() throws Exception {
    mockMvc.perform(delete("/users/2").with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk());
  }

  @Test
  void deleteUsers() throws Exception {

    DeleteBulkRQ deleteBulkRQ = new DeleteBulkRQ();
    deleteBulkRQ.setIds(Lists.newArrayList(2L));

    mockMvc.perform(delete("/users").with(token(oAuthHelper.getSuperadminToken()))
            .contentType(APPLICATION_JSON)
            .param("ids", "1", "2"))
        .andExpect(status().isOk());
  }

  @Test
  void editUserPositive() throws Exception {
    EditUserRQ rq = new EditUserRQ();
    rq.setFullName("Vasya Pupkin");
    rq.setEmail("defaultemail@domain.com");
    rq.setRole("USER");
    mockMvc.perform(put("/users/default").with(token(oAuthHelper.getSuperadminToken()))
        .contentType(APPLICATION_JSON)
        .content(objectMapper.writeValueAsBytes(rq))).andExpect(status().isOk());
  }

  @Test
  void editUserShortName() throws Exception {
    EditUserRQ editUserRQ = new EditUserRQ();
    editUserRQ.setEmail("defaltemail@domain.com");
    editUserRQ.setFullName("1");
    mockMvc.perform(put("/users/default").with(token(oAuthHelper.getSuperadminToken()))
        .contentType(APPLICATION_JSON)
        .content(objectMapper.writeValueAsBytes(editUserRQ))).andExpect(status().isBadRequest());
  }

  @Test
  void editUserLongName() throws Exception {
    EditUserRQ editUserRQ = new EditUserRQ();
    editUserRQ.setEmail("defaltemail@domain.com");
    editUserRQ.setFullName(RandomStringUtils.randomAlphabetic(257));
    mockMvc.perform(put("/users/default").with(token(oAuthHelper.getSuperadminToken()))
        .contentType(APPLICATION_JSON)
        .content(objectMapper.writeValueAsBytes(editUserRQ))).andExpect(status().isBadRequest());
  }

  @Test
  void editUserNotUniqueEmail() throws Exception {
    EditUserRQ editUserRQ = new EditUserRQ();
    editUserRQ.setFullName("Vasya Pupkin");
    editUserRQ.setEmail("superadminemail@domain.com");
    mockMvc.perform(put("/users/default").with(token(oAuthHelper.getSuperadminToken()))
        .contentType(APPLICATION_JSON)
        .content(objectMapper.writeValueAsBytes(editUserRQ))).andExpect(status().is(409));
  }

  @Test
  void editUserUniqueEmail() throws Exception {
    EditUserRQ editUserRQ = new EditUserRQ();
    editUserRQ.setFullName("Vasya Pupkin");
    editUserRQ.setEmail("user1uniquemail@epam.com");
    mockMvc.perform(put("/users/default").with(token(oAuthHelper.getSuperadminToken()))
        .contentType(APPLICATION_JSON)
        .content(objectMapper.writeValueAsBytes(editUserRQ))).andExpect(status().isOk());
  }

  @Test
  void editAccountTypeByAdmin() throws Exception {
    EditUserRQ editUserRQ = new EditUserRQ();
    editUserRQ.setAccountType("INTERNAL");
    mockMvc.perform(put("/users/default").with(token(oAuthHelper.getSuperadminToken()))
        .contentType(APPLICATION_JSON)
        .content(objectMapper.writeValueAsBytes(editUserRQ))).andExpect(status().isOk());
  }

  @Test
  void editAccountTypeByAdminNegative() throws Exception {
    EditUserRQ editUserRQ = new EditUserRQ();
    editUserRQ.setAccountType("GITHUB");
    mockMvc.perform(put("/users/default").with(token(oAuthHelper.getSuperadminToken()))
        .contentType(APPLICATION_JSON)
        .content(objectMapper.writeValueAsBytes(editUserRQ))).andExpect(status().isBadRequest());
  }

  @Test
  void editActiveByAdmin() throws Exception {
    EditUserRQ editUserRQ = new EditUserRQ();
    editUserRQ.setActive(false);
    mockMvc.perform(put("/users/default").with(token(oAuthHelper.getSuperadminToken()))
        .contentType(APPLICATION_JSON)
        .content(objectMapper.writeValueAsBytes(editUserRQ))).andExpect(status().isOk());
  }

  @Test
  void editAccountTypeByNotAdmin() throws Exception {
    EditUserRQ editUserRQ = new EditUserRQ();
    editUserRQ.setAccountType("INTERNAL");
    mockMvc.perform(put("/users/default").with(token(oAuthHelper.getDefaultToken()))
        .contentType(APPLICATION_JSON)
        .content(objectMapper.writeValueAsBytes(editUserRQ))).andExpect(status().isForbidden());
  }

  @Test
  void editActiveByNotAdmin() throws Exception {
    EditUserRQ editUserRQ = new EditUserRQ();
    editUserRQ.setActive(false);
    mockMvc.perform(put("/users/default").with(token(oAuthHelper.getDefaultToken()))
        .contentType(APPLICATION_JSON)
        .content(objectMapper.writeValueAsBytes(editUserRQ))).andExpect(status().isForbidden());
  }

  @Test
  void editExternalIdByNotAdmin() throws Exception {
    EditUserRQ editUserRQ = new EditUserRQ();
    editUserRQ.setExternalId("test");
    mockMvc.perform(put("/users/default").with(token(oAuthHelper.getDefaultToken()))
        .contentType(APPLICATION_JSON)
        .content(objectMapper.writeValueAsBytes(editUserRQ))).andExpect(status().isOk());
  }

  @Test
  void getUserPositive() throws Exception {
    mockMvc.perform(get("/users/default").with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk());
  }

  @Test
  void getUserPositiveUsingApiToken() throws Exception {
    mockMvc.perform(get("/users/default").with(
            token("test__ET4Byc1QUqO8VV8kiCGSP3O4SERb5MJWIowQQ3SiEqHO6hjicoPw-vm1tnrQI5V")))
        .andExpect(status().isOk());
  }

  @Test
  void getUsersPositive() throws Exception {
    mockMvc.perform(get("/users/all").with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk());
  }

  @Test
  void validateUserInfoUsernamePositive() throws Exception {
    mockMvc.perform(get("/users/registration/info?username=default").with(
            token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk());
  }

  @Test
  void changePasswordWrongOldPassword() throws Exception {
    ChangePasswordRQ rq = new ChangePasswordRQ();
    rq.setOldPassword("password");
    rq.setNewPassword("12345");
    mockMvc.perform(post("/users/password/change").with(token(oAuthHelper.getDefaultToken()))
        .content(objectMapper.writeValueAsBytes(rq))
        .contentType(APPLICATION_JSON)).andExpect(status().isBadRequest());
  }

  @Test
  void changePasswordPositive() throws Exception {
    ChangePasswordRQ rq = new ChangePasswordRQ();
    rq.setOldPassword("1q2w3e");
    rq.setNewPassword("12345");

    when(mailServiceFactory.getDefaultEmailService(true)).thenReturn(emailService);
    doNothing().when(emailService).sendChangePasswordConfirmation(any(), any(), any());

    mockMvc.perform(post("/users/password/change").with(token(oAuthHelper.getDefaultToken()))
        .content(objectMapper.writeValueAsBytes(rq))
        .contentType(APPLICATION_JSON)).andExpect(status().isOk());
  }

  @Test
  void changePasswordLongNewPassword() throws Exception {
    ChangePasswordRQ rq = new ChangePasswordRQ();
    rq.setOldPassword("1q2w3e");
    rq.setNewPassword(
        RandomStringUtils.randomAlphabetic(ValidationConstraints.MAX_PASSWORD_LENGTH + 1));
    mockMvc.perform(post("/users/password/change").with(token(oAuthHelper.getDefaultToken()))
        .content(objectMapper.writeValueAsBytes(rq))
        .contentType(APPLICATION_JSON)).andExpect(status().isBadRequest());
  }

  @Test
  void changePasswordShortNewPassword() throws Exception {
    ChangePasswordRQ rq = new ChangePasswordRQ();
    rq.setOldPassword("1q2w3e");
    rq.setNewPassword(
        RandomStringUtils.randomAlphabetic(ValidationConstraints.MIN_PASSWORD_LENGTH - 1));
    mockMvc.perform(post("/users/password/change").with(token(oAuthHelper.getDefaultToken()))
        .content(objectMapper.writeValueAsBytes(rq))
        .contentType(APPLICATION_JSON)).andExpect(status().isBadRequest());
  }

  @Test
  void restorePassword() throws Exception {
    final RestorePasswordRQ restorePasswordRQ = new RestorePasswordRQ();
    restorePasswordRQ.setEmail("defaultemail@domain.com");

    when(mailServiceFactory.getDefaultEmailService(true)).thenReturn(emailService);
    doNothing().when(emailService).sendRestorePasswordEmail(any(), any(), any(), any());

    mockMvc.perform(post("/users/password/restore").with(token(oAuthHelper.getDefaultToken()))
        .contentType(APPLICATION_JSON)
        .content(objectMapper.writeValueAsBytes(restorePasswordRQ))).andExpect(status().isOk());
  }

  @Test
  void resetPassword() throws Exception {
    final ResetPasswordRQ resetPasswordRQ = new ResetPasswordRQ();
    resetPasswordRQ.setPassword("password");
    resetPasswordRQ.setUuid("e5f98deb-8966-4b2d-ba2f-35bc69d30c06");
    mockMvc.perform(post("/users/password/reset").with(token(oAuthHelper.getDefaultToken()))
        .contentType(APPLICATION_JSON)
        .content(objectMapper.writeValueAsBytes(resetPasswordRQ))).andExpect(status().isOk());
  }

  @Test
  void isRestorePasswordBidExist() throws Exception {
    mockMvc.perform(get("/users/password/reset/e5f98deb-8966-4b2d-ba2f-35bc69d30c06").with(
            token(oAuthHelper.getDefaultToken()))
        .contentType(APPLICATION_JSON)).andExpect(status().isOk());
  }

  @Test
  void getUserProjects() throws Exception {
    mockMvc.perform(get("/users/default/projects").with(token(oAuthHelper.getDefaultToken())))
        .andExpect(status().isOk());
  }

  @Test
  void getMyself() throws Exception {
    mockMvc.perform(get("/users").with(token(oAuthHelper.getDefaultToken())))
        .andExpect(status().isOk());
  }

  @Test
  void findUsers() throws Exception {
    MvcResult mvcResult = mockMvc.perform(
            get("/users/search?term=e").with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andReturn();
    Page userResources = objectMapper.readValue(mvcResult.getResponse().getContentAsString(),
        Page.class);

    Assertions.assertNotNull(userResources);
    Assertions.assertEquals(2, userResources.getContent().size());
  }

  @Test
  void exportUsers() throws Exception {
    mockMvc.perform(get("/users/export").with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk());
  }
}
