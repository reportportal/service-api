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

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.Collections;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ForwardedHeaderUtils;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@Component
public final class LinkGenerator {

  private static final String UI_PREFIX = "/ui/#";
  private static final String LAUNCHES = "/launches/all/";

  private static String path;

  @Value("${server.servlet.context-path:/api}")
  private String pathValue;

  @PostConstruct
  public void init() {
    LinkGenerator.path = this.pathValue;
  }

  private LinkGenerator() {
    //static only
  }

  public static String generateLaunchLink(String baseUrl, String projectName, String id) {
    return StringUtils.isEmpty(baseUrl) ? null : baseUrl + UI_PREFIX + projectName + LAUNCHES + id;
  }

  @SneakyThrows
  public static String composeBaseUrl(HttpServletRequest request) {

    String processedPath = "/".equals(path) ? null : path.replace("/api", "");
    /*
     * Use Uri components since they are aware of x-forwarded-host headers
     */

    HttpHeaders httpHeaders = new HttpHeaders();
    Collections.list(request.getHeaderNames())
        .forEach(headerName-> httpHeaders.add(headerName, request.getHeader(headerName)));

    URI uri = new URI(request.getRequestURI());

    return ForwardedHeaderUtils.adaptFromForwardedHeaders(uri, httpHeaders)
        .replacePath(processedPath)
        .replaceQuery(null)
        .build()
        .toUri()
        .toASCIIString();
  }
}
