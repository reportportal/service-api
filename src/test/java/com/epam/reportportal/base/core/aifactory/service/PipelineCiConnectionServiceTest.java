package com.epam.reportportal.base.core.aifactory.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.reportportal.base.core.aifactory.connector.CiTriggerConnector;
import com.epam.reportportal.base.core.aifactory.enums.CiConnectionReasonCode;
import com.epam.reportportal.base.core.aifactory.enums.CiConnectionState;
import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.infrastructure.persistence.dao.IntegrationRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.IntegrationTypeRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.aifactory.PipelineRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.Pipeline;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineSettings;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.IntegrationGroupEnum;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.Integration;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.IntegrationParams;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.IntegrationType;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.MembershipDetails;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.OrganizationRole;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.Project;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.UserRole;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;

/**
 * The rules of the pipeline CI connection that need no database: who may configure it, which state lets jobs start,
 * which integration a job uses and what a started job records.
 */
@ExtendWith(MockitoExtension.class)
class PipelineCiConnectionServiceTest {

  private static final long PROJECT_ID = 1L;
  private static final long PIPELINE_ID = 5L;
  private static final long OWN_INTEGRATION_ID = 77L;

  @Mock
  private PipelineRepository pipelineRepository;
  @Mock
  private IntegrationRepository integrationRepository;
  @Mock
  private IntegrationTypeRepository integrationTypeRepository;
  @Mock
  private CiTriggerConnector gitlabConnector;
  @Mock
  private ApplicationEventPublisher eventPublisher;
  @Mock
  private PlatformTransactionManager transactionManager;

  private PipelineCiConnectionService sut;
  private Project project;
  private Pipeline pipeline;

  @BeforeEach
  void setUp() {
    lenient().when(transactionManager.getTransaction(any())).thenReturn(mock(TransactionStatus.class));
    lenient().when(gitlabConnector.getIntegrationTypeName()).thenReturn("gitlab-ci");
    sut = new PipelineCiConnectionService(pipelineRepository, integrationRepository, integrationTypeRepository,
        List.of(gitlabConnector), eventPublisher, transactionManager);

    project = new Project();
    project.setId(PROJECT_ID);
    pipeline = new Pipeline();
    pipeline.setId(PIPELINE_ID);
    pipeline.setProject(project);
  }

  private Integration integration(long id, String typeName, Map<String, Object> params) {
    var type = new IntegrationType();
    type.setName(typeName);
    var integration = new Integration();
    integration.setId(id);
    integration.setType(type);
    integration.setEnabled(true);
    integration.setParams(new IntegrationParams(new HashMap<>(params)));
    return integration;
  }

  private void givenTheProjectHas(Integration... integrations) {
    when(integrationRepository.findAllProjectByGroup(project, IntegrationGroupEnum.AUTOMATION))
        .thenReturn(List.of(integrations));
  }

  private void givenThePipelineOwnsAnIntegrationWithState(String state, Map<String, Object> params) {
    var entry = new LinkedHashMap<String, Object>();
    entry.put("integrationId", OWN_INTEGRATION_ID);
    entry.put("state", state);
    pipeline.setSettings(new PipelineSettings(Map.of(PipelineCiConnectionService.SETTINGS_KEY, entry)));
    lenient().when(integrationRepository.findByIdAndProjectId(OWN_INTEGRATION_ID, PROJECT_ID))
        .thenReturn(Optional.of(integration(OWN_INTEGRATION_ID, "gitlab-ci", params)));
  }

  // ---- who may configure -----------------------------------------------------------------------------------------

  private ReportPortalUser user(UserRole role) {
    var user = mock(ReportPortalUser.class);
    lenient().when(user.getUserRole()).thenReturn(role);
    return user;
  }

  private MembershipDetails membership(OrganizationRole orgRole) {
    return MembershipDetails.builder().withOrgRole(orgRole).withProjectId(PROJECT_ID).build();
  }

  @Test
  void assertCanConfigure_ForAnAdministratorAndAnOrganizationManager_ShouldPass() {
    assertDoesNotThrow(() -> sut.assertCanConfigure(user(UserRole.ADMINISTRATOR), membership(OrganizationRole.MEMBER)));
    assertDoesNotThrow(() -> sut.assertCanConfigure(user(UserRole.USER), membership(OrganizationRole.MANAGER)));
  }

  @Test
  void assertCanConfigure_ForAnEditorWhoIsOnlyAnOrganizationMember_ShouldBeRefused() {
    var thrown = assertThrows(ReportPortalException.class,
        () -> sut.assertCanConfigure(user(UserRole.USER), membership(OrganizationRole.MEMBER)));

    assertEquals(ErrorType.ACCESS_DENIED.getCode(), thrown.getErrorType().getCode());
  }

  // ---- which state lets jobs start -------------------------------------------------------------------------------

  @Test
  void summary_ForAPipelineOnACompleteProjectIntegration_ShouldBeConnected() {
    givenTheProjectHas(integration(1L, "gitlab-ci", Map.of("projectId", "42", "triggerToken", "t")));

    var summary = sut.summary(pipeline);

    assertEquals(CiConnectionState.CONNECTED, summary.getState());
    assertEquals(true, summary.isCanStartJobs());
  }

  @Test
  void summary_ForAPipelineOnAnIncompleteProjectIntegration_ShouldNotBeConnected() {
    givenTheProjectHas(integration(1L, "gitlab-ci", Map.of("projectId", "42")));

    assertEquals(CiConnectionState.NOT_CONNECTED, sut.summary(pipeline).getState());
  }

  @Test
  void summary_WithNoIntegrationAtAll_ShouldNotBeConnected() {
    givenTheProjectHas();

    var summary = sut.summary(pipeline);

    assertEquals(CiConnectionState.NOT_CONNECTED, summary.getState());
    assertEquals(false, summary.isCanStartJobs());
  }

  @Test
  void summary_ForAPipelineThatOwnsAnIntegration_ShouldFollowItsSavedState() {
    givenThePipelineOwnsAnIntegrationWithState("CONNECTED", Map.of("projectId", "a/b", "triggerToken", "t"));

    assertEquals(CiConnectionState.CONNECTED, sut.summary(pipeline).getState());
  }

  @Test
  void summary_ForAPipelineThatOwnsAnIntegrationButLostItsToken_ShouldNotBeConnectedWhateverWasSaved() {
    givenThePipelineOwnsAnIntegrationWithState("CONNECTED", Map.of("projectId", "a/b"));

    assertEquals(CiConnectionState.NOT_CONNECTED, sut.summary(pipeline).getState());
  }

  @Test
  void assertCanStartJobs_WhenTheLastStartFailed_ShouldRefuseWithCiNotConnected() {
    givenThePipelineOwnsAnIntegrationWithState("CONNECTION_FAILED", Map.of("projectId", "a/b", "triggerToken", "t"));

    var thrown = assertThrows(ReportPortalException.class, () -> sut.assertCanStartJobs(pipeline));

    assertEquals(ErrorType.CI_NOT_CONNECTED.getCode(), thrown.getErrorType().getCode());
  }

  @Test
  void assertCanStartJobs_WhenConnected_ShouldPass() {
    givenThePipelineOwnsAnIntegrationWithState("CONNECTED", Map.of("projectId", "a/b", "triggerToken", "t"));

    assertDoesNotThrow(() -> sut.assertCanStartJobs(pipeline));
  }

  // ---- which integration a job uses ------------------------------------------------------------------------------

  @Test
  void integrationFor_ShouldPreferThePipelinesOwnIntegration() {
    givenThePipelineOwnsAnIntegrationWithState("CONNECTED", Map.of("projectId", "a/b", "triggerToken", "t"));

    assertEquals(OWN_INTEGRATION_ID, sut.integrationFor(pipeline, gitlabConnector).getId());
  }

  @Test
  void integrationFor_ForAPipelineWithoutItsOwn_ShouldSkipTheIntegrationsOfOtherPipelines() {
    var ofAnotherPipeline = integration(8L, "gitlab-ci",
        Map.of("projectId", "x/y", "triggerToken", "t", PipelineCiConnectionService.PARAM_OWNER_PIPELINE, 99L));
    var shared = integration(9L, "gitlab-ci", Map.of("projectId", "42", "triggerToken", "t"));
    givenTheProjectHas(ofAnotherPipeline, shared);

    assertSame(shared, sut.integrationFor(pipeline, gitlabConnector));
  }

  @Test
  void integrationFor_WhenOnlyAnotherPipelinesIntegrationExists_ShouldNotUseIt() {
    givenTheProjectHas(integration(8L, "gitlab-ci",
        Map.of("projectId", "x/y", "triggerToken", "t", PipelineCiConnectionService.PARAM_OWNER_PIPELINE, 99L)));

    assertThrows(ReportPortalException.class, () -> sut.integrationFor(pipeline, gitlabConnector));
  }

  // ---- what a started job records --------------------------------------------------------------------------------

  @Test
  void recordJobStart_WhenTheCiAcceptedIt_ShouldMarkTheOwnedConnectionConnected() {
    givenThePipelineOwnsAnIntegrationWithState("CONNECTION_FAILED", Map.of("projectId", "a/b", "triggerToken", "t"));
    when(pipelineRepository.findById(PIPELINE_ID)).thenReturn(Optional.of(pipeline));

    sut.recordJobStart(PIPELINE_ID, null);

    var saved = ArgumentCaptor.forClass(Pipeline.class);
    verify(pipelineRepository).save(saved.capture());
    var entry = (Map<?, ?>) saved.getValue().getSettings().getValues().get(PipelineCiConnectionService.SETTINGS_KEY);
    assertEquals("CONNECTED", entry.get("state"));
    assertEquals(OWN_INTEGRATION_ID, ((Number) entry.get("integrationId")).longValue());
  }

  @Test
  void recordJobStart_WhenTheCiRefusedIt_ShouldMarkItFailedWithTheReason() {
    givenThePipelineOwnsAnIntegrationWithState("CONNECTED", Map.of("projectId", "a/b", "triggerToken", "t"));
    when(pipelineRepository.findById(PIPELINE_ID)).thenReturn(Optional.of(pipeline));

    sut.recordJobStart(PIPELINE_ID, "HTTP code: 404");

    var saved = ArgumentCaptor.forClass(Pipeline.class);
    verify(pipelineRepository).save(saved.capture());
    var entry = (Map<?, ?>) saved.getValue().getSettings().getValues().get(PipelineCiConnectionService.SETTINGS_KEY);
    assertEquals("CONNECTION_FAILED", entry.get("state"));
    assertEquals(CiConnectionReasonCode.UNEXPECTED_RESPONSE.name(), entry.get("reasonCode"));
    assertEquals("HTTP code: 404", entry.get("reason"));
  }

  @Test
  void recordJobStart_ForAPipelineOnTheProjectIntegration_ShouldRecordNothing() {
    when(pipelineRepository.findById(PIPELINE_ID)).thenReturn(Optional.of(pipeline));

    sut.recordJobStart(PIPELINE_ID, "boom");

    verify(pipelineRepository, never()).save(any());
  }

  // ---- repository URL --------------------------------------------------------------------------------------------

  @Test
  void repositoryRef_ShouldTakeTheProjectPathFromTheUrlAndDropDotGitAndSlashes() {
    var ref = PipelineCiConnectionService.RepositoryRef.parse(" https://git.epam.com/EPM-RPP/rp-tests.git/ ");

    assertEquals("https://git.epam.com", ref.baseUrl());
    assertEquals("EPM-RPP/rp-tests", ref.projectPath());
  }

  @Test
  void repositoryRef_ShouldKeepAPortAndNestedGroups() {
    var ref = PipelineCiConnectionService.RepositoryRef.parse("https://gitlab.internal:8443/a/b/c");

    assertEquals("https://gitlab.internal:8443", ref.baseUrl());
    assertEquals("a/b/c", ref.projectPath());
  }

  @Test
  void repositoryRef_ShouldRejectWhatIsNotAnHttpsUrlWithAPath() {
    assertThrows(ReportPortalException.class, () -> PipelineCiConnectionService.RepositoryRef.parse("git@x:a/b.git"));
    assertThrows(ReportPortalException.class,
        () -> PipelineCiConnectionService.RepositoryRef.parse("http://git.epam.com/a/b"));
    assertThrows(ReportPortalException.class,
        () -> PipelineCiConnectionService.RepositoryRef.parse("https://git.epam.com/"));
  }
}
