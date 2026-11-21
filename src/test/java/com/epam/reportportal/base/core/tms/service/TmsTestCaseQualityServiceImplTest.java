package com.epam.reportportal.base.core.tms.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.reportportal.base.core.tms.dto.TmsTestCaseGenerationMetadataRQ;
import com.epam.reportportal.base.core.tms.dto.TmsTestCaseQualityScoreRQ;
import com.epam.reportportal.base.infrastructure.persistence.dao.aifactory.PipelineIterationRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.PipelineStageTestCaseRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsQualityStandardCriterionRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestCase;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineIteration;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsTestCaseGenerationMetadataRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsTestCaseQualityScoreRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsQualityStandardCriterion;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestCaseGenerationMetadata;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestCaseQualityScore;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestCaseVersion;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TmsTestCaseQualityServiceImplTest {

  @Mock
  private TmsTestCaseQualityScoreRepository tmsTestCaseQualityScoreRepository;
  @Mock
  private TmsTestCaseGenerationMetadataRepository tmsTestCaseGenerationMetadataRepository;
  @Mock
  private TmsQualityStandardCriterionRepository tmsQualityStandardCriterionRepository;

  @Mock
  private PipelineIterationRepository pipelineIterationRepository;
  @Mock
  private PipelineStageTestCaseRepository stageTestCaseRepository;

  @InjectMocks
  private TmsTestCaseQualityServiceImpl sut;

  private Long projectId;
  private TmsTestCaseVersion version;
  private TmsQualityStandardCriterion criterion;

  @BeforeEach
  void setUp() {
    projectId = 1L;

    version = new TmsTestCaseVersion();
    version.setId(10L);

    criterion = new TmsQualityStandardCriterion();
    criterion.setId(20L);
    criterion.setMaxPoints(25);
  }

  @Test
  void applyQualityScores_WithScoreWithinRange_ShouldSave() {
    var rq = TmsTestCaseQualityScoreRQ.builder().criterionId(20L).score(20).build();

    when(tmsQualityStandardCriterionRepository.findByIdAndStandard_ProjectId(20L, projectId))
        .thenReturn(Optional.of(criterion));

    sut.applyQualityScores(projectId, version, List.of(rq));

    verify(tmsTestCaseQualityScoreRepository).deleteByTestCaseVersionId(version.getId());
    verify(tmsTestCaseQualityScoreRepository).saveAll(any());
  }

  @Test
  void applyQualityScores_WithScoreAboveCriterionMax_ShouldThrowBadRequest() {
    var rq = TmsTestCaseQualityScoreRQ.builder().criterionId(20L).score(26).build();

    when(tmsQualityStandardCriterionRepository.findByIdAndStandard_ProjectId(20L, projectId))
        .thenReturn(Optional.of(criterion));

    assertThrows(ReportPortalException.class, () -> sut.applyQualityScores(projectId, version, List.of(rq)));
    verify(tmsTestCaseQualityScoreRepository, never()).saveAll(any());
  }

  @Test
  void applyQualityScores_WithNegativeScore_ShouldThrowBadRequest() {
    var rq = TmsTestCaseQualityScoreRQ.builder().criterionId(20L).score(-1).build();

    when(tmsQualityStandardCriterionRepository.findByIdAndStandard_ProjectId(20L, projectId))
        .thenReturn(Optional.of(criterion));

    assertThrows(ReportPortalException.class, () -> sut.applyQualityScores(projectId, version, List.of(rq)));
    verify(tmsTestCaseQualityScoreRepository, never()).saveAll(any());
  }

  @Test
  void applyQualityScores_WithDuplicateCriterionId_ShouldThrowBadRequestBeforeDeleting() {
    var rq1 = TmsTestCaseQualityScoreRQ.builder().criterionId(20L).score(10).build();
    var rq2 = TmsTestCaseQualityScoreRQ.builder().criterionId(20L).score(15).build();

    when(tmsQualityStandardCriterionRepository.findByIdAndStandard_ProjectId(20L, projectId))
        .thenReturn(Optional.of(criterion));

    assertThrows(ReportPortalException.class,
        () -> sut.applyQualityScores(projectId, version, List.of(rq1, rq2)));
    verify(tmsTestCaseQualityScoreRepository, never()).deleteByTestCaseVersionId(any());
    verify(tmsTestCaseQualityScoreRepository, never()).saveAll(any());
  }

  @Test
  void applyQualityScores_WithCriterionKey_ShouldResolveCriterionByKey() {
    criterion.setKey("clear_steps");
    var rq = TmsTestCaseQualityScoreRQ.builder().criterionKey("clear_steps").score(20).build();

    when(tmsQualityStandardCriterionRepository.findByKeyAndStandard_ProjectId("clear_steps", projectId))
        .thenReturn(Optional.of(criterion));

    sut.applyQualityScores(projectId, version, List.of(rq));

    verify(tmsTestCaseQualityScoreRepository).saveAll(any());
  }

  @Test
  void applyQualityScores_WithUnknownCriterionKeys_ShouldAnswerBadRequestListingThemAndTheKnownKeys() {
    var good = TmsTestCaseQualityScoreRQ.builder().criterionKey("clear_steps").score(20).build();
    var wrong = TmsTestCaseQualityScoreRQ.builder().criterionKey("expected_result").score(1).build();
    var wrongToo = TmsTestCaseQualityScoreRQ.builder().criterionKey("nope").score(1).build();
    criterion.setKey("clear_steps");

    when(tmsQualityStandardCriterionRepository.findByKeyAndStandard_ProjectId("clear_steps", projectId))
        .thenReturn(Optional.of(criterion));
    when(tmsQualityStandardCriterionRepository.findByKeyAndStandard_ProjectId("expected_result", projectId))
        .thenReturn(Optional.empty());
    when(tmsQualityStandardCriterionRepository.findByKeyAndStandard_ProjectId("nope", projectId))
        .thenReturn(Optional.empty());
    when(tmsQualityStandardCriterionRepository.findAllByStandard_ProjectIdOrderBySequenceAsc(projectId))
        .thenReturn(List.of(criterion));

    var ex = assertThrows(ReportPortalException.class,
        () -> sut.applyQualityScores(projectId, version, List.of(good, wrong, wrongToo)));

    assertEquals(ErrorType.BAD_REQUEST_ERROR.getCode(), ex.getErrorType().getCode());
    assertTrue(ex.getMessage().contains("key 'expected_result'"), ex.getMessage());
    assertTrue(ex.getMessage().contains("key 'nope'"), ex.getMessage());
    assertTrue(ex.getMessage().contains("Known: [clear_steps]"), ex.getMessage());
    verify(tmsTestCaseQualityScoreRepository, never()).deleteByTestCaseVersionId(any());
    verify(tmsTestCaseQualityScoreRepository, never()).saveAll(any());
  }

  @Test
  void applyQualityScores_WhenTheProjectHasNoStandard_ShouldAnswerBadRequestSayingSo() {
    var rq = TmsTestCaseQualityScoreRQ.builder().criterionKey("atomicity").score(1).build();

    when(tmsQualityStandardCriterionRepository.findByKeyAndStandard_ProjectId("atomicity", projectId))
        .thenReturn(Optional.empty());
    when(tmsQualityStandardCriterionRepository.findAllByStandard_ProjectIdOrderBySequenceAsc(projectId))
        .thenReturn(List.of());

    var ex = assertThrows(ReportPortalException.class,
        () -> sut.applyQualityScores(projectId, version, List.of(rq)));

    assertEquals(ErrorType.BAD_REQUEST_ERROR.getCode(), ex.getErrorType().getCode());
    assertTrue(ex.getMessage().contains("no quality standard"), ex.getMessage());
  }

  @Test
  void applyQualityScores_WithBothOrNeitherIdentifier_ShouldThrowBadRequest() {
    var both = TmsTestCaseQualityScoreRQ.builder().criterionId(20L).criterionKey("clear_steps").score(1).build();
    var neither = TmsTestCaseQualityScoreRQ.builder().score(1).build();

    assertThrows(ReportPortalException.class, () -> sut.applyQualityScores(projectId, version, List.of(both)));
    assertThrows(ReportPortalException.class, () -> sut.applyQualityScores(projectId, version, List.of(neither)));
    verify(tmsTestCaseQualityScoreRepository, never()).saveAll(any());
  }

  @Test
  void applyQualityScores_WithSameCriterionByIdAndByKey_ShouldThrowDuplicate() {
    criterion.setKey("clear_steps");
    var byId = TmsTestCaseQualityScoreRQ.builder().criterionId(20L).score(10).build();
    var byKey = TmsTestCaseQualityScoreRQ.builder().criterionKey("clear_steps").score(15).build();

    when(tmsQualityStandardCriterionRepository.findByIdAndStandard_ProjectId(20L, projectId))
        .thenReturn(Optional.of(criterion));
    when(tmsQualityStandardCriterionRepository.findByKeyAndStandard_ProjectId("clear_steps", projectId))
        .thenReturn(Optional.of(criterion));

    assertThrows(ReportPortalException.class,
        () -> sut.applyQualityScores(projectId, version, List.of(byId, byKey)));
    verify(tmsTestCaseQualityScoreRepository, never()).deleteByTestCaseVersionId(any());
  }

  @Test
  void applyQualityScores_WithNullScores_ShouldBeNoOp() {
    sut.applyQualityScores(projectId, version, null);

    verify(tmsTestCaseQualityScoreRepository, never()).deleteByTestCaseVersionId(any());
    verify(tmsTestCaseQualityScoreRepository, never()).saveAll(any());
  }

  @Test
  void hasAiSignal_WithQualityScoresOnly_ShouldReturnTrue() {
    var rq = TmsTestCaseQualityScoreRQ.builder().criterionId(20L).score(10).build();

    assertTrue(sut.hasAiSignal(List.of(rq), null));
  }

  @Test
  void hasAiSignal_WithGenerationMetadataOnly_ShouldReturnTrue() {
    assertTrue(sut.hasAiSignal(null, TmsTestCaseGenerationMetadataRQ.builder().model("claude-opus-5").build()));
  }

  @Test
  void hasAiSignal_WithNeitherScoresNorGeneration_ShouldReturnFalse() {
    assertFalse(sut.hasAiSignal(null, null));
    assertFalse(sut.hasAiSignal(List.of(), null));
  }

  @Test
  void applyGenerationMetadata_WithNullGeneration_ShouldBeNoOp() {
    sut.applyGenerationMetadata(version, null);

    verify(tmsTestCaseGenerationMetadataRepository, never()).findByTestCaseVersionId(any());
    verify(tmsTestCaseGenerationMetadataRepository, never()).save(any());
  }

  @Test
  void applyGenerationMetadata_WhenNoExistingRow_ShouldCreateNewOne() {
    var generation = TmsTestCaseGenerationMetadataRQ.builder()
        .tokensIn(90000).tokensOut(42000).model("claude-opus-5").skill("create-test-cases@0.4")
        .costUsd(BigDecimal.valueOf(0.98)).build();
    when(tmsTestCaseGenerationMetadataRepository.findByTestCaseVersionId(version.getId())).thenReturn(Optional.empty());

    sut.applyGenerationMetadata(version, generation);

    var captor = ArgumentCaptor.forClass(TmsTestCaseGenerationMetadata.class);
    verify(tmsTestCaseGenerationMetadataRepository).save(captor.capture());
    assertEquals(version, captor.getValue().getTestCaseVersion());
    assertEquals(90000, captor.getValue().getTokensIn());
    assertEquals("claude-opus-5", captor.getValue().getModel());
    assertEquals(BigDecimal.valueOf(0.98), captor.getValue().getCostUsd());
  }

  @Test
  void applyGenerationMetadata_WhenExistingRowForVersion_ShouldUpdateItInPlace() {
    var existing = new TmsTestCaseGenerationMetadata();
    existing.setId(77L);
    var generation = TmsTestCaseGenerationMetadataRQ.builder().model("claude-opus-5-2").build();
    when(tmsTestCaseGenerationMetadataRepository.findByTestCaseVersionId(version.getId())).thenReturn(Optional.of(existing));

    sut.applyGenerationMetadata(version, generation);

    var captor = ArgumentCaptor.forClass(TmsTestCaseGenerationMetadata.class);
    verify(tmsTestCaseGenerationMetadataRepository).save(captor.capture());
    assertEquals(77L, captor.getValue().getId());
    assertEquals("claude-opus-5-2", captor.getValue().getModel());
  }

  @Test
  void buildMetrics_WhenVersionIsNull_ShouldReturnNull() {
    assertNull(sut.buildMetrics(null));
  }

  @Test
  void buildMetrics_WhenNoScoresAndNoGeneration_ShouldReturnNull() {
    when(tmsTestCaseQualityScoreRepository.findByTestCaseVersionIdIn(any())).thenReturn(List.of());
    when(tmsTestCaseGenerationMetadataRepository.findByTestCaseVersionIdIn(any())).thenReturn(List.of());

    assertNull(sut.buildMetrics(version));
  }

  @Test
  void buildMetrics_WithScoresEvaluatedAfterVersionUpdate_ShouldSumOverallScoreAndNotBeObsolete() {
    version.setUpdatedAt(Instant.parse("2026-09-01T00:00:00Z"));
    var score = new TmsTestCaseQualityScore();
    score.setTestCaseVersion(version);
    score.setCriterion(criterion);
    score.setScore(20);
    score.setEvaluatedAt(Instant.parse("2026-09-02T00:00:00Z"));
    when(tmsTestCaseQualityScoreRepository.findByTestCaseVersionIdIn(any())).thenReturn(List.of(score));
    when(tmsTestCaseGenerationMetadataRepository.findByTestCaseVersionIdIn(any())).thenReturn(List.of());

    var result = sut.buildMetrics(version);

    assertEquals(20, result.getOverallScore());
    assertFalse(result.isObsolete());
    assertEquals(1, result.getCriteria().size());
    assertEquals(20L, result.getCriteria().get(0).getCriterionId());
  }

  @Test
  void buildMetrics_WhenTheVersionComesFromAPipelineIteration_ShouldNameThatIteration() {
    version.setPipelineIterationId(31L);
    var score = new TmsTestCaseQualityScore();
    score.setTestCaseVersion(version);
    score.setCriterion(criterion);
    score.setScore(20);
    score.setEvaluatedAt(Instant.parse("2026-09-02T00:00:00Z"));
    score.setPipelineIterationId(31L);
    var iteration = new PipelineIteration();
    iteration.setId(31L);
    iteration.setIterationNumber(1);
    when(tmsTestCaseQualityScoreRepository.findByTestCaseVersionIdIn(any())).thenReturn(List.of(score));
    when(tmsTestCaseGenerationMetadataRepository.findByTestCaseVersionIdIn(any())).thenReturn(List.of());
    when(pipelineIterationRepository.findAllById(any())).thenReturn(List.of(iteration));

    var result = sut.buildMetrics(version);

    assertEquals(31L, result.getPipelineIterationId());
    assertEquals(1, result.getPipelineIterationNumber());
  }

  @Test
  void buildMetrics_WhenTheCaseWasGradedBeforeItsIterationReportedIt_ShouldNameTheIterationThatListsTheCase() {
    var testCase = new TmsTestCase();
    testCase.setId(106L);
    version.setTestCase(testCase);
    var score = new TmsTestCaseQualityScore();
    score.setTestCaseVersion(version);
    score.setCriterion(criterion);
    score.setScore(20);
    score.setEvaluatedAt(Instant.parse("2026-09-02T00:00:00Z"));
    var iteration = new PipelineIteration();
    iteration.setId(31L);
    iteration.setIterationNumber(1);
    var link = org.mockito.Mockito.mock(PipelineStageTestCaseRepository.TestCaseIteration.class);
    when(link.getTestCaseId()).thenReturn(106L);
    when(link.getIterationId()).thenReturn(31L);
    when(tmsTestCaseQualityScoreRepository.findByTestCaseVersionIdIn(any())).thenReturn(List.of(score));
    when(tmsTestCaseGenerationMetadataRepository.findByTestCaseVersionIdIn(any())).thenReturn(List.of());
    when(stageTestCaseRepository.findCreatingIterationIdsByTestCaseIdIn(any())).thenReturn(List.of(link));
    when(pipelineIterationRepository.findAllById(any())).thenReturn(List.of(iteration));

    var result = sut.buildMetrics(version);

    assertEquals(31L, result.getPipelineIterationId());
    assertEquals(1, result.getPipelineIterationNumber());
  }

  @Test
  void buildMetrics_WhenNoPipelineProducedTheVersion_ShouldLeaveTheIterationOut() {
    var score = new TmsTestCaseQualityScore();
    score.setTestCaseVersion(version);
    score.setCriterion(criterion);
    score.setScore(20);
    score.setEvaluatedAt(Instant.parse("2026-09-02T00:00:00Z"));
    when(tmsTestCaseQualityScoreRepository.findByTestCaseVersionIdIn(any())).thenReturn(List.of(score));
    when(tmsTestCaseGenerationMetadataRepository.findByTestCaseVersionIdIn(any())).thenReturn(List.of());

    var result = sut.buildMetrics(version);

    assertNull(result.getPipelineIterationId());
    assertNull(result.getPipelineIterationNumber());
    verify(pipelineIterationRepository, never()).findAllById(any());
  }

  @Test
  void buildMetrics_WithScoresEvaluatedBeforeVersionUpdate_ShouldBeObsolete() {
    version.setUpdatedAt(Instant.parse("2026-09-02T00:00:00Z"));
    var score = new TmsTestCaseQualityScore();
    score.setTestCaseVersion(version);
    score.setCriterion(criterion);
    score.setScore(20);
    score.setEvaluatedAt(Instant.parse("2026-09-01T00:00:00Z"));
    when(tmsTestCaseQualityScoreRepository.findByTestCaseVersionIdIn(any())).thenReturn(List.of(score));
    when(tmsTestCaseGenerationMetadataRepository.findByTestCaseVersionIdIn(any())).thenReturn(List.of());

    var result = sut.buildMetrics(version);

    assertTrue(result.isObsolete());
  }

  @Test
  void buildMetrics_WithGenerationOnlyAndNoScores_ShouldPopulateGenerationFieldsWithNullOverallScore() {
    var generation = new TmsTestCaseGenerationMetadata();
    generation.setTokensIn(1000);
    generation.setTestCaseVersion(version);
    generation.setModel("claude-opus-5");
    when(tmsTestCaseQualityScoreRepository.findByTestCaseVersionIdIn(any())).thenReturn(List.of());
    when(tmsTestCaseGenerationMetadataRepository.findByTestCaseVersionIdIn(any()))
        .thenReturn(List.of(generation));

    var result = sut.buildMetrics(version);

    assertNull(result.getOverallScore());
    assertEquals(1000, result.getTokensIn());
    assertEquals("claude-opus-5", result.getModel());
  }

  @Test
  void buildMetricsBatch_WithEmptyCollection_ShouldReturnEmptyMapWithoutQueryingRepositories() {
    var result = sut.buildMetricsBatch(List.of());

    assertTrue(result.isEmpty());
    verify(tmsTestCaseQualityScoreRepository, never()).findByTestCaseVersionIdIn(any());
    verify(tmsTestCaseGenerationMetadataRepository, never()).findByTestCaseVersionIdIn(any());
  }

  @Test
  void buildMetricsBatch_ShouldGroupScoresAndGenerationPerVersionAndSkipVersionsWithNeither() {
    var versionWithScore = new TmsTestCaseVersion();
    versionWithScore.setId(101L);
    var versionWithNothing = new TmsTestCaseVersion();
    versionWithNothing.setId(102L);

    var scoreForVersion101 = new TmsTestCaseQualityScore();
    scoreForVersion101.setTestCaseVersion(versionWithScore);
    scoreForVersion101.setCriterion(criterion);
    scoreForVersion101.setScore(15);
    scoreForVersion101.setEvaluatedAt(Instant.now());

    when(tmsTestCaseQualityScoreRepository.findByTestCaseVersionIdIn(any())).thenReturn(List.of(scoreForVersion101));
    when(tmsTestCaseGenerationMetadataRepository.findByTestCaseVersionIdIn(any())).thenReturn(List.of());

    var result = sut.buildMetricsBatch(List.of(versionWithScore, versionWithNothing));

    assertEquals(1, result.size());
    assertTrue(result.containsKey(101L));
    assertEquals(15, result.get(101L).getOverallScore());
    assertFalse(result.containsKey(102L));
  }

  @Test
  void buildMetricsBatch_ShouldDeduplicateByVersionIdAndSkipNullVersions() {
    var duplicate1 = new TmsTestCaseVersion();
    duplicate1.setId(101L);
    var duplicate2 = new TmsTestCaseVersion();
    duplicate2.setId(101L);
    when(tmsTestCaseQualityScoreRepository.findByTestCaseVersionIdIn(any())).thenReturn(List.of());
    when(tmsTestCaseGenerationMetadataRepository.findByTestCaseVersionIdIn(any())).thenReturn(List.of());

    var versions = new java.util.ArrayList<TmsTestCaseVersion>();
    versions.add(duplicate1);
    versions.add(duplicate2);
    versions.add(null);
    var result = sut.buildMetricsBatch(versions);

    assertTrue(result.isEmpty());
    var idsCaptor = ArgumentCaptor.forClass(java.util.Collection.class);
    verify(tmsTestCaseQualityScoreRepository).findByTestCaseVersionIdIn(idsCaptor.capture());
    assertEquals(java.util.Set.of(101L), java.util.Set.copyOf(idsCaptor.getValue()));
  }
}
