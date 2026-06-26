/*
 * Copyright 2025 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.reportportal.base.core.launch.util;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.util.ForwardedHeaderUtils;

/**
 * Builds UI and deep links to launches and test items.
 *
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@Service
@Slf4j
public class LinkGenerator {

  private static final String UI_PREFIX = "/ui/#";
  private static final String ORGANIZATIONS = "organizations/";
  private static final String PROJECTS = "/projects/";
  private static final String LAUNCHES = "/launches/all/";

  @Value("${server.servlet.context-path:/api}")
  private String pathValue;

  /**
   * Generates the project-level UI path for notifications.
   *
   * @param baseUrl     the base URL, or null/empty for a relative path
   * @param orgSlug     the organization slug
   * @param projectSlug the project slug
   * @return the project UI path (e.g. {@code http://host/ui/#organizations/org/projects/proj})
   */
  public String generateProjectUiPath(String baseUrl, String orgSlug, String projectSlug) {
    String uiPath = UI_PREFIX + ORGANIZATIONS + orgSlug + PROJECTS + projectSlug;
    return StringUtils.isEmpty(baseUrl) ? uiPath : baseUrl + uiPath;
  }

  /**
   * Generates a launch link for the given parameters.
   *
   * @param baseUrl     the base URL
   * @param orgSlug     the organization slug
   * @param projectSlug the project slug
   * @param id          the launch ID
   * @return the generated launch link or null if baseUrl is empty
   */
  public String generateLaunchLink(String baseUrl, String orgSlug, String projectSlug, String id) {
    return StringUtils.isEmpty(baseUrl) ? null
        : generateProjectUiPath(baseUrl, orgSlug, projectSlug) + LAUNCHES + id;
  }

  public URI generateInvitationUrl(HttpServletRequest httpServletRequest, String invitationId) {
    var baseUrl = composeBaseUrl(httpServletRequest);
    return URI.create(baseUrl + "/ui/#registration?uuid=" + invitationId);
  }

  @SneakyThrows
  public String composeBaseUrl(HttpServletRequest request) {

    String processedPath = "/".equals(pathValue) ? null : pathValue.replace("/api", "");
    /*
     * Use Uri components since they are aware of x-forwarded-host headers
     */

    HttpHeaders httpHeaders = new HttpHeaders();
    // Only include relevant forwarding headers
    String[] forwardedHeaders = {"x-forwarded-host", "x-forwarded-proto", "x-forwarded-port", "x-forwarded-for",
        "forwarded"};
    for (String headerName : forwardedHeaders) {
      String headerValue = request.getHeader(headerName);
      if (headerValue != null) {
        httpHeaders.add(headerName, headerValue);
      }
    }

    URI uri = new URI(request.getRequestURL().toString());

    return ForwardedHeaderUtils.adaptFromForwardedHeaders(uri, httpHeaders)
        .replacePath(processedPath)
        .replaceQuery(null)
        .build()
        .toUri()
        .toASCIIString();
  }
}
