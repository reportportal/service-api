package com.epam.reportportal.base.core.aifactory.connector;

import com.epam.reportportal.base.core.aifactory.enums.CiConnectionReasonCode;

/**
 * The result of {@link CiTriggerConnector#checkConnection}: the CI accepts the integration's credential, or why not.
 * <p>
 * Example: {@code CiConnectionCheck.ok()} for a good token;
 * {@code CiConnectionCheck.failed(UNREACHABLE, "GitLab answered 502")} for a server error.
 *
 * @param connected  true when the CI accepted the credential for the repository
 * @param reasonCode why it did not, null when connected
 * @param reason     a short text for people, null when connected; never holds the credential
 */
public record CiConnectionCheck(boolean connected, CiConnectionReasonCode reasonCode, String reason) {

  public static CiConnectionCheck ok() {
    return new CiConnectionCheck(true, null, null);
  }

  public static CiConnectionCheck failed(CiConnectionReasonCode reasonCode, String reason) {
    return new CiConnectionCheck(false, reasonCode, reason);
  }
}
