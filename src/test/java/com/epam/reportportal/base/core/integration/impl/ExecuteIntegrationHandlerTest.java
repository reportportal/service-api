package com.epam.reportportal.base.core.integration.impl;

import static com.epam.reportportal.base.ReportPortalUserUtil.getRpUser;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.epam.reportportal.api.model.PluginCommandContext;
import com.epam.reportportal.api.model.PluginCommandRQ;
import com.epam.reportportal.base.core.integration.ExecuteIntegrationHandler;
import com.epam.reportportal.base.core.plugin.PluginBox;
import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.infrastructure.persistence.dao.IntegrationRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.IntegrationTypeRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.ProjectRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.ProjectUserRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.organization.OrganizationUserRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.PluginTypeEnum;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.Integration;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.IntegrationType;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.IntegrationTypeDetails;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.MembershipDetails;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.OrganizationRole;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.Project;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.ProjectRole;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.UserRole;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.base.util.ProjectExtractor;
import com.epam.reportportal.extension.CommonPluginCommand;
import com.epam.reportportal.extension.ReportPortalExtensionPoint;
import com.epam.reportportal.extension.builtin.BuiltinCommandResolver;
import com.epam.reportportal.extension.command.AbstractExtensionCommand;
import com.epam.reportportal.extension.command.ExtensionCommand;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

public class ExecuteIntegrationHandlerTest {

  private static final String PUBLIC_COMMAND_PREFIX = "public_";

  private final IntegrationRepository integrationRepository = mock(IntegrationRepository.class);
  private final IntegrationTypeRepository integrationTypeRepository = mock(IntegrationTypeRepository.class);
  private final BuiltinCommandResolver builtinCommandResolver = mock(BuiltinCommandResolver.class);
  private final PluginBox pluginBox = mock(PluginBox.class);
  private final ProjectRepository projectRepository = mock(ProjectRepository.class);
  private final ProjectExtractor projectExtractor = mock(ProjectExtractor.class);
  private final OrganizationUserRepository organizationUserRepository = mock(OrganizationUserRepository.class);

  private final ProjectUserRepository projectUserRepository = mock(ProjectUserRepository.class);

  private final ExecuteIntegrationHandler executeIntegrationHandler = new ExecuteIntegrationHandlerImpl(
      integrationRepository, integrationTypeRepository, builtinCommandResolver, pluginBox, projectRepository,
      projectExtractor, organizationUserRepository, projectUserRepository);

  private static final String EMAIL = "email";
  private static final String TEST_CONNECTION = "testConnection";
  private static final Long INTEGRATION_ID = 5L;
  private static final Long PROJECT_ID = 10L;
  private static final Long ORG_ID = 1L;

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  private ReportPortalUser authenticate(UserRole role) {
    ReportPortalUser user = getRpUser("user", role, OrganizationRole.MEMBER, ProjectRole.VIEWER, PROJECT_ID);
    SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(user, null));
    return user;
  }

  private ExtensionCommand<Object> mockBuiltinCommand(Integration integration) {
    ExtensionCommand<Object> command = mock(ExtensionCommand.class);
    when(integrationTypeRepository.existsByPluginTypeAndName(PluginTypeEnum.BUILT_IN, EMAIL)).thenReturn(true);
    when(builtinCommandResolver.resolve(EMAIL, TEST_CONNECTION)).thenReturn((ExtensionCommand) command);
    when(integrationRepository.findById(INTEGRATION_ID)).thenReturn(Optional.of(integration));
    return command;
  }

  private static Integration integration(String typeName, Long projectId, Long orgId) {
    IntegrationType type = new IntegrationType();
    type.setName(typeName);
    Integration integration = new Integration();
    integration.setId(INTEGRATION_ID);
    integration.setType(type);
    integration.setOrganizationId(orgId);
    if (projectId != null) {
      Project project = new Project();
      project.setId(projectId);
      integration.setProject(project);
    }
    return integration;
  }

  private void mockProject() {
    Project project = new Project();
    project.setId(PROJECT_ID);
    project.setKey("project-key");
    project.setOrganizationId(ORG_ID);
    when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(project));
  }

  private static MembershipDetails membership(OrganizationRole orgRole, ProjectRole projectRole) {
    MembershipDetails details = new MembershipDetails();
    details.setOrgId(ORG_ID);
    details.setOrgRole(orgRole);
    details.setProjectId(PROJECT_ID);
    details.setProjectRole(projectRole);
    return details;
  }

  private static PluginCommandRQ request(PluginCommandContext context) {
    return new PluginCommandRQ().context(context);
  }

  @Test
  void extensionCommandOnForeignProjectIntegrationIsDenied() {
    ReportPortalUser user = authenticate(UserRole.USER);
    Integration integration = integration(EMAIL, PROJECT_ID, null);
    ExtensionCommand<Object> command = mockBuiltinCommand(integration);
    mockProject();
    when(projectExtractor.findMembershipDetails(user, "project-key")).thenReturn(Optional.empty());

    ReportPortalException ex = assertThrows(ReportPortalException.class,
        () -> executeIntegrationHandler.executeExtensionCommand(EMAIL, TEST_CONNECTION,
            request(new PluginCommandContext().integrationId(INTEGRATION_ID))));

    assertEquals(ErrorType.ACCESS_DENIED, ex.getErrorType());
    verify(command, never()).executeCommand(any(Integration.class), any(PluginCommandRQ.class));
  }

  @Test
  void extensionCommandWithContextProjectNotMatchingIntegrationIsDenied() {
    authenticate(UserRole.ADMINISTRATOR);
    ExtensionCommand<Object> command = mockBuiltinCommand(integration(EMAIL, PROJECT_ID, null));

    ReportPortalException ex = assertThrows(ReportPortalException.class,
        () -> executeIntegrationHandler.executeExtensionCommand(EMAIL, TEST_CONNECTION,
            request(new PluginCommandContext().integrationId(INTEGRATION_ID).projectId(99L))));

    assertEquals(ErrorType.ACCESS_DENIED, ex.getErrorType());
    verify(command, never()).executeCommand(any(Integration.class), any(PluginCommandRQ.class));
  }

  @Test
  void extensionCommandWithoutDeclaredRoleRequiresProjectEditor() {
    ReportPortalUser user = authenticate(UserRole.USER);
    ExtensionCommand<Object> command = mockBuiltinCommand(integration(EMAIL, PROJECT_ID, null));
    mockProject();
    when(projectExtractor.findMembershipDetails(user, "project-key"))
        .thenReturn(Optional.of(membership(OrganizationRole.MEMBER, ProjectRole.VIEWER)));

    PluginCommandRQ rq = request(new PluginCommandContext().integrationId(INTEGRATION_ID));
    ReportPortalException ex = assertThrows(ReportPortalException.class,
        () -> executeIntegrationHandler.executeExtensionCommand(EMAIL, TEST_CONNECTION, rq));
    assertEquals(ErrorType.ACCESS_DENIED, ex.getErrorType());

    when(projectExtractor.findMembershipDetails(user, "project-key"))
        .thenReturn(Optional.of(membership(OrganizationRole.MEMBER, ProjectRole.EDITOR)));
    executeIntegrationHandler.executeExtensionCommand(EMAIL, TEST_CONNECTION, rq);
    verify(command).executeCommand(any(Integration.class), any(PluginCommandRQ.class));
  }

  @Test
  void extensionCommandOnForeignOrganizationIntegrationIsDenied() {
    ReportPortalUser user = authenticate(UserRole.USER);
    ExtensionCommand<Object> command = mockBuiltinCommand(integration(EMAIL, null, ORG_ID));
    when(organizationUserRepository.findByUserIdAndOrganization_Id(user.getUserId(), ORG_ID))
        .thenReturn(Optional.empty());

    ReportPortalException ex = assertThrows(ReportPortalException.class,
        () -> executeIntegrationHandler.executeExtensionCommand(EMAIL, TEST_CONNECTION,
            request(new PluginCommandContext().integrationId(INTEGRATION_ID))));

    assertEquals(ErrorType.ACCESS_DENIED, ex.getErrorType());
    verify(command, never()).executeCommand(any(Integration.class), any(PluginCommandRQ.class));
  }

  @Test
  void extensionCommandWithIntegrationOfAnotherPluginIsRejected() {
    authenticate(UserRole.ADMINISTRATOR);
    ExtensionCommand<Object> command = mockBuiltinCommand(integration("jira", PROJECT_ID, null));

    ReportPortalException ex = assertThrows(ReportPortalException.class,
        () -> executeIntegrationHandler.executeExtensionCommand(EMAIL, TEST_CONNECTION,
            request(new PluginCommandContext().integrationId(INTEGRATION_ID))));

    assertEquals(ErrorType.INTEGRATION_NOT_FOUND, ex.getErrorType());
    verify(command, never()).executeCommand(any(Integration.class), any(PluginCommandRQ.class));
  }

  /** Stubs the existence queries the way the database answers them for a user with the given memberships. */
  private void stubMemberships(ReportPortalUser user, ProjectRole projectRole, OrganizationRole orgRole) {
    when(projectUserRepository.existsByUser_IdAndProjectRoleIn(eq(user.getUserId()), any()))
        .thenAnswer(inv -> projectRole != null && ((Collection<ProjectRole>) inv.getArgument(1)).contains(projectRole));
    when(organizationUserRepository.existsByUser_IdAndOrganizationRoleIn(eq(user.getUserId()), any()))
        .thenAnswer(inv -> orgRole != null && ((Collection<OrganizationRole>) inv.getArgument(1)).contains(orgRole));
  }

  private ReportPortalException executeGlobalWithoutContext() {
    return assertThrows(ReportPortalException.class,
        () -> executeIntegrationHandler.executeExtensionCommand(EMAIL, TEST_CONNECTION,
            request(new PluginCommandContext().integrationId(INTEGRATION_ID))));
  }

  @Test
  void globalIntegrationWithoutContextIsDeniedForUserWithoutMemberships() {
    ReportPortalUser user = authenticate(UserRole.USER);
    ExtensionCommand<Object> command = mockBuiltinCommand(integration(EMAIL, null, null));
    stubMemberships(user, null, null);

    assertEquals(ErrorType.ACCESS_DENIED, executeGlobalWithoutContext().getErrorType());
    verify(command, never()).executeCommand(any(Integration.class), any(PluginCommandRQ.class));
  }

  @Test
  void globalIntegrationWithoutContextIsDeniedForProjectViewerAndOrganizationMember() {
    ReportPortalUser user = authenticate(UserRole.USER);
    ExtensionCommand<Object> command = mockBuiltinCommand(integration(EMAIL, null, null));
    stubMemberships(user, ProjectRole.VIEWER, OrganizationRole.MEMBER);

    assertEquals(ErrorType.ACCESS_DENIED, executeGlobalWithoutContext().getErrorType());
    verify(command, never()).executeCommand(any(Integration.class), any(PluginCommandRQ.class));
  }

  @Test
  void globalIntegrationWithoutContextIsAllowedForProjectEditorInAnyProject() {
    ReportPortalUser user = authenticate(UserRole.USER);
    ExtensionCommand<Object> command = mockBuiltinCommand(integration(EMAIL, null, null));
    stubMemberships(user, ProjectRole.EDITOR, null);

    executeIntegrationHandler.executeExtensionCommand(EMAIL, TEST_CONNECTION,
        request(new PluginCommandContext().integrationId(INTEGRATION_ID)));

    verify(command).executeCommand(any(Integration.class), any(PluginCommandRQ.class));
  }

  @Test
  void globalIntegrationWithoutContextIsAllowedForOrganizationManager() {
    ReportPortalUser user = authenticate(UserRole.USER);
    ExtensionCommand<Object> command = mockBuiltinCommand(integration(EMAIL, null, null));
    stubMemberships(user, null, OrganizationRole.MANAGER);

    executeIntegrationHandler.executeExtensionCommand(EMAIL, TEST_CONNECTION,
        request(new PluginCommandContext().integrationId(INTEGRATION_ID)));

    verify(command).executeCommand(any(Integration.class), any(PluginCommandRQ.class));
  }

  @Test
  void globalIntegrationWithoutContextHonoursTheRoleTheCommandDeclares() {
    ReportPortalUser user = authenticate(UserRole.USER);
    AbstractExtensionCommand<Object> command = mock(AbstractExtensionCommand.class);
    when(command.getMinProjectRole()).thenReturn(ProjectRole.VIEWER);
    when(integrationTypeRepository.existsByPluginTypeAndName(PluginTypeEnum.BUILT_IN, EMAIL)).thenReturn(true);
    when(builtinCommandResolver.resolve(EMAIL, TEST_CONNECTION)).thenReturn((ExtensionCommand) command);
    when(integrationRepository.findById(INTEGRATION_ID)).thenReturn(Optional.of(integration(EMAIL, null, null)));
    stubMemberships(user, ProjectRole.VIEWER, null);

    executeIntegrationHandler.executeExtensionCommand(EMAIL, TEST_CONNECTION,
        request(new PluginCommandContext().integrationId(INTEGRATION_ID)));

    verify(command).executeCommand(any(Integration.class), any(PluginCommandRQ.class));
  }

  @Test
  void globalIntegrationWithoutContextDeniedAndMissingIntegrationLookIdenticalToUserWithoutRole() {
    ReportPortalUser user = authenticate(UserRole.USER);
    mockBuiltinCommand(integration(EMAIL, null, null));
    when(integrationRepository.findById(404L)).thenReturn(Optional.empty());
    stubMemberships(user, ProjectRole.VIEWER, null);

    ReportPortalException denied = executeGlobalWithoutContext();
    ReportPortalException missing = assertThrows(ReportPortalException.class,
        () -> executeIntegrationHandler.executeExtensionCommand(EMAIL, TEST_CONNECTION,
            request(new PluginCommandContext().integrationId(404L))));

    assertEquals(ErrorType.ACCESS_DENIED, denied.getErrorType());
    assertEquals(denied.getErrorType(), missing.getErrorType());
    assertEquals(denied.getMessage(), missing.getMessage());
    assertTrue(denied.getMessage().contains("not found or access denied"));
  }

  @Test
  void globalIntegrationWithoutContextIsAllowedForAdministrator() {
    authenticate(UserRole.ADMINISTRATOR);
    ExtensionCommand<Object> command = mockBuiltinCommand(integration(EMAIL, null, null));

    executeIntegrationHandler.executeExtensionCommand(EMAIL, TEST_CONNECTION,
        request(new PluginCommandContext().integrationId(INTEGRATION_ID)));

    verify(command).executeCommand(any(Integration.class), any(PluginCommandRQ.class));
    verifyNoInteractions(projectUserRepository, organizationUserRepository);
  }

  @Test
  void globalIntegrationWithProjectContextWorksForProjectEditor() {
    ReportPortalUser user = authenticate(UserRole.USER);
    ExtensionCommand<Object> command = mockBuiltinCommand(integration(EMAIL, null, null));
    mockProject();
    when(projectExtractor.findMembershipDetails(user, "project-key"))
        .thenReturn(Optional.of(membership(OrganizationRole.MEMBER, ProjectRole.EDITOR)));

    executeIntegrationHandler.executeExtensionCommand(EMAIL, TEST_CONNECTION,
        request(new PluginCommandContext().integrationId(INTEGRATION_ID).projectId(PROJECT_ID)));

    verify(command).executeCommand(any(Integration.class), any(PluginCommandRQ.class));
  }

  @Test
  void missingIntegrationAndDeniedIntegrationLookIdenticalToNonAdministrator() {
    authenticate(UserRole.USER);
    mockBuiltinCommand(integration(EMAIL, null, null));
    when(integrationRepository.findById(404L)).thenReturn(Optional.empty());

    ReportPortalException missing = assertThrows(ReportPortalException.class,
        () -> executeIntegrationHandler.executeExtensionCommand(EMAIL, TEST_CONNECTION,
            request(new PluginCommandContext().integrationId(404L))));
    ReportPortalException denied = assertThrows(ReportPortalException.class,
        () -> executeIntegrationHandler.executeExtensionCommand(EMAIL, TEST_CONNECTION,
            request(new PluginCommandContext().integrationId(INTEGRATION_ID))));

    assertEquals(ErrorType.ACCESS_DENIED, missing.getErrorType());
    assertEquals(denied.getErrorType(), missing.getErrorType());
    assertEquals(denied.getMessage(), missing.getMessage());
  }

  @Test
  void missingIntegrationIsReportedPreciselyToAdministrator() {
    authenticate(UserRole.ADMINISTRATOR);
    mockBuiltinCommand(integration(EMAIL, null, null));
    when(integrationRepository.findById(404L)).thenReturn(Optional.empty());

    ReportPortalException ex = assertThrows(ReportPortalException.class,
        () -> executeIntegrationHandler.executeExtensionCommand(EMAIL, TEST_CONNECTION,
            request(new PluginCommandContext().integrationId(404L))));

    assertEquals(ErrorType.INTEGRATION_NOT_FOUND, ex.getErrorType());
  }

  @Test
  void commonExtensionCommandOnForeignProjectIsDenied() {
    ReportPortalUser user = authenticate(UserRole.USER);
    when(integrationTypeRepository.existsByPluginTypeAndName(PluginTypeEnum.BUILT_IN, "jira")).thenReturn(false);
    ExtensionCommand<Object> command = mock(ExtensionCommand.class);
    ReportPortalExtensionPoint pluginInstance = mock(ReportPortalExtensionPoint.class);
    when(pluginInstance.getCommonExtensionCommand("search")).thenReturn((ExtensionCommand) command);
    when(pluginBox.getInstance("jira", ReportPortalExtensionPoint.class)).thenReturn(Optional.of(pluginInstance));
    mockProject();
    when(projectExtractor.findMembershipDetails(user, "project-key")).thenReturn(Optional.empty());

    PluginCommandRQ rq = new PluginCommandRQ().putArgumentsItem("projectId", PROJECT_ID);
    ReportPortalException ex = assertThrows(ReportPortalException.class,
        () -> executeIntegrationHandler.executeExtensionCommand("jira", "search", rq));

    assertEquals(ErrorType.ACCESS_DENIED, ex.getErrorType());
    verify(command, never()).executeCommand(any(PluginCommandRQ.class));
  }


  private void stubPluginAccessType(String pluginName, String accessType) {
    IntegrationType type = new IntegrationType();
    type.setName(pluginName);
    if (accessType != null) {
      IntegrationTypeDetails details = new IntegrationTypeDetails();
      details.setDetails(Map.of("accessType", accessType));
      type.setDetails(details);
    }
    when(integrationTypeRepository.findByName(pluginName)).thenReturn(Optional.of(type));
  }

  @Test
  @DisplayName("Negative Test. Plugin without public access type is refused")
  public void executePublicCommandOnNonPublicPluginTest() {
    final String pluginName = "jira";
    final String publicCommand = PUBLIC_COMMAND_PREFIX + "testCommand";
    stubPluginAccessType(pluginName, null);

    ReportPortalException ex = assertThrows(ReportPortalException.class, () ->
        executeIntegrationHandler.executePublicCommand(pluginName, publicCommand, Collections.emptyMap()));

    assertEquals(ErrorType.ACCESS_DENIED, ex.getErrorType());
    verifyNoInteractions(pluginBox);
  }

  @Test
  @DisplayName("Negative Test. Unknown plugin type is refused")
  public void executePublicCommandOnUnknownPluginTest() {
    when(integrationTypeRepository.findByName("ghost")).thenReturn(Optional.empty());

    ReportPortalException ex = assertThrows(ReportPortalException.class, () ->
        executeIntegrationHandler.executePublicCommand("ghost", PUBLIC_COMMAND_PREFIX + "x", Collections.emptyMap()));

    assertEquals(ErrorType.ACCESS_DENIED, ex.getErrorType());
    verifyNoInteractions(pluginBox);
  }

  @Test
  @DisplayName("Positive Test. Everything is fine")
  public void executePublicCommandPositiveTest() {
    final String pluginName = "signup";
    final String publicCommand = PUBLIC_COMMAND_PREFIX + "testCommand";
    final Map<String, Object> params = Collections.emptyMap();
    stubPluginAccessType(pluginName, "public");

    CommonPluginCommand<String> commonPluginCommand = mock(CommonPluginCommand.class);
    when(commonPluginCommand.executeCommand(params)).thenReturn("Ok");

    ReportPortalExtensionPoint pluginInstance = mock(ReportPortalExtensionPoint.class);
    when(pluginInstance.getCommonCommand(publicCommand)).thenReturn(commonPluginCommand);

    when(pluginBox.getInstance(pluginName, ReportPortalExtensionPoint.class)).thenReturn(
        Optional.of(pluginInstance));

    executeIntegrationHandler.executePublicCommand(pluginName, publicCommand, params);

    verify(pluginBox).getInstance(eq(pluginName), eq(ReportPortalExtensionPoint.class));
    verify(pluginInstance).getCommonCommand(eq(publicCommand));
  }

  @Test
  @DisplayName("Negative Test. When command is not public")
  public void executeNotPublicCommandTest() {
    final String pluginName = "signup";
    final String publicCommand = "testCommand";
    final Map<String, Object> params = Collections.emptyMap();

    assertThrows(ReportPortalException.class, () ->
        executeIntegrationHandler.executePublicCommand(pluginName, publicCommand, params));

    verifyNoInteractions(pluginBox);
  }

  @Test
  @DisplayName("Negative Test. When Plugin not found")
  public void executePublicCommandWOPluginTest() {
    final String pluginName = "signup";
    final String publicCommand = PUBLIC_COMMAND_PREFIX + "testCommand";
    final Map<String, Object> params = Collections.emptyMap();
    stubPluginAccessType(pluginName, "public");

    CommonPluginCommand<String> commonPluginCommand = mock(CommonPluginCommand.class);
    when(commonPluginCommand.executeCommand(params)).thenReturn("Ok");

    ReportPortalExtensionPoint pluginInstance = mock(ReportPortalExtensionPoint.class);

    when(pluginBox.getInstance(pluginName, ReportPortalExtensionPoint.class)).thenReturn(
        Optional.empty());

    assertThrows(ReportPortalException.class, () ->
        executeIntegrationHandler.executePublicCommand(pluginName, publicCommand, params));

    verify(pluginBox).getInstance(eq(pluginName), eq(ReportPortalExtensionPoint.class));
    verifyNoInteractions(pluginInstance);
  }

  @Test
  @DisplayName("Negative Test. When Command not found")
  public void executePublicCommandWOCommandTest() {
    final String pluginName = "signup";
    final String publicCommand = PUBLIC_COMMAND_PREFIX + "testCommand";
    final Map<String, Object> params = Collections.emptyMap();
    stubPluginAccessType(pluginName, "public");

    CommonPluginCommand<String> commonPluginCommand = mock(CommonPluginCommand.class);
    when(commonPluginCommand.executeCommand(params)).thenReturn("Ok");

    ReportPortalExtensionPoint pluginInstance = mock(ReportPortalExtensionPoint.class);
    when(pluginInstance.getCommonCommand(publicCommand)).thenReturn(null);

    when(pluginBox.getInstance(pluginName, ReportPortalExtensionPoint.class)).thenReturn(
        Optional.of(pluginInstance));

    assertThrows(ReportPortalException.class, () ->
        executeIntegrationHandler.executePublicCommand(pluginName, publicCommand, params));

    verify(pluginBox).getInstance(eq(pluginName), eq(ReportPortalExtensionPoint.class));
    verify(pluginInstance).getCommonCommand(eq(publicCommand));
  }

}
