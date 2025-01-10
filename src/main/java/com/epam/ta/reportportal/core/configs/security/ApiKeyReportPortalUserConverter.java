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

package com.epam.ta.reportportal.core.configs.security;

import com.epam.ta.reportportal.auth.util.AuthUtils;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.google.common.collect.Maps;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

public class ApiKeyReportPortalUserConverter implements
    Converter<ReportPortalUser, AbstractAuthenticationToken> {

  public ApiKeyReportPortalUserConverter() {
  }

  @Override
  public final AbstractAuthenticationToken convert(ReportPortalUser rpUser) {
    var userWithAuthorities = ReportPortalUser.userBuilder()
        .withUserName(rpUser.getUsername())
        .withPassword(rpUser.getPassword())
        .withAuthorities(AuthUtils.AS_AUTHORITIES.apply(rpUser.getUserRole()))
        .withUserId(rpUser.getUserId()).withUserRole(rpUser.getUserRole())
        .withProjectDetails(Maps.newHashMapWithExpectedSize(1)).withEmail(rpUser.getEmail())
        .build();

    return new UsernamePasswordAuthenticationToken(
        userWithAuthorities,
        null,
        userWithAuthorities.getAuthorities());

  }

}
