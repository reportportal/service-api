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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epam.reportportal.api.model.FilterOperation;
import com.epam.reportportal.api.model.SearchCriteriaRQ;
import com.epam.reportportal.api.model.SearchCriteriaSearchCriteriaInner;
import com.epam.reportportal.model.ValidationConstraints;
import com.epam.ta.reportportal.core.user.ApiKeyHandler;
import com.epam.ta.reportportal.dao.UserRepository;
import com.epam.ta.reportportal.entity.integration.Integration;
import com.epam.ta.reportportal.model.ApiKeyRQ;
import com.epam.ta.reportportal.model.Page;
import com.epam.ta.reportportal.model.user.ChangePasswordRQ;
import com.epam.ta.reportportal.model.user.CreateUserBidRS;
import com.epam.ta.reportportal.model.user.CreateUserRQ;
import com.epam.ta.reportportal.model.user.CreateUserRQConfirm;
import com.epam.ta.reportportal.model.user.CreateUserRS;
import com.epam.ta.reportportal.model.user.EditUserRQ;
import com.epam.ta.reportportal.model.user.ResetPasswordRQ;
import com.epam.ta.reportportal.model.user.RestorePasswordRQ;
import com.epam.ta.reportportal.ws.BaseMvcTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@Sql("/db/user/user-fill.sql")
class UserControllerTest extends BaseMvcTest {

  private static final String USER_PERSONAL_URL = "/v1/users";
  private static final String USERS_URL = "/v1/users/";
  private static final String NEW_USERS_URL = "/users/";
  private static final String DEFAULT_USERNAME = "default@reportportal.internal";
  @Autowired
  ApiKeyHandler apiKeyHandler;
  @Autowired
  private ObjectMapper objectMapper;
  @Autowired
  private UserRepository userRepository;

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
            post(USERS_URL + "bid").with(token(oAuthHelper.getDefaultToken()))
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
    rq.setPassword("testPassword%123");
    rq.setFullName("Test User");
    rq.setEmail("test@domain.com");
    MvcResult mvcResult = mockMvc.perform(
            post(USERS_URL + "registration?uuid=e5f98deb-8966-4b2d-ba2f-35bc69d30c06")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(rq)))
        .andExpect(status().isCreated()).andReturn();

    CreateUserRS createUserRS = objectMapper.readValue(mvcResult.getResponse().getContentAsString(),
        CreateUserRS.class);

    assertNotNull(createUserRS.getId());
    assertEquals(normalizeId(rq.getEmail()), createUserRS.getLogin());
    assertTrue(userRepository.findById(createUserRS.getId()).isPresent());
  }


  @Test
  void deleteUserNegative() throws Exception {
    /* Administrator cannot remove him/her-self */
    mockMvc.perform(delete(USERS_URL + "1").with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isForbidden());
  }

  @Test
  void deleteUserPositive() throws Exception {
    mockMvc.perform(delete(USERS_URL + "2").with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk());
  }

  @Test
  void deleteUsers() throws Exception {
    mockMvc.perform(delete(USER_PERSONAL_URL).with(token(oAuthHelper.getSuperadminToken()))
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
    mockMvc.perform(put(USERS_URL + DEFAULT_USERNAME).with(token(oAuthHelper.getSuperadminToken()))
        .contentType(APPLICATION_JSON)
        .content(objectMapper.writeValueAsBytes(rq))).andExpect(status().isOk());
  }

  @Test
  void editUserShortName() throws Exception {
    EditUserRQ editUserRQ = new EditUserRQ();
    editUserRQ.setEmail("defaltemail@domain.com");
    editUserRQ.setFullName("1");
    mockMvc.perform(put(USERS_URL + DEFAULT_USERNAME).with(token(oAuthHelper.getSuperadminToken()))
        .contentType(APPLICATION_JSON)
        .content(objectMapper.writeValueAsBytes(editUserRQ))).andExpect(status().isBadRequest());
  }

  @Test
  void editUserLongName() throws Exception {
    EditUserRQ editUserRQ = new EditUserRQ();
    editUserRQ.setEmail("defaltemail@domain.com");
    editUserRQ.setFullName(RandomStringUtils.insecure().nextAlphabetic(257));
    mockMvc.perform(put(USERS_URL + DEFAULT_USERNAME).with(token(oAuthHelper.getSuperadminToken()))
        .contentType(APPLICATION_JSON)
        .content(objectMapper.writeValueAsBytes(editUserRQ))).andExpect(status().isBadRequest());
  }

  @Test
  void editUserNotUniqueEmail() throws Exception {
    EditUserRQ editUserRQ = new EditUserRQ();
    editUserRQ.setFullName("Vasya Pupkin");
    editUserRQ.setEmail("admin@reportportal.internal");
    mockMvc.perform(put(USERS_URL + DEFAULT_USERNAME).with(token(oAuthHelper.getSuperadminToken()))
        .contentType(APPLICATION_JSON)
        .content(objectMapper.writeValueAsBytes(editUserRQ))).andExpect(status().is(409));
  }

  @Test
  void editUserUniqueEmail() throws Exception {
    EditUserRQ editUserRQ = new EditUserRQ();
    editUserRQ.setFullName("Vasya Pupkin");
    editUserRQ.setEmail("user1uniquemail@epam.com");
    mockMvc.perform(put(USERS_URL + DEFAULT_USERNAME).with(token(oAuthHelper.getSuperadminToken()))
        .contentType(APPLICATION_JSON)
        .content(objectMapper.writeValueAsBytes(editUserRQ))).andExpect(status().isOk());
  }

  @Test
  void editAccountTypeByAdmin() throws Exception {
    EditUserRQ editUserRQ = new EditUserRQ();
    editUserRQ.setAccountType("INTERNAL");
    mockMvc.perform(put(USERS_URL + DEFAULT_USERNAME).with(token(oAuthHelper.getSuperadminToken()))
        .contentType(APPLICATION_JSON)
        .content(objectMapper.writeValueAsBytes(editUserRQ))).andExpect(status().isOk());
  }

  @Test
  void editAccountTypeByAdminNegative() throws Exception {
    EditUserRQ editUserRQ = new EditUserRQ();
    editUserRQ.setAccountType("GITHUB");
    mockMvc.perform(put(USERS_URL + DEFAULT_USERNAME).with(token(oAuthHelper.getSuperadminToken()))
        .contentType(APPLICATION_JSON)
        .content(objectMapper.writeValueAsBytes(editUserRQ))).andExpect(status().isBadRequest());
  }

  @Test
  void editActiveByAdmin() throws Exception {
    EditUserRQ editUserRQ = new EditUserRQ();
    editUserRQ.setActive(false);
    mockMvc.perform(put(USERS_URL + DEFAULT_USERNAME).with(token(oAuthHelper.getSuperadminToken()))
        .contentType(APPLICATION_JSON)
        .content(objectMapper.writeValueAsBytes(editUserRQ))).andExpect(status().isOk());
  }

  @Test
  void editAccountTypeByNotAdmin() throws Exception {
    EditUserRQ editUserRQ = new EditUserRQ();
    editUserRQ.setAccountType("INTERNAL");
    mockMvc.perform(put(USERS_URL + DEFAULT_USERNAME).with(token(oAuthHelper.getDefaultToken()))
        .contentType(APPLICATION_JSON)
        .content(objectMapper.writeValueAsBytes(editUserRQ))).andExpect(status().isForbidden());
  }

  @Test
  void editActiveByNotAdmin() throws Exception {
    EditUserRQ editUserRQ = new EditUserRQ();
    editUserRQ.setActive(false);
    mockMvc.perform(put(USERS_URL + DEFAULT_USERNAME).with(token(oAuthHelper.getDefaultToken()))
        .contentType(APPLICATION_JSON)
        .content(objectMapper.writeValueAsBytes(editUserRQ))).andExpect(status().isForbidden());
  }

  @Test
  void editExternalIdByNotAdmin() throws Exception {
    EditUserRQ editUserRQ = new EditUserRQ();
    editUserRQ.setExternalId("test");
    mockMvc.perform(put(USERS_URL + DEFAULT_USERNAME).with(token(oAuthHelper.getDefaultToken()))
        .contentType(APPLICATION_JSON)
        .content(objectMapper.writeValueAsBytes(editUserRQ))).andExpect(status().isOk());
  }

  @Test
  void getUserPositive() throws Exception {
    mockMvc.perform(get(USERS_URL + DEFAULT_USERNAME).with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk());
  }

  @Test
  void getUserPositiveUsingApiToken() throws Exception {
    var apikeyRs = apiKeyHandler.createApiKey("test", 1L);

    mockMvc.perform(get(USERS_URL + DEFAULT_USERNAME).with(
            token(apikeyRs.getApiKey())))
        .andExpect(status().isOk());
  }

  @Test
  void getUsersPositive() throws Exception {
    mockMvc.perform(get(USERS_URL + "all").with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk());
  }

  @Test
  void validateUserInfoUsernamePositive() throws Exception {
    mockMvc.perform(get(USERS_URL + "registration/info?username=default").with(
            token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk());
  }

  @Test
  void changePasswordWrongOldPassword() throws Exception {
    ChangePasswordRQ rq = new ChangePasswordRQ();
    rq.setOldPassword("password");
    rq.setNewPassword("12345");
    mockMvc.perform(post(USERS_URL + "password/change").with(token(oAuthHelper.getDefaultToken()))
        .content(objectMapper.writeValueAsBytes(rq))
        .contentType(APPLICATION_JSON)).andExpect(status().isBadRequest());
  }

  @Test
  void changePasswordPositive() throws Exception {
    ChangePasswordRQ rq = new ChangePasswordRQ();
    rq.setOldPassword("1q2w3e");
    rq.setNewPassword("newPassword%123");

    when(mailServiceFactory.getDefaultEmailService(true)).thenReturn(emailService);
    doNothing().when(emailService).sendChangePasswordConfirmation(any(), any(), any());

    mockMvc.perform(post(USERS_URL + "password/change").with(token(oAuthHelper.getDefaultToken()))
        .content(objectMapper.writeValueAsBytes(rq))
        .contentType(APPLICATION_JSON)).andExpect(status().isOk());
  }

  @Test
  void changePasswordLongNewPassword() throws Exception {
    ChangePasswordRQ rq = new ChangePasswordRQ();
    rq.setOldPassword("1q2w3e");
    rq.setNewPassword(
        RandomStringUtils.insecure().nextAlphabetic(ValidationConstraints.MAX_PASSWORD_LENGTH + 1));
    mockMvc.perform(post(USERS_URL + "password/change").with(token(oAuthHelper.getDefaultToken()))
        .content(objectMapper.writeValueAsBytes(rq))
        .contentType(APPLICATION_JSON)).andExpect(status().isBadRequest());
  }

  @Test
  void changePasswordShortNewPassword() throws Exception {
    ChangePasswordRQ rq = new ChangePasswordRQ();
    rq.setOldPassword("1q2w3e");
    rq.setNewPassword(
        RandomStringUtils.insecure().nextAlphabetic(ValidationConstraints.MIN_PASSWORD_LENGTH - 1));
    mockMvc.perform(post(USERS_URL + "password/change").with(token(oAuthHelper.getDefaultToken()))
        .content(objectMapper.writeValueAsBytes(rq))
        .contentType(APPLICATION_JSON)).andExpect(status().isBadRequest());
  }

  @Test
  void restorePassword() throws Exception {
    final RestorePasswordRQ restorePasswordRQ = new RestorePasswordRQ();
    restorePasswordRQ.setEmail("default@reportportal.internal");

    when(mailServiceFactory.getDefaultEmailService(true)).thenReturn(emailService);
    doNothing().when(emailService).sendRestorePasswordEmail(any(), any(), any(), any());

    mockMvc.perform(post(USERS_URL + "password/restore").with(token(oAuthHelper.getDefaultToken()))
        .contentType(APPLICATION_JSON)
        .content(objectMapper.writeValueAsBytes(restorePasswordRQ))).andExpect(status().isOk());
  }

  @Test
  void resetPassword() throws Exception {
    final ResetPasswordRQ resetPasswordRQ = new ResetPasswordRQ();
    resetPasswordRQ.setPassword("Password%123");
    resetPasswordRQ.setUuid("e5f98deb-8966-4b2d-ba2f-35bc69d30c06");
    mockMvc.perform(post(USERS_URL + "password/reset").with(token(oAuthHelper.getDefaultToken()))
        .contentType(APPLICATION_JSON)
        .content(objectMapper.writeValueAsBytes(resetPasswordRQ))).andExpect(status().isOk());
  }

  @Test
  void isRestorePasswordBidExist() throws Exception {
    mockMvc.perform(get(USERS_URL + "password/reset/e5f98deb-8966-4b2d-ba2f-35bc69d30c06").with(
            token(oAuthHelper.getDefaultToken()))
        .contentType(APPLICATION_JSON)).andExpect(status().isOk());
  }

  @Test
  void getUserProjects() throws Exception {
    mockMvc.perform(get(USERS_URL + "default/projects").with(token(oAuthHelper.getDefaultToken())))
        .andExpect(status().isOk());
  }

  @Test
  void getMyself() throws Exception {
    mockMvc.perform(get(USERS_URL).with(token(oAuthHelper.getDefaultToken())))
        .andExpect(status().isOk());
  }

  @Test
  void findUsers() throws Exception {
    MvcResult mvcResult = mockMvc.perform(
            get(USERS_URL + "search?term=e").with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andReturn();
    var userResources = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), Page.class);

    Assertions.assertNotNull(userResources);
    assertEquals(2, userResources.getContent().size());
  }

  @Test
  void exportUsers() throws Exception {
    mockMvc.perform(get(USERS_URL + "export").with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk());
  }

  @Test
  void userPhoto() throws Exception {
    final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.multipart(
            "/users/2/avatar")
        .file(new MockMultipartFile("file", "file", "image/png",
            new ClassPathResource("image/image.png").getInputStream()))
        .contentType(MediaType.MULTIPART_FORM_DATA);

    mockMvc.perform(requestBuilder.with(token(oAuthHelper.getDefaultToken())))
        .andExpect(status().isCreated());

    mockMvc.perform(get("/users/2/avatar?thumbnail=false")
            .with(token(oAuthHelper.getDefaultToken())))
        .andExpect(status().isOk());

    mockMvc.perform(get("/users/2/avatar?thumbnail=true").with(
            token(oAuthHelper.getDefaultToken())))
        .andExpect(status().isOk());

    mockMvc.perform(delete("/users/2/avatar").with(token(oAuthHelper.getDefaultToken())))
        .andExpect(status().isNoContent());
  }

  @ParameterizedTest
  @CsvSource(value = {
      "abcABC123",
      "abcABC123-",
      "abcABC123.",
      "abcABC123_",
      "abcABC123~",
      "abcABC123+",
      "abcABC123/"
  }, delimiter = '|')
  void createApiKey(String name) throws Exception {
    var apiKeyRq = new ApiKeyRQ();
    apiKeyRq.setName(name);
    mockMvc.perform(post("/v1/users/1/api-keys")
        .with(token(oAuthHelper.getSuperadminToken()))
        .contentType(APPLICATION_JSON)
        .content(objectMapper.writeValueAsBytes(apiKeyRq))).andExpect(status().isCreated());
  }

  @ParameterizedTest
  @CsvSource(value = {
      "abcABC123,",
      "abcABC123!",
      "abcABC123{",
      "abcABC123-._~+/,!{}",
      "abcABC123=",
      "abcABC 123"
  }, delimiter = '|')
  void createApiKeyWrongPattern(String name) throws Exception {
    var apiKeyRq = new ApiKeyRQ();
    apiKeyRq.setName(name);
    mockMvc.perform(post("/v1/users/1/api-keys")
        .with(token(oAuthHelper.getDefaultToken()))
        .contentType(APPLICATION_JSON)
        .content(objectMapper.writeValueAsBytes(apiKeyRq))).andExpect(status().is4xxClientError());
  }


  @ParameterizedTest
  @CsvSource(value = {
      "uuid|NE|9a1e9d3d-3587-4bce-9cff-36db5eecb439|2",
      "external_id|EQ|9a1e9d3d-3587-4bce-9cff-36db5eecb439|0",
      "email|CNT|admin@reportportal.internal|1",
      "full_name|CNT|ter|2",
      "account_type|EQ|INTERNAL|2",
      "instance_role|EQ|USER|1",
      "active|EQ|true|2",
      "active|EQ|false|0",
      "created_at|GT|2025-05-05T13:26:48.856881Z|2",
      "updated_at|GT|2025-05-05T13:26:48.856881Z|2",
      "org_id|EQ|1|2",
      "org_id|EQ|2|0"
  }, delimiter = '|')
  void getInstanceUsersPositive(String field, String op, String value, int expCount)
      throws Exception {
    SearchCriteriaSearchCriteriaInner inner = new SearchCriteriaSearchCriteriaInner()
        .filterKey(field)
        .operation(FilterOperation.fromValue(op))
        .value(value);

    var criteria = new SearchCriteriaRQ().addSearchCriteriaItem(inner);

    mockMvc.perform(post(NEW_USERS_URL + "searches")
            .with(token(oAuthHelper.getSuperadminToken()))
            .contentType(APPLICATION_JSON)
            .content(objectMapper.writeValueAsBytes(criteria)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.total_count").value(expCount));
  }

  @Test
  void searchInstanceUsersPositiveEmptyRequest() throws Exception {
    mockMvc.perform(post(NEW_USERS_URL + "searches")
            .with(token(oAuthHelper.getSuperadminToken()))
            .contentType(APPLICATION_JSON)
            .content("{}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.total_count").value(2));
  }
}
