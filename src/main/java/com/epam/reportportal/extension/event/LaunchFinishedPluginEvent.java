package com.epam.reportportal.extension.event;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
public class LaunchFinishedPluginEvent extends LaunchEvent<Long> {

  private final Long projectId;

  private String launchLink;

  public LaunchFinishedPluginEvent(Long source, Long projectId) {
    super(source);
    this.projectId = projectId;
  }

  public LaunchFinishedPluginEvent(Long source, Long projectId, String launchLink) {
    this(source, projectId);
    this.launchLink = launchLink;
  }

  public Long getProjectId() {
    return projectId;
  }

  public String getLaunchLink() {
    return launchLink;
  }
}
