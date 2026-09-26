package com.epam.reportportal.base.core.aifactory.service;

import com.epam.reportportal.base.core.aifactory.dto.PipelineRS;
import com.epam.reportportal.base.core.aifactory.dto.PipelineSettingsRQ;
import com.epam.reportportal.base.util.OffsetRequest;
import org.springframework.data.domain.Page;

public interface PipelineService {

  /**
   * Pipelines of a project, ordered by name and id. {@code search} looks at the name and at the attributes of the
   * iterations; {@code name} looks at the pipeline name only. Both narrow the result before it is cut into pages.
   * <p>
   * Example: {@code name = "gener"} returns "Test case generation" and no pipeline that merely has an iteration
   * about a "generated" requirement.
   */
  Page<PipelineRS> getPipelines(Long projectId, String search, String name, OffsetRequest offsetRequest);

  /**
   * One pipeline of a project with its settings, e.g. to show the current Auto-Ready threshold when the page was
   * opened from a link to an iteration and the list of pipelines was never loaded.
   */
  PipelineRS getPipeline(Long projectId, Long pipelineId);

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
