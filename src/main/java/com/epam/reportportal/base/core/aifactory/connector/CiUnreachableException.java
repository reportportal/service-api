package com.epam.reportportal.base.core.aifactory.connector;

/**
 * The CI did not answer at all (connection refused, timeout, broken response), as opposed to answering with an
 * error status.
 * <p>
 * Example: {@code CiHttpClient.postForResponse} to a host that is down throws it with the network error text.
 */
public class CiUnreachableException extends RuntimeException {

  public CiUnreachableException(String message) {
    super(message);
  }
}
