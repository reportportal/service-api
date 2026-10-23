package com.epam.reportportal.base.core.aifactory.connector;

import java.util.Map;

/**
 * What it takes to start a brand-new run in a CI system, as opposed to running a stage again.
 * <p>
 * Example: {@code new CiRunRequest("org/repo", "fix-test-case.yml", Map.of("test_case_id", "103",
 * "push_id", "7"))} starts the workflow {@code fix-test-case.yml} of the repository {@code org/repo} with
 * the two inputs. For GitLab CI {@code repo} is the project id, {@code workflowRef} the git ref, and the inputs
 * become pipeline variables.
 *
 * @param repo        the repository (GitHub) or project (GitLab) the run belongs to; it must be the one the
 *                    project's integration is configured for
 * @param workflowRef the workflow file (GitHub) or git ref (GitLab); an integration may override it with its own
 *                    setting for this kind of run
 * @param inputs      what the run is told, as plain strings
 */
public record CiRunRequest(String repo, String workflowRef, Map<String, String> inputs) {
}
