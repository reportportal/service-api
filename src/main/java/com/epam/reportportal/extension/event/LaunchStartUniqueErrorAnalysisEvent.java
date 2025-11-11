package com.epam.reportportal.extension.event;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class LaunchStartUniqueErrorAnalysisEvent extends LaunchEvent<Long> {

  private final Long projectId;

  public LaunchStartUniqueErrorAnalysisEvent(Long id, Long projectId) {
    super(id);
    this.projectId = projectId;
  }

  public Long getProjectId() {
    return projectId;
  }
}
