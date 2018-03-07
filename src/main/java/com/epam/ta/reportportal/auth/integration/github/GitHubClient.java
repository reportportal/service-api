/*
 * Copyright 2016 EPAM Systems
 *
 *
 * This file is part of EPAM Report Portal.
 * https://github.com/reportportal/service-authorization
 *
 * Report Portal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Report Portal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Report Portal.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.epam.ta.reportportal.auth.integration.github;

import com.google.common.base.Charsets;
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

import java.io.IOException;
import java.util.List;

/**
 * Simple GitHub client
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
            @Override
            public void handleError(ClientHttpResponse response) throws IOException {
                String errorMessage = "Unable to load Github Data:" + new String(getResponseBody(response), Charsets.UTF_8);
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

    public List<EmailResource> getUserEmails() {
        return getForObject(GITHUB_BASE_URL + "/user/emails", new ParameterizedTypeReference<List<EmailResource>>() {
        });
    }

    public List<OrganizationResource> getUserOrganizations(String user) {
        return getForObject(GITHUB_BASE_URL + "/users/{}/orgs", new ParameterizedTypeReference<List<OrganizationResource>>() {
        }, user);
    }

    public List<OrganizationResource> getUserOrganizations(UserResource user) {
        return getForObject(user.organizationsUrl, new ParameterizedTypeReference<List<OrganizationResource>>() {
        });
    }

    public ResponseEntity<Resource> downloadResource(String url) {
        return this.restTemplate.getForEntity(url, Resource.class);
    }

    private <T> T getForObject(String url, ParameterizedTypeReference<T> type, Object... urlVars) {
        return this.restTemplate.exchange(url, HttpMethod.GET, null, type, urlVars).getBody();
    }
}
