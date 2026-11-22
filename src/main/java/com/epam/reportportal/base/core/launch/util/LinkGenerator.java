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
import java.util.Arrays;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

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

  @Value("${rp.ui.base-url:}")
  private String uiBaseUrl;

  @Value("${rp.ui.allowed-hosts:}")
  private String uiAllowedHosts;

  /**
   * Generates the project-level UI path for notifications.
   *
   * @param baseUrl     the base URL, or null/empty for a relative path
   * @param orgSlug     the organization slug
   * @param projectSlug the project slug
   * @return the project UI path (e.g. {@code http://host/ui/#organizations/org/projects/proj})
   */
  public String generateProjectUiPath(String baseUrl, String orgSlug, String projectSlug) {
    if (StringUtils.isBlank(orgSlug) || StringUtils.isBlank(projectSlug)) {
      log.warn("Cannot generate project UI path: orgSlug or projectSlug is blank");
      return null;
    }
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
    if (StringUtils.isEmpty(baseUrl)) {
      return null;
    }
    String projectPath = generateProjectUiPath(baseUrl, orgSlug, projectSlug);
    return projectPath == null ? null : projectPath + LAUNCHES + id;
  }

  public URI generateInvitationUrl(HttpServletRequest httpServletRequest, String invitationId) {
    var baseUrl = composeBaseUrl(httpServletRequest);
    return URI.create(baseUrl + "/ui/#registration?uuid=" + invitationId);
  }

  public String composeBaseUrl(HttpServletRequest request) {
    String processedPath = "/".equals(pathValue) ? null : pathValue.replace("/api", "");
    return ServletUriComponentsBuilder.fromRequest(request)
        .replacePath(processedPath)
        .replaceQuery(null)
        .build()
        .toUri()
        .toASCIIString();
  }

  /**
   * Composes the base URL for links e-mailed from unauthenticated flows (password restore), where the request host
   * cannot be trusted. Uses {@code rp.ui.base-url} when configured. Otherwise falls back to the request, and when
   * {@code rp.ui.allowed-hosts} is configured the request host must be one of them.
   *
   * @param request the inbound request
   * @return the base URL, or empty when the request host is not allowed
   */
  public Optional<String> composeTrustedBaseUrl(HttpServletRequest request) {
    if (StringUtils.isNotBlank(uiBaseUrl)) {
      return Optional.of(StringUtils.removeEnd(uiBaseUrl.trim(), "/"));
    }
    String requestBaseUrl = composeBaseUrl(request);
    if (StringUtils.isBlank(uiAllowedHosts)) {
      log.warn("rp.ui.base-url is not configured; e-mail links are built from the request host");
      return Optional.of(requestBaseUrl);
    }
    String host = URI.create(requestBaseUrl).getHost();
    boolean allowed = host != null && Arrays.stream(uiAllowedHosts.split(","))
        .map(String::trim)
        .anyMatch(host::equalsIgnoreCase);
    if (!allowed) {
      log.warn("Request host '{}' is not in rp.ui.allowed-hosts; e-mail link is not generated", host);
      return Optional.empty();
    }
    return Optional.of(requestBaseUrl);
  }
}
