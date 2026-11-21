package com.epam.reportportal.base.core.aifactory.enums;

/**
 * Why a pipeline's CI connection is not usable; a stable code the UI can switch on, next to a free text reason.
 * <p>
 * Example: GitLab answers {@code 404 Not Found} to the connection test - the project does not exist or the trigger
 * token does not belong to it (GitLab gives the same answer for both), so the code is
 * {@link #REPOSITORY_OR_CREDENTIAL_INVALID}.
 */
public enum CiConnectionReasonCode {
  /** No credential is saved, so GitLab is not even called. */
  NO_CREDENTIAL,
  /** GitLab answered 404 (or refused the token): no such project, or the credential does not fit it. */
  REPOSITORY_OR_CREDENTIAL_INVALID,
  /** The request did not get an answer (network, timeout) or GitLab answered with a server error. */
  UNREACHABLE,
  /** Any other answer; the reason text holds the status and the message. */
  UNEXPECTED_RESPONSE
}
