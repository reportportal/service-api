package com.epam.reportportal.base.core.aifactory.service;

import com.epam.reportportal.base.core.aifactory.dto.PipelineCompareRS;
import com.epam.reportportal.base.core.aifactory.dto.PipelineIterationDetailRS;
import com.epam.reportportal.base.core.aifactory.dto.PipelineIterationPatchRQ;
import com.epam.reportportal.base.core.aifactory.dto.PipelineIterationRQ;
import com.epam.reportportal.base.core.aifactory.dto.PipelineIterationSummaryRS;
import com.epam.reportportal.base.core.aifactory.dto.PipelineStageRQ;
import com.epam.reportportal.base.util.OffsetRequest;
import org.springframework.data.domain.Page;

public interface PipelineIterationService {

  /**
   * Creates an iteration, with the stages known so far. With a non-null {@code idempotencyKey} the call
   * is idempotent per pipeline: the same key with the same body answers with the iteration created the
   * first time ({@code replayed = true}); the same key with a different body is a conflict.
   */
  PipelineIterationIngestResult createPipelineIteration(Long projectId, Long userId,
      PipelineIterationRQ rq, String idempotencyKey);

  default PipelineIterationDetailRS createPipelineIteration(Long projectId, Long userId,
      PipelineIterationRQ rq) {
    return createPipelineIteration(projectId, userId, rq, null).iteration();
  }

  /**
   * Reports one stage of an existing iteration: creates it, or updates it in place. A stage that already
   * finished and is reported again by a different CI job starts a new attempt; the finished one is kept.
   */
  PipelineIterationDetailRS reportStage(Long projectId, Long iterationId, String stageKey, PipelineStageRQ rq);

  PipelineIterationDetailRS patchPipelineIteration(Long projectId, Long iterationId, PipelineIterationPatchRQ rq);

  Page<PipelineIterationSummaryRS> getPipelineIterations(Long projectId, Long pipelineId,
      PipelineIterationFilter filter, OffsetRequest offsetRequest);

  PipelineIterationDetailRS getPipelineIteration(Long projectId, Long iterationId);

  /**
   * Compares {@code candidateId} against {@code baselineId}; both must belong to the same pipeline.
   */
  PipelineCompareRS comparePipelineIterations(Long projectId, Long candidateId, Long baselineId);
}
