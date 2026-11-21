package com.epam.reportportal.base.core.tms.pipeline;

import com.epam.reportportal.base.core.aifactory.enums.StatusCategory;
import com.epam.reportportal.base.core.aifactory.service.PipelineCiConnectionService;
import com.epam.reportportal.base.core.tms.dto.TmsAutomationIterationRefRS;
import com.epam.reportportal.base.core.tms.dto.TmsAutomationStateRS;
import com.epam.reportportal.base.core.tms.dto.TmsAutomationStatus;
import com.epam.reportportal.base.core.tms.service.TmsTestCaseVersionService;
import com.epam.reportportal.base.infrastructure.persistence.dao.ProjectRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.aifactory.PipelineIterationRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.aifactory.PipelineRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsTestCaseAutomationRunRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsTestCaseFixRoundRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.Pipeline;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineIteration;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestCaseAutomationRun;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestCaseVersion;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.enums.TmsFixRoundStatus;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Where Test Cases stand in automation, derived from the automation run of the case and the iteration it belongs to.
 * <p>
 * Example: TC42 has a run in iteration #8. While #8 is pending, running or waiting the case is {@code IN_PROGRESS};
 * when #8 passes it is {@code AUTOMATED}; when #8 fails, times out or is stopped it is {@code FAILED}. A case
 * without a run is {@code NOT_AUTOMATED}. Nothing is stored: reading the iteration is enough, so there is no job
 * that has to keep the case in step.
 * <p>
 * The state is only published for projects that have a pipeline at all; elsewhere the Test Case does not mention
 * automation.
 */
@Service
@RequiredArgsConstructor
public class TmsAutomationStateService {

  private final TmsTestCaseAutomationRunRepository runRepository;
  private final PipelineIterationRepository iterationRepository;
  private final PipelineRepository pipelineRepository;
  private final ProjectRepository projectRepository;
  private final TmsTestCaseFixRoundRepository fixRoundRepository;
  private final TmsTestCaseVersionService versionService;
  private final PipelineCiConnectionService ciConnectionService;

  /**
   * The latest run of every given case that has one, with the iteration it belongs to.
   */
  @Transactional(readOnly = true)
  public Map<Long, LatestRun> latestRuns(Collection<Long> testCaseIds) {
    var result = new HashMap<Long, LatestRun>();
    if (testCaseIds == null || testCaseIds.isEmpty()) {
      return result;
    }
    var runs = runRepository.findAllByTestCaseIdsLatestFirst(testCaseIds);
    var iterations = new HashMap<Long, PipelineIteration>();
    iterationRepository.findAllById(runs.stream().map(TmsTestCaseAutomationRun::getIterationId).distinct().toList())
        .forEach(iteration -> iterations.put(iteration.getId(), iteration));
    for (var run : runs) {
      var iteration = iterations.get(run.getIterationId());
      if (iteration != null) {
        result.putIfAbsent(run.getTestCaseId(), new LatestRun(run, iteration));
      }
    }
    return result;
  }

  /**
   * The status a run's iteration implies; no run is {@code NOT_AUTOMATED}.
   */
  public static TmsAutomationStatus statusOf(LatestRun latest) {
    if (latest == null) {
      return TmsAutomationStatus.NOT_AUTOMATED;
    }
    var category = latest.iteration().getStatusCategory();
    if (category == StatusCategory.PASSED) {
      return TmsAutomationStatus.AUTOMATED;
    }
    return category != null && category.endedUnsuccessfully() ? TmsAutomationStatus.FAILED
        : TmsAutomationStatus.IN_PROGRESS;
  }

  /**
   * The automation block of each given case, or an empty map when the project has no pipeline.
   *
   * @return state by test case id
   */
  @Transactional(readOnly = true)
  public Map<Long, TmsAutomationStateRS> statesFor(long projectId, Collection<Long> testCaseIds) {
    var result = new HashMap<Long, TmsAutomationStateRS>();
    if (testCaseIds == null || testCaseIds.isEmpty() || !pipelineRepository.existsByProjectId(projectId)) {
      return result;
    }
    var ids = List.copyOf(new HashSet<>(testCaseIds));
    var latestByCase = latestRuns(ids);
    var fixing = new HashSet<>(fixRoundRepository.findTestCaseIdsWithStatus(ids, TmsFixRoundStatus.PENDING));
    var currentVersions = versionService.getDefaultVersions(
        latestByCase.keySet().stream().toList());
    var ciState = ciConnectionService.summary(automationPipelineOrTransient(projectId)).getState();

    for (var id : ids) {
      var latest = latestByCase.get(id);
      var builder = TmsAutomationStateRS.builder()
          .ciConnection(ciState)
          .fixInProgress(fixing.contains(id))
          .status(statusOf(latest));
      if (latest != null) {
        builder.scenarioChangedAfterAutomation(scenarioChanged(latest.run(), currentVersions.get(id)))
            .iteration(TmsAutomationIterationRefRS.builder()
                .pipelineId(latest.iteration().getPipeline().getId())
                .iterationId(latest.iteration().getId())
                .number(latest.iteration().getIterationNumber())
                .build());
      }
      result.put(id, builder.build());
    }
    return result;
  }

  /**
   * The Test automation pipeline of the project, or an unsaved stand-in that has no connection of its own, so the
   * answer is what the project's CI integration says until the pipeline exists.
   */
  public Pipeline automationPipelineOrTransient(long projectId) {
    return pipelineRepository.findFirstByProjectIdAndTypeOrderByIdAsc(projectId, TmsAutomationStartService.PIPELINE_TYPE)
        .orElseGet(() -> {
          var standIn = new Pipeline();
          standIn.setProject(projectRepository.getReferenceById(projectId));
          return standIn;
        });
  }

  private static boolean scenarioChanged(TmsTestCaseAutomationRun run, TmsTestCaseVersion current) {
    return run.getTestCaseVersionId() != null && current != null
        && !Objects.equals(run.getTestCaseVersionId(), current.getId());
  }

  /**
   * A run and the iteration it belongs to.
   */
  public record LatestRun(TmsTestCaseAutomationRun run, PipelineIteration iteration) {
  }
}
