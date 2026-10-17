package com.epam.reportportal.base.core.aifactory.service;

import com.epam.reportportal.base.core.aifactory.dto.PipelineRS;
import com.epam.reportportal.base.core.aifactory.dto.PipelineSettingsRQ;
import com.epam.reportportal.base.util.OffsetRequest;
import org.springframework.data.domain.Page;

public interface PipelineService {

  Page<PipelineRS> getPipelines(Long projectId, String search, OffsetRequest offsetRequest);

  /**
   * Changes the settings of a pipeline definition. Each setting named in the request replaces the stored one,
   * a setting sent as {@code null} is removed, the others stay. The result is checked by every
   * {@link PipelineSettingsValidator} before it is saved.
   * <p>
   * Example: {@code {"autoReady": {"enabled": true, "threshold": 90}}} turns on Auto-Ready (validated by the
   * test case Auto-Ready validator, which requires a threshold between 0 and 100).
   */
  PipelineRS patchPipelineSettings(Long projectId, Long pipelineId, PipelineSettingsRQ rq);
}
