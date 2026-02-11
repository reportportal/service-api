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

import com.epam.reportportal.base.infrastructure.rules.commons.validation.BusinessRule;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.reporting.OperationCompletionRS;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * GitHUB synchronization endpoint.
 *
 * @author <a href="mailto:andrei_varabyeu@epam.com">Andrei Varabyeu</a>
 */
@RestController
@Tag(name = "github-endpoint", description = "Github Endpoint")
public class GithubEndpoint {

  private final GitHubUserReplicator replicator;

  @Autowired
  public GithubEndpoint(GitHubUserReplicator replicator) {
    this.replicator = replicator;
  }

  @Operation(summary = "Synchronizes logged-in GitHub user")
  @RequestMapping(value = {"/sso/me/github/synchronize"}, method = RequestMethod.POST)
  public OperationCompletionRS synchronize(UsernamePasswordAuthenticationToken user) {
    String upstreamToken = (String) user.getDetails();
    BusinessRule.expect(upstreamToken, Objects::nonNull)
        .verify(ErrorType.INCORRECT_AUTHENTICATION_TYPE, "Cannot synchronize GitHub User");
    this.replicator.synchronizeUser(upstreamToken.toString());
    return new OperationCompletionRS("User info successfully synchronized");
  }
}
