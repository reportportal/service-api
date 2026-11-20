package com.epam.reportportal.base.core.aifactory.service;

import static com.epam.reportportal.base.ws.converter.converters.IntegrationConverter.TO_ACTIVITY_RESOURCE;

import com.epam.reportportal.base.core.aifactory.connector.CiConnectionCheck;
import com.epam.reportportal.base.core.aifactory.connector.CiTriggerConnector;
import com.epam.reportportal.base.core.aifactory.dto.PipelineCiConnectionRQ;
import com.epam.reportportal.base.core.aifactory.dto.PipelineCiConnectionRS;
import com.epam.reportportal.base.core.aifactory.dto.PipelineCiConnectionSummaryRS;
import com.epam.reportportal.base.core.aifactory.dto.PipelineCiConnectionTestRS;
import com.epam.reportportal.base.core.aifactory.enums.CiConnectionReasonCode;
import com.epam.reportportal.base.core.aifactory.enums.CiConnectionState;
import com.epam.reportportal.base.core.events.domain.IntegrationCreatedEvent;
import com.epam.reportportal.base.core.events.domain.IntegrationUpdatedEvent;
import com.epam.reportportal.base.core.integration.util.CiTriggerIntegrationService;
import com.epam.reportportal.base.core.tms.pipeline.TestCaseReviewContributor;
import com.epam.reportportal.base.core.tms.pipeline.TmsAutomationStartService;
import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.infrastructure.persistence.dao.IntegrationRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.IntegrationTypeRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.aifactory.PipelineRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.Pipeline;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineSettings;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.IntegrationGroupEnum;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.Integration;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.IntegrationParams;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.MembershipDetails;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.OrganizationRole;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.Project;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.UserRole;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.base.ws.converter.builders.IntegrationBuilder;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * The CI connection of a pipeline: the repository, credential, branch, jobs, models and environments its jobs run
 * with, and whether they can be started right now.
 * <p>
 * Every pipeline owns its integration of type {@code gitlab-ci}, marked with {@code ownerPipelineId}, so changing the
 * credential or branch of one pipeline never touches another. The pipeline's settings keep only the pointer and the
 * verdict: {@code settings.ciConnection = {integrationId, state, reasonCode, reason, checkedAt}}. A pipeline without
 * its own integration keeps using the single project integration, as before: its state is derived from it (present,
 * enabled, all required parameters -> CONNECTED), so existing Push to agent and Retry keep working.
 * <p>
 * Example: saving {@code repository=https://git.epam.com/EPM-RPP/rp-tests, credential=..., jobs, environments} on a
 * pipeline creates its integration with {@code url=https://git.epam.com}, {@code projectId=EPM-RPP/rp-tests}; the
 * state stays NOT_CONNECTED until "Test connection" gets the answer "Reference not found" from GitLab.
 */
@Service
public class PipelineCiConnectionService {

  /** Key under which the pipeline's settings keep the connection verdict. */
  public static final String SETTINGS_KEY = "ciConnection";

  static final String INTEGRATION_TYPE = "gitlab-ci";
  static final String PARAM_URL = "url";
  static final String PARAM_PROJECT = "projectId";
  static final String PARAM_TOKEN = "triggerToken";
  static final String PARAM_REF = "ref";
  static final String PARAM_JOBS = "jobs";
  static final String PARAM_MODELS = "models";
  static final String PARAM_ENVIRONMENTS = "environments";
  /** Marks an integration as the property of one pipeline; project-wide lookups skip such integrations. */
  static final String PARAM_OWNER_PIPELINE = "ownerPipelineId";

  private static final String DEFAULT_BRANCH = "main";
  private static final String DEFAULT_BASE_URL = "https://gitlab.com";
  private static final String PROVIDER = "GITLAB";
  private static final List<String> LEGACY_TYPES = List.of(INTEGRATION_TYPE, "github-actions");

  private final PipelineRepository pipelineRepository;
  private final IntegrationRepository integrationRepository;
  private final IntegrationTypeRepository integrationTypeRepository;
  private final Map<String, CiTriggerConnector> connectorsByIntegrationType;
  private final ApplicationEventPublisher eventPublisher;
  private final TransactionTemplate transactionTemplate;

  public PipelineCiConnectionService(PipelineRepository pipelineRepository,
      IntegrationRepository integrationRepository, IntegrationTypeRepository integrationTypeRepository,
      List<CiTriggerConnector> connectors, ApplicationEventPublisher eventPublisher,
      PlatformTransactionManager transactionManager) {
    this.pipelineRepository = pipelineRepository;
    this.integrationRepository = integrationRepository;
    this.integrationTypeRepository = integrationTypeRepository;
    this.connectorsByIntegrationType = connectors.stream()
        .collect(Collectors.toMap(CiTriggerConnector::getIntegrationTypeName, Function.identity(), (a, b) -> a));
    this.eventPublisher = eventPublisher;
    this.transactionTemplate = new TransactionTemplate(transactionManager);
  }

  /**
   * Only an Administrator or an Organization Manager (of the project's organization) may read or change the full
   * connection; an Editor may start jobs but not see or change how they are started.
   *
   * @throws ReportPortalException ACCESS_DENIED (HTTP 403) for anybody else
   */
  public void assertCanConfigure(ReportPortalUser user, MembershipDetails membership) {
    if (user.getUserRole() == UserRole.ADMINISTRATOR) {
      return;
    }
    var orgRole = membership == null ? null : membership.getOrgRole();
    if (orgRole == null || !orgRole.sameOrHigherThan(OrganizationRole.MANAGER)) {
      throw new ReportPortalException(ErrorType.ACCESS_DENIED,
          "Only an Organization Manager or an Administrator can manage the CI connection");
    }
  }

  @Transactional(readOnly = true)
  public PipelineCiConnectionRS get(Long projectId, Long pipelineId) {
    return toRS(findPipeline(projectId, pipelineId));
  }

  /**
   * Creates the pipeline's integration on the first save, updates it afterwards. The state is not touched by a save,
   * and a credential left out keeps the saved one.
   */
  @Transactional
  public PipelineCiConnectionRS save(Long projectId, Long pipelineId, PipelineCiConnectionRQ rq,
      ReportPortalUser user) {
    var pipeline = findPipeline(projectId, pipelineId);
    var repository = RepositoryRef.parse(rq.getRepository());
    validate(pipeline, rq);

    var owned = ownedIntegration(pipeline);
    var params = new LinkedHashMap<String, Object>(
        owned.map(Integration::getParams).map(IntegrationParams::getParams).orElseGet(LinkedHashMap::new));
    params.put(PARAM_URL, repository.baseUrl());
    params.put(PARAM_PROJECT, repository.projectPath());
    params.put(PARAM_REF, blankToNull(rq.getBranch()) == null ? DEFAULT_BRANCH : rq.getBranch().trim());
    params.put(PARAM_JOBS, new LinkedHashMap<>(rq.getJobs()));
    params.put(PARAM_MODELS, rq.getModels() == null ? List.of() : List.copyOf(rq.getModels()));
    params.put(PARAM_ENVIRONMENTS, List.copyOf(rq.getEnvironments()));
    params.put(PARAM_OWNER_PIPELINE, pipelineId);
    if (blankToNull(rq.getCredential()) != null) {
      params.put(PARAM_TOKEN, rq.getCredential().trim());
    }

    var project = pipeline.getProject();
    if (owned.isPresent()) {
      var integration = owned.get();
      var before = TO_ACTIVITY_RESOURCE.apply(integration);
      integration.setParams(new IntegrationParams(params));
      integrationRepository.save(integration);
      eventPublisher.publishEvent(new IntegrationUpdatedEvent(user.getUserId(), user.getUsername(), before,
          TO_ACTIVITY_RESOURCE.apply(integration), project.getOrganizationId()));
    } else {
      var type = integrationTypeRepository.findByName(INTEGRATION_TYPE)
          .orElseThrow(() -> new ReportPortalException(ErrorType.INTEGRATION_NOT_FOUND, INTEGRATION_TYPE));
      var integration = new IntegrationBuilder()
          .withCreationDate(Instant.now())
          .withType(type)
          .withEnabled(true)
          .withName("CI - " + pipeline.getName() + " (pipeline " + pipelineId + ")")
          .withProject(project)
          .withCreator(user.getUsername())
          .withParams(new IntegrationParams(params))
          .get();
      integration = integrationRepository.save(integration);
      writeEntry(pipeline, entryFor(integration.getId(), CiConnectionState.NOT_CONNECTED, null, null, null));
      eventPublisher.publishEvent(new IntegrationCreatedEvent(TO_ACTIVITY_RESOURCE.apply(integration),
          user.getUserId(), user.getUsername(), project.getOrganizationId()));
    }
    return toRS(pipeline);
  }

  /**
   * Removes the saved credential; the pipeline can no longer start jobs until a new one is saved and tested.
   */
  @Transactional
  public PipelineCiConnectionRS deleteCredential(Long projectId, Long pipelineId, ReportPortalUser user) {
    var pipeline = findPipeline(projectId, pipelineId);
    var owned = ownedIntegration(pipeline);
    if (owned.isPresent()) {
      var integration = owned.get();
      var before = TO_ACTIVITY_RESOURCE.apply(integration);
      var params = new LinkedHashMap<>(integration.getParams().getParams());
      params.remove(PARAM_TOKEN);
      integration.setParams(new IntegrationParams(params));
      integrationRepository.save(integration);
      writeEntry(pipeline, entryFor(integration.getId(), CiConnectionState.NOT_CONNECTED,
          CiConnectionReasonCode.NO_CREDENTIAL, "No credential is saved", Instant.now()));
      eventPublisher.publishEvent(new IntegrationUpdatedEvent(user.getUserId(), user.getUsername(), before,
          TO_ACTIVITY_RESOURCE.apply(integration), pipeline.getProject().getOrganizationId()));
    }
    return toRS(pipeline);
  }

  /**
   * Asks the CI whether it accepts the saved credential and keeps the verdict. The HTTP call is made outside any
   * database transaction.
   * <p>
   * Example: a good token -> CONNECTED; a wrong one -> CONNECTION_FAILED with
   * {@code REPOSITORY_OR_CREDENTIAL_INVALID}. A pipeline that only uses the project integration is tested too, but
   * has nowhere to keep the verdict, so it is only returned.
   */
  public PipelineCiConnectionTestRS test(Long projectId, Long pipelineId) {
    var target = transactionTemplate.execute(status -> {
      var pipeline = findPipeline(projectId, pipelineId);
      var effective = effective(pipeline);
      return new TestTarget(effective.integration(), effective.owned());
    });

    CiConnectionCheck check;
    if (target.integration() == null) {
      check = CiConnectionCheck.failed(CiConnectionReasonCode.NO_CREDENTIAL, "No credential is saved");
    } else {
      var connector = connectorsByIntegrationType.get(target.integration().getType().getName());
      check = connector == null
          ? CiConnectionCheck.failed(CiConnectionReasonCode.UNEXPECTED_RESPONSE, "No connector for the integration")
          : connector.checkConnection(target.integration());
    }

    var checkedAt = Instant.now();
    var state = check.connected() ? CiConnectionState.CONNECTED : stateOfFailure(check.reasonCode());
    if (target.owned()) {
      transactionTemplate.executeWithoutResult(status -> {
        var pipeline = findPipeline(projectId, pipelineId);
        writeEntry(pipeline, entryFor(target.integration().getId(), state, check.reasonCode(), check.reason(),
            checkedAt));
      });
    }
    return PipelineCiConnectionTestRS.builder()
        .state(state)
        .canStartJobs(state == CiConnectionState.CONNECTED)
        .reasonCode(check.reasonCode())
        .reason(check.reason())
        .checkedAt(checkedAt)
        .build();
  }

  /**
   * What every project member may know: the state and whether jobs can start.
   */
  @Transactional(readOnly = true)
  public PipelineCiConnectionSummaryRS summary(Pipeline pipeline) {
    var effective = effective(pipeline);
    return PipelineCiConnectionSummaryRS.builder()
        .state(effective.state())
        .canStartJobs(effective.state() == CiConnectionState.CONNECTED)
        .build();
  }

  /**
   * Refuses to start a job while the pipeline is not connected.
   * <p>
   * Example: Push to agent on a pipeline whose last start failed throws CI_NOT_CONNECTED (HTTP 409) with the
   * reason, before any fix round is recorded.
   *
   * @throws ReportPortalException CI_NOT_CONNECTED when {@code canStartJobs} would be false
   */
  @Transactional(readOnly = true)
  public void assertCanStartJobs(Pipeline pipeline) {
    var effective = effective(pipeline);
    if (effective.state() != CiConnectionState.CONNECTED) {
      throw new ReportPortalException(ErrorType.CI_NOT_CONNECTED,
          effective.reason() == null ? effective.state().name() : effective.reason());
    }
  }

  /**
   * The integration a job of this pipeline starts with: the pipeline's own one, else the single enabled project
   * integration of the connector's type (the behaviour before pipelines owned their integration).
   *
   * @throws ReportPortalException BAD_REQUEST when there is none
   */
  @Transactional(readOnly = true)
  public Integration integrationFor(Pipeline pipeline, CiTriggerConnector connector) {
    var typeName = connector.getIntegrationTypeName();
    if (entry(pipeline) != null) {
      return ownedIntegration(pipeline)
          .filter(i -> i.getType() != null && typeName.equalsIgnoreCase(i.getType().getName()))
          .orElseThrow(() -> new ReportPortalException(ErrorType.BAD_REQUEST_ERROR,
              "No enabled '" + typeName + "' automation integration configured for this pipeline"));
    }
    return projectIntegration(pipeline.getProject(), typeName)
        .orElseThrow(() -> new ReportPortalException(ErrorType.BAD_REQUEST_ERROR,
            "No enabled '" + typeName + "' automation integration configured for this project"));
  }

  /**
   * The environments the pipeline's jobs may run against, the first being the default; empty when the pipeline has
   * no connection of its own or none are saved.
   * <p>
   * Example: a pipeline saved with {@code environments: ["beta5", "qa"]} gives {@code ["beta5", "qa"]}.
   */
  @Transactional(readOnly = true)
  public List<String> environmentsOf(Pipeline pipeline) {
    return ownedIntegration(pipeline)
        .map(integration -> stringList(integration.getParams().getParams().get(PARAM_ENVIRONMENTS)))
        .orElseGet(ArrayList::new);
  }

  /**
   * Keeps the outcome of a job start: a start that went through proves the connection, a refused one makes it
   * CONNECTION_FAILED until somebody runs "Test connection". Pipelines on the project integration have nowhere to
   * keep it and are left alone.
   *
   * @param pipelineId the pipeline the job was started for
   * @param failure    null when the CI accepted the start, otherwise why it did not
   */
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void recordJobStart(Long pipelineId, String failure) {
    pipelineRepository.findById(pipelineId).ifPresent(pipeline -> {
      var entry = entry(pipeline);
      var integrationId = entry == null ? null : asLong(entry.get("integrationId"));
      if (integrationId == null) {
        return;
      }
      if (failure == null) {
        writeEntry(pipeline, entryFor(integrationId, CiConnectionState.CONNECTED, null, null, Instant.now()));
      } else {
        writeEntry(pipeline, entryFor(integrationId, CiConnectionState.CONNECTION_FAILED,
            CiConnectionReasonCode.UNEXPECTED_RESPONSE, shortened(failure), Instant.now()));
      }
    });
  }

  // ---------------------------------------------------------------------------------------------------------------

  private PipelineCiConnectionRS toRS(Pipeline pipeline) {
    var effective = effective(pipeline);
    var builder = PipelineCiConnectionRS.builder()
        .provider(PROVIDER)
        .state(effective.state())
        .canStartJobs(effective.state() == CiConnectionState.CONNECTED)
        .reasonCode(effective.code())
        .reason(effective.reason())
        .checkedAt(effective.checkedAt());
    var integration = effective.integration();
    var params = integration == null || integration.getParams() == null || integration.getParams().getParams() == null
        ? Map.<String, Object>of() : integration.getParams().getParams();
    var projectPath = text(params.get(PARAM_PROJECT));
    var baseUrl = text(params.get(PARAM_URL));
    var effectiveBaseUrl = baseUrl == null ? DEFAULT_BASE_URL : baseUrl;
    return builder
        .repository(projectPath == null ? null : effectiveBaseUrl + "/" + projectPath)
        .branch(text(params.get(PARAM_REF)) == null ? DEFAULT_BRANCH : text(params.get(PARAM_REF)))
        .credentialSaved(text(params.get(PARAM_TOKEN)) != null)
        .jobs(stringMap(params.get(PARAM_JOBS)))
        .models(stringList(params.get(PARAM_MODELS)))
        .environments(stringList(params.get(PARAM_ENVIRONMENTS)))
        .build();
  }

  /**
   * The state a pipeline has right now, with the integration it comes from.
   */
  private Effective effective(Pipeline pipeline) {
    var entry = entry(pipeline);
    if (entry != null) {
      var integration = ownedIntegration(pipeline).orElse(null);
      if (integration == null || text(integration.getParams().getParams().get(PARAM_TOKEN)) == null) {
        return new Effective(CiConnectionState.NOT_CONNECTED, CiConnectionReasonCode.NO_CREDENTIAL,
            "No credential is saved", null, integration, true);
      }
      return new Effective(parseState(entry.get("state")), parseCode(entry.get("reasonCode")),
          text(entry.get("reason")), parseInstant(entry.get("checkedAt")), integration, true);
    }
    for (var typeName : LEGACY_TYPES) {
      var legacy = projectIntegration(pipeline.getProject(), typeName);
      if (legacy.isPresent()) {
        var missing = CiTriggerIntegrationService.missingRequiredParam(legacy.get());
        return missing.isPresent()
            ? new Effective(CiConnectionState.NOT_CONNECTED, CiConnectionReasonCode.NO_CREDENTIAL,
                "The project's " + typeName + " integration has no '" + missing.get() + "'", null, legacy.get(), false)
            : new Effective(CiConnectionState.CONNECTED, null, null, null, legacy.get(), false);
      }
    }
    return new Effective(CiConnectionState.NOT_CONNECTED, CiConnectionReasonCode.NO_CREDENTIAL,
        "No CI integration is configured", null, null, false);
  }

  /** The first enabled project integration of the type that does not belong to a pipeline. */
  private Optional<Integration> projectIntegration(Project project, String typeName) {
    return integrationRepository.findAllProjectByGroup(project, IntegrationGroupEnum.AUTOMATION).stream()
        .filter(i -> i.isEnabled() && i.getType() != null && typeName.equalsIgnoreCase(i.getType().getName()))
        .filter(i -> i.getParams() == null || i.getParams().getParams() == null
            || !i.getParams().getParams().containsKey(PARAM_OWNER_PIPELINE))
        .findFirst();
  }

  private Optional<Integration> ownedIntegration(Pipeline pipeline) {
    var entry = entry(pipeline);
    var id = entry == null ? null : asLong(entry.get("integrationId"));
    if (id == null) {
      return Optional.empty();
    }
    return integrationRepository.findByIdAndProjectId(id, pipeline.getProject().getId())
        .filter(i -> i.isEnabled() && i.getParams() != null && i.getParams().getParams() != null);
  }

  private void validate(Pipeline pipeline, PipelineCiConnectionRQ rq) {
    rq.getJobs().forEach((purpose, job) -> {
      if (blankToNull(purpose) == null || blankToNull(job) == null) {
        throw bad("Every job needs a purpose and a name");
      }
    });
    var generation = TestCaseReviewContributor.PIPELINE_TYPE.equals(pipeline.getType());
    if (generation && (!rq.getJobs().containsKey("generation") || !rq.getJobs().containsKey("fix"))) {
      throw bad("A Test case generation pipeline needs the jobs 'generation' and 'fix'");
    }
    if (generation && (rq.getModels() == null || rq.getModels().isEmpty())) {
      throw bad("A Test case generation pipeline needs at least one model");
    }
    if (TmsAutomationStartService.PIPELINE_TYPE.equals(pipeline.getType())
        && !rq.getJobs().containsKey("automation")) {
      throw bad("A Test automation pipeline needs the job 'automation'");
    }
    requireDistinctNames("models", rq.getModels());
    requireDistinctNames(PARAM_ENVIRONMENTS, rq.getEnvironments());
  }

  private void requireDistinctNames(String what, List<String> names) {
    if (names == null) {
      return;
    }
    var seen = new HashSet<String>();
    for (var name : names) {
      if (blankToNull(name) == null) {
        throw bad("The list of " + what + " has an empty entry");
      }
      if (!seen.add(name.trim())) {
        throw bad("The list of " + what + " has '" + name.trim() + "' twice");
      }
    }
  }

  private Pipeline findPipeline(Long projectId, Long pipelineId) {
    return pipelineRepository.findById(pipelineId)
        .filter(p -> p.getProject().getId().equals(projectId))
        .orElseThrow(() -> new ReportPortalException(ErrorType.NOT_FOUND, "Pipeline '" + pipelineId + "'"));
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> entry(Pipeline pipeline) {
    if (pipeline.getSettings() == null) {
      return null;
    }
    return pipeline.getSettings().getValues().get(SETTINGS_KEY) instanceof Map<?, ?> map
        ? (Map<String, Object>) map : null;
  }

  private void writeEntry(Pipeline pipeline, Map<String, Object> entry) {
    var settings = new LinkedHashMap<String, Object>(
        pipeline.getSettings() == null ? Map.of() : pipeline.getSettings().getValues());
    settings.put(SETTINGS_KEY, entry);
    pipeline.setSettings(new PipelineSettings(settings));
    pipelineRepository.save(pipeline);
  }

  private static Map<String, Object> entryFor(Long integrationId, CiConnectionState state,
      CiConnectionReasonCode code, String reason, Instant checkedAt) {
    var entry = new LinkedHashMap<String, Object>();
    entry.put("integrationId", integrationId);
    entry.put("state", state.name());
    if (code != null) {
      entry.put("reasonCode", code.name());
    }
    if (reason != null) {
      entry.put("reason", reason);
    }
    if (checkedAt != null) {
      entry.put("checkedAt", checkedAt.toString());
    }
    return entry;
  }

  private static CiConnectionState stateOfFailure(CiConnectionReasonCode code) {
    return code == CiConnectionReasonCode.NO_CREDENTIAL ? CiConnectionState.NOT_CONNECTED
        : CiConnectionState.CONNECTION_FAILED;
  }

  private static CiConnectionState parseState(Object value) {
    try {
      return value == null ? CiConnectionState.NOT_CONNECTED : CiConnectionState.valueOf(value.toString());
    } catch (IllegalArgumentException _) {
      return CiConnectionState.NOT_CONNECTED;
    }
  }

  private static CiConnectionReasonCode parseCode(Object value) {
    try {
      return value == null ? null : CiConnectionReasonCode.valueOf(value.toString());
    } catch (IllegalArgumentException _) {
      return null;
    }
  }

  private static Instant parseInstant(Object value) {
    try {
      return value == null ? null : Instant.parse(value.toString());
    } catch (RuntimeException _) {
      return null;
    }
  }

  private static Long asLong(Object value) {
    return value instanceof Number number ? number.longValue() : null;
  }

  private static String text(Object value) {
    return value instanceof String text && !text.isBlank() ? text : null;
  }

  private static String blankToNull(String value) {
    return value == null || value.isBlank() ? null : value;
  }

  private static String shortened(String text) {
    return text.length() <= 300 ? text : text.substring(0, 300) + "...";
  }

  private static Map<String, String> stringMap(Object value) {
    var result = new LinkedHashMap<String, String>();
    if (value instanceof Map<?, ?> map) {
      map.forEach((k, v) -> result.put(String.valueOf(k), String.valueOf(v)));
    }
    return result;
  }

  private static List<String> stringList(Object value) {
    var result = new ArrayList<String>();
    if (value instanceof List<?> list) {
      list.forEach(item -> result.add(String.valueOf(item)));
    }
    return result;
  }

  private static ReportPortalException bad(String message) {
    return new ReportPortalException(ErrorType.BAD_REQUEST_ERROR, message);
  }

  private record Effective(CiConnectionState state, CiConnectionReasonCode code, String reason, Instant checkedAt,
                           Integration integration, boolean owned) {
  }

  private record TestTarget(Integration integration, boolean owned) {
  }

  /**
   * A repository URL split into what the GitLab connector needs.
   * <p>
   * Example: {@code https://git.epam.com/EPM-RPP/rp-tests.git} -> base {@code https://git.epam.com}, project path
   * {@code EPM-RPP/rp-tests}.
   */
  record RepositoryRef(String baseUrl, String projectPath) {

    static RepositoryRef parse(String repository) {
      URI uri;
      try {
        uri = new URI(repository.trim());
      } catch (URISyntaxException _) {
        throw bad("The repository must be a URL, e.g. https://git.epam.com/EPM-RPP/rp-tests");
      }
      if (!"https".equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null) {
        throw bad("The repository must be an https URL, e.g. https://git.epam.com/EPM-RPP/rp-tests");
      }
      var path = stripSlashes(uri.getPath());
      if (path.endsWith(".git")) {
        path = path.substring(0, path.length() - 4);
      }
      if (path.isBlank()) {
        throw bad("The repository URL must contain the project path, e.g. https://git.epam.com/EPM-RPP/rp-tests");
      }
      return new RepositoryRef("https://" + uri.getAuthority(), path);
    }

    /**
     * The text without slashes at its start and end; no regex, so no anchored alternation to get wrong.
     * <p>
     * Example: {@code "/EPM-RPP/rp-tests//"} gives {@code "EPM-RPP/rp-tests"}, {@code null} gives {@code ""}.
     */
    private static String stripSlashes(String text) {
      if (text == null) {
        return "";
      }
      var start = 0;
      var end = text.length();
      while (start < end && text.charAt(start) == '/') {
        start++;
      }
      while (end > start && text.charAt(end - 1) == '/') {
        end--;
      }
      return text.substring(start, end);
    }
  }
}
