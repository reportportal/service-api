package com.epam.reportportal.base.core.aifactory.connector;

/**
 * An answer of the CI to a request, whatever its status.
 * <p>
 * Example: {@code new CiHttpResponse(400, "{\"message\":{\"base\":[\"Reference not found\"]}}")}.
 *
 * @param status HTTP status code
 * @param body   response body, empty when there is none
 */
public record CiHttpResponse(int status, String body) {
}
