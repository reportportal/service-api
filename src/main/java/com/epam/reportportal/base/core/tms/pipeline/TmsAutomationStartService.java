package com.epam.reportportal.base.core.tms.pipeline;

import static com.epam.reportportal.base.infrastructure.rules.exception.ErrorType.BAD_REQUEST_ERROR;
import static com.epam.reportportal.base.infrastructure.rules.exception.ErrorType.NOT_FOUND;

import com.epam.reportportal.base.core.aifactory.connector.CiRunRequest;
import com.epam.reportportal.base.core.aifactory.connector.CiTriggerConnector;
import com.epam.reportportal.base.core.aifactory.dto.IterationRQ;
import com.epam.reportportal.base.core.aifactory.dto.PipelineIterationRQ;
import com.epam.reportportal.base.core.aifactory.enums.AttributeType;
import com.epam.reportportal.base.core.aifactory.model.PipelineAttribute;
import com.epam.reportportal.base.core.aifactory.service.PipelineCiConnectionService;
import com.epam.reportportal.base.core.aifactory.service.PipelineIterationService;
import com.epam.reportportal.base.core.tms.dto.TmsAutomationEnvironmentRS;
import com.epam.reportportal.base.core.tms.dto.TmsAutomationIterationRefRS;
import com.epam.reportportal.base.core.tms.dto.TmsAutomationSkipReason;
import com.epam.reportportal.base.core.tms.dto.TmsAutomationSkippedRS;
import com.epam.reportportal.base.core.tms.dto.TmsAutomationStartRQ;
import com.epam.reportportal.base.core.tms.dto.TmsAutomationStartRS;
import com.epam.reportportal.base.core.tms.dto.TmsAutomationStatus;
import com.epam.reportportal.base.core.tms.service.TmsTestCaseVersionService;
import com.epam.reportportal.base.infrastructure.persistence.dao.aifactory.PipelineIterationRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.aifactory.PipelineRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsTestCaseAutomationRunRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsTestCaseFixRoundRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsTestCaseRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.Pipeline;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.Integration;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestCase;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestCaseAutomationRun;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.enums.TmsFixRoundStatus;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.enums.TmsTestCaseStatus;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * "Automate": sends Ready Test Cases to the Test automation pipeline by creating ONE iteration for all accepted
 * cases and starting the CI job for it.
 * <p>
 * Example: the editor selects TC42, TC43 and TC44 and picks {@code beta5}. TC43 became Draft a moment ago and
 * TC44 is already in an automation iteration, so they are skipped ({@code NOT_READY}, {@code AUTOMATION_IN_PROGRESS}).
 * For TC42 the call creates iteration #8 of "Test automation" (Pending, started by the editor), records the run
 * and starts the job with {@code TEST_CASES=TC42} (display ids, never internal ids: the job also serves cases made
 * by hand), {@code ENVIRONMENT=beta5}, {@code ITERATION_ID=452}, {@code PIPELINE_ID=17}. The job reports its stages into iteration 452 itself and does not create an iteration.
 * <p>
 * Deliberately not one transaction, like Push to agent: the CI call in the middle must not run inside a database
 * transaction. The iteration and the runs are recorded first (a second Automate then sees them), the job is started,
 * and everything recorded is deleted again when the CI refuses, so no empty iteration is left behind. The Test
 * automation pipeline row is locked while the cases are checked and recorded, so two Automate calls of one project
 * never both accept the same case.
 */
@Service
public class TmsAutomationStartService {

  /** Kind of the pipeline the automation iterations belong to. */
  public static final String PIPELINE_TYPE = "TEST_AUTOMATION";
  static final String PIPELINE_NAME = "Test automation";
  static final String INPUT_TEST_CASES = "TEST_CASES";
  static final String INPUT_ENVIRONMENT = "ENVIRONMENT";
  static final String INPUT_ITERATION_ID = "ITERATION_ID";
  static final String INPUT_PIPELINE_ID = "PIPELINE_ID";
  private static final String CI_INTEGRATION_TYPE = "gitlab-ci";

  private final TmsTestCaseRepository testCaseRepository;
  private final TmsTestCaseFixRoundRepository fixRoundRepository;
  private final TmsTestCaseAutomationRunRepository runRepository;
  private final TmsTestCaseVersionService versionService;
  private final TmsAutomationStateService stateService;
  private final PipelineRepository pipelineRepository;
  private final PipelineIterationRepository iterationRepository;
  private final PipelineIterationService iterationService;
  private final PipelineCiConnectionService ciConnectionService;
  private final TmsAutomationPipelineProvider pipelineProvider;
  private final Map<String, CiTriggerConnector> connectorsByIntegrationType;
  private final TransactionTemplate transactionTemplate;

  public TmsAutomationStartService(TmsTestCaseRepository testCaseRepository,
      TmsTestCaseFixRoundRepository fixRoundRepository, TmsTestCaseAutomationRunRepository runRepository,
      TmsTestCaseVersionService versionService, TmsAutomationStateService stateService,
      PipelineRepository pipelineRepository, PipelineIterationRepository iterationRepository,
      PipelineIterationService iterationService, PipelineCiConnectionService ciConnectionService,
      TmsAutomationPipelineProvider pipelineProvider, List<CiTriggerConnector> connectors,
      PlatformTransactionManager transactionManager) {
    this.testCaseRepository = testCaseRepository;
    this.fixRoundRepository = fixRoundRepository;
    this.runRepository = runRepository;
    this.versionService = versionService;
    this.stateService = stateService;
    this.pipelineRepository = pipelineRepository;
    this.iterationRepository = iterationRepository;
    this.iterationService = iterationService;
    this.ciConnectionService = ciConnectionService;
    this.pipelineProvider = pipelineProvider;
    this.connectorsByIntegrationType = connectors.stream()
        .collect(Collectors.toMap(CiTriggerConnector::getIntegrationTypeName, c -> c, (a, b) -> a));
    this.transactionTemplate = new TransactionTemplate(transactionManager);
    this.transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
  }

  /**
   * What an Automate call came to.
   */
  public sealed interface Outcome permits Started, ConfirmRequired, NothingEligible {
  }

  /** The job was started; the iteration holds the accepted cases. */
  public record Started(TmsAutomationStartRS response) implements Outcome {
  }

  /** Some cases are already automated and the user has not confirmed them; nothing was started. */
  public record ConfirmRequired(List<Long> testCaseIds) implements Outcome {
  }

  /** Every submitted case is ineligible; nothing was started. */
  public record NothingEligible(List<TmsAutomationSkippedRS> skipped) implements Outcome {
  }

  /**
   * The environments of the Test automation pipeline's CI connection, the first being the default.
   *
   * @throws ReportPortalException AUTOMATION_ENVIRONMENTS_NOT_CONFIGURED (400) when none is configured
   */
  public TmsAutomationEnvironmentRS getEnvironments(long projectId) {
    var pipeline = pipelineRepository.findFirstByProjectIdAndTypeOrderByIdAsc(projectId, PIPELINE_TYPE).orElse(null);
    var environments = pipeline == null ? List.<String>of() : ciConnectionService.environmentsOf(pipeline);
    requireEnvironments(environments);
    return TmsAutomationEnvironmentRS.builder()
        .environments(environments)
        .defaultEnvironment(environments.get(0))
        .build();
  }

  /**
   * Starts automation for the accepted cases.
   *
   * @return {@link Started}, or {@link ConfirmRequired} / {@link NothingEligible} when nothing was started
   * @throws ReportPortalException NOT_FOUND for an id that is not a Test Case of the project, CI_NOT_CONNECTED
   *                               while the pipeline cannot start jobs, BAD_REQUEST for an unknown environment or
   *                               when the CI refuses the start (then nothing stays recorded)
   */
  public Outcome start(long projectId, Long userId, TmsAutomationStartRQ rq) {
    var claim = transactionTemplate.execute(status -> claim(projectId, userId, rq));
    if (claim.outcome() != null) {
      return claim.outcome();
    }
    var claimed = claim.claimed();

    String workflowUrl;
    try {
      workflowUrl = claimed.connector().triggerRun(claimed.integration(), claimed.runRequest());
    } catch (RuntimeException e) {
      transactionTemplate.executeWithoutResult(status -> iterationRepository.deleteById(claimed.iterationId()));
      ciConnectionService.recordJobStart(claimed.pipelineId(), String.valueOf(e.getMessage()));
      throw e;
    }
    ciConnectionService.recordJobStart(claimed.pipelineId(), null);
    transactionTemplate.executeWithoutResult(status -> {
      var runs = runRepository.findByIterationId(claimed.iterationId());
      runs.forEach(run -> run.setWorkflowUrl(workflowUrl));
      runRepository.saveAll(runs);
    });
    return new Started(claimed.response());
  }

  /**
   * The same rule for the list of environments and for the start: without a configured list nothing can be offered
   * or sent, and the client gets the machine code {@code AUTOMATION_ENVIRONMENTS_NOT_CONFIGURED} (40060).
   */
  private static void requireEnvironments(List<String> environments) {
    if (environments.isEmpty()) {
      throw new ReportPortalException(ErrorType.AUTOMATION_ENVIRONMENTS_NOT_CONFIGURED,
          "Set the environments in the CI connection of the Test automation pipeline (or in the project CI "
              + "integration, parameter 'environments')");
    }
  }

  private Claim claim(long projectId, Long userId, TmsAutomationStartRQ rq) {
    var ids = new ArrayList<>(new LinkedHashSet<>(rq.getTestCaseIds()));
    var found = testCaseRepository.findByProjectIdAndIds(projectId, ids).stream()
        .collect(Collectors.toMap(TmsTestCase::getId, c -> c, (first, ignored) -> first));
    for (var id : ids) {
      if (!found.containsKey(id)) {
        throw new ReportPortalException(NOT_FOUND, "Test Case '" + id + "'");
      }
    }

    var pipeline = pipelineProvider.getOrCreate(projectId);
    // one Automate of the project at a time: the checks below and the records they lead to are one step
    var locked = pipelineRepository.findByIdForUpdate(pipeline.getId()).orElseThrow();
    ciConnectionService.assertCanStartJobs(locked);
    var environments = ciConnectionService.environmentsOf(locked);
    requireEnvironments(environments);
    if (!environments.contains(rq.getEnvironment())) {
      throw new ReportPortalException(BAD_REQUEST_ERROR,
          "Environment '" + rq.getEnvironment() + "' is not one of " + environments);
    }
    var connector = connectorsByIntegrationType.get(CI_INTEGRATION_TYPE);
    if (connector == null) {
      throw new ReportPortalException(BAD_REQUEST_ERROR, "Test automation runs in GitLab CI, which is not available");
    }
    var integration = ciConnectionService.integrationFor(locked, connector);

    var triage = triage(ids, found);
    var accepted = triage.accepted();
    var skipped = triage.skipped();
    if (accepted.isEmpty()) {
      return new Claim(new NothingEligible(skipped), null);
    }
    if (!triage.needConfirmation().isEmpty() && !isConfirmed(rq, triage.needConfirmation())) {
      return new Claim(new ConfirmRequired(triage.needConfirmation()), null);
    }

    var iteration = iterationService.createPipelineIteration(projectId, userId,
        PipelineIterationRQ.builder()
            .name(locked.getName())
            .type(PIPELINE_TYPE)
            .iteration(IterationRQ.builder()
                .startedAt(Instant.now())
                .description("Automation of " + accepted.size() + " Test Case(s)")
                .attributes(List.of(PipelineAttribute.builder()
                    .key("environment").label("Environment").value(rq.getEnvironment()).type(AttributeType.TAG)
                    .build()))
                .build())
            .build(), null).iteration();
    var versions = versionService.getDefaultVersions(accepted);
    var now = Instant.now();
    for (var id : accepted) {
      var run = new TmsTestCaseAutomationRun();
      run.setTestCaseId(id);
      run.setProjectId(projectId);
      run.setIterationId(iteration.getId());
      run.setEnvironment(rq.getEnvironment());
      run.setTestCaseVersionId(versions.get(id) == null ? null : versions.get(id).getId());
      run.setRequestedBy(userId);
      run.setRequestedAt(now);
      runRepository.save(run);
    }

    var inputs = new LinkedHashMap<String, String>();
    // the job takes the human display ids (TC107), never the internal ids: it also serves cases created by hand
    inputs.put(INPUT_TEST_CASES, accepted.stream().map(id -> found.get(id).getDisplayId())
        .collect(Collectors.joining(",")));
    inputs.put(INPUT_ENVIRONMENT, rq.getEnvironment());
    inputs.put(INPUT_ITERATION_ID, String.valueOf(iteration.getId()));
    inputs.put(INPUT_PIPELINE_ID, String.valueOf(locked.getId()));
    var repo = projectOf(integration);
    var runRequest = new CiRunRequest(repo, null, inputs);
    var response = TmsAutomationStartRS.builder()
        .iteration(TmsAutomationIterationRefRS.builder()
            .pipelineId(locked.getId()).iterationId(iteration.getId()).number(iteration.getIterationNumber()).build())
        .accepted(accepted)
        .skipped(skipped)
        .build();
    return new Claim(null, new Claimed(locked.getId(), iteration.getId(), connector, integration, runRequest,
        response));
  }

  /**
   * Splits the submitted cases into the ones to send, the ones to skip (with the reason) and, among those to send,
   * the ones already automated that the user has to confirm.
   * <p>
   * Example: 42 is Ready and never sent, 43 is Draft, 44 is automated: accepted [42, 44], skipped [43 NOT_READY],
   * needConfirmation [44].
   */
  private Triage triage(List<Long> ids, Map<Long, TmsTestCase> found) {
    var fixing = new HashSet<>(fixRoundRepository.findTestCaseIdsWithStatus(ids, TmsFixRoundStatus.PENDING));
    var latestRuns = stateService.latestRuns(ids);
    var accepted = new ArrayList<Long>();
    var skipped = new ArrayList<TmsAutomationSkippedRS>();
    var needConfirmation = new ArrayList<Long>();
    for (var id : ids) {
      var testCase = found.get(id);
      var status = TmsAutomationStateService.statusOf(latestRuns.get(id));
      var reason = skipReason(testCase, fixing.contains(id), status);
      if (reason != null) {
        skipped.add(TmsAutomationSkippedRS.builder().id(id).displayId(testCase.getDisplayId()).reason(reason).build());
      } else {
        accepted.add(id);
        if (status == TmsAutomationStatus.AUTOMATED) {
          needConfirmation.add(id);
        }
      }
    }
    return new Triage(accepted, skipped, needConfirmation);
  }

  private static TmsAutomationSkipReason skipReason(TmsTestCase testCase, boolean fixRunning,
      TmsAutomationStatus status) {
    if (testCase.getStatus() != TmsTestCaseStatus.READY) {
      return TmsAutomationSkipReason.NOT_READY;
    }
    if (fixRunning) {
      return TmsAutomationSkipReason.FIX_RUNNING;
    }
    return status == TmsAutomationStatus.IN_PROGRESS ? TmsAutomationSkipReason.AUTOMATION_IN_PROGRESS : null;
  }

  private static boolean isConfirmed(TmsAutomationStartRQ rq, List<Long> needConfirmation) {
    Set<Long> confirmed = rq.getConfirmedTestCaseIds() == null ? Set.of() : new HashSet<>(rq.getConfirmedTestCaseIds());
    return rq.isConfirmReautomate() && confirmed.containsAll(needConfirmation);
  }

  /**
   * The GitLab project the integration is set up for; a trigger for any other project is refused by the connector.
   */
  private static String projectOf(Integration integration) {
    var params = integration.getParams() == null ? null : integration.getParams().getParams();
    return params != null && params.get("projectId") instanceof String project ? project : null;
  }

  private record Claim(Outcome outcome, Claimed claimed) {
  }

  private record Triage(List<Long> accepted, List<TmsAutomationSkippedRS> skipped, List<Long> needConfirmation) {
  }

  private record Claimed(Long pipelineId, Long iterationId, CiTriggerConnector connector, Integration integration,
                         CiRunRequest runRequest, TmsAutomationStartRS response) {
  }
}
