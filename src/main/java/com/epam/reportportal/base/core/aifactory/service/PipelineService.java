package com.epam.reportportal.base.core.aifactory.service;

import com.epam.reportportal.base.core.aifactory.dto.PipelineRS;
import com.epam.reportportal.base.core.aifactory.dto.PipelineSettingsRQ;
import com.epam.reportportal.base.util.OffsetRequest;
import org.springframework.data.domain.Page;

public interface PipelineService {

  Page<PipelineRS> getPipelines(Long projectId, OffsetRequest offsetRequest);

  /**
   * Updates a pipeline definition's Auto-Ready settings. Requires
   * {@code autoReadyThreshold} (0-100) when {@code autoReadyEnabled} is {@code true}.
   */
  PipelineRS patchPipelineSettings(Long projectId, Long pipelineId, PipelineSettingsRQ rq);
}
