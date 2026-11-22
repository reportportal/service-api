/*
 * Copyright 2025 EPAM Systems
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

package com.epam.reportportal.base.core.integration.impl;

import static com.epam.reportportal.base.ReportPortalUserUtil.getRpUser;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.reportportal.auth.integration.handler.CreateAuthIntegrationHandler;
import com.epam.reportportal.base.core.integration.util.EmailServerIntegrationService;
import com.epam.reportportal.base.core.integration.util.IntegrationParamsEncryptor;
import com.epam.reportportal.base.core.integration.util.IntegrationService;
import com.epam.reportportal.base.core.plugin.PluginBox;
import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.infrastructure.persistence.dao.IntegrationRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.IntegrationTypeRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.ProjectRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.Integration;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.IntegrationParams;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.IntegrationType;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.OrganizationRole;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.Project;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.ProjectRole;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.UserRole;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.base.model.integration.IntegrationRQ;
import com.epam.reportportal.base.util.email.MailServiceFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.jasypt.util.text.BasicTextEncryptor;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class CreateIntegrationHandlerImplTest {

  @Mock
  private Map<String, IntegrationService> integrationServiceMapping;
  @Mock
  private IntegrationRepository integrationRepository;
  @Mock
  private ProjectRepository projectRepository;
  @Mock
  private ApplicationEventPublisher eventPublisher;
  @Mock
  private IntegrationTypeRepository integrationTypeRepository;
  @Mock
  private IntegrationService basicIntegrationService;
  @Mock
  private CreateAuthIntegrationHandler createAuthIntegrationHandler;

  @InjectMocks
  private CreateIntegrationHandlerImpl handler;

  private static final Long EMAIL_TYPE_ID = 10L;
  private static final String PROJECT_KEY = "test-project";

  private ReportPortalUser rpUser;
  private IntegrationType emailType;

  @BeforeEach
  void setUp() {
    rpUser = getRpUser("testuser", UserRole.USER, OrganizationRole.MANAGER, ProjectRole.EDITOR, 1L);

    emailType = new IntegrationType();
    emailType.setId(EMAIL_TYPE_ID);
    emailType.setName("email");
  }

  @Test
  @DisplayName("Should throw INTEGRATION_ALREADY_EXISTS when global email integration already exists")
  void createGlobalIntegrationWhenEmailAlreadyExistsShouldThrow() {
    // Given
    when(integrationTypeRepository.findByName("email")).thenReturn(Optional.of(emailType));
    when(integrationRepository.existsByTypeIdAndProjectIdIsNullAndOrganizationIdIsNull(EMAIL_TYPE_ID))
        .thenReturn(true);

    var request = new IntegrationRQ();
    request.setName("email-server-2");

    // When & Then
    var ex = assertThrows(ReportPortalException.class,
        () -> handler.createGlobalIntegration(request, "email", rpUser));
    assertEquals(ErrorType.INTEGRATION_ALREADY_EXISTS, ex.getErrorType());
  }

  @Test
  @DisplayName("Should throw INTEGRATION_ALREADY_EXISTS when project email integration already exists")
  void createProjectIntegrationWhenEmailAlreadyExistsShouldThrow() {
    // Given
    var project = new Project();
    project.setId(20L);
    project.setName(PROJECT_KEY);

    when(projectRepository.findByKey(PROJECT_KEY)).thenReturn(Optional.of(project));
    when(integrationTypeRepository.findByName("email")).thenReturn(Optional.of(emailType));
    when(integrationRepository.existsByTypeIdAndProjectId(EMAIL_TYPE_ID, 20L)).thenReturn(true);

    var request = new IntegrationRQ();
    request.setName("email-server-2");

    // When & Then
    var ex = assertThrows(ReportPortalException.class,
        () -> handler.createProjectIntegration(PROJECT_KEY, request, "email", rpUser));
    assertEquals(ErrorType.INTEGRATION_ALREADY_EXISTS, ex.getErrorType());
  }

  @Test
  @DisplayName("Should reject AUTH group integration at project scope")
  void createProjectIntegrationWithAuthGroupShouldThrow() {
    var project = new Project();
    project.setId(20L);
    var ldap = new IntegrationType();
    ldap.setName("ldap");
    ldap.setIntegrationGroup(com.epam.reportportal.base.infrastructure.persistence.entity.enums.IntegrationGroupEnum.AUTH);
    when(projectRepository.findByKey(PROJECT_KEY)).thenReturn(Optional.of(project));
    when(integrationTypeRepository.findByName("ldap")).thenReturn(Optional.of(ldap));

    var request = new IntegrationRQ();
    request.setName("ldap-1");

    var ex = assertThrows(ReportPortalException.class,
        () -> handler.createProjectIntegration(PROJECT_KEY, request, "ldap", rpUser));
    assertEquals(ErrorType.INCORRECT_REQUEST, ex.getErrorType());
  }

  @Test
  @DisplayName("Should reject host change without a new secret on project integration update")
  void updateProjectIntegrationWithTargetChangeAndNoSecretShouldThrow() {
    var project = new Project();
    project.setId(20L);
    var integration = new com.epam.reportportal.base.infrastructure.persistence.entity.integration.Integration();
    integration.setType(emailType);
    integration.setParams(new com.epam.reportportal.base.infrastructure.persistence.entity.integration
        .IntegrationParams(new java.util.HashMap<>(Map.of("host", "smtp.old", "password", "cipher"))));
    when(projectRepository.findByKey(PROJECT_KEY)).thenReturn(Optional.of(project));
    when(integrationRepository.findByIdAndProjectId(5L, 20L)).thenReturn(Optional.of(integration));

    var request = new IntegrationRQ();
    request.setIntegrationParams(Map.of("host", "evil.example"));

    var ex = assertThrows(ReportPortalException.class,
        () -> handler.updateProjectIntegration(5L, PROJECT_KEY, request, rpUser));
    assertEquals(ErrorType.INCORRECT_REQUEST, ex.getErrorType());
    org.mockito.Mockito.verifyNoInteractions(basicIntegrationService);
  }

  private BasicTextEncryptor textEncryptor;

  /**
   * A real email integration service (real params encryptor, no mail server) so the test sees the
   * params exactly as they would be persisted after the merge.
   */
  private EmailServerIntegrationService realEmailService() {
    textEncryptor = new BasicTextEncryptor();
    textEncryptor.setPassword("test-secret");
    var service = new EmailServerIntegrationService(integrationRepository, mock(PluginBox.class),
        new IntegrationParamsEncryptor(textEncryptor), mock(MailServiceFactory.class));
    when(integrationServiceMapping.getOrDefault(eq("email"), any())).thenReturn(service);
    return service;
  }

  private Integration storedEmailIntegration() {
    var params = new HashMap<String, Object>();
    params.put("host", "smtp.old");
    params.put("port", 25);
    params.put("authEnabled", true);
    params.put("username", "mailer");
    params.put("password", textEncryptor.encrypt("old-secret"));
    var integration = new Integration();
    integration.setId(5L);
    integration.setType(emailType);
    integration.setParams(new IntegrationParams(params));
    return integration;
  }

  private Map<String, Object> updateProject(Map<String, Object> requestParams) {
    var project = new Project();
    project.setId(20L);
    var integration = storedEmailIntegration();
    when(projectRepository.findByKey(PROJECT_KEY)).thenReturn(Optional.of(project));
    when(integrationRepository.findByIdAndProjectId(5L, 20L)).thenReturn(Optional.of(integration));
    var request = new IntegrationRQ();
    request.setIntegrationParams(requestParams);
    handler.updateProjectIntegration(5L, PROJECT_KEY, request, rpUser);
    verify(integrationRepository).save(integration);
    return integration.getParams().getParams();
  }

  private void assertOldSecretGone(Map<String, Object> params) {
    params.values().stream().filter(String.class::isInstance).map(String.class::cast).forEach(v -> {
      try {
        assertFalse("old-secret".equals(textEncryptor.decrypt(v)), "old secret survived");
      } catch (org.jasypt.exceptions.EncryptionOperationNotPossibleException ignored) {
        // not a ciphertext
      }
    });
  }

  @Test
  @DisplayName("Project: new host plus another secret key does not keep the stored password")
  void updateProjectHostWithOtherSecretKeyDropsStoredPassword() {
    realEmailService();
    var params = updateProject(Map.of("host", "smtp.attacker.tld", "authEnabled", true, "apiToken", "x"));
    assertEquals("smtp.attacker.tld", params.get("host"));
    assertFalse(params.containsKey("password"));
    assertOldSecretGone(params);
  }

  @Test
  @DisplayName("Project: new host with a re-entered password stores the new password")
  void updateProjectHostWithNewPasswordStoresIt() {
    realEmailService();
    var params = updateProject(Map.of("host", "smtp.new", "authEnabled", true, "password", "new-secret"));
    assertEquals("smtp.new", params.get("host"));
    assertEquals("new-secret", textEncryptor.decrypt((String) params.get("password")));
  }

  @Test
  @DisplayName("Project: unchanged host without a secret keeps the stored password")
  void updateProjectUnchangedHostKeepsPassword() {
    realEmailService();
    var params = updateProject(Map.of("host", "smtp.old", "authEnabled", true, "from", "rp@corp"));
    assertEquals("old-secret", textEncryptor.decrypt((String) params.get("password")));
  }

  @Test
  @DisplayName("Project: switching auth off while changing host works and leaves no password")
  void updateProjectAuthOffWithNewHostClearsPassword() {
    realEmailService();
    var request = new HashMap<String, Object>();
    request.put("host", "smtp.new");
    request.put("authEnabled", false);
    request.put("username", "");
    request.put("password", "");
    var params = updateProject(request);
    assertEquals("smtp.new", params.get("host"));
    assertEquals(false, params.get("authEnabled"));
    assertNull(params.get("password"));
    assertOldSecretGone(params);
  }

  @Test
  @DisplayName("Global: host change without a secret is rejected")
  void updateGlobalIntegrationWithTargetChangeAndNoSecretShouldThrow() {
    textEncryptor = new BasicTextEncryptor();
    textEncryptor.setPassword("test-secret");
    var integration = storedEmailIntegration();
    when(integrationRepository.findGlobalById(5L)).thenReturn(Optional.of(integration));
    var request = new IntegrationRQ();
    request.setIntegrationParams(Map.of("host", "evil.example"));

    var ex = assertThrows(ReportPortalException.class,
        () -> handler.updateGlobalIntegration(5L, request, rpUser));
    assertEquals(ErrorType.INCORRECT_REQUEST, ex.getErrorType());
    org.mockito.Mockito.verifyNoInteractions(integrationServiceMapping);
  }

  private Integration storedLdapIntegration() {
    IntegrationType ldapType = new IntegrationType();
    ldapType.setId(20L);
    ldapType.setName("ldap");
    ldapType.setIntegrationGroup(
        com.epam.reportportal.base.infrastructure.persistence.entity.enums.IntegrationGroupEnum.AUTH);
    var stored = new java.util.HashMap<String, Object>();
    stored.put("url", "ldaps://directory.corp");
    stored.put("managerDn", "cn=manager,dc=corp");
    stored.put("managerPassword", "encrypted-manager-password");
    Integration integration = new Integration();
    integration.setId(7L);
    integration.setType(ldapType);
    integration.setParams(new com.epam.reportportal.base.infrastructure.persistence.entity.integration.IntegrationParams(stored));
    return integration;
  }

  @Test
  @DisplayName("Global AUTH: server address change without the secret is rejected before the strategy runs")
  void updateGlobalAuthIntegrationWithTargetChangeAndNoSecretShouldThrow() {
    var integration = storedLdapIntegration();
    when(integrationRepository.findGlobalById(7L)).thenReturn(Optional.of(integration));
    var request = new IntegrationRQ();
    request.setIntegrationParams(Map.of("url", "ldap://evil.example"));

    var ex = assertThrows(ReportPortalException.class,
        () -> handler.updateGlobalIntegration(7L, request, rpUser));
    assertEquals(ErrorType.INCORRECT_REQUEST, ex.getErrorType());
    org.mockito.Mockito.verifyNoInteractions(createAuthIntegrationHandler);
  }

  @Test
  @DisplayName("Global AUTH: server address change drops a stored secret the request does not supply again")
  void updateGlobalAuthIntegrationTargetChangeDropsStoredSecret() {
    var integration = storedLdapIntegration();
    when(integrationRepository.findGlobalById(7L)).thenReturn(Optional.of(integration));
    when(createAuthIntegrationHandler.updateAuthIntegration(org.mockito.ArgumentMatchers.any(),
        org.mockito.ArgumentMatchers.any())).thenAnswer(invocation -> invocation.getArgument(0));
    var request = new IntegrationRQ();
    request.setIntegrationParams(Map.of("url", "ldap://evil.example", "passwordAttribute", ""));

    handler.updateGlobalIntegration(7L, request, rpUser);

    assertFalse(integration.getParams().getParams().containsKey("managerPassword"));
    assertEquals("cn=manager,dc=corp", integration.getParams().getParams().get("managerDn"));
  }

  @Test
  @DisplayName("Global AUTH: unchanged server address keeps the stored secret")
  void updateGlobalAuthIntegrationUnchangedTargetKeepsSecret() {
    var integration = storedLdapIntegration();
    when(integrationRepository.findGlobalById(7L)).thenReturn(Optional.of(integration));
    when(createAuthIntegrationHandler.updateAuthIntegration(org.mockito.ArgumentMatchers.any(),
        org.mockito.ArgumentMatchers.any())).thenAnswer(invocation -> invocation.getArgument(0));
    var request = new IntegrationRQ();
    request.setIntegrationParams(Map.of("url", "ldaps://directory.corp", "baseDn", "dc=corp"));

    handler.updateGlobalIntegration(7L, request, rpUser);

    assertEquals("encrypted-manager-password", integration.getParams().getParams().get("managerPassword"));
  }

  @Test
  @DisplayName("Global: new host plus another secret key does not keep the stored password")
  void updateGlobalHostWithOtherSecretKeyDropsStoredPassword() {
    realEmailService();
    var integration = storedEmailIntegration();
    when(integrationRepository.findGlobalById(5L)).thenReturn(Optional.of(integration));
    var request = new IntegrationRQ();
    request.setIntegrationParams(Map.of("host", "smtp.attacker.tld", "authEnabled", true, "apiToken", "x"));

    handler.updateGlobalIntegration(5L, request, rpUser);

    var params = integration.getParams().getParams();
    assertFalse(params.containsKey("password"));
    assertOldSecretGone(params);
    verify(integrationRepository).save(integration);
  }

  @Test
  @DisplayName("Global: new host with a re-entered password stores the new password")
  void updateGlobalHostWithNewPasswordStoresIt() {
    realEmailService();
    var integration = storedEmailIntegration();
    when(integrationRepository.findGlobalById(5L)).thenReturn(Optional.of(integration));
    var request = new IntegrationRQ();
    request.setIntegrationParams(Map.of("host", "smtp.new", "authEnabled", true, "password", "new-secret"));

    handler.updateGlobalIntegration(5L, request, rpUser);

    assertEquals("new-secret",
        textEncryptor.decrypt((String) integration.getParams().getParams().get("password")));
  }
}
