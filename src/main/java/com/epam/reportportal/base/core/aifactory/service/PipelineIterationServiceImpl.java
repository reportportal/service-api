package com.epam.reportportal.base.core.aifactory.service;

import com.epam.reportportal.base.core.aifactory.dto.PipelineCompareRS;
import com.epam.reportportal.base.core.aifactory.dto.PipelineIterationDetailRS;
import com.epam.reportportal.base.core.aifactory.dto.PipelineIterationPatchRQ;
import com.epam.reportportal.base.core.aifactory.dto.PipelineIterationRQ;
import com.epam.reportportal.base.core.aifactory.dto.PipelineIterationSummaryRS;
import com.epam.reportportal.base.core.aifactory.dto.PipelineStageRQ;
import com.epam.reportportal.base.core.aifactory.dto.PipelineWarningRS;
import com.epam.reportportal.base.core.aifactory.enums.AttributeType;
import com.epam.reportportal.base.core.aifactory.enums.AttributeValueType;
import com.epam.reportportal.base.core.aifactory.enums.StageOwner;
import com.epam.reportportal.base.core.aifactory.enums.StatusCategory;
import com.epam.reportportal.base.core.aifactory.event.PipelineIterationIngestedEvent;
import com.epam.reportportal.base.core.aifactory.mapper.PipelineMapper;
import com.epam.reportportal.base.core.aifactory.model.PipelineAttribute;
import com.epam.reportportal.base.core.aifactory.model.PipelineSection;
import com.epam.reportportal.base.infrastructure.persistence.dao.ProjectRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.aifactory.PipelineIterationRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.aifactory.PipelineRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.aifactory.PipelineSpecifications;
import com.epam.reportportal.base.infrastructure.persistence.dao.aifactory.PipelineStageAttemptRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.aifactory.PipelineStageRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.Pipeline;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineAttributes;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineIteration;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineStage;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineStageAttempt;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.base.util.OffsetRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PipelineIterationServiceImpl implements PipelineIterationService {

  static final int MAX_IDEMPOTENCY_KEY_LENGTH = 128;
  static final long MAX_REPORT_BYTES = 5L * 1024 * 1024;

  private final PipelineRepository pipelineRepository;
  private final PipelineIterationRepository pipelineIterationRepository;
  private final PipelineStageRepository pipelineStageRepository;
  private final PipelineStageAttemptRepository pipelineStageAttemptRepository;
  private final List<PipelineRelatedEntityResolver> relatedEntityResolvers;
  private final List<PipelineStageReportedHandler> stageReportedHandlers;
  private final ProjectRepository projectRepository;
  private final PipelineMapper pipelineMapper;
  private final PipelineViewAssembler viewAssembler;
  private final PipelineIterationRefresher refresher;
  private final PipelineComparator comparator;
  private final ApplicationEventPublisher eventPublisher;
  private final PipelineDocumentValidator documentValidator;

  @Override
  @Transactional
  public PipelineIterationIngestResult createPipelineIteration(Long projectId, Long userId,
      PipelineIterationRQ rq, String idempotencyKey) {
    validateSize(rq);
    var stageRQs = rq.getStages() == null ? List.<PipelineStageRQ>of() : rq.getStages();
    validateStages(stageRQs);

    var pipeline = pipelineRepository
        .findByProjectIdAndName(projectId, rq.getPipeline().getName())
        .orElseGet(() -> createPipeline(projectId, rq.getPipeline().getName(), rq.getPipeline().getType()));
    // Serializes concurrent ingests of this pipeline: next iteration number, idempotency lookup.
    var lockedPipeline = pipelineRepository.findByIdForUpdate(pipeline.getId())
        .orElseThrow(() -> new ReportPortalException(ErrorType.NOT_FOUND, "Pipeline '" + pipeline.getId() + "'"));
    applyPipelineDescription(lockedPipeline, rq);

    String fingerprint = null;
    if (idempotencyKey != null) {
      validateIdempotencyKey(idempotencyKey);
      fingerprint = IngestFingerprint.of(rq);
      var previous = pipelineIterationRepository.findByPipelineIdAndIdempotencyKey(lockedPipeline.getId(),
          idempotencyKey);
      if (previous.isPresent()) {
        if (!fingerprint.equals(previous.get().getIdempotencyHash())) {
          throw new ReportPortalException(ErrorType.IDEMPOTENCY_KEY_CONFLICT,
              "Idempotency-Key '" + idempotencyKey + "' was already used with a different request body");
        }
        return new PipelineIterationIngestResult(viewAssembler.detail(previous.get()), true);
      }
    }

    var iteration = new PipelineIteration();
    iteration.setPipeline(lockedPipeline);
    iteration.setIterationNumber(pipelineIterationRepository
        .findFirstByPipelineIdOrderByIterationNumberDesc(lockedPipeline.getId())
        .map(PipelineIteration::getIterationNumber)
        .orElse(0) + 1);
    iteration.setCreatedBy(userId);
    if (rq.getRerunOfIterationId() != null) {
      var source = pipelineIterationRepository.findById(rq.getRerunOfIterationId())
          .filter(it -> it.getPipeline().getId().equals(lockedPipeline.getId()))
          .orElseThrow(() -> new ReportPortalException(ErrorType.NOT_FOUND,
              "Pipeline iteration '" + rq.getRerunOfIterationId() + "' for pipeline '"
                  + lockedPipeline.getName() + "'"));
      iteration.setRerun(true);
      iteration.setRerunOfIterationId(source.getId());
    }
    if (idempotencyKey != null) {
      iteration.setIdempotencyKey(idempotencyKey);
      iteration.setIdempotencyHash(fingerprint);
    }
    iteration.setStartedAt(rq.getStartedAt());
    iteration.setFinishedAt(rq.getFinishedAt());
    setAttributes(iteration, PipelineAttributeSupport.normalize(rq.getAttributes()));
    iteration.setStatus(StatusCategory.PENDING.defaultLabel());
    iteration.setStatusCategory(StatusCategory.PENDING);
    var savedIteration = pipelineIterationRepository.save(iteration);

    var stages = new ArrayList<PipelineStage>();
    for (int i = 0; i < stageRQs.size(); i++) {
      var stageRQ = stageRQs.get(i);
      var stage = pipelineMapper.toStageEntity(stageRQ);
      checkDocument(stage);
      stage.setIteration(savedIteration);
      stage.setAttempt(1);
      if (stage.getSequence() == null) {
        stage.setSequence(i + 1);
      }
      stages.add(stage);
    }
    var savedStages = pipelineStageRepository.saveAll(stages);
    assignStagePaths(stageRQs, savedStages);
    pipelineStageRepository.saveAll(savedStages);

    var warnings = new ArrayList<PipelineWarningRS>();
    for (int i = 0; i < stageRQs.size(); i++) {
      var stage = savedStages.get(i);
      warnings.addAll(resolveRelatedEntities(projectId, lockedPipeline, stage, false));
      notifyStageReportedHandlers(projectId, lockedPipeline, stage, stage.getStatusCategory() == StatusCategory.PASSED);
    }

    var refreshed = refresher.refresh(savedIteration);
    eventPublisher.publishEvent(
        new PipelineIterationIngestedEvent(refreshed.getId(), lockedPipeline.getId(), projectId));

    var detail = viewAssembler.detail(refreshed);
    if (!warnings.isEmpty()) {
      detail.setWarnings(warnings);
    }
    return new PipelineIterationIngestResult(detail, false);
  }

  @Override
  @Transactional
  public PipelineIterationDetailRS reportStage(Long projectId, Long iterationId, String stageKey, PipelineStageRQ rq) {
    validateSize(rq);
    if (rq.getStageKey() != null && !rq.getStageKey().equals(stageKey)) {
      throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR,
          "'stageKey' in the body does not match the stage key in the path");
    }
    var iteration = lockIterationOrThrow(projectId, iterationId);
    var existing = pipelineStageRepository.findByIterationIdAndStageKey(iterationId, stageKey).orElse(null);

    PipelineStage stage;
    boolean becamePassed;
    boolean hadRelatedEntities = false;
    if (existing == null) {
      rq.setStageKey(stageKey);
      stage = pipelineMapper.toStageEntity(rq);
      checkDocument(stage);
      stage.setIteration(iteration);
      stage.setAttempt(1);
      if (stage.getSequence() == null) {
        stage.setSequence(pipelineStageRepository.findByIterationIdOrderBySequenceAsc(iterationId).stream()
            .map(PipelineStage::getSequence).filter(Objects::nonNull).max(Integer::compare).orElse(0) + 1);
      }
      var parent = resolveParent(iterationId, rq.getParentStageKey(), null);
      stage.setParentStageId(parent == null ? null : parent.getId());
      stage = pipelineStageRepository.save(stage);
      stage.setPath(parent == null ? String.valueOf(stage.getId()) : parent.getPath() + "." + stage.getId());
      stage = pipelineStageRepository.save(stage);
      becamePassed = stage.getStatusCategory() == StatusCategory.PASSED;
    } else {
      stage = existing;
      if (stage.getOwnedBy() == StageOwner.REPORTPORTAL) {
        throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR,
            "Stage '" + stageKey + "' is maintained by ReportPortal and cannot be reported");
      }
      if (rq.getParentStageKey() != null) {
        resolveParent(iterationId, rq.getParentStageKey(), stage);
      }
      hadRelatedEntities = stage.getRelatedEntities() != null && !stage.getRelatedEntities().getItems().isEmpty();
      var previousCategory = stage.getStatusCategory();
      if (startsNewAttempt(stage, rq)) {
        pipelineStageAttemptRepository.save(PipelineStageAttempt.snapshotOf(stage));
        stage.setAttempt(stage.getAttempt() + 1);
        previousCategory = StatusCategory.PENDING;
      }
      pipelineMapper.applyStage(stage, rq);
      checkDocument(stage);
      stage = pipelineStageRepository.save(stage);
      becamePassed = previousCategory != StatusCategory.PASSED && stage.getStatusCategory() == StatusCategory.PASSED;
    }

    var warnings = resolveRelatedEntities(projectId, iteration.getPipeline(), stage, hadRelatedEntities);
    notifyStageReportedHandlers(projectId, iteration.getPipeline(), stage, becamePassed);

    var refreshed = refresher.refresh(iteration);
    var detail = viewAssembler.detail(refreshed);
    if (!warnings.isEmpty()) {
      detail.setWarnings(warnings);
    }
    return detail;
  }

  @Override
  @Transactional
  public PipelineIterationDetailRS patchPipelineIteration(Long projectId, Long iterationId,
      PipelineIterationPatchRQ rq) {
    var iteration = lockIterationOrThrow(projectId, iterationId);
    if (rq.getAttributes() != null || rq.getRemoveAttributes() != null) {
      setAttributes(iteration,
          PipelineAttributeSupport.merge(iteration.getAttributes(), rq.getAttributes(), rq.getRemoveAttributes()));
    }
    if (rq.getStartedAt() != null) {
      iteration.setStartedAt(rq.getStartedAt());
    }
    if (rq.getFinishedAt() != null) {
      iteration.setFinishedAt(rq.getFinishedAt());
    }
    return viewAssembler.detail(refresher.refresh(iteration));
  }

  @Override
  @Transactional(readOnly = true)
  public Page<PipelineIterationSummaryRS> getPipelineIterations(Long projectId, Long pipelineId,
      PipelineIterationFilter filter, OffsetRequest offsetRequest) {
    var pipeline = getPipelineOrThrow(projectId, pipelineId);
    var page = pipelineIterationRepository.findAll(
        PipelineSpecifications.iterationsOfPipeline(pipeline.getId(), filter.search(), filter.statuses(),
            filter.attributes()),
        PipelinePaging.capLimit(offsetRequest, Sort.by(Sort.Order.desc("iterationNumber"))));
    return new PageImpl<>(viewAssembler.summaries(page.getContent()), page.getPageable(), page.getTotalElements());
  }

  @Override
  @Transactional(readOnly = true)
  public PipelineIterationDetailRS getPipelineIteration(Long projectId, Long iterationId) {
    return viewAssembler.detail(getIterationOrThrow(projectId, iterationId));
  }

  @Override
  @Transactional(readOnly = true)
  public PipelineCompareRS comparePipelineIterations(Long projectId, Long candidateId, Long baselineId) {
    var candidate = getIterationOrThrow(projectId, candidateId);
    var baseline = getIterationOrThrow(projectId, baselineId);
    if (!candidate.getPipeline().getId().equals(baseline.getPipeline().getId())) {
      throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR,
          "Iterations must belong to the same pipeline definition to be compared");
    }
    return comparator.compare(viewAssembler.detail(baseline), viewAssembler.detail(candidate));
  }

  private void validateIdempotencyKey(String idempotencyKey) {
    if (idempotencyKey.isBlank() || idempotencyKey.length() > MAX_IDEMPOTENCY_KEY_LENGTH) {
      throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR,
          "'Idempotency-Key' must be 1-" + MAX_IDEMPOTENCY_KEY_LENGTH + " characters");
    }
  }

  private void validateSize(Object report) {
    if (IngestFingerprint.sizeInBytes(report) > MAX_REPORT_BYTES) {
      throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR, "A report must not exceed 5 MB");
    }
  }

  /**
   * Stage keys must be present and unique, so the rest of the report can refer to stages by key.
   */
  private void validateStages(List<PipelineStageRQ> stageRQs) {
    var keys = new HashSet<String>();
    for (var stageRQ : stageRQs) {
      if (stageRQ.getStageKey() == null || stageRQ.getStageKey().isBlank()) {
        throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR, "'stageKey' is required for every stage");
      }
      if (!keys.add(stageRQ.getStageKey())) {
        throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR,
            "Duplicate stageKey '" + stageRQ.getStageKey() + "' in ingest payload");
      }
    }
  }

  /**
   * A reported document that does not match the schema it names is rejected: it is not kept, and the
   * stage is marked failed with the reason.
   */
  private void checkDocument(PipelineStage stage) {
    var document = stage.getDocument() == null ? null : stage.getDocument().getContent();
    documentValidator.validate(stage.getDocumentSchemaName(), document).ifPresent(reason -> {
      stage.setDocument(null);
      stage.setStatus("Failed");
      stage.setStatusCategory(StatusCategory.FAILED);
      var attributes = new ArrayList<PipelineAttribute>(
          stage.getAttributes() == null ? List.of() : stage.getAttributes().getItems());
      attributes.removeIf(attribute -> "failureReason".equals(attribute.getKey()));
      attributes.add(PipelineAttribute.builder().key("failureReason").label("Failure reason").value(reason)
          .type(AttributeType.META).valueType(AttributeValueType.TEXT).build());
      stage.setAttributes(new PipelineAttributes(attributes));
    });
  }

  private Pipeline createPipeline(Long projectId, String name, String type) {
    var pipeline = new Pipeline();
    pipeline.setProject(projectRepository.getReferenceById(projectId));
    pipeline.setName(name);
    pipeline.setType(type);
    return pipelineRepository.save(pipeline);
  }

  /**
   * Records the pipeline's type the first time it is reported and merges its attributes; a different
   * type than the recorded one is rejected.
   */
  private void applyPipelineDescription(Pipeline pipeline, PipelineIterationRQ rq) {
    var reportedType = rq.getPipeline().getType();
    if (reportedType != null && !reportedType.isBlank()) {
      if (pipeline.getType() == null) {
        pipeline.setType(reportedType);
      } else if (!pipeline.getType().equals(reportedType)) {
        throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR,
            "Pipeline '" + pipeline.getName() + "' is of type '" + pipeline.getType() + "', not '" + reportedType + "'");
      }
    }
    if (rq.getPipeline().getAttributes() != null && !rq.getPipeline().getAttributes().isEmpty()) {
      pipeline.setAttributes(PipelineAttributeSupport.toColumn(
          PipelineAttributeSupport.merge(pipeline.getAttributes(), rq.getPipeline().getAttributes(), null)));
    }
    pipelineRepository.save(pipeline);
  }

  private void setAttributes(PipelineIteration iteration, List<com.epam.reportportal.base.core.aifactory.model.PipelineAttribute> attributes) {
    iteration.setAttributes(PipelineAttributeSupport.toColumn(attributes));
    iteration.setSearchText(PipelineAttributeSupport.searchText(attributes));
    iteration.setAttributeIndex(PipelineAttributeSupport.attributeIndex(attributes));
  }

  private Pipeline getPipelineOrThrow(Long projectId, Long pipelineId) {
    return pipelineRepository.findById(pipelineId)
        .filter(p -> p.getProject().getId().equals(projectId))
        .orElseThrow(
            () -> new ReportPortalException(ErrorType.NOT_FOUND, "Pipeline '" + pipelineId + "'"));
  }

  private PipelineIteration getIterationOrThrow(Long projectId, Long iterationId) {
    return pipelineIterationRepository.findById(iterationId)
        .filter(it -> it.getPipeline().getProject().getId().equals(projectId))
        .orElseThrow(() -> new ReportPortalException(ErrorType.NOT_FOUND,
            "Pipeline iteration '" + iterationId + "'"));
  }

  private PipelineIteration lockIterationOrThrow(Long projectId, Long iterationId) {
    return pipelineIterationRepository.findByIdForUpdate(iterationId)
        .filter(it -> it.getPipeline().getProject().getId().equals(projectId))
        .orElseThrow(() -> new ReportPortalException(ErrorType.NOT_FOUND,
            "Pipeline iteration '" + iterationId + "'"));
  }

  /**
   * A finished stage reported again by a different run is a new attempt; reports from the same run,
   * or reports of a stage that has not finished, update the current attempt.
   */
  private boolean startsNewAttempt(PipelineStage stage, PipelineStageRQ rq) {
    var finished = switch (stage.getStatusCategory()) {
      case PASSED, FAILED, SKIPPED -> true;
      default -> false;
    };
    var reportedRun = rq.getExecution() == null ? null : rq.getExecution().getExternalRunId();
    var currentRun = stage.getExecution() == null || stage.getExecution().getContent() == null
        ? null : stage.getExecution().getContent().getExternalRunId();
    return finished && reportedRun != null && !reportedRun.equals(currentRun);
  }

  /**
   * The parent a stage is nested in, looked up by key among the iteration's stages. A stage that already
   * exists cannot be moved to another parent.
   */
  private PipelineStage resolveParent(Long iterationId, String parentStageKey, PipelineStage stage) {
    if (parentStageKey == null) {
      return null;
    }
    var parent = pipelineStageRepository.findByIterationIdAndStageKey(iterationId, parentStageKey)
        .orElseThrow(() -> new ReportPortalException(ErrorType.BAD_REQUEST_ERROR,
            "'parentStageKey' \"" + parentStageKey + "\" does not reference a stage of this iteration"));
    if (stage != null && !Objects.equals(stage.getParentStageId(), parent.getId())) {
      throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR,
          "Stage '" + stage.getStageKey() + "' is already reported under a different parent");
    }
    return parent;
  }

  /**
   * Hands the entities the stage's result tables point to (the {@code target} of their cells) to the resolver
   * of their type, which replaces what the stage was related to before. Entities a resolver cannot find come
   * back as warnings; the report itself is not rejected. Entities of a type no resolver handles need no
   * handling: they stay in the table as reported.
   * <p>
   * Example: an Upload report has a row whose cell target is {@code {entityType: "TEST_CASE", entityId: "TC-12"}};
   * the resolver for {@code TEST_CASE} links the stage to test case TC-12.
   */
  private List<PipelineWarningRS> resolveRelatedEntities(Long projectId, Pipeline pipeline, PipelineStage stage,
      boolean hadRelatedEntities) {
    var relatedEntities = stage.getRelatedEntities() == null ? List.<PipelineSection.EntityReference>of()
        : stage.getRelatedEntities().getItems();
    if (relatedEntities.isEmpty() && !hadRelatedEntities) {
      return List.of();
    }
    var entityIdsByType = new LinkedHashMap<String, List<String>>();
    relatedEntities.forEach(entity -> entityIdsByType
        .computeIfAbsent(entity.getEntityType(), type -> new ArrayList<>()).add(entity.getEntityId()));

    var warnings = new ArrayList<PipelineWarningRS>();
    for (var resolver : relatedEntityResolvers) {
      var wanted = entityIdsByType.get(resolver.entityType());
      var notFound = resolver.resolve(projectId, pipeline, stage,
          wanted == null ? List.of() : wanted.stream().distinct().toList());
      if (!notFound.isEmpty()) {
        warnings.add(PipelineWarningRS.builder()
            .code(PipelineWarningRS.RELATED_ENTITY_NOT_FOUND)
            .message("Related entities not found; they were not linked")
            .stageId(stage.getId())
            .entityType(resolver.entityType())
            .entityIds(notFound)
            .build());
      }
    }
    return warnings;
  }

  /**
   * Lets the behaviour plugged into the pipeline react to the saved stage and adds the attributes it answers
   * with to the stage, replacing ones with the same key.
   * <p>
   * Example: the Auto-Ready handler answers {@code autoReadyPromoted = 2} after the Upload stage passed.
   */
  private void notifyStageReportedHandlers(Long projectId, Pipeline pipeline, PipelineStage stage,
      boolean stagePassed) {
    var report = new PipelineStageReportedHandler.StageReport(projectId, pipeline, stage, stagePassed);
    var added = new ArrayList<PipelineAttribute>();
    stageReportedHandlers.forEach(handler -> added.addAll(handler.onStageReported(report)));
    if (added.isEmpty()) {
      return;
    }
    var merged = PipelineAttributeSupport.merge(stage.getAttributes(), added, null);
    stage.setAttributes(PipelineAttributeSupport.toColumn(merged));
    pipelineStageRepository.save(stage);
  }

  /**
   * Resolves each stage's {@code parentStageKey} (a payload-local reference - child stage ids
   * don't exist until after the first {@code saveAll}) into a real {@code parentStageId}, and
   * builds {@code path} the same way {@code StartTestItemHandlerImpl} builds
   * {@code TestItem.path}: a root stage's path is its own id, a nested stage's path is its
   * parent's path + "." + its own id.
   */
  private void assignStagePaths(List<PipelineStageRQ> stageRQs, List<PipelineStage> savedStages) {
    // Keyed off the RQ list (not the saved entity's own stageKey) and paired positionally with
    // savedStages, same positional pairing the ref resolution uses - the RQ payload is the only
    // place 'parentStageKey' references are declared against.
    var stageByKey = new HashMap<String, PipelineStage>();
    var parentKeyByKey = new HashMap<String, String>();
    for (int i = 0; i < stageRQs.size(); i++) {
      var key = stageRQs.get(i).getStageKey();
      stageByKey.put(key, savedStages.get(i));
      if (stageRQs.get(i).getParentStageKey() != null) {
        parentKeyByKey.put(key, stageRQs.get(i).getParentStageKey());
      }
    }

    var pathByKey = new HashMap<String, String>();
    for (var key : stageByKey.keySet()) {
      computeStagePath(key, stageByKey, parentKeyByKey, pathByKey, new LinkedHashSet<>());
    }

    for (int i = 0; i < stageRQs.size(); i++) {
      var key = stageRQs.get(i).getStageKey();
      var stage = savedStages.get(i);
      stage.setPath(pathByKey.get(key));
      var parentKey = parentKeyByKey.get(key);
      if (parentKey != null) {
        stage.setParentStageId(stageByKey.get(parentKey).getId());
      }
    }
  }

  private String computeStagePath(String key, Map<String, PipelineStage> stageByKey,
      Map<String, String> parentKeyByKey, Map<String, String> pathByKey, Set<String> visiting) {
    var cached = pathByKey.get(key);
    if (cached != null) {
      return cached;
    }
    if (!visiting.add(key)) {
      throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR,
          "Cycle detected in stage 'parentStageKey' chain at '" + key + "'");
    }

    var parentKey = parentKeyByKey.get(key);
    String path;
    if (parentKey == null) {
      path = String.valueOf(stageByKey.get(key).getId());
    } else {
      var parentStage = stageByKey.get(parentKey);
      if (parentStage == null) {
        throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR,
            "'parentStageKey' \"" + parentKey + "\" does not reference a stage in this ingest payload");
      }
      var parentPath = computeStagePath(parentKey, stageByKey, parentKeyByKey, pathByKey, visiting);
      path = parentPath + "." + stageByKey.get(key).getId();
    }
    pathByKey.put(key, path);
    return path;
  }
}
