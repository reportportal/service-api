/*
 * Copyright 2019 EPAM Systems
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

package com.epam.ta.reportportal.core.launch.util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * Service for generating launch links and composing base URLs
 *
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@Service
@Slf4j
public class LinkGenerator {

  private static final String UI_PREFIX = "/ui/#";
  private static final String LAUNCHES = "/launches/all/";

  @Value("${server.servlet.context-path:/api}")
  private String contextPath;

  /**
   * Generates a launch link for the given parameters
   *
   * @param baseUrl     the base URL
   * @param projectName the project name
   * @param id          the launch ID
   * @return the generated launch link or null if baseUrl is empty
   */
  public String generateLaunchLink(String baseUrl, String projectName, String id) {
    return StringUtils.isEmpty(baseUrl) ? null : baseUrl + UI_PREFIX + projectName + LAUNCHES + id;
  }

  /**
   * Composes the base URL from the current HTTP request, handling proxy headers
   *
   * @param request the HTTP request
   * @return the composed base URL
   */
  public String composeBaseUrl(HttpServletRequest request) {
    String adjustedPath = ("/".equals(contextPath) || StringUtils.isEmpty(contextPath)) ? ""
        : contextPath.replace("/api", "");
    return ServletUriComponentsBuilder.fromRequestUri(request)
        .replacePath(adjustedPath)
        .replaceQuery(null)
        .build()
        .toUriString();
  }
}
