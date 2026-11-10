package com.epam.reportportal.base.core.aifactory.connector;

import com.epam.reportportal.base.core.aifactory.model.PipelineExecution;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineStage;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.Integration;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;

/**
 * Shared integration-parameter lookup for {@link CiTriggerConnector} implementations.
 */
abstract class AbstractCiTriggerConnector implements CiTriggerConnector {

  protected String requiredParam(Integration integration, String paramName) {
    var value = stringParam(integration, paramName);
    if (value == null || value.isBlank()) {
      throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR, "Missing required integration parameter: " + paramName);
    }
    return value;
  }

  protected String stringParam(Integration integration, String paramName) {
    if (integration == null || integration.getParams() == null || integration.getParams().getParams() == null) {
      return null;
    }
    var value = integration.getParams().getParams().get(paramName);
    return value instanceof String stringValue ? stringValue : null;
  }

  /**
   * What the stage's execution reported for the given parameter, or null.
   */
  protected String retryParameter(PipelineStage stage, String name) {
    var execution = execution(stage);
    return execution == null || execution.getRetryParameters() == null ? null : execution.getRetryParameters().get(name);
  }

  /**
   * The identifier of the stage's run in the system that ran it, or null.
   */
  protected String externalRunId(PipelineStage stage) {
    var execution = execution(stage);
    return execution == null ? null : execution.getExternalRunId();
  }

  private PipelineExecution execution(PipelineStage stage) {
    return stage.getExecution() == null ? null : stage.getExecution().getContent();
  }
}
