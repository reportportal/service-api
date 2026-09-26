package com.epam.reportportal.base.core.aifactory.connector;

import com.epam.reportportal.base.core.aifactory.enums.CiProvider;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineStage;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.Integration;

/**
 * Triggers a CI job/workflow to rerun the exact agent job that produced a
 * {@link PipelineStage}. This is the outbound direction RP needs for stage
 * retry — the opposite of the inbound TMS sync connectors, which only ever pull
 * data into RP.
 */
public interface CiTriggerConnector {

  CiProvider getSupportedProvider();

  /**
   * Triggers a rerun of the CI job identified by the stage's {@code ci.*} fields.
   *
   * @param integration credentials/config for the CI system (token, base URL)
   * @param stage the stage to retry; its {@code ci*} fields identify the job
   * @return the URL of the newly triggered CI run
   */
  String triggerStageRerun(Integration integration, PipelineStage stage);
}
