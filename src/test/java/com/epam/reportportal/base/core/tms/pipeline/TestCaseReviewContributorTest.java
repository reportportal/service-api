package com.epam.reportportal.base.core.tms.pipeline;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.epam.reportportal.base.core.aifactory.enums.StageOwner;
import com.epam.reportportal.base.core.aifactory.enums.StatusCategory;
import com.epam.reportportal.base.core.aifactory.model.PipelineAttribute;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.PipelineStageTestCaseRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.Pipeline;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineIteration;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineStage;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.enums.TmsTestCaseStatus;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TestCaseReviewContributorTest {

  @Mock
  private PipelineStageTestCaseRepository stageTestCaseRepository;

  @InjectMocks
  private TestCaseReviewContributor contributor;

  private PipelineIteration iteration;

  @BeforeEach
  void setUp() {
    iteration = new PipelineIteration();
    iteration.setId(7L);
  }

  private static PipelineStage stage(String key, int sequence, StatusCategory category) {
    var stage = new PipelineStage();
    stage.setId((long) sequence);
    stage.setStageKey(key);
    stage.setSequence(sequence);
    stage.setStatus(category.defaultLabel());
    stage.setStatusCategory(category);
    stage.setOwnedBy(StageOwner.PIPELINE);
    return stage;
  }

  private static PipelineStageTestCaseRepository.IterationTestCaseCounts counts(long total, long ready) {
    return new PipelineStageTestCaseRepository.IterationTestCaseCounts() {
      @Override
      public Long getIterationId() {
        return 7L;
      }

      @Override
      public Long getTestCasesCount() {
        return total;
      }

      @Override
      public Long getReadyCount() {
        return ready;
      }
    };
  }

  private static String attribute(PipelineStage stage, String key) {
    return stage.getAttributes().getItems().stream().filter(a -> key.equals(a.getKey()))
        .map(PipelineAttribute::getValue).findFirst().orElse(null);
  }

  @Test
  void supports_OnlyTestCaseGenerationPipelines() {
    var generation = new Pipeline();
    generation.setType("TEST_CASE_GENERATION");
    var other = new Pipeline();
    other.setType("TEST_AUTOMATION");

    assertTrue(contributor.supports(generation));
    assertFalse(contributor.supports(other));
    assertFalse(contributor.supports(new Pipeline()));
  }

  @Test
  void contribute_BeforeUploadPassed_ShouldCreatePendingReviewThatDoesNotCount() {
    var stages = List.of(stage("create", 1, StatusCategory.PASSED), stage("upload", 3, StatusCategory.FAILED));

    var result = contributor.contribute(iteration, stages);

    assertEquals(1, result.size());
    var review = result.get(0);
    assertNull(review.getId());
    assertEquals("review", review.getStageKey());
    assertEquals("Review", review.getName());
    assertEquals(4, review.getSequence());
    assertEquals(StageOwner.REPORTPORTAL, review.getOwnedBy());
    assertEquals(StatusCategory.PENDING, review.getStatusCategory());
    assertFalse(review.isAffectsIterationStatus());
    assertSame(iteration, review.getIteration());
  }

  private static void assertSame(Object expected, Object actual) {
    org.junit.jupiter.api.Assertions.assertSame(expected, actual);
  }

  @Test
  void contribute_WithUploadPassedAndDraftCases_ShouldBeInProgressWithReadyOverTotal() {
    var stages = List.of(stage("upload", 3, StatusCategory.PASSED));
    when(stageTestCaseRepository.countTestCasesByIterationIdIn(anyCollection(), eq(TmsTestCaseStatus.READY)))
        .thenReturn(List.of(counts(4, 1)));

    var review = contributor.contribute(iteration, stages).get(0);

    assertEquals("In progress", review.getStatus());
    assertEquals(StatusCategory.WAITING, review.getStatusCategory());
    assertTrue(review.isAffectsIterationStatus());
    assertEquals("1/4", attribute(review, "review"));
    assertEquals("1", attribute(review, "ready"));
    assertEquals("1/4 Ready", review.getSummary());
    assertEquals("4", attribute(review, "total"));
    assertNotNull(review.getStartedAt());
    assertNull(review.getFinishedAt());
  }

  @Test
  void contribute_ShouldDescribeWhatIsPendingInTheStageMessage() {
    var stages = List.of(stage("upload", 3, StatusCategory.PASSED));
    when(stageTestCaseRepository.countTestCasesByIterationIdIn(anyCollection(), eq(TmsTestCaseStatus.READY)))
        .thenReturn(List.of(counts(4, 1)), List.of(counts(4, 3)), List.of(counts(4, 4)));

    assertEquals("3 Draft Test Cases wait for review in the Test Case Library",
        contributor.contribute(iteration, stages).get(0).getMessage());
    assertEquals("1 Draft Test Case waits for review in the Test Case Library",
        contributor.contribute(iteration, stages).get(0).getMessage());
    assertEquals("All Test Cases of this iteration are Ready",
        contributor.contribute(iteration, stages).get(0).getMessage());
  }

  @Test
  void contribute_BeforeUploadPassed_ShouldSayItIsWaitingForUpload() {
    assertEquals("Waiting for Upload", contributor.contribute(iteration,
        List.of(stage("upload", 3, StatusCategory.RUNNING))).get(0).getMessage());
  }

  @Test
  void iterationAttributes_ShouldExposeReadyNowFromTheReviewStage() {
    var stages = new java.util.ArrayList<>(List.of(stage("upload", 3, StatusCategory.PASSED)));
    when(stageTestCaseRepository.countTestCasesByIterationIdIn(anyCollection(), eq(TmsTestCaseStatus.READY)))
        .thenReturn(List.of(counts(4, 1)));
    stages.add(contributor.contribute(iteration, stages).get(0));

    var attributes = contributor.iterationAttributes(iteration, stages);

    assertEquals(1, attributes.size());
    assertEquals("readyNow", attributes.get(0).getKey());
    assertEquals("1", attributes.get(0).getValue());
  }

  @Test
  void iterationAttributes_BeforeUploadPassed_ShouldBeEmpty() {
    var stages = new java.util.ArrayList<>(List.of(stage("upload", 3, StatusCategory.RUNNING)));
    stages.add(contributor.contribute(iteration, stages).get(0));

    assertTrue(contributor.iterationAttributes(iteration, stages).isEmpty());
  }

  @Test
  void contribute_WhenAllCasesReady_ShouldBeDoneAndFinished() {
    var stages = List.of(stage("upload", 3, StatusCategory.PASSED));
    when(stageTestCaseRepository.countTestCasesByIterationIdIn(anyCollection(), eq(TmsTestCaseStatus.READY)))
        .thenReturn(List.of(counts(4, 4)));

    var review = contributor.contribute(iteration, stages).get(0);

    assertEquals("Done", review.getStatus());
    assertEquals(StatusCategory.PASSED, review.getStatusCategory());
    assertNotNull(review.getFinishedAt());
    assertEquals("4/4", attribute(review, "review"));
  }

  @Test
  void contribute_WhenACaseGoesBackToDraft_ShouldReopenAnExistingReviewStage() {
    var existing = stage("review", 4, StatusCategory.PASSED);
    existing.setOwnedBy(StageOwner.REPORTPORTAL);
    existing.setFinishedAt(java.time.Instant.parse("2026-01-01T00:00:00Z"));
    var stages = List.of(stage("upload", 3, StatusCategory.PASSED), existing);
    when(stageTestCaseRepository.countTestCasesByIterationIdIn(anyCollection(), eq(TmsTestCaseStatus.READY)))
        .thenReturn(List.of(counts(4, 3)));

    var result = contributor.contribute(iteration, stages);

    assertSame(existing, result.get(0));
    assertEquals("In progress", existing.getStatus());
    assertEquals(StatusCategory.WAITING, existing.getStatusCategory());
    assertNull(existing.getFinishedAt());
  }

  @Test
  void contribute_WithNoLinkedCases_ShouldBeDone() {
    var stages = List.of(stage("upload", 3, StatusCategory.PASSED));
    when(stageTestCaseRepository.countTestCasesByIterationIdIn(anyCollection(), eq(TmsTestCaseStatus.READY)))
        .thenReturn(List.of());

    var review = contributor.contribute(iteration, stages).get(0);

    assertEquals("Done", review.getStatus());
    assertEquals("0/0", attribute(review, "review"));
  }

  @Test
  void contribute_WhenThePipelineReportsItsOwnStageUnderTheKey_ShouldLeaveItAlone() {
    var own = stage("review", 4, StatusCategory.RUNNING);

    var result = contributor.contribute(iteration, List.of(stage("upload", 3, StatusCategory.PASSED), own));

    assertTrue(result.isEmpty());
    assertEquals(StatusCategory.RUNNING, own.getStatusCategory());
  }

  @Test
  void iterationStatusLabel_ShouldUseReviewWording() {
    assertEquals("In review", contributor.iterationStatusLabel(StatusCategory.WAITING).orElseThrow());
    assertEquals("Completed", contributor.iterationStatusLabel(StatusCategory.PASSED).orElseThrow());
    assertTrue(contributor.iterationStatusLabel(StatusCategory.FAILED).isEmpty());
    assertTrue(contributor.iterationStatusLabel(StatusCategory.RUNNING).isEmpty());
  }
}
