/*
 * Copyright 2026 EPAM Systems
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;

class LinkGeneratorTest {

  private final LinkGenerator linkGenerator = new LinkGenerator();

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(linkGenerator, "pathValue", "/");
    ReflectionTestUtils.setField(linkGenerator, "uiBaseUrl", "");
    ReflectionTestUtils.setField(linkGenerator, "uiAllowedHosts", "");
  }

  private static MockHttpServletRequest requestFrom(String host) {
    MockHttpServletRequest request = new MockHttpServletRequest("POST", "/v1/user/password/restore");
    request.setScheme("https");
    request.setServerName(host);
    request.setServerPort(443);
    return request;
  }

  @Test
  void trustedBaseUrlUsesConfiguredUrlAndIgnoresRequestHost() {
    ReflectionTestUtils.setField(linkGenerator, "uiBaseUrl", "https://rp.example.com/");

    Optional<String> baseUrl = linkGenerator.composeTrustedBaseUrl(requestFrom("evil.example"));

    assertEquals(Optional.of("https://rp.example.com"), baseUrl);
  }

  @Test
  void trustedBaseUrlRejectsHostOutsideAllowList() {
    ReflectionTestUtils.setField(linkGenerator, "uiAllowedHosts", "rp.example.com, rp.internal");

    assertTrue(linkGenerator.composeTrustedBaseUrl(requestFrom("evil.example")).isEmpty());
  }

  @Test
  void trustedBaseUrlAcceptsAllowedHost() {
    ReflectionTestUtils.setField(linkGenerator, "uiAllowedHosts", "rp.example.com, rp.internal");

    assertEquals(Optional.of("https://rp.internal"),
        linkGenerator.composeTrustedBaseUrl(requestFrom("rp.internal")));
  }

  @Test
  void trustedBaseUrlFallsBackToRequestWhenNothingConfigured() {
    assertEquals(Optional.of("https://rp.example.com"),
        linkGenerator.composeTrustedBaseUrl(requestFrom("rp.example.com")));
  }
}
