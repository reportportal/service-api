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

  /**
   * Starts a new run, not tied to any stage, e.g. the job that fixes a test case after a review.
   * <p>
   * Example: the fix job of GitHub Actions is the workflow {@code fix-test-case.yml} started with the inputs
   * {@code test_case_id} and {@code push_id}. An integration may name that workflow itself in its parameter
   * {@code fixWorkflowRef}, which then wins over the one in the request.
   *
   * @param integration credentials/config for the system (token, base URL)
   * @param request     where and with what to start the run
   * @return the URL where the started run can be followed (a workflow dispatch does not return the run itself,
   *         so this can be the page of the workflow)
   */
  String triggerRun(Integration integration, CiRunRequest request);
}
