package com.epam.reportportal.base.core.tms.service;

import com.epam.reportportal.base.core.tms.dto.TmsTestCaseGenerationMetadataRQ;
import com.epam.reportportal.base.core.tms.dto.TmsTestCaseMetricsRS;
import com.epam.reportportal.base.core.tms.dto.TmsTestCaseQualityScoreRQ;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsQualityStandardCriterionRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsTestCaseGenerationMetadataRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsTestCaseQualityScoreRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsQualityStandardCriterion;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestCaseGenerationMetadata;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestCaseQualityScore;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestCaseVersion;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TmsTestCaseQualityServiceImpl implements TmsTestCaseQualityService {

  private final TmsTestCaseQualityScoreRepository tmsTestCaseQualityScoreRepository;
  private final TmsTestCaseGenerationMetadataRepository tmsTestCaseGenerationMetadataRepository;
  private final TmsQualityStandardCriterionRepository tmsQualityStandardCriterionRepository;

  @Override
  public boolean hasAiSignal(List<TmsTestCaseQualityScoreRQ> qualityScores, TmsTestCaseGenerationMetadataRQ generation) {
    return CollectionUtils.isNotEmpty(qualityScores) || generation != null;
  }

  @Override
  public void applyQualityScores(Long projectId, TmsTestCaseVersion version, List<TmsTestCaseQualityScoreRQ> qualityScores) {
    if (qualityScores == null) {
      return;
    }
    var resolved = qualityScores.stream()
        .map(rq -> Map.entry(resolveCriterion(projectId, rq), rq))
        .toList();
    validateNoDuplicateCriteria(resolved.stream().map(entry -> entry.getKey().getId()).toList());
    tmsTestCaseQualityScoreRepository.deleteByTestCaseVersionId(version.getId());
    var now = Instant.now();
    var scores = resolved.stream()
        .map(entry -> {
          var criterion = entry.getKey();
          var rq = entry.getValue();
          validateScoreInRange(rq, criterion);
          var score = new TmsTestCaseQualityScore();
          score.setTestCaseVersion(version);
          score.setCriterion(criterion);
          score.setScore(rq.getScore());
          score.setEvaluatedAt(now);
          // the score points at the iteration that produced the version it grades, when there is one
          score.setPipelineIterationId(version.getPipelineIterationId());
          return score;
        })
        .collect(Collectors.toList());
    tmsTestCaseQualityScoreRepository.saveAll(scores);
  }

  /**
   * A score names its criterion either by id or by the criterion's stable key - exactly one.
   */
  private TmsQualityStandardCriterion resolveCriterion(Long projectId, TmsTestCaseQualityScoreRQ rq) {
    var hasId = rq.getCriterionId() != null;
    var hasKey = rq.getCriterionKey() != null && !rq.getCriterionKey().isBlank();
    if (hasId == hasKey) {
      throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR,
          "Exactly one of 'criterionId' and 'criterionKey' must be set for each quality score");
    }
    if (hasId) {
      return tmsQualityStandardCriterionRepository
          .findByIdAndStandard_ProjectId(rq.getCriterionId(), projectId)
          .orElseThrow(() -> new ReportPortalException(ErrorType.NOT_FOUND,
              "Quality standard criterion '" + rq.getCriterionId() + "' for project '" + projectId + "'"));
    }
    return tmsQualityStandardCriterionRepository
        .findByKeyAndStandard_ProjectId(rq.getCriterionKey(), projectId)
        .orElseThrow(() -> new ReportPortalException(ErrorType.NOT_FOUND,
            "Quality standard criterion with key '" + rq.getCriterionKey() + "' for project '" + projectId + "'"));
  }

  private void validateNoDuplicateCriteria(List<Long> criterionIds) {
    var duplicateCriterionIds = criterionIds.stream()
        .collect(Collectors.groupingBy(id -> id, Collectors.counting()))
        .entrySet().stream()
        .filter(entry -> entry.getValue() > 1)
        .map(Map.Entry::getKey)
        .toList();
    if (!duplicateCriterionIds.isEmpty()) {
      throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR,
          "Duplicate criteria in qualityScores: " + duplicateCriterionIds);
    }
  }

  private void validateScoreInRange(TmsTestCaseQualityScoreRQ rq, TmsQualityStandardCriterion criterion) {
    if (rq.getScore() < 0 || rq.getScore() > criterion.getMaxPoints()) {
      throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR,
          "Score " + rq.getScore() + " for criterion '" + criterion.getId()
              + "' must be within [0, " + criterion.getMaxPoints() + "]");
    }
  }

  @Override
  public void applyGenerationMetadata(TmsTestCaseVersion version, TmsTestCaseGenerationMetadataRQ generation) {
    if (generation == null) {
      return;
    }
    var metadata = tmsTestCaseGenerationMetadataRepository.findByTestCaseVersionId(version.getId())
        .orElseGet(TmsTestCaseGenerationMetadata::new);
    metadata.setTestCaseVersion(version);
    metadata.setTokensIn(generation.getTokensIn());
    metadata.setTokensOut(generation.getTokensOut());
    metadata.setModel(generation.getModel());
    metadata.setSkill(generation.getSkill());
    metadata.setCostUsd(generation.getCostUsd());
    tmsTestCaseGenerationMetadataRepository.save(metadata);
  }

  @Override
  public TmsTestCaseMetricsRS buildMetrics(TmsTestCaseVersion version) {
    if (version == null) {
      return null;
    }
    return buildMetricsBatch(List.of(version)).get(version.getId());
  }

  @Override
  public Map<Long, TmsTestCaseMetricsRS> buildMetricsBatch(Collection<TmsTestCaseVersion> versions) {
    var versionsById = versions.stream()
        .filter(Objects::nonNull)
        .collect(Collectors.toMap(TmsTestCaseVersion::getId, Function.identity(), (first, second) -> first));
    if (versionsById.isEmpty()) {
      return Map.of();
    }

    var versionIds = versionsById.keySet();
    var scoresByVersionId = tmsTestCaseQualityScoreRepository.findByTestCaseVersionIdIn(versionIds).stream()
        .collect(Collectors.groupingBy(score -> score.getTestCaseVersion().getId()));
    var generationByVersionId = tmsTestCaseGenerationMetadataRepository.findByTestCaseVersionIdIn(versionIds).stream()
        .collect(Collectors.toMap(metadata -> metadata.getTestCaseVersion().getId(), Function.identity()));

    var result = new HashMap<Long, TmsTestCaseMetricsRS>();
    for (var versionId : versionIds) {
      var metrics = buildMetrics(versionsById.get(versionId),
          scoresByVersionId.getOrDefault(versionId, List.of()),
          generationByVersionId.get(versionId));
      if (metrics != null) {
        result.put(versionId, metrics);
      }
    }
    return result;
  }

  private TmsTestCaseMetricsRS buildMetrics(TmsTestCaseVersion version, List<TmsTestCaseQualityScore> scores,
      TmsTestCaseGenerationMetadata generation) {
    if (scores.isEmpty() && generation == null) {
      return null;
    }

    var builder = TmsTestCaseMetricsRS.builder();
    if (!scores.isEmpty()) {
      var overallScore = scores.stream().mapToInt(TmsTestCaseQualityScore::getScore).sum();
      var evaluatedAt = scores.stream().map(TmsTestCaseQualityScore::getEvaluatedAt)
          .max(Instant::compareTo).orElse(null);
      var criteria = scores.stream()
          .map(score -> TmsTestCaseMetricsRS.CriterionScore.builder()
              .criterionId(score.getCriterion().getId())
              .name(score.getCriterion().getName())
              .maxPoints(score.getCriterion().getMaxPoints())
              .score(score.getScore())
              .build())
          .collect(Collectors.toList());
      var obsolete = evaluatedAt != null && version.getUpdatedAt() != null
          && version.getUpdatedAt().isAfter(evaluatedAt);
      builder.overallScore(overallScore)
          .evaluatedAt(evaluatedAt == null ? null : evaluatedAt.toEpochMilli())
          .obsolete(obsolete)
          .criteria(criteria);
    }
    if (generation != null) {
      builder.tokensIn(generation.getTokensIn())
          .tokensOut(generation.getTokensOut())
          .model(generation.getModel())
          .skill(generation.getSkill())
          .costUsd(generation.getCostUsd());
    }
    return builder.build();
  }
}
