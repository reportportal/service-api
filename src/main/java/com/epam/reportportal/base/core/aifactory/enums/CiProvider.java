package com.epam.reportportal.base.core.aifactory.enums;

/**
 * CI system that runs a pipeline stage and can be asked to retry it.
 */
public enum CiProvider {
  GITHUB_ACTIONS,
  GITLAB_CI
}
