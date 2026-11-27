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

package com.epam.reportportal.base.info;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.info.InfoEndpoint;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;

class AnonymousInfoTest {

  private static final Map<String, Object> FULL_INFO = Map.of(
      "build", Map.of(
          "name", "API Service",
          "version", "26.1.0-42",
          "description", "ReportPortal API Service",
          "branch", "develop",
          "repo", "reportportal/service-api",
          "group", "com.epam.reportportal"),
      "authExtensions", Map.of(
          "github", Map.of("button", "<span>Login with GitHub</span>", "path", "/oauth/login/github")),
      "metadata", Map.of("project_roles", List.of("EDITOR", "VIEWER")),
      "extensions", Map.of(
          "result", Map.of(
              "server.analytics.all", "true",
              "server.details.instance", "c9d4b1f2-instance",
              "server.password.min.length", "10",
              "server.users.sso", "false",
              "server.session.expiration", "3600",
              "server.features.important.enabled", "true",
              "server.sidebar.links", "[]"),
          "extension", Set.of("jira", "saml"),
          "analyzers", Set.of(Map.of("analyzer", "analyzer", "version", "5.14.0", "available", true))),
      "environment", Map.of(
          "ga_measurement_id", "G-TEST",
          "instance_type", "SAAS",
          "allow_delete_account", "true",
          "binarystore_type", "minio",
          "some_secret", "do-not-show"),
      "jobs", Map.of(
          "flushingDataTrigger", Map.of("triggersIn", 1234L),
          "cleanLogsTrigger", Map.of("triggersIn", 42L)),
      "jobsInfo", Map.of("build", Map.of("name", "Jobs Service", "version", "5.14.0")));

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void keepsOnlyAllowListedKeys() {
    Map<String, Object> anonymous = AnonymousInfo.of(FULL_INFO);

    assertEquals(Set.of("build", "authExtensions", "extensions", "environment", "jobs"), anonymous.keySet());
    assertEquals(Map.of("name", "API Service", "version", "26.1.0-42"), anonymous.get("build"));
    assertEquals(FULL_INFO.get("authExtensions"), anonymous.get("authExtensions"));
    assertEquals(Map.of("result", Map.of(
            "server.analytics.all", "true",
            "server.details.instance", "c9d4b1f2-instance",
            "server.password.min.length", "10")),
        anonymous.get("extensions"));
    assertEquals(Map.of("ga_measurement_id", "G-TEST", "instance_type", "SAAS"), anonymous.get("environment"));
    assertEquals(Map.of("flushingDataTrigger", Map.of()), anonymous.get("jobs"));
  }

  @Test
  void settingNotOnTheAllowListIsPrivateByDefault() {
    Map<String, Object> info = Map.of("extensions", Map.of("result", Map.of(
        "server.password.min.length", "10",
        "server.some.future.setting", "value")));

    Map<String, Object> anonymous = AnonymousInfo.of(info);

    assertEquals(Map.of("result", Map.of("server.password.min.length", "10")), anonymous.get("extensions"));
  }

  @Test
  void unknownTopLevelKeyIsPrivateByDefault() {
    Map<String, Object> anonymous = AnonymousInfo.of(Map.of("someNewContributor", Map.of("a", "b")));

    assertTrue(anonymous.isEmpty());
  }

  @Test
  void jobsAreLeftOutWhenThisIsNotADemoInstance() {
    Map<String, Object> anonymous = AnonymousInfo.of(Map.of("jobs", Map.of("cleanLogsTrigger", Map.of())));

    assertTrue(anonymous.isEmpty());
  }

  @Test
  void allowListsAreExactlyWhatTheUiNeedsBeforeLogin() {
    assertEquals(Set.of("name", "version"), AnonymousInfo.BUILD_KEYS);
    assertEquals(Set.of("server.analytics.all", "server.details.instance", "server.password.min.length"),
        AnonymousInfo.SERVER_SETTINGS_KEYS);
    assertEquals(Set.of("ga_measurement_id", "instance_type"), AnonymousInfo.ENVIRONMENT_KEYS);
  }

  @Test
  void webExtensionReturnsSubsetToAnonymousCaller() {
    SecurityContextHolder.getContext().setAuthentication(new AnonymousAuthenticationToken("key", "anonymousUser",
        AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")));

    Map<String, Object> info = new InfoEndpointWebExtension(endpoint()).info();

    assertEquals(AnonymousInfo.of(FULL_INFO), info);
  }

  @Test
  void webExtensionReturnsSubsetWhenThereIsNoAuthentication() {
    Map<String, Object> info = new InfoEndpointWebExtension(endpoint()).info();

    assertEquals(AnonymousInfo.of(FULL_INFO), info);
  }

  @Test
  void webExtensionReturnsEverythingToAuthenticatedCaller() {
    SecurityContextHolder.getContext().setAuthentication(
        UsernamePasswordAuthenticationToken.authenticated("user", null, List.of()));

    Map<String, Object> info = new InfoEndpointWebExtension(endpoint()).info();

    assertEquals(FULL_INFO, info);
  }

  private static InfoEndpoint endpoint() {
    return new InfoEndpoint(List.of(builder -> builder.withDetails(FULL_INFO)));
  }
}
