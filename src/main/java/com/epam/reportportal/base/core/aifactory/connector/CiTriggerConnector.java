package com.epam.reportportal.base.core.aifactory.connector;

import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineStage;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.Integration;

/**
 * Runs a stage again in the CI system that ran it (the "Retry Upload" button).
 * <p>
 * Example: the Upload stage was reported with {@code executorType: "GITHUB_ACTIONS"} and retry parameters
 * {@code {repo: "org/repo", workflowRef: "ai-pipeline.yml"}}. The connector whose
 * {@link #getSupportedExecutorType()} is {@code GITHUB_ACTIONS} reads those parameters, calls GitHub with the
 * project's GitHub Actions integration (token) and returns the URL of the new run.
 * <p>
 * This is the outbound direction ReportPortal needs for stage retry, the opposite of the inbound TMS sync
 * connectors, which only ever pull data in. An execution of any other kind simply has no connector and is
 * not retryable.
 */
public interface CiTriggerConnector {

  /**
   * The executor type this connector handles, e.g. {@code GITHUB_ACTIONS}.
   */
  String getSupportedExecutorType();

  /**
   * Name of the project integration type holding the credentials this connector needs.
   */
  String getIntegrationTypeName();

  /**
   * Triggers a rerun of the job the stage's execution describes.
   *
   * @param integration credentials/config for the system (token, base URL)
   * @param stage the stage to retry; its execution identifies the job
   * @return the URL of the newly triggered run
   */
  String triggerStageRerun(Integration integration, PipelineStage stage);
}
