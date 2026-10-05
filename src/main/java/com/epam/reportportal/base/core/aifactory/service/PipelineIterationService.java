package com.epam.reportportal.base.core.aifactory.service;

import com.epam.reportportal.base.core.aifactory.dto.PipelineCompareRS;
import com.epam.reportportal.base.core.aifactory.dto.PipelineIterationDetailRS;
import com.epam.reportportal.base.core.aifactory.dto.PipelineIterationRQ;
import com.epam.reportportal.base.core.aifactory.dto.PipelineIterationSummaryRS;
import com.epam.reportportal.base.util.OffsetRequest;
import org.springframework.data.domain.Page;

public interface PipelineIterationService {

  PipelineIterationDetailRS createPipelineIteration(Long projectId, Long userId,
      PipelineIterationRQ rq);

  Page<PipelineIterationSummaryRS> getPipelineIterations(Long projectId, Long pipelineId,
      OffsetRequest offsetRequest);

  PipelineIterationDetailRS getPipelineIteration(Long projectId, Long iterationId);

  PipelineCompareRS comparePipelineIterations(Long projectId, Long iterationId,
      Long otherIterationId);
}
