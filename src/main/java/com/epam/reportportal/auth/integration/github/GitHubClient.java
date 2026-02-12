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

package com.epam.reportportal.auth.integration.github;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

/**
 * Simple GitHub client.
 *
 * @author <a href="mailto:andrei_varabyeu@epam.com">Andrei Varabyeu</a>
 */
public class GitHubClient {

  private static final Logger LOGGER = LoggerFactory.getLogger(GitHubClient.class);

  private static final String GITHUB_BASE_URL = "https://api.github.com";

  private final RestTemplate restTemplate;


  private GitHubClient(String accessToken) {
    this.restTemplate = new RestTemplate();
    this.restTemplate.setErrorHandler(new DefaultResponseErrorHandler() {
      //todo remove???
      public void handleError(ClientHttpResponse response) {
        String errorMessage =
            "Unable to load Github Data:" + new String(getResponseBody(response), StandardCharsets.UTF_8);
        LOGGER.error(errorMessage);
        throw new AuthenticationServiceException(errorMessage);
      }
    });
    this.restTemplate.getInterceptors().add((request, body, execution) -> {
      request.getHeaders().add("Authorization", "bearer " + accessToken);
      return execution.execute(request, body);
    });
  }

  public static GitHubClient withAccessToken(String accessToken) {
    return new GitHubClient(accessToken);
  }

  public UserResource getUser() {
    return this.restTemplate.getForObject(GITHUB_BASE_URL + "/user", UserResource.class);
  }

  public Map<String, Object> getUserAttributes() {
    return getForObject(GITHUB_BASE_URL + "/user", new ParameterizedTypeReference<>() {
    });
  }

  public List<EmailResource> getUserEmails() {
    return getForObject(GITHUB_BASE_URL + "/user/emails", new ParameterizedTypeReference<>() {
    });
  }

  public List<OrganizationResource> getUserOrganizations(String user) {
    return getForObject(GITHUB_BASE_URL + "/user/orgs",
        new ParameterizedTypeReference<>() {
        },
        user);
  }

  public List<OrganizationResource> getUserOrganizations(UserResource user) {
    return getForObject(user.getOrganizationsUrl(), new ParameterizedTypeReference<>() {
    });
  }

  public ResponseEntity<Resource> downloadResource(String url) {
    return this.restTemplate.getForEntity(url, Resource.class);
  }

  private <T> T getForObject(String url, ParameterizedTypeReference<T> type, Object... urlVars) {
    return this.restTemplate.exchange(url, HttpMethod.GET, null, type, urlVars).getBody();
  }
}
