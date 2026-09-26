package com.epam.reportportal.base.core.tms.pipeline;

import com.epam.reportportal.base.core.aifactory.service.PipelineStageContributor;
import com.epam.reportportal.base.core.aifactory.enums.AttributeType;
import com.epam.reportportal.base.core.aifactory.enums.AttributeValueType;
import com.epam.reportportal.base.core.aifactory.enums.StageOrigin;
import com.epam.reportportal.base.core.aifactory.enums.StatusCategory;
import com.epam.reportportal.base.core.aifactory.model.PipelineAttribute;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.PipelineStageTestCaseRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.Pipeline;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineAttributes;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineIteration;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineStage;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.enums.TmsTestCaseStatus;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Owns the human review stage of a test case generation pipeline: it exists from the start but only
 * counts once the upload stage has passed, then shows how many of the iteration's test cases are Ready
 * and stays in progress until all of them are.
 */
@Component
@RequiredArgsConstructor
public class TestCaseReviewContributor implements PipelineStageContributor {

  public static final String PIPELINE_TYPE = "TEST_CASE_GENERATION";
  static final String REVIEW_KEY = "review";
  static final String UPLOAD_KEY = "upload";

  private final PipelineStageTestCaseRepository stageTestCaseRepository;

  @Override
  public boolean supports(Pipeline pipeline) {
    return PIPELINE_TYPE.equals(pipeline.getType());
  }

  @Override
  public List<PipelineStage> contribute(PipelineIteration iteration, List<PipelineStage> stages) {
    var review = stages.stream().filter(stage -> REVIEW_KEY.equals(stage.getStageKey())).findFirst()
        .orElseGet(() -> newReviewStage(iteration, stages));
    if (review.getId() != null && review.getOrigin() != StageOrigin.DERIVED) {
      // the pipeline reports a stage of its own under this key; leave it alone
      return List.of();
    }
    var uploadPassed = stages.stream()
        .anyMatch(stage -> UPLOAD_KEY.equals(stage.getStageKey())
            && stage.getStatusCategory() == StatusCategory.PASSED);

    if (!uploadPassed) {
      review.setStatus("Pending");
      review.setStatusCategory(StatusCategory.PENDING);
      review.setAffectsIterationStatus(false);
      review.setMessage("Waiting for Upload");
      return List.of(review);
    }

    var counts = iteration.getId() == null ? List.<PipelineStageTestCaseRepository.IterationTestCaseCounts>of()
        : stageTestCaseRepository.countTestCasesByIterationIdIn(List.of(iteration.getId()), TmsTestCaseStatus.READY);
    long total = counts.isEmpty() ? 0 : counts.get(0).getTestCasesCount();
    long ready = counts.isEmpty() ? 0 : counts.get(0).getReadyCount();
    boolean done = ready == total;

    review.setAffectsIterationStatus(true);
    review.setStatus(done ? "Done" : "In progress");
    review.setStatusCategory(done ? StatusCategory.PASSED : StatusCategory.WAITING);
    review.setMessage(done ? "All Test Cases of this iteration are Ready"
        : (total - ready) + " Draft Test Case" + (total - ready == 1 ? " waits" : "s wait")
        + " for review in the Test Case Library");
    if (review.getStartedAt() == null) {
      review.setStartedAt(Instant.now());
    }
    review.setFinishedAt(done ? Objects.requireNonNullElseGet(review.getFinishedAt(), Instant::now) : null);
    review.setAttributes(new PipelineAttributes(List.of(
        PipelineAttribute.builder().key("review").label("Ready").value(ready + "/" + total)
            .type(AttributeType.MAIN).valueType(AttributeValueType.TEXT).build(),
        PipelineAttribute.builder().key("ready").label("Ready").value(String.valueOf(ready))
            .type(AttributeType.HIDDEN).valueType(AttributeValueType.NUMBER).build(),
        PipelineAttribute.builder().key("total").label("Test Cases").value(String.valueOf(total))
            .type(AttributeType.HIDDEN).valueType(AttributeValueType.NUMBER).build())));
    return List.of(review);
  }

  @Override
  public Optional<String> iterationStatusLabel(StatusCategory category) {
    return switch (category) {
      case WAITING -> Optional.of("In review");
      case PASSED -> Optional.of("Completed");
      default -> Optional.empty();
    };
  }

  @Override
  public List<PipelineAttribute> iterationAttributes(PipelineIteration iteration, List<PipelineStage> stages) {
    return stages.stream()
        .filter(stage -> REVIEW_KEY.equals(stage.getStageKey()) && stage.getOrigin() == StageOrigin.DERIVED)
        .findFirst()
        .map(PipelineStage::getAttributes)
        .flatMap(attributes -> attributes.getItems().stream()
            .filter(attribute -> "ready".equals(attribute.getKey()))
            .findFirst())
        .map(ready -> List.of(PipelineAttribute.builder().key("readyNow").label("Ready now")
            .value(ready.getValue()).type(AttributeType.MAIN).valueType(AttributeValueType.NUMBER).build()))
        .orElse(List.of());
  }

  private PipelineStage newReviewStage(PipelineIteration iteration, List<PipelineStage> stages) {
    var review = new PipelineStage();
    review.setIteration(iteration);
    review.setStageKey(REVIEW_KEY);
    review.setName("Review");
    review.setSequence(stages.stream().map(PipelineStage::getSequence).filter(Objects::nonNull)
        .max(Integer::compare).orElse(0) + 1);
    review.setOrigin(StageOrigin.DERIVED);
    review.setAttempt(1);
    review.setStatus("Pending");
    review.setStatusCategory(StatusCategory.PENDING);
    review.setAffectsIterationStatus(false);
    return review;
  }
}
