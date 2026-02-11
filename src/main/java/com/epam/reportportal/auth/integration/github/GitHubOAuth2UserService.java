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
package com.epam.reportportal.auth.integration.github;

import static com.epam.reportportal.auth.integration.github.GithubOauthProvider.PROVIDER_NAME;

import com.epam.reportportal.auth.model.settings.OAuthRegistrationResource;
import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.google.common.base.Splitter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;

/**
 * @author <a href="mailto:andrei_piankouski@epam.com">Andrei Piankouski</a>
 */
public class GitHubOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

  private final GitHubUserReplicator replicator;
  private final Supplier<OAuthRegistrationResource> oAuthRegistrationSupplier;

  public GitHubOAuth2UserService(GitHubUserReplicator replicator,
      Supplier<OAuthRegistrationResource> oAuthRegistrationSupplier) {
    this.replicator = replicator;
    this.oAuthRegistrationSupplier = oAuthRegistrationSupplier;
  }

  @Override
  public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
    if (!userRequest.getClientRegistration().getRegistrationId().equals(PROVIDER_NAME)) {
      return null;
    }
    String accessToken = userRequest.getAccessToken().getTokenValue();

    GitHubClient gitHubClient = GitHubClient.withAccessToken(accessToken);
    UserResource gitHubUser = gitHubClient.getUser();

    OAuthRegistrationResource registration = oAuthRegistrationSupplier.get();
    List<String> allowedOrgs = parseAllowedOrganizations(registration);
    if (!allowedOrgs.isEmpty()) {
      validateUserOrganizations(gitHubUser.getLogin(), gitHubClient, allowedOrgs);
    }

    ReportPortalUser user = replicator.replicateUser(gitHubUser, gitHubClient);

    return new RPOAuth2User(user, accessToken);
  }

  private List<String> parseAllowedOrganizations(OAuthRegistrationResource registration) {
    return Optional.ofNullable(registration.getRestrictions())
        .map(restrictions -> restrictions.get("organizations"))
        .map(orgs -> Splitter.on(',').omitEmptyStrings().splitToList(orgs))
        .orElse(Collections.emptyList());
  }

  private void validateUserOrganizations(String username,
      GitHubClient client,
      List<String> allowedOrgs) {
    boolean hasAccess = client.getUserOrganizations(username)
        .stream()
        .map(OrganizationResource::getLogin)
        .anyMatch(allowedOrgs::contains);

    if (!hasAccess) {
      throw new OAuth2AuthenticationException(
          "User '" + username + "' does not belong to allowed GitHub organization"
      );
    }
  }
}
