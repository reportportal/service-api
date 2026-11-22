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
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.info.Info;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class JobsInfoContributorTest {

  private static final String JOBS_URL = "http://jobs:8686";

  private MockRestServiceServer server;
  private JobsInfoContributor contributor;

  @BeforeEach
  void setUp() {
    RestClient.Builder builder = RestClient.builder();
    server = MockRestServiceServer.bindTo(builder).build();
    contributor = new JobsInfoContributor(builder.build());
    ReflectionTestUtils.setField(contributor, "jobsBaseUrl", JOBS_URL);
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void contributesJobsInfoForAuthenticatedCaller() {
    SecurityContextHolder.getContext().setAuthentication(
        UsernamePasswordAuthenticationToken.authenticated("user", null, List.of()));
    server.expect(requestTo(JOBS_URL + "/info"))
        .andRespond(withSuccess("""
            {"build":{"name":"Jobs Service"}}""", MediaType.APPLICATION_JSON));

    Info.Builder builder = new Info.Builder();
    contributor.contribute(builder);

    assertEquals(Map.of("build", Map.of("name", "Jobs Service")), builder.build().get("jobsInfo"));
    server.verify();
  }

  @Test
  void doesNotCallJobsServiceForAnonymousCaller() {
    Info.Builder builder = new Info.Builder();
    contributor.contribute(builder);

    assertTrue(builder.build().getDetails().isEmpty());
    server.verify();
  }
}
