package com.epam.reportportal.base.core.aifactory.mapper;

import com.epam.reportportal.base.core.aifactory.dto.PipelineIterationDetailRS;
import com.epam.reportportal.base.core.aifactory.dto.PipelineIterationRefRS;
import com.epam.reportportal.base.core.aifactory.dto.PipelineIterationSummaryRS;
import com.epam.reportportal.base.core.aifactory.dto.PipelineRS;
import com.epam.reportportal.base.core.aifactory.dto.PipelineStageAttemptRS;
import com.epam.reportportal.base.core.aifactory.dto.PipelineStageRQ;
import com.epam.reportportal.base.core.aifactory.dto.PipelineStageRS;
import com.epam.reportportal.base.core.aifactory.dto.PipelineStageSummaryRS;
import com.epam.reportportal.base.core.aifactory.dto.PipelineStepResultRS;
import com.epam.reportportal.base.core.aifactory.dto.PipelineUserRS;
import com.epam.reportportal.base.core.aifactory.enums.StageOwner;
import com.epam.reportportal.base.core.aifactory.enums.StatusCategory;
import com.epam.reportportal.base.core.aifactory.model.PipelineAttribute;
import com.epam.reportportal.base.core.aifactory.model.PipelineExecution;
import com.epam.reportportal.base.core.aifactory.model.PipelineLink;
import com.epam.reportportal.base.core.aifactory.model.PipelineSection;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.Pipeline;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineAttributes;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineDocument;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineExecutionData;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineIteration;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineLinks;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineRelatedEntities;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineSections;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineStage;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineStageAttempt;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.User;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Maps the pipeline entities to their response models and builds stage entities from reports.
 * Nothing here interprets attributes, sections or statuses beyond carrying them.
 */
@Component
public class PipelineMapper {

  public PipelineRS toPipelineRS(Pipeline pipeline, long iterationsCount, PipelineIterationSummaryRS latestIteration) {
    return PipelineRS.builder()
        .id(pipeline.getId())
        .name(pipeline.getName())
        .type(pipeline.getType())
        .description(pipeline.getDescription())
        .attributes(attributeList(pipeline.getAttributes()))
        .settings(pipeline.getSettings() == null ? null : pipeline.getSettings().getValues())
        .iterationsCount(iterationsCount)
        .latestIteration(latestIteration)
        .createdAt(pipeline.getCreatedAt())
        .build();
  }

  public PipelineIterationSummaryRS toIterationSummaryRS(PipelineIteration iteration, List<PipelineAttribute> attributes,
      List<PipelineStageSummaryRS> stages, PipelineUserRS startedBy, PipelineIterationRefRS rerunOf) {
    return PipelineIterationSummaryRS.builder()
        .id(iteration.getId())
        .pipelineId(iteration.getPipeline().getId())
        .pipelineName(iteration.getPipeline().getName())
        .pipelineType(iteration.getPipeline().getType())
        .iterationNumber(iteration.getIterationNumber())
        .status(iteration.getStatus())
        .statusCategory(iteration.getStatusCategory())
        .attributes(attributes)
        .stages(stages)
        .startedAt(iteration.getStartedAt())
        .finishedAt(iteration.getFinishedAt())
        .durationMillis(durationMillis(iteration.getStartedAt(), iteration.getFinishedAt()))
        .startedBy(startedBy)
        .rerunOf(rerunOf)
        .createdAt(iteration.getCreatedAt())
        .build();
  }

  public PipelineIterationDetailRS toIterationDetailRS(PipelineIteration iteration, List<PipelineAttribute> attributes,
      List<PipelineStageRS> stages,
      PipelineUserRS startedBy, PipelineIterationRefRS rerunOf) {
    return PipelineIterationDetailRS.builder()
        .id(iteration.getId())
        .pipelineId(iteration.getPipeline().getId())
        .pipelineName(iteration.getPipeline().getName())
        .pipelineType(iteration.getPipeline().getType())
        .iterationNumber(iteration.getIterationNumber())
        .status(iteration.getStatus())
        .statusCategory(iteration.getStatusCategory())
        .attributes(attributes)
        .startedAt(iteration.getStartedAt())
        .finishedAt(iteration.getFinishedAt())
        .durationMillis(durationMillis(iteration.getStartedAt(), iteration.getFinishedAt()))
        .startedBy(startedBy)
        .rerunOf(rerunOf)
        .createdAt(iteration.getCreatedAt())
        .stages(stages)
        .build();
  }

  public PipelineUserRS toUserRS(User user) {
    return PipelineUserRS.builder()
        .id(user.getId())
        .login(user.getLogin())
        .fullName(user.getFullName())
        .build();
  }

  public PipelineStageSummaryRS toStageSummaryRS(PipelineStage stage) {
    return PipelineStageSummaryRS.builder()
        .id(stage.getId())
        .name(stage.getName())
        .sequence(stage.getSequence())
        .attempt(stage.getAttempt())
        .parentStageId(stage.getParentStageId())
        .status(stage.getStatus())
        .statusCategory(stage.getStatusCategory())
        .ownedBy(stage.getOwnedBy())
        .startedAt(stage.getStartedAt())
        .finishedAt(stage.getFinishedAt())
        .durationMillis(durationMillis(stage.getStartedAt(), stage.getFinishedAt()))
        .attributes(attributeList(stage.getAttributes()))
        .summary(stage.getSummary())
        .message(stage.getMessage())
        .affectsIterationStatus(stage.isAffectsIterationStatus())
        .build();
  }

  public PipelineStageRS toStageRS(PipelineStage stage) {
    return PipelineStageRS.builder()
        .id(stage.getId())
        .name(stage.getName())
        .sequence(stage.getSequence())
        .attempt(stage.getAttempt())
        .parentStageId(stage.getParentStageId())
        .status(stage.getStatus())
        .statusCategory(stage.getStatusCategory())
        .ownedBy(stage.getOwnedBy())
        .startedAt(stage.getStartedAt())
        .finishedAt(stage.getFinishedAt())
        .durationMillis(durationMillis(stage.getStartedAt(), stage.getFinishedAt()))
        .attributes(attributeList(stage.getAttributes()))
        .summary(stage.getSummary())
        .message(stage.getMessage())
        .affectsIterationStatus(stage.isAffectsIterationStatus())
        .relatedEntities(relatedEntityList(stage.getRelatedEntities()))
        .sections(sectionList(stage.getSections()))
        .result(toStepResultRS(stage.getResultType(), stage.getResultRef()))
        .document(stage.getDocument() == null ? null : stage.getDocument().getContent())
        .documentSchemaName(stage.getDocumentSchemaName())
        .links(allLinks(stage))
        .retryable(execution(stage.getExecution()) != null && execution(stage.getExecution()).isRetryable())
        .lastRetriedAt(stage.getLastRetriedAt())
        .lastRetriedBy(stage.getLastRetriedBy())
        .build();
  }

  public PipelineStageAttemptRS toAttemptRS(PipelineStageAttempt attempt) {
    return PipelineStageAttemptRS.builder()
        .attempt(attempt.getAttempt())
        .status(attempt.getStatus())
        .statusCategory(attempt.getStatusCategory())
        .startedAt(attempt.getStartedAt())
        .finishedAt(attempt.getFinishedAt())
        .durationMillis(durationMillis(attempt.getStartedAt(), attempt.getFinishedAt()))
        .links(runLinks(attempt.getExecution()))
        .result(toStepResultRS(attempt.getResultType(), attempt.getResultRef()))
        .attributes(attributeList(attempt.getAttributes()))
        .sections(sectionList(attempt.getSections()))
        .build();
  }

  /**
   * Builds a new (unsaved) stage entity from its report. The caller sets {@code iteration} and, once
   * ids exist, {@code parentStageId}/{@code path}; test case links are resolved and persisted
   * separately by the service (display ids need a repository lookup).
   */
  public PipelineStage toStageEntity(PipelineStageRQ rq) {
    var stage = new PipelineStage();
    stage.setStageKey(rq.getStageKey());
    stage.setOwnedBy(StageOwner.PIPELINE);
    applyStage(stage, rq);
    return stage;
  }

  /**
   * Copies what the pipeline reported onto the stage; the stage key, owner, attempt and tree position
   * are not touched. A report replaces the previous one as a whole - what it does not mention is cleared,
   * except the name and the counting flag which keep their value when absent.
   */
  public void applyStage(PipelineStage stage, PipelineStageRQ rq) {
    if (rq.getName() != null || stage.getName() == null) {
      stage.setName(rq.getName() != null ? rq.getName() : rq.getStageKey());
    }
    if (rq.getSequence() != null) {
      stage.setSequence(rq.getSequence());
    }
    stage.setStatus(rq.getStatus());
    stage.setStatusCategory(rq.getStatusCategory() != null ? rq.getStatusCategory()
        : StatusCategory.fromReported(rq.getStatus()));
    if (rq.getAffectsIterationStatus() != null) {
      stage.setAffectsIterationStatus(rq.getAffectsIterationStatus());
    }
    stage.setStartedAt(rq.getStartedAt());
    stage.setFinishedAt(rq.getFinishedAt());
    stage.setAttributes(rq.getAttributes() == null ? null : new PipelineAttributes(rq.getAttributes()));
    stage.setSections(rq.getSections() == null ? null : new PipelineSections(rq.getSections()));
    stage.setSummary(rq.getSummary());
    stage.setSummary(rq.getSummary());
    stage.setMessage(rq.getMessage());
    var related = PipelineSection.entityReferences(rq.getSections());
    stage.setRelatedEntities(related.isEmpty() ? null : new PipelineRelatedEntities(related));
    stage.setLinks(rq.getLinks() == null || rq.getLinks().isEmpty() ? null : new PipelineLinks(rq.getLinks()));
    stage.setDocument(rq.getDocument() == null ? null : new PipelineDocument(rq.getDocument()));
    stage.setDocumentSchemaName(rq.getDocumentSchemaName());
    stage.setResultType(rq.getResult() == null ? null : rq.getResult().getResultType());
    stage.setResultRef(rq.getResult() == null ? null : rq.getResult().getResultRef());
    if (rq.getExecution() != null) {
      stage.setExecution(new PipelineExecutionData(rq.getExecution()));
    }
  }

  public List<PipelineAttribute> attributeList(PipelineAttributes attributes) {
    return attributes == null || attributes.getItems().isEmpty() ? null : attributes.getItems();
  }

  public List<PipelineSection.EntityReference> relatedEntityList(PipelineRelatedEntities relatedEntities) {
    return relatedEntities == null || relatedEntities.getItems().isEmpty() ? null : relatedEntities.getItems();
  }

  public List<PipelineSection> sectionList(PipelineSections sections) {
    return sections == null || sections.getItems().isEmpty() ? null : sections.getItems();
  }

  private PipelineStepResultRS toStepResultRS(String resultType, String resultRef) {
    if (resultType == null && resultRef == null) {
      return null;
    }
    return PipelineStepResultRS.builder().resultType(resultType).resultRef(resultRef).build();
  }

  /**
   * The links the stage was given plus the CI job link derived from its CI fields.
   */
  private List<PipelineLink> allLinks(PipelineStage stage) {
    var derived = runLinks(stage.getExecution());
    var given = stage.getLinks() == null ? List.<PipelineLink>of() : stage.getLinks().getItems();
    if (given.isEmpty()) {
      return derived;
    }
    var all = new ArrayList<PipelineLink>(given);
    if (derived != null) {
      all.addAll(derived);
    }
    return all;
  }

  private PipelineExecution execution(PipelineExecutionData data) {
    return data == null ? null : data.getContent();
  }

  /**
   * The link to the page of the run that executed the stage, if it reported one.
   */
  private List<PipelineLink> runLinks(PipelineExecutionData data) {
    var execution = execution(data);
    if (execution == null || execution.getRunUrl() == null || execution.getRunUrl().isBlank()) {
      return null;
    }
    var links = new ArrayList<PipelineLink>();
    links.add(PipelineLink.builder()
        .type(PipelineLink.RUN)
        .url(execution.getRunUrl())
        .label(execution.getExternalRunId() == null ? null : "#" + execution.getExternalRunId())
        .build());
    return links;
  }

  private Long durationMillis(Instant startedAt, Instant finishedAt) {
    if (startedAt == null || finishedAt == null) {
      return null;
    }
    return Duration.between(startedAt, finishedAt).toMillis();
  }

  /**
   * Convenience for callers that look users up in bulk.
   */
  public Map<Long, PipelineUserRS> toUserMap(List<User> users) {
    var result = new java.util.HashMap<Long, PipelineUserRS>();
    users.forEach(user -> result.put(user.getId(), toUserRS(user)));
    return result;
  }
}
