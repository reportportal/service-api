package com.epam.reportportal.base.core.aifactory.service;

import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.Pipeline;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineStage;
import java.util.List;

/**
 * Extension point for the things elsewhere in ReportPortal that a stage reports it worked on or produced.
 * <p>
 * Example: a test case generation stage reports
 * {@code relatedEntities: [{entityType: "TEST_CASE", entityId: "TC-12"}]}. The resolver whose
 * {@link #entityType()} is {@code TEST_CASE} (see {@code TestCaseRelatedEntityResolver}) finds test case
 * TC-12 and links it to the stage. A plain CI/CD pipeline reports no related entities, so no resolver is
 * involved.
 * <p>
 * Each resolver owns one entity type: it knows how to find the entities, what to keep about the link and
 * what else follows from it. The pipeline core never knows which types exist.
 */
public interface PipelineRelatedEntityResolver {

  /**
   * The kind of entity this resolver handles, e.g. {@code TEST_CASE}.
   */
  String entityType();

  /**
   * Replaces what the stage is related to, among entities of this type, with {@code entityIds}
   * (possibly none).
   *
   * @param entityIds   ids as the pipeline reported them, e.g. {@code ["TC-12", "TC-13"]}
   * @return the ids that could not be found, e.g. {@code ["TC-13"]}; they are reported back as warnings
   */
  List<String> resolve(Long projectId, Pipeline pipeline, PipelineStage stage, List<String> entityIds);
}
