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
import java.util.List;
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
    Instant now = Instant.now();
    List<TmsTestCaseQualityScore> scores = qualityScores.stream()
        .map(rq -> {
          TmsQualityStandardCriterion criterion = tmsQualityStandardCriterionRepository
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
    List<TmsTestCaseQualityScore> scores = tmsTestCaseQualityScoreRepository.findByTestCaseVersionId(version.getId());
    var generation = tmsTestCaseGenerationMetadataRepository.findByTestCaseVersionId(version.getId()).orElse(null);
    if (scores.isEmpty() && generation == null) {
      return null;
    }

    var builder = TmsTestCaseMetricsRS.builder();
    if (!scores.isEmpty()) {
      int overallScore = scores.stream().mapToInt(TmsTestCaseQualityScore::getScore).sum();
      Instant evaluatedAt = scores.stream().map(TmsTestCaseQualityScore::getEvaluatedAt)
          .max(Instant::compareTo).orElse(null);
      List<TmsTestCaseMetricsRS.CriterionScore> criteria = scores.stream()
          .map(score -> TmsTestCaseMetricsRS.CriterionScore.builder()
              .criterionId(score.getCriterion().getId())
              .name(score.getCriterion().getName())
              .maxPoints(score.getCriterion().getMaxPoints())
              .score(score.getScore())
              .build())
          .collect(Collectors.toList());
      builder.overallScore(overallScore)
          .evaluatedAt(evaluatedAt == null ? null : evaluatedAt.toEpochMilli())
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
