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

package com.epam.reportportal.auth.integration.saml;

import com.epam.reportportal.auth.util.AuthUtils;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.User;
import java.util.Collections;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.saml2.provider.service.authentication.DefaultSaml2AuthenticatedPrincipal;
import org.springframework.security.saml2.provider.service.authentication.Saml2Authentication;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticationToken;
import org.springframework.stereotype.Component;

/**
 * Implementation of authentication manager for SAML integration.
 *
 * @author Yevgeniy Svalukhin
 */
@Component
public class ReportPortalSamlAuthenticationManager implements AuthenticationManager {

  private final SamlUserReplicator samlUserReplicator;

  public ReportPortalSamlAuthenticationManager(SamlUserReplicator samlUserReplicator) {
    this.samlUserReplicator = samlUserReplicator;
  }

  @Override
  public Authentication authenticate(Authentication authentication) throws AuthenticationException {
    if (authentication instanceof Saml2AuthenticationToken defaultSamlAuthentication) {

      User user = samlUserReplicator.replicateUser(defaultSamlAuthentication);

      Saml2Authentication saml2Authentication = new Saml2Authentication(
          new DefaultSaml2AuthenticatedPrincipal(user.getLogin(),
              Collections.emptyMap()),
          defaultSamlAuthentication.getSaml2Response(), AuthUtils.AS_AUTHORITIES.apply(user.getRole()));

      SecurityContextHolder.getContext().setAuthentication(saml2Authentication);

      return saml2Authentication;
    }
    throw new BadCredentialsException("Bad credentials");
  }
}
