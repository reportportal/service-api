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

package com.epam.reportportal.base.auth.authenticator;

import static com.epam.reportportal.base.auth.UserRoleHierarchy.ROLE_REGISTERED;

import com.epam.reportportal.base.auth.UserRoleHierarchy;
import com.epam.reportportal.base.auth.acl.ReportPortalAclAuthorizationStrategyImpl;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.User;
import com.google.common.collect.Sets;
import org.springframework.security.acls.model.Acl;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@Deprecated(since = "ACL logic was removed", forRemoval = true)
public class RegisteredUserAuthenticator implements UserAuthenticator {

  /**
   * Required for {@link org.springframework.security.acls.domain.AclAuthorizationStrategy#securityCheck(Acl, int)} with
   * custom implementation {@link ReportPortalAclAuthorizationStrategyImpl} to permit shared objects to the newly
   * created user
   *
   * @param user {@link User}
   * @return {@link Authentication} with authenticated user with the role {@link UserRoleHierarchy#ROLE_REGISTERED}
   */
  @Override
  public Authentication authenticate(User user) {
    final Authentication authentication = new UsernamePasswordAuthenticationToken(user.getLogin(),
        user.getPassword(),
        Sets.newHashSet(new SimpleGrantedAuthority(ROLE_REGISTERED))
    );
    SecurityContextHolder.getContext().setAuthentication(authentication);
    return authentication;
  }
}
