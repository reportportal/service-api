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
    tmsTestCaseQualityScoreRepository.deleteByTestCaseVersionId(version.getId());
    var now = Instant.now();
    var scores = qualityScores.stream()
        .map(rq -> {
          var criterion = tmsQualityStandardCriterionRepository
              .findByIdAndStandard_ProjectId(rq.getCriterionId(), projectId)
              .orElseThrow(() -> new ReportPortalException(ErrorType.NOT_FOUND,
                  "Quality standard criterion '" + rq.getCriterionId() + "' for project '" + projectId + "'"));
          var score = new TmsTestCaseQualityScore();
          score.setTestCaseVersion(version);
          score.setCriterion(criterion);
          score.setScore(rq.getScore());
          score.setEvaluatedAt(now);
          return score;
        })
        .collect(Collectors.toList());
    tmsTestCaseQualityScoreRepository.saveAll(scores);
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
