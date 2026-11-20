package com.epam.reportportal.base.core.aifactory.enums;

/**
 * Whether a pipeline can start jobs in its CI. Only {@link #CONNECTED} lets a job start.
 * <p>
 * Example: a pipeline that was just given a repository and a credential is {@code NOT_CONNECTED} until somebody
 * runs "Test connection" and GitLab accepts the credential; a start that GitLab refused turns it into
 * {@code CONNECTION_FAILED}.
 */
public enum CiConnectionState {
  NOT_CONNECTED,
  CONNECTED,
  CONNECTION_FAILED
}
