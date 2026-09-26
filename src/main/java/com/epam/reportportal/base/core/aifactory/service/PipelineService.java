package com.epam.reportportal.base.core.aifactory.service;

import com.epam.reportportal.base.core.aifactory.dto.PipelineRS;
import com.epam.reportportal.base.util.OffsetRequest;
import org.springframework.data.domain.Page;

public interface PipelineService {

  Page<PipelineRS> listPipelines(Long projectId, OffsetRequest offsetRequest);
}
