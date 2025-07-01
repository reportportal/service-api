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

package com.epam.ta.reportportal.core.configs.security.converters;

import jakarta.validation.constraints.NotNull;
import java.util.Collection;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

public class ReportPortalJwtConverter extends AbstractJwtConverter {

  public ReportPortalJwtConverter(UserDetailsService userDetailsService) {
    super(userDetailsService);
  }

  @Override
  public final AbstractAuthenticationToken convert(Jwt jwt) {
    var username = jwt.getSubject();
    var principal = userDetailsService.loadUserByUsername(username);

    var authorities = extractAuthorities(jwt);
    return new UsernamePasswordAuthenticationToken(principal, null, authorities);
  }
}
