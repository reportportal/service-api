package com.epam.reportportal.base.core.aifactory.service;

/**
 * Creates the pipelines a project is expected to have, so that they can be set up before anything runs.
 * <p>
 * Implemented outside the pipeline core (it must not know test case code); {@link PipelineServiceImpl} calls it
 * before listing the pipelines of a project. Example: the Test automation pipeline of a project that already has
 * the Test case generation pipeline.
 */
public interface PipelineProvisioner {

  /**
   * Makes sure the project has the pipelines this provisioner owns. Must not fail the caller.
   */
  void ensureForProject(long projectId);
}
