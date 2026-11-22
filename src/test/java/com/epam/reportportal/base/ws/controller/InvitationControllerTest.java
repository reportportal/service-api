/*
 * Copyright 2024 EPAM Systems
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

package com.epam.reportportal.base.ws.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epam.reportportal.api.model.Invitation;
import com.epam.reportportal.api.model.InvitationActivation;
import com.epam.reportportal.api.model.InvitationActivation.StatusEnum;
import com.epam.reportportal.api.model.InvitationRequest;
import com.epam.reportportal.api.model.InvitationRequestOrganizationsInner;
import com.epam.reportportal.api.model.InvitationStatus;
import com.epam.reportportal.api.model.OrgRole;
import com.epam.reportportal.api.model.ProjectRole;
import com.epam.reportportal.api.model.UserProjectInfo;
import com.epam.reportportal.auth.event.UiAuthenticationFailureEventHandler;
import com.epam.reportportal.base.infrastructure.persistence.dao.ProjectUserRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.UserCreationBidRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.UserRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.organization.OrganizationUserRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.UserType;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.ws.BaseMvcTest;
import jakarta.persistence.EntityManagerFactory;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.jpa.EntityManagerHolder;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import tools.jackson.databind.json.JsonMapper;

@Sql("/db/organization/full_organization_samples.sql")
class InvitationControllerTest extends BaseMvcTest {

  private static final String INVITATIONS_ENDPOINT = "/invitations";

  private static final InvitationActivation activationRq = new InvitationActivation()
      .fullName("Stanley Jobson")
      .password("sw0rd_Fi$h")
      .status(StatusEnum.ACTIVATED);

  @Autowired
  private JsonMapper objectMapper;

  @Autowired
  OrganizationUserRepository organizationUserRepository;
  @Autowired
  ProjectUserRepository projectUserRepository;
  @Autowired
  UserRepository userRepository;
  @Autowired
  UserCreationBidRepository userCreationBidRepository;
  @Autowired
  EntityManagerFactory entityManagerFactory;

  private static final String WRONG_PASSWORD = "not-the-password-1A!";

  /**
   * The real login failure counter, shared by all tests of this class (each test uses its own addresses and accounts).
   * The auth module's bean is sliced out of this module test, so the mock declared in BaseMvcTest delegates to it.
   */
  private static final UiAuthenticationFailureEventHandler FAILURE_COUNTER = new UiAuthenticationFailureEventHandler();

  @BeforeEach
  void useRealFailureCounter() {
    when(uiAuthenticationFailureEventHandler.isBlocked(any(), any())).thenAnswer(
        inv -> FAILURE_COUNTER.isBlocked(inv.getArgument(0, HttpServletRequest.class), inv.getArgument(1)));
    doAnswer(inv -> {
      FAILURE_COUNTER.recordFailure(inv.getArgument(0, HttpServletRequest.class), inv.getArgument(1));
      return null;
    }).when(uiAuthenticationFailureEventHandler).recordFailure(any(), any());
  }

  @Test
  void createInvitationAnonymousIsUnauthorized() throws Exception {
    mockMvc.perform(post(INVITATIONS_ENDPOINT)
            .contentType(APPLICATION_JSON)
            .content(objectMapper.writeValueAsBytes(new InvitationRequest().email("anonymous@example.com"))))
        .andExpect(status().isUnauthorized());
  }

  @ParameterizedTest
  @CsvSource(value = {
      "MANAGER|EDITOR|1",
      "MANAGER|VIEWER|1",
      "MEMBER|EDITOR|1",
      "MEMBER|VIEWER|1",
      "MANAGER|EDITOR|2",
      "MANAGER|VIEWER|2",
      "MEMBER|EDITOR|2",
      "MEMBER|VIEWER|2"
  }, delimiter = '|')
  void createInvitationByAdmin(OrgRole orgRole, ProjectRole projectRole) throws Exception {
    var rq = getInvitationRequest(orgRole, projectRole);

    var result = mockMvc.perform(post(INVITATIONS_ENDPOINT)
            .content(objectMapper.writeValueAsBytes(rq))
            .contentType(APPLICATION_JSON)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isCreated())
        .andReturn()
        .getResponse().getContentAsString();

    var invitation = objectMapper.readValue(result, Invitation.class);

    assertNotNull(invitation);

    assertEquals(InvitationStatus.PENDING, invitation.getStatus());

    var storedInvitationString = mockMvc.perform(
            get(INVITATIONS_ENDPOINT + "/" + invitation.getId())
                .content(objectMapper.writeValueAsBytes(rq))
                .contentType(APPLICATION_JSON)
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse().getContentAsString();
    var storedInvitation = objectMapper.readValue(storedInvitationString, Invitation.class);

    assertEquals(storedInvitation, invitation);

  }

  private static InvitationRequest getInvitationRequest(OrgRole orgRole, ProjectRole projectRole) {
    List<UserProjectInfo> projects = new ArrayList<>();
    InvitationRequestOrganizationsInner orgInfo = new InvitationRequestOrganizationsInner();
    UserProjectInfo projectInfo = new UserProjectInfo()
        .id(1L)
        .projectRole(projectRole);

    projects.add(projectInfo);

    orgInfo.setId(1L);
    orgInfo.setOrgRole(orgRole);
    orgInfo.setProjects(projects);

    List<InvitationRequestOrganizationsInner> organizations = new ArrayList<>();
    organizations.add(orgInfo);

    var rq = new InvitationRequest();

    rq.setEmail("invitation@example.com");
    rq.setOrganizations(organizations);
    return rq;
  }


  @ParameterizedTest
  @CsvSource(value = {
      "MANAGER|EDITOR|1",
      "MANAGER|VIEWER|1",
      "MEMBER|EDITOR|1",
      "MEMBER|VIEWER|1",
      "MANAGER|EDITOR|2",
      "MANAGER|VIEWER|2"
  }, delimiter = '|')
  void memberEditorNotEnoughPermissionsFor(OrgRole orgRole, ProjectRole projectRole, long prjId)
      throws Exception {
    List<UserProjectInfo> projects = new ArrayList<>();
    var orgInfo = new InvitationRequestOrganizationsInner();
    UserProjectInfo projectInfo = new UserProjectInfo()
        .id(prjId)
        .projectRole(projectRole);

    projects.add(projectInfo);

    orgInfo.setId(1L);
    orgInfo.setOrgRole(orgRole);
    orgInfo.setProjects(projects);

    List<InvitationRequestOrganizationsInner> organizations = new ArrayList<>();
    organizations.add(orgInfo);

    var rq = new InvitationRequest();

    rq.setEmail("invitation@example.com");
    rq.setOrganizations(organizations);

    mockMvc.perform(post(INVITATIONS_ENDPOINT)
            .content(objectMapper.writeValueAsBytes(rq))
            .contentType(APPLICATION_JSON)
            .with(token(oAuthHelper.getDefaultToken())))
        .andExpect(status().isForbidden());

  }


  @ParameterizedTest
  @CsvSource(value = {
      "MEMBER|VIEWER|2"
  }, delimiter = '|')
  void memberEditorCanInvite(OrgRole orgRole, ProjectRole projectRole, long prjId)
      throws Exception {
    List<UserProjectInfo> projects = new ArrayList<>();
    var orgInfo = new InvitationRequestOrganizationsInner();
    UserProjectInfo projectInfo = new UserProjectInfo()
        .id(prjId)
        .projectRole(projectRole);

    projects.add(projectInfo);

    orgInfo.setId(1L);
    orgInfo.setOrgRole(orgRole);
    orgInfo.setProjects(projects);

    List<InvitationRequestOrganizationsInner> organizations = new ArrayList<>();
    organizations.add(orgInfo);

    var rq = new InvitationRequest();

    rq.setEmail("invitation@example.com");
    rq.setOrganizations(organizations);

    mockMvc.perform(post(INVITATIONS_ENDPOINT)
            .content(objectMapper.writeValueAsBytes(rq))
            .contentType(APPLICATION_JSON)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isCreated());

  }

  @Test
  void activateInvitation() throws Exception {
    var rq = getInvitationRequest(OrgRole.MANAGER, ProjectRole.EDITOR);

    var invitationAsString = mockMvc.perform(post(INVITATIONS_ENDPOINT)
            .content(objectMapper.writeValueAsBytes(rq))
            .contentType(APPLICATION_JSON)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isCreated())
        .andReturn()
        .getResponse().getContentAsString();
    var invitation = objectMapper.readValue(invitationAsString, Invitation.class);

    mockMvc.perform(put(INVITATIONS_ENDPOINT + "/" + invitation.getId())
            // no token required
            .content(objectMapper.writeValueAsBytes(activationRq))
            .contentType(APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isOk());
  }

  @Test
  void activateManagerViewerInvitation() throws Exception {
    var rq = getInvitationRequest(OrgRole.MANAGER, ProjectRole.VIEWER);

    var invitationAsString = mockMvc.perform(post(INVITATIONS_ENDPOINT)
            .content(objectMapper.writeValueAsBytes(rq))
            .contentType(APPLICATION_JSON)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isCreated())
        .andReturn()
        .getResponse().getContentAsString();
    var invitation = objectMapper.readValue(invitationAsString, Invitation.class);

    var response = objectMapper.readValue(mockMvc.perform(put(INVITATIONS_ENDPOINT + "/" + invitation.getId())
            // no token required
            .content(objectMapper.writeValueAsBytes(activationRq))
            .contentType(APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isOk())
        .andReturn().getResponse().getContentAsString(), Invitation.class);
    var pu = projectUserRepository.findProjectUserByUserIdAndProjectId(response.getUserId(), 1L).get();
    assertEquals(com.epam.reportportal.base.infrastructure.persistence.entity.project.ProjectRole.EDITOR,
        pu.getProjectRole());

  }

  @Test
  void activateInvitationNotFound() throws Exception {
    mockMvc.perform(put(INVITATIONS_ENDPOINT + "/" + UUID.randomUUID())
            // no token required
            .content(objectMapper.writeValueAsBytes(activationRq))
            .contentType(APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().is4xxClientError());
  }

  @Test
  void activateInvitationUserAlreadyExists() throws Exception {
    var rq = getInvitationRequest(OrgRole.MANAGER, ProjectRole.EDITOR);

    var invitationAsString = mockMvc.perform(post(INVITATIONS_ENDPOINT)
            .content(objectMapper.writeValueAsBytes(rq))
            .contentType(APPLICATION_JSON)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isCreated())
        .andReturn()
        .getResponse().getContentAsString();
    var invitation = objectMapper.readValue(invitationAsString, Invitation.class);

    var invitationAsString2 = mockMvc.perform(post(INVITATIONS_ENDPOINT)
            .content(objectMapper.writeValueAsBytes(rq))
            .contentType(APPLICATION_JSON)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isCreated())
        .andReturn()
        .getResponse().getContentAsString();
    var invitation2 = objectMapper.readValue(invitationAsString2, Invitation.class);

    mockMvc.perform(put(INVITATIONS_ENDPOINT + "/" + invitation.getId())
            // no token required
            .content(objectMapper.writeValueAsBytes(activationRq))
            .contentType(APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isOk());

    mockMvc.perform(put(INVITATIONS_ENDPOINT + "/" + invitation2.getId())
            // no token required
            .content(objectMapper.writeValueAsBytes(activationRq))
            .contentType(APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isOk());
  }

  @Test
  void createInvitationForExistingUserByAdmin() throws Exception {
    var rq = getInvitationRequest(OrgRole.MANAGER, ProjectRole.EDITOR);
    rq.setEmail("no-orgs-user@example.com");

    var result = mockMvc.perform(post(INVITATIONS_ENDPOINT)
            .content(objectMapper.writeValueAsBytes(rq))
            .contentType(APPLICATION_JSON)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isCreated())
        .andReturn()
        .getResponse().getContentAsString();

    var invitation = objectMapper.readValue(result, Invitation.class);

    assertNotNull(invitation);
    assertEquals(InvitationStatus.ACTIVATED, invitation.getStatus());
    organizationUserRepository.findByUserIdAndOrganization_Id(108L, 1L).orElseThrow();

    var prjIds = projectUserRepository.findProjectIdsByUserId(108L);
    var expectedPrjIds = rq.getOrganizations().stream()
        .flatMap(orgs -> orgs.getProjects().stream())
        .map(UserProjectInfo::getId)
        .toList();
    assertTrue(prjIds.containsAll(expectedPrjIds));

    mockMvc.perform(
            get(INVITATIONS_ENDPOINT + "/" + invitation.getId())
                .content(objectMapper.writeValueAsBytes(rq))
                .contentType(APPLICATION_JSON)
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isNotFound());
  }

  @Test
  void createdInvitationExpiresAfterDefaultTtl() throws Exception {
    var invitation = objectMapper.readValue(mockMvc.perform(post(INVITATIONS_ENDPOINT)
            .content(objectMapper.writeValueAsBytes(getInvitationRequest(OrgRole.MEMBER, ProjectRole.VIEWER)))
            .contentType(APPLICATION_JSON)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isCreated())
        .andReturn().getResponse().getContentAsString(), Invitation.class);

    assertEquals(Duration.ofHours(24),
        Duration.between(invitation.getCreatedAt(), invitation.getExpiresAt()));
  }

  /**
   * Password checks for an existing account share the login brute-force protection: failures from one address against
   * different invitations and accounts add up, and once the address is blocked even the right password is refused the
   * way a blocked login is. Another address is not affected.
   */
  @Test
  void existingAccountActivationIsSubjectToLoginLockout() throws Exception {
    var accountA = existingAccountWithPendingInvitations("lockout-a@example.com", 2);
    var accountB = existingAccountWithPendingInvitations("lockout-b@example.com", 1);
    var address = "10.31.0.1";

    activate(accountA.get(0), WRONG_PASSWORD, address).andExpect(status().isForbidden());
    activate(accountA.get(0), WRONG_PASSWORD, address).andExpect(status().isForbidden());
    activate(accountB.get(0), WRONG_PASSWORD, address).andExpect(status().isForbidden());
    activate(accountB.get(0), WRONG_PASSWORD, address).andExpect(status().isForbidden());

    activate(accountA.get(1), activationRq.getPassword(), address)
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.errorCode").value(ErrorType.ADDRESS_LOCKED.getCode()));
    assertTrue(userCreationBidRepository.findById(accountA.get(1)).isPresent());

    activate(accountB.get(0), activationRq.getPassword(), "10.31.0.2").andExpect(status().isOk());
  }

  /**
   * Wrong password, external account and inactive account (right password) are refused with the same response.
   */
  @Test
  void refusalsForExistingAccountsAreIndistinguishable() throws Exception {
    var internal = existingAccountWithPendingInvitations("refusal-internal@example.com", 1);
    var external = existingAccountWithPendingInvitations("refusal-external@example.com", 1);
    var inactive = existingAccountWithPendingInvitations("refusal-inactive@example.com", 1);

    var externalUser = userRepository.findByEmail("refusal-external@example.com").orElseThrow();
    externalUser.setUserType(UserType.SAML);
    externalUser.setPassword(null);
    userRepository.save(externalUser);
    var inactiveUser = userRepository.findByEmail("refusal-inactive@example.com").orElseThrow();
    inactiveUser.setActive(false);
    userRepository.save(inactiveUser);

    var wrongPassword = activate(internal.get(0), WRONG_PASSWORD, "10.32.0.1")
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.errorCode").value(ErrorType.ACCESS_DENIED.getCode()))
        .andReturn().getResponse().getContentAsString();
    var externalAccount = activate(external.get(0), activationRq.getPassword(), "10.32.0.2")
        .andExpect(status().isForbidden())
        .andReturn().getResponse().getContentAsString();
    var inactiveAccount = activate(inactive.get(0), activationRq.getPassword(), "10.32.0.3")
        .andExpect(status().isForbidden())
        .andReturn().getResponse().getContentAsString();

    assertEquals(wrongPassword, externalAccount);
    assertEquals(wrongPassword, inactiveAccount);
    // the refused invitations stay pending (below the failure limit)
    assertTrue(userCreationBidRepository.findById(inactive.get(0)).isPresent());
  }

  /**
   * After the failure limit the invitation is deleted, and the right password no longer works with that link.
   */
  @Test
  void invitationIsInvalidatedAfterFailureLimit() throws Exception {
    var invitations = existingAccountWithPendingInvitations("limit@example.com", 1);
    var invitationId = invitations.get(0);

    activate(invitationId, WRONG_PASSWORD, "10.33.0.1").andExpect(status().isForbidden());
    activate(invitationId, WRONG_PASSWORD, "10.33.0.2").andExpect(status().isForbidden());
    assertEquals(2, ((Number) userCreationBidRepository.findById(invitationId).orElseThrow()
        .getMetadata().getMetadata().get("activationFailures")).intValue());
    activate(invitationId, WRONG_PASSWORD, "10.33.0.3").andExpect(status().isForbidden());

    // the refusals did not mark the activation transaction (joined to the test's) rollback-only, so outside a test
    // they commit and the count and the deletion survive
    var holder = (EntityManagerHolder) TransactionSynchronizationManager.getResource(entityManagerFactory);
    assertFalse(holder.isRollbackOnly() || holder.getEntityManager().getTransaction().getRollbackOnly());
    assertFalse(userCreationBidRepository.findById(invitationId).isPresent());
    activate(invitationId, activationRq.getPassword(), "10.33.0.4")
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errorCode").value(ErrorType.INCORRECT_REQUEST.getCode()));
  }

  /**
   * Creates {@code pending + 1} invitations for a new address and activates the first one, which creates the account
   * with {@link #activationRq}'s password.
   *
   * @return the ids of the invitations still pending for the now existing account
   */
  private List<String> existingAccountWithPendingInvitations(String email, int pending) throws Exception {
    var rq = getInvitationRequest(OrgRole.MEMBER, ProjectRole.VIEWER);
    rq.setEmail(email);
    List<String> ids = new ArrayList<>();
    for (int i = 0; i <= pending; i++) {
      var invitation = objectMapper.readValue(mockMvc.perform(post(INVITATIONS_ENDPOINT)
              .content(objectMapper.writeValueAsBytes(rq))
              .contentType(APPLICATION_JSON)
              .with(token(oAuthHelper.getSuperadminToken())))
          .andExpect(status().isCreated())
          .andReturn().getResponse().getContentAsString(), Invitation.class);
      ids.add(invitation.getId().toString());
    }
    activate(ids.get(0), activationRq.getPassword(), "10.30.0.1").andExpect(status().isOk());
    return ids.subList(1, ids.size());
  }

  private ResultActions activate(String invitationId, String password, String remoteAddress) throws Exception {
    var rq = new InvitationActivation()
        .fullName(activationRq.getFullName())
        .password(password)
        .status(StatusEnum.ACTIVATED);
    return mockMvc.perform(put(INVITATIONS_ENDPOINT + "/" + invitationId)
        .content(objectMapper.writeValueAsBytes(rq))
        .contentType(APPLICATION_JSON)
        .with(request -> {
          request.setRemoteAddr(remoteAddress);
          return request;
        }));
  }
}
