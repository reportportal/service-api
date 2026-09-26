package com.epam.reportportal.base.core.aifactory.service;

import com.epam.reportportal.base.core.aifactory.dto.PipelineStageRetryRQ;
import com.epam.reportportal.base.core.aifactory.dto.PipelineStageRetryRS;

public interface PipelineStageRetryService {

  PipelineStageRetryRS retryStage(Long projectId, Long userId, Long iterationId, Long stageId, PipelineStageRetryRQ rq);
}
